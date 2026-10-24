"""
Grimoire 5E - Core Rules and Adjudication Engine
"""

from core_5e.dice import DiceEngine, RollOutcome, RollType, DieResult
from core_5e.mechanics import (
    Ability,
    Skill,
    Cover,
    ExhaustionLevel,
    DeathSaveTracker,
    calculate_ability_modifier,
    calculate_proficiency_bonus,
    calculate_spell_save_dc,
    calculate_spell_attack_modifier,
    calculate_passive_score,
    calculate_concentration_dc,
    SKILL_GOVERNING_ABILITY
)
from core_5e.character import (
    Character,
    CharacterBuilder,
    CharacterClass,
    Item,
    SpellSlotPool,
    POINT_BUY_COSTS,
    STANDARD_ARRAY,
    CLASS_HIT_DICE,
    SRD_SUBCLASSES
)

__all__ = [
    "DiceEngine",
    "RollOutcome",
    "RollType",
    "DieResult",
    "Ability",
    "Skill",
    "Cover",
    "ExhaustionLevel",
    "DeathSaveTracker",
    "calculate_ability_modifier",
    "calculate_proficiency_bonus",
    "calculate_spell_save_dc",
    "calculate_spell_attack_modifier",
    "calculate_passive_score",
    "calculate_concentration_dc",
    "SKILL_GOVERNING_ABILITY",
    "Character",
    "CharacterBuilder",
    "CharacterClass",
    "Item",
    "SpellSlotPool",
    "POINT_BUY_COSTS",
    "STANDARD_ARRAY",
    "CLASS_HIT_DICE",
    "SRD_SUBCLASSES"
]
