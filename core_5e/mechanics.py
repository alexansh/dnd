"""
Grimoire 5E - Core Rules & Mechanics Adjudicator
Implements official SRD 5.1 formulas: ability modifiers, proficiency bonuses,
skills, death saves, resting, exhaustion, and concentration.
"""

from dataclasses import dataclass
from enum import Enum, IntEnum
import math
from typing import Dict, List, Optional, Tuple


class Ability(str, Enum):
    STR = "strength"
    DEX = "dexterity"
    CON = "constitution"
    INT = "intelligence"
    WIS = "wisdom"
    CHA = "charisma"


class Skill(str, Enum):
    ACROBATICS = "Acrobatics"
    ANIMAL_HANDLING = "Animal Handling"
    ARCANA = "Arcana"
    ATHLETICS = "Athletics"
    DECEPTION = "Deception"
    HISTORY = "History"
    INSIGHT = "Insight"
    INTIMIDATION = "Intimidation"
    INVESTIGATION = "Investigation"
    MEDICINE = "Medicine"
    NATURE = "Nature"
    PERCEPTION = "Perception"
    PERFORMANCE = "Performance"
    PERSUASION = "Persuasion"
    RELIGION = "Religion"
    SLEIGHT_OF_HAND = "Sleight of Hand"
    STEALTH = "Stealth"
    SURVIVAL = "Survival"


SKILL_GOVERNING_ABILITY: Dict[Skill, Ability] = {
    Skill.ATHLETICS: Ability.STR,
    Skill.ACROBATICS: Ability.DEX,
    Skill.SLEIGHT_OF_HAND: Ability.DEX,
    Skill.STEALTH: Ability.DEX,
    Skill.ARCANA: Ability.INT,
    Skill.HISTORY: Ability.INT,
    Skill.INVESTIGATION: Ability.INT,
    Skill.NATURE: Ability.INT,
    Skill.RELIGION: Ability.INT,
    Skill.ANIMAL_HANDLING: Ability.WIS,
    Skill.INSIGHT: Ability.WIS,
    Skill.MEDICINE: Ability.WIS,
    Skill.PERCEPTION: Ability.WIS,
    Skill.SURVIVAL: Ability.WIS,
    Skill.DECEPTION: Ability.CHA,
    Skill.INTIMIDATION: Ability.CHA,
    Skill.PERFORMANCE: Ability.CHA,
    Skill.PERSUASION: Ability.CHA,
}


class Cover(Enum):
    NONE = 0
    HALF = 2             # +2 AC and DEX saving throws
    THREE_QUARTERS = 5   # +5 AC and DEX saving throws
    FULL = 999           # Cannot be targeted directly


class ExhaustionLevel(IntEnum):
    NONE = 0
    LEVEL_1 = 1  # Disadvantage on ability checks
    LEVEL_2 = 2  # Speed halved
    LEVEL_3 = 3  # Disadvantage on attack rolls and saving throws
    LEVEL_4 = 4  # Hit point maximum halved
    LEVEL_5 = 5  # Speed reduced to 0
    LEVEL_6 = 6  # Death


def calculate_ability_modifier(score: int) -> int:
    """
    Standard 5E modifier formula: floor((Score - 10) / 2)
    Example: 8 -> -1, 10 -> 0, 14 -> +2, 18 -> +4, 20 -> +5.
    """
    return math.floor((score - 10) / 2)


def calculate_proficiency_bonus(level: int) -> int:
    """
    Standard 5E Proficiency Bonus progression:
    Levels 1-4:   +2
    Levels 5-8:   +3
    Levels 9-12:  +4
    Levels 13-16: +5
    Levels 17-20: +6
    Formula: ceil(1 + level / 4)
    """
    if level < 1:
        raise ValueError("Character level must be at least 1.")
    return math.ceil(1 + (level / 4))


def calculate_spell_save_dc(proficiency_bonus: int, spellcasting_ability_mod: int) -> int:
    """Standard 5E Spell Save DC = 8 + Proficiency + Casting Mod"""
    return 8 + proficiency_bonus + spellcasting_ability_mod


def calculate_spell_attack_modifier(proficiency_bonus: int, spellcasting_ability_mod: int) -> int:
    """Standard 5E Spell Attack Mod = Proficiency + Casting Mod"""
    return proficiency_bonus + spellcasting_ability_mod


def calculate_passive_score(ability_mod: int, is_proficient: bool, proficiency_bonus: int, bonus_modifier: int = 0) -> int:
    """Standard 5E Passive score = 10 + ability mod + (prof if proficient) + bonus"""
    prof = proficiency_bonus if is_proficient else 0
    return 10 + ability_mod + prof + bonus_modifier


def calculate_concentration_dc(damage_taken: int) -> int:
    """
    Concentration Check DC = max(10, damage // 2).
    Taking 8 damage -> DC 10. Taking 26 damage -> DC 13.
    """
    return max(10, damage_taken // 2)


@dataclass
class DeathSaveTracker:
    successes: int = 0
    failures: int = 0
    is_stabilized: bool = False
    is_dead: bool = False
    hp_regained: int = 0

    def record_roll(self, roll_value: int) -> Tuple[bool, str]:
        """
        Adjudicates a Death Saving Throw roll (1-20).
        Returns (is_turn_resolved, summary_message).
        """
        if self.is_dead:
            return True, "Character is already dead."
        if self.is_stabilized:
            return True, "Character is already stabilized."

        if roll_value == 20:
            # Nat 20: Regain 1 HP and immediately become conscious
            self.hp_regained = 1
            self.is_stabilized = True
            self.successes = 0
            self.failures = 0
            return True, "Natural 20! You regain 1 HP and regain consciousness!"

        if roll_value == 1:
            # Nat 1: Counts as two failures
            self.failures += 2
            if self.failures >= 3:
                self.is_dead = True
                return True, "Natural 1! Suffered 2 failures. The character has perished."
            return False, f"Natural 1! Suffered 2 failures ({self.failures}/3 failures)."

        if roll_value >= 10:
            self.successes += 1
            if self.successes >= 3:
                self.is_stabilized = True
                return True, "3 Successes! The character is stable at 0 HP."
            return False, f"Success ({self.successes}/3 successes)."
        else:
            self.failures += 1
            if self.failures >= 3:
                self.is_dead = True
                return True, "3 Failures! The character has perished."
            return False, f"Failure ({self.failures}/3 failures)."

    def take_damage_at_zero_hp(self, is_critical: bool = False):
        """Taking damage at 0 HP causes 1 failure, or 2 on critical hit."""
        added = 2 if is_critical else 1
        self.failures += added
        self.is_stabilized = False
        if self.failures >= 3:
            self.is_dead = True
