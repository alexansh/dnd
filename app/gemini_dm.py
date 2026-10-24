import os
import json
import logging
import httpx
from typing import Dict, Any, List, Optional
from app.models import Character, Campaign, ChatMessage, DiceRoll
from app.database import get_setting

logger = logging.getLogger("gemini_dm")

GEMINI_API_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models"

SYSTEM_PROMPT = """You are Franz, an expert, cinematic, and fair AI Dungeon Master presiding over a solo Dungeons & Dragons 5th Edition (D&D 5E) adventure.

Your primary mission:
1. Deliver vivid, immersive, second-person narrative ("You step forward...", "The cold iron of your blade rattles...").
2. Adhere faithfully to official D&D 5E rules, mechanics, and math (Armor Class, DC benchmarks, Advantage/Disadvantage, Hit Points, Spell Slots, Conditions).
3. React dynamically to the player's choices, granting them full agency. Never railroad or dictate how the player character thinks or feels.
4. When the player attempts an action involving risk, uncertainty, or combat, DO NOT unilaterally declare success or failure. Instead, instruct the player to roll a specific D20 check, saving throw, or attack roll with a target DC or enemy AC.
5. When the player provides a completed dice roll in their message, narrate the dramatic outcome based on the total vs DC (e.g. Critical 20 is heroic triumph, Natural 1 is a dramatic complication, meeting or exceeding DC is success).
6. Track concrete game state: award loot, deduct/restore HP, add or advance quests, track location changes, and toggle combat state when hostiles engage.

CRITICAL OUTPUT FORMAT:
You MUST respond with valid JSON adhering strictly to this schema:
{
  "narrative": "Vivid narrative prose with rich markdown formatting, atmospheric descriptions, and dialogue.",
  "suggested_actions": ["Option A", "Option B", "Option C"],
  "required_check": {
    "type": "check" | "save" | "attack",
    "skill_or_ability": "Perception" | "Stealth" | "Athletics" | "Dexterity" | etc.,
    "dc": 14,
    "reason": "Brief explanation of why this check is required"
  } | null,
  "state_updates": {
    "hp_change": integer or 0,
    "items_added": [{"name": "Item name", "quantity": 1, "description": "Brief description"}],
    "items_removed": ["Item name"],
    "gold_change": integer or 0,
    "location": "New location name or null",
    "quest_added": {"title": "Quest title", "description": "Objective details"} | null,
    "quest_completed_id_or_title": "Title of quest finished" | null,
    "combat_state": "start" | "end" | null,
    "conditions_added": ["Poisoned"] | [],
    "conditions_removed": []
  }
}
"""

RESPONSE_SCHEMA = {
    "type": "OBJECT",
    "properties": {
        "narrative": {"type": "STRING"},
        "suggested_actions": {
            "type": "ARRAY",
            "items": {"type": "STRING"}
        },
        "required_check": {
            "type": "OBJECT",
            "nullable": True,
            "properties": {
                "type": {"type": "STRING"},
                "skill_or_ability": {"type": "STRING"},
                "dc": {"type": "INTEGER"},
                "reason": {"type": "STRING"}
            }
        },
        "state_updates": {
            "type": "OBJECT",
            "properties": {
                "hp_change": {"type": "INTEGER"},
                "items_added": {
                    "type": "ARRAY",
                    "items": {
                        "type": "OBJECT",
                        "properties": {
                            "name": {"type": "STRING"},
                            "quantity": {"type": "INTEGER"},
                            "description": {"type": "STRING"}
                        },
                        "required": ["name", "quantity"]
                    }
                },
                "items_removed": {
                    "type": "ARRAY",
                    "items": {"type": "STRING"}
                },
                "gold_change": {"type": "INTEGER"},
                "location": {"type": "STRING", "nullable": True},
                "quest_added": {
                    "type": "OBJECT",
                    "nullable": True,
                    "properties": {
                        "title": {"type": "STRING"},
                        "description": {"type": "STRING"}
                    }
                },
                "quest_completed_id_or_title": {"type": "STRING", "nullable": True},
                "combat_state": {"type": "STRING", "nullable": True},
                "conditions_added": {
                    "type": "ARRAY",
                    "items": {"type": "STRING"}
                },
                "conditions_removed": {
                    "type": "ARRAY",
                    "items": {"type": "STRING"}
                }
            }
        }
    },
    "required": ["narrative", "suggested_actions", "state_updates"]
}

