import os
import uuid
import datetime
from fastapi import FastAPI, HTTPException
from fastapi.staticfiles import StaticFiles
from fastapi.responses import FileResponse, JSONResponse
from typing import Dict, Any, List

from app.models import (
    Character, Campaign, ChatMessage, DiceRoll,
    PlayerActionRequest, CreateCampaignRequest, UpdateSettingsRequest,
    Quest, InventoryItem
)
from app.database import (
    init_db, get_setting, set_setting,
    save_character, get_character, list_characters,
    save_campaign, get_campaign, list_campaigns, delete_campaign,
    save_message, get_messages
)
from app.srd_data import CHARACTER_PRESETS, STARTER_SCENARIOS
from app.gemini_dm import generate_dm_response, get_effective_api_key, get_effective_model

app = FastAPI(title="Mythos 5E - Solo AI D&D Tabletop", version="1.0.0")

STATIC_DIR = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "static")

@app.on_event("startup")
async def startup_event():
    await init_db()

# ---- SETTINGS & HEALTH ----
@app.get("/api/status")
async def api_status():
    api_key = await get_effective_api_key()
    model = await get_effective_model()
    masked_key = ""
    if api_key:
        masked_key = f"{api_key[:4]}...{api_key[-4:]}" if len(api_key) > 8 else "***"
    return {
        "status": "ready",
        "has_api_key": bool(api_key),
        "masked_key": masked_key,
        "model_name": model
    }

@app.post("/api/settings")
async def update_settings(req: UpdateSettingsRequest):
    if req.gemini_api_key is not None:
        await set_setting("gemini_api_key", req.gemini_api_key.strip())
    if req.model_name is not None:
        await set_setting("model_name", req.model_name.strip())
    if req.custom_dm_instructions is not None:
        await set_setting("custom_dm_instructions", req.custom_dm_instructions)
    return {"status": "updated"}

# ---- PRESETS & METADATA ----
@app.get("/api/presets")
async def get_presets():
    return {
        "characters": CHARACTER_PRESETS,
        "scenarios": STARTER_SCENARIOS
    }

# ---- CAMPAIGNS ----
@app.get("/api/campaigns")
async def get_all_campaigns():
    return await list_campaigns()

@app.post("/api/campaigns")
async def create_new_campaign(req: CreateCampaignRequest):
    char_data = req.character_preset_or_data
    char_id = f"char-{uuid.uuid4().hex[:8]}"
    char_data["id"] = char_id
    
    # Instantiate character
    character = Character(**char_data)
    await save_character(character)

    now_iso = datetime.datetime.now().isoformat()
    camp_id = f"camp-{uuid.uuid4().hex[:8]}"

    # Scenario setup
    scenario = None
    initial_quest = None
    location = "A tranquil crossroads"
    if req.setting_description:
        # Match scenario by id if found
        matched = next((s for s in STARTER_SCENARIOS if s["id"] == req.setting_description or s["title"] == req.setting_description), None)
        if matched:
            scenario = matched
            location = matched["location"]
            initial_quest = Quest(
                id=f"quest-{uuid.uuid4().hex[:6]}",
                title=matched["initial_quest"]["title"],
                description=matched["initial_quest"]["description"],
                status="active"
            )

    campaign = Campaign(
        id=camp_id,
        title=req.title or (scenario["title"] if scenario else "New Adventure"),
        description=scenario["description"] if scenario else (req.setting_description or "An uncharted solo quest."),
        character_id=char_id,
        current_location=location,
        quests=[initial_quest] if initial_quest else [],
        created_at=now_iso,
        updated_at=now_iso
    )
    await save_campaign(campaign)

    # Generate initial DM introduction message
    starter_prompt = (
        f"Set the scene for the beginning of this adventure: '{campaign.title}'. "
        f"Location: '{campaign.current_location}'. "
        f"Setting context: {campaign.description}. "
        f"Introduce the scene, describe the sights, sounds, and initial situation facing the player character {character.name} the {character.race} {character.class_name}, "
        f"and present them with their first decisions and choices."
    )

    custom_style = await get_setting("custom_dm_instructions")
    dm_result = await generate_dm_response(
        character=character,
        campaign=campaign,
        history=[],
        new_user_input=starter_prompt,
        custom_system_addon=custom_style
    )

    dm_msg = ChatMessage(
        id=f"msg-{uuid.uuid4().hex[:8]}",
        campaign_id=camp_id,
        role="assistant",
        content=dm_result.get("narrative", "Welcome to your adventure."),
        suggested_actions=dm_result.get("suggested_actions", ["Look around", "Check inventory", "Proceed"]),
        required_check=dm_result.get("required_check"),
        state_updates=dm_result.get("state_updates"),
        timestamp=datetime.datetime.now().isoformat()
    )
    await save_message(dm_msg)

    return {
        "campaign": campaign.model_dump(),
        "character": character.model_dump(),
        "messages": [dm_msg.model_dump()]
    }

@app.get("/api/campaigns/{campaign_id}")
async def get_campaign_details(campaign_id: str):
    campaign = await get_campaign(campaign_id)
    if not campaign:
        raise HTTPException(status_code=404, detail="Campaign not found")
    
    character = await get_character(campaign.character_id)
    messages = await get_messages(campaign_id)
    return {
        "campaign": campaign.model_dump(),
        "character": character.model_dump() if character else None,
        "messages": [m.model_dump() for m in messages]
    }

