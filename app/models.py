from typing import List, Dict, Optional, Any
from pydantic import BaseModel, Field

class AbilityScores(BaseModel):
    strength: int = Field(default=10, alias="str")
    dexterity: int = Field(default=10, alias="dex")
    constitution: int = Field(default=10, alias="con")
    intelligence: int = Field(default=10, alias="int")
    wisdom: int = Field(default=10, alias="wis")
    charisma: int = Field(default=10, alias="cha")

    model_config = {"populate_by_name": True}

    def __getitem__(self, item: str) -> int:
        mapping = {
            "str": self.strength,
            "dex": self.dexterity,
            "con": self.constitution,
            "int": self.intelligence,
            "wis": self.wisdom,
            "cha": self.charisma,
        }
        return mapping.get(item, 10)

class InventoryItem(BaseModel):
    name: str
    quantity: int = 1
    description: Optional[str] = ""
    equipped: bool = False

class Currency(BaseModel):
    cp: int = 0
    sp: int = 0
    ep: int = 0
    gp: int = 15
    pp: int = 0

class SpellSlot(BaseModel):
    total: int = 0
    used: int = 0

class DeathSaves(BaseModel):
    successes: int = 0
    failures: int = 0

class Character(BaseModel):
    id: str
    name: str
    race: str
    class_name: str
    level: int = 1
    background: str = "Folk Hero"
    alignment: str = "Neutral Good"
    experience: int = 0
    
    # Core stats
    ability_scores: AbilityScores = Field(default_factory=AbilityScores)
    hp_current: int = 12
    hp_max: int = 12
    hp_temp: int = 0
    armor_class: int = 14
    speed: int = 30
    initiative_bonus: int = 0
    proficiency_bonus: int = 2
    
    # Skills & Proficiencies
    proficient_skills: List[str] = Field(default_factory=list)
    saving_throws: List[str] = Field(default_factory=list)
    features_and_traits: List[str] = Field(default_factory=list)
    
    # Equipment & Currency
    inventory: List[InventoryItem] = Field(default_factory=list)
    currency: Currency = Field(default_factory=Currency)
    
    # Magic
    spellcasting_ability: Optional[str] = None
    spell_save_dc: Optional[int] = None
    spell_attack_bonus: Optional[int] = None
    spell_slots: Dict[str, SpellSlot] = Field(default_factory=dict)
    known_spells: List[Dict[str, Any]] = Field(default_factory=list)
    
    # Status
    conditions: List[str] = Field(default_factory=list)
    death_saves: DeathSaves = Field(default_factory=DeathSaves)
    backstory: Optional[str] = ""

    def model_dump(self, *args, **kwargs) -> Dict[str, Any]:
        kwargs.setdefault("by_alias", True)
        return super().model_dump(*args, **kwargs)

class Quest(BaseModel):
    id: str
    title: str
    description: str
    status: str = "active" # active, completed, failed

class NPC(BaseModel):
    name: str
    description: str
    disposition: str = "friendly" # friendly, neutral, hostile

class Campaign(BaseModel):
    id: str
    title: str
    description: str
    character_id: str
    current_location: str = "Tavern of the Dancing Bear"
    quests: List[Quest] = Field(default_factory=list)
    discovered_npcs: List[NPC] = Field(default_factory=list)
    combat_active: bool = False
    combat_round: int = 0
    created_at: str
    updated_at: str

    def model_dump(self, *args, **kwargs) -> Dict[str, Any]:
        kwargs.setdefault("by_alias", True)
        return super().model_dump(*args, **kwargs)

class DiceRoll(BaseModel):
    dice: str
    total: int
    natural: Optional[int] = None
    modifier: int = 0
    roll_type: str = "custom" # check, save, attack, damage, custom
    skill_or_ability: Optional[str] = None
    advantage: bool = False
    disadvantage: bool = False
    is_crit: bool = False
    is_fumble: bool = False

class ChatMessage(BaseModel):
    id: str
    campaign_id: str
    role: str # "user", "assistant", "system"
    content: str
    dice_roll: Optional[DiceRoll] = None
    suggested_actions: Optional[List[str]] = None
    required_check: Optional[Dict[str, Any]] = None
    state_updates: Optional[Dict[str, Any]] = None
    timestamp: str

    def model_dump(self, *args, **kwargs) -> Dict[str, Any]:
        kwargs.setdefault("by_alias", True)
        return super().model_dump(*args, **kwargs)

class PlayerActionRequest(BaseModel):
    campaign_id: str
    user_input: str
    dice_roll: Optional[DiceRoll] = None

class CreateCampaignRequest(BaseModel):
    title: str
    character_preset_or_data: Dict[str, Any]
    setting_description: Optional[str] = None
    dm_style: Optional[str] = "balanced"

class UpdateSettingsRequest(BaseModel):
    gemini_api_key: Optional[str] = None
    model_name: Optional[str] = "gemini-3.8-flash"
    custom_dm_instructions: Optional[str] = None