async def get_effective_api_key() -> Optional[str]:
    # Check database settings first
    key = await get_setting("gemini_api_key")
    if key and key.strip():
        return key.strip()
    # Check environment variable
    env_key = os.environ.get("GEMINI_API_KEY")
    if env_key and env_key.strip():
        return env_key.strip()
    return None

async def get_effective_model() -> str:
    model = await get_setting("model_name")
    if model and model.strip():
        m = model.strip()
        if m in ("gemini-2.5-flash", "gemini-2.0-flash", "gemini-1.5-flash"):
            return "gemini-3.8-flash"
        return m
    return "gemini-3.8-flash"

def build_context_prompt(character: Character, campaign: Campaign) -> str:
    scores = character.ability_scores
    s_str = scores['str']
    s_dex = scores['dex']
    s_con = scores['con']
    s_int = scores['int']
    s_wis = scores['wis']
    s_cha = scores['cha']

    char_summary = f"""[CHARACTER STATUS]
Name: {character.name} | Race: {character.race} | Class: {character.class_name} (Lvl {character.level})
HP: {character.hp_current}/{character.hp_max} (Temp: {character.hp_temp}) | AC: {character.armor_class} | Speed: {character.speed} ft
Ability Scores: STR {s_str} (+{(s_str-10)//2}), DEX {s_dex} (+{(s_dex-10)//2}), CON {s_con} (+{(s_con-10)//2}), INT {s_int} (+{(s_int-10)//2}), WIS {s_wis} (+{(s_wis-10)//2}), CHA {s_cha} (+{(s_cha-10)//2})
Proficient Skills: {', '.join(character.proficient_skills) if character.proficient_skills else 'None'}
Proficient Saves: {', '.join(character.saving_throws) if character.saving_throws else 'None'}
Equipped Gear: {', '.join([item.name for item in character.inventory if item.equipped])}
Gold: {character.currency.gp} GP, {character.currency.sp} SP
Active Conditions: {', '.join(character.conditions) if character.conditions else 'None'}
"""
    active_quests = [q.title for q in campaign.quests if q.status == "active"]
    campaign_summary = f"""[CAMPAIGN CONTEXT]
Adventure: {campaign.title}
Current Location: {campaign.current_location}
Combat Active: {'YES (Round ' + str(campaign.combat_round) + ')' if campaign.combat_active else 'NO'}
Active Quests: {', '.join(active_quests) if active_quests else 'None'}
"""
    return char_summary + "\n" + campaign_summary