@app.post("/api/campaigns/{campaign_id}/save")
async def save_campaign_checkpoint(campaign_id: str):
    campaign = await get_campaign(campaign_id)
    if not campaign:
        raise HTTPException(status_code=404, detail="Campaign not found")
    campaign.updated_at = datetime.datetime.now().isoformat()
    await save_campaign(campaign)
    return {"status": "saved", "updated_at": campaign.updated_at}

@app.delete("/api/campaigns/{campaign_id}")
async def remove_campaign(campaign_id: str):
    await delete_campaign(campaign_id)
    return {"status": "deleted"}

# ---- PLAYER ACTIONS & DM LOOP ----
@app.post("/api/action")
async def take_action(req: PlayerActionRequest):
    campaign = await get_campaign(req.campaign_id)
    if not campaign:
        raise HTTPException(status_code=404, detail="Campaign not found")
    
    character = await get_character(campaign.character_id)
    if not character:
        raise HTTPException(status_code=404, detail="Character not found")

    # Record User Message
    user_msg_id = f"msg-{uuid.uuid4().hex[:8]}"
    user_msg = ChatMessage(
        id=user_msg_id,
        campaign_id=campaign.id,
        role="user",
        content=req.user_input,
        dice_roll=req.dice_roll,
        timestamp=datetime.datetime.now().isoformat()
    )
    await save_message(user_msg)

    # Fetch recent history
    history = await get_messages(campaign.id)

    # Call AI DM
    custom_style = await get_setting("custom_dm_instructions")
    dm_res = await generate_dm_response(
        character=character,
        campaign=campaign,
        history=history,
        new_user_input=req.user_input,
        dice_roll=req.dice_roll,
        custom_system_addon=custom_style
    )

    # Apply state updates to character and campaign
    state_updates = dm_res.get("state_updates") or {}
    
    # HP adjustments
    hp_change = state_updates.get("hp_change", 0)
    if hp_change:
        character.hp_current = max(0, min(character.hp_max, character.hp_current + hp_change))
    
    # Items added
    items_added = state_updates.get("items_added", [])
    for it in items_added:
        existing = next((x for x in character.inventory if x.name.lower() == it["name"].lower()), None)
        if existing:
            existing.quantity += it.get("quantity", 1)
        else:
            character.inventory.append(InventoryItem(
                name=it["name"],
                quantity=it.get("quantity", 1),
                description=it.get("description", "")
            ))

    # Items removed
    items_removed = state_updates.get("items_removed", [])
    for it_name in items_removed:
        character.inventory = [x for x in character.inventory if it_name.lower() not in x.name.lower()]

    # Gold change
    gold_change = state_updates.get("gold_change", 0)
    if gold_change:
        character.currency.gp = max(0, character.currency.gp + gold_change)

    # Conditions
    for cond in state_updates.get("conditions_added", []):
        if cond not in character.conditions:
            character.conditions.append(cond)
    for cond in state_updates.get("conditions_removed", []):
        if cond in character.conditions:
            character.conditions.remove(cond)

    # Location change
    new_loc = state_updates.get("location")
    if new_loc:
        campaign.current_location = new_loc

    # Combat toggle
    combat_state = state_updates.get("combat_state")
    if combat_state == "start":
        campaign.combat_active = True
        campaign.combat_round = 1
    elif combat_state == "end":
        campaign.combat_active = False
        campaign.combat_round = 0
    elif campaign.combat_active:
        campaign.combat_round += 1

    # Quests
    new_quest_data = state_updates.get("quest_added")
    if new_quest_data and new_quest_data.get("title"):
        campaign.quests.append(Quest(
            id=f"quest-{uuid.uuid4().hex[:6]}",
            title=new_quest_data["title"],
            description=new_quest_data.get("description", ""),
            status="active"
        ))
    completed_q = state_updates.get("quest_completed_id_or_title")
    if completed_q:
        for q in campaign.quests:
            if completed_q.lower() in q.title.lower() or completed_q == q.id:
                q.status = "completed"

    campaign.updated_at = datetime.datetime.now().isoformat()
    await save_character(character)
    await save_campaign(campaign)

    # Save DM response message
    dm_msg_id = f"msg-{uuid.uuid4().hex[:8]}"
    dm_msg = ChatMessage(
        id=dm_msg_id,
        campaign_id=campaign.id,
        role="assistant",
        content=dm_res.get("narrative", "..."),
        suggested_actions=dm_res.get("suggested_actions", []),
        required_check=dm_res.get("required_check"),
        state_updates=state_updates,
        timestamp=datetime.datetime.now().isoformat()
    )
    await save_message(dm_msg)

    return {
        "user_message": user_msg.model_dump(),
        "dm_message": dm_msg.model_dump(),
        "character": character.model_dump(),
        "campaign": campaign.model_dump()
    }

# ---- DIRECT CHARACTER MANUAL ADJUSTMENTS ----
@app.post("/api/character/{character_id}/update")
async def manual_character_update(character_id: str, payload: Dict[str, Any]):
    char = await get_character(character_id)
    if not char:
        raise HTTPException(status_code=404, detail="Character not found")
    
    # Update fields passed in payload
    updated = char.model_copy(update=payload)
    await save_character(updated)
    return {"status": "saved", "character": updated.model_dump()}

# Mount Static Files (Web UI)
app.mount("/static", StaticFiles(directory=STATIC_DIR), name="static")

@app.get("/")
async def serve_index():
    return FileResponse(os.path.join(STATIC_DIR, "index.html"))