async def generate_dm_response(
    character: Character,
    campaign: Campaign,
    history: List[ChatMessage],
    new_user_input: str,
    dice_roll: Optional[DiceRoll] = None,
    custom_system_addon: Optional[str] = None
) -> Dict[str, Any]:
    api_key = await get_effective_api_key()
    if not api_key:
        return {
            "narrative": (
                "### ⚠️ Gemini API Key Required\n\n"
                "To awaken the AI Dungeon Master, please enter your free **Google Gemini API Key** in the **Settings** menu (click the ⚙️ icon in the top right).\n\n"
                "You can get a free API key in seconds from [Google AI Studio](https://aistudio.google.com/app/apikey).\n\n"
                "Once saved, you can immediately begin your adventure!"
            ),
            "suggested_actions": ["Open Settings (⚙️) to enter API key"],
            "required_check": None,
            "state_updates": {
                "hp_change": 0,
                "items_added": [],
                "items_removed": [],
                "gold_change": 0,
                "location": None
            }
        }

    model = await get_effective_model()
    # Normalize model name
    if not model.startswith("gemini-"):
        model = "gemini-3.8-flash"

    url = f"{GEMINI_API_ENDPOINT}/{model}:generateContent?key={api_key}"

    # Build system instruction
    full_system = SYSTEM_PROMPT
    if custom_system_addon:
        full_system += f"\n\n[USER CUSTOM PREFERENCES]:\n{custom_system_addon}"

    context_prompt = build_context_prompt(character, campaign)

    # Build contents array
    contents = []
    
    # We feed context prompt as a preamble or first system-like user turn
    contents.append({
        "role": "user",
        "parts": [{"text": f"Context update for the adventure:\n{context_prompt}"}]
    })
    contents.append({
        "role": "model",
        "parts": [{"text": json.dumps({
            "narrative": "Understood. The game world and character sheet are synchronized.",
            "suggested_actions": [],
            "state_updates": {"hp_change": 0, "items_added": [], "items_removed": [], "gold_change": 0}
        })}]
    })

    # Add recent message history (last 12 messages for good context and token budget)
    recent_history = history[-12:]
    for msg in recent_history:
        role = "user" if msg.role == "user" else "model"
        text = msg.content
        if msg.role == "user" and msg.dice_roll:
            roll_str = f"\n[DICE ROLL RESULT]: {msg.dice_roll.dice} -> Total: {msg.dice_roll.total} (Natural: {msg.dice_roll.natural}, Mod: {msg.dice_roll.modifier:+d}) for {msg.dice_roll.skill_or_ability or msg.dice_roll.roll_type}"
            text += roll_str
        contents.append({
            "role": role,
            "parts": [{"text": text}]
        })

    # Build current user message
    user_text = new_user_input
    if dice_roll:
        crit_msg = ""
        if dice_roll.is_crit:
            crit_msg = " - CRITICAL HIT / SUCCESS (Natural 20!)"
        elif dice_roll.is_fumble:
            crit_msg = " - CRITICAL FUMBLE (Natural 1!)"
        adv_msg = ""
        if dice_roll.advantage:
            adv_msg = " with Advantage"
        elif dice_roll.disadvantage:
            adv_msg = " with Disadvantage"

        user_text += f"\n\n[DICE ROLL RESULT]: Rolled {dice_roll.dice}{adv_msg} -> Total: {dice_roll.total} (Natural: {dice_roll.natural}, Mod: {dice_roll.modifier:+d}){crit_msg} for {dice_roll.skill_or_ability or dice_roll.roll_type}."

    contents.append({
        "role": "user",
        "parts": [{"text": user_text}]
    })

    payload = {
        "system_instruction": {
            "parts": [{"text": full_system}]
        },
        "contents": contents,
        "generationConfig": {
            "response_mime_type": "application/json",
            "response_schema": RESPONSE_SCHEMA,
            "temperature": 0.85,
            "maxOutputTokens": 2048
        }
    }

    async with httpx.AsyncClient(timeout=45.0) as client:
        try:
            response = await client.post(url, json=payload)
            if response.status_code != 200:
                err_body = response.text
                logger.error(f"Gemini API error ({response.status_code}): {err_body}")
                return {
                    "narrative": f"### ⚠️ Gemini API Encountered an Error\n\n**Status:** {response.status_code}\n\n```{err_body[:400]}```\n\nPlease verify your API key or chosen model in the Settings menu.",
                    "suggested_actions": ["Check Settings (⚙️)", "Try again"],
                    "required_check": None,
                    "state_updates": {"hp_change": 0, "items_added": [], "items_removed": [], "gold_change": 0}
                }

            result_json = response.json()
            candidates = result_json.get("candidates", [])
            if not candidates:
                return {
                    "narrative": "The Dungeon Master paused in thought. No narrative was produced. Please try rephrasing your action.",
                    "suggested_actions": ["Look around", "Check surroundings"],
                    "required_check": None,
                    "state_updates": {"hp_change": 0, "items_added": [], "items_removed": [], "gold_change": 0}
                }

            raw_text = candidates[0].get("content", {}).get("parts", [{}])[0].get("text", "{}")
            parsed = json.loads(raw_text)
            return parsed

        except httpx.TimeoutException:
            return {
                "narrative": "⏳ The Dungeon Master's response timed out. The network was slow. Please click 'Try again' to re-send.",
                "suggested_actions": ["Try again"],
                "required_check": None,
                "state_updates": {"hp_change": 0, "items_added": [], "items_removed": [], "gold_change": 0}
            }
        except Exception as e:
            logger.exception("Error calling Gemini API")
            return {
                "narrative": f"### ⚠️ Error processing request\n\n`{str(e)}`",
                "suggested_actions": ["Try again"],
                "required_check": None,
                "state_updates": {"hp_change": 0, "items_added": [], "items_removed": [], "gold_change": 0}
            }
