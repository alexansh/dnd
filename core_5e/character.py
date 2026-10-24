"""
Grimoire 5E - Character Engine & 5E SRD Models
Handles complete character generation, point buy, standard array, leveling,
resting, spell slots, and state serialization.
"""

from dataclasses import dataclass, field, asdict
from enum import Enum
import json
import math
from typing import Dict, List, Optional, Set, Tuple

from core_5e.mechanics import (
    Ability, Skill, SKILL_GOVERNING_ABILITY,
    calculate_ability_modifier, calculate_proficiency_bonus,
    calculate_spell_save_dc, calculate_spell_attack_modifier,
    calculate_passive_score, DeathSaveTracker, ExhaustionLevel
)
from core_5e.dice import DiceEngine


class CharacterClass(str, Enum):
    BARBARIAN = "Barbarian"
    BARD = "Bard"
    CLERIC = "Cleric"
    DRUID = "Druid"
    FIGHTER = "Fighter"
    MONK = "Monk"
    PALADIN = "Paladin"
    RANGER = "Ranger"
    ROGUE = "Rogue"
    SORCERER = "Sorcerer"
    WARLOCK = "Warlock"
    WIZARD = "Wizard"


CLASS_HIT_DICE: Dict[CharacterClass, int] = {
    CharacterClass.BARBARIAN: 12,
    CharacterClass.FIGHTER: 10,
    CharacterClass.PALADIN: 10,
    CharacterClass.RANGER: 10,
    CharacterClass.BARD: 8,
    CharacterClass.CLERIC: 8,
    CharacterClass.DRUID: 8,
    CharacterClass.MONK: 8,
    CharacterClass.ROGUE: 8,
    CharacterClass.WARLOCK: 8,
    CharacterClass.SORCERER: 6,
    CharacterClass.WIZARD: 6,
}

CLASS_SAVING_THROWS: Dict[CharacterClass, Tuple[Ability, Ability]] = {
    CharacterClass.BARBARIAN: (Ability.STR, Ability.CON),
    CharacterClass.BARD: (Ability.DEX, Ability.CHA),
    CharacterClass.CLERIC: (Ability.WIS, Ability.CHA),
    CharacterClass.DRUID: (Ability.INT, Ability.WIS),
    CharacterClass.FIGHTER: (Ability.STR, Ability.CON),
    CharacterClass.MONK: (Ability.STR, Ability.DEX),
    CharacterClass.PALADIN: (Ability.WIS, Ability.CHA),
    CharacterClass.RANGER: (Ability.STR, Ability.DEX),
    CharacterClass.ROGUE: (Ability.DEX, Ability.INT),
    CharacterClass.SORCERER: (Ability.CON, Ability.CHA),
    CharacterClass.WARLOCK: (Ability.WIS, Ability.CHA),
    CharacterClass.WIZARD: (Ability.INT, Ability.WIS),
}

SRD_SUBCLASSES: Dict[CharacterClass, str] = {
    CharacterClass.BARBARIAN: "Path of the Berserker",
    CharacterClass.BARD: "College of Lore",
    CharacterClass.CLERIC: "Life Domain",
    CharacterClass.DRUID: "Circle of the Land",
    CharacterClass.FIGHTER: "Champion",
    CharacterClass.MONK: "Way of the Open Hand",
    CharacterClass.PALADIN: "Oath of Devotion",
    CharacterClass.RANGER: "Hunter",
    CharacterClass.ROGUE: "Thief",
    CharacterClass.SORCERER: "Draconic Bloodline",
    CharacterClass.WARLOCK: "The Fiend",
    CharacterClass.WIZARD: "School of Evocation",
}

# Standard 5E Point Buy Cost Table (Score: Cost)
POINT_BUY_COSTS: Dict[int, int] = {
    8: 0,
    9: 1,
    10: 2,
    11: 3,
    12: 4,
    13: 5,
    14: 7,
    15: 9
}

STANDARD_ARRAY = [15, 14, 13, 12, 10, 8]


@dataclass
class Item:
    id: str
    name: str
    quantity: int = 1
    weight_lbs: float = 0.0
    is_equipped: bool = False
    is_attuned: bool = False
    armor_class_bonus: int = 0
    damage_formula: Optional[str] = None
    description: str = ""


@dataclass
class SpellSlotPool:
    level: int
    current: int
    maximum: int


@dataclass
class Character:
    id: str
    name: str
    race: str
    character_class: CharacterClass
    subclass: Optional[str] = None
    level: int = 1
    xp: int = 0

    # Ability Scores
    strength: int = 10
    dexterity: int = 10
    constitution: int = 10
    intelligence: int = 10
    wisdom: int = 10
    charisma: int = 10

    # Vitals
    hp_current: int = 10
    hp_max: int = 10
    temp_hp: int = 0
    hit_dice_current: int = 1
    hit_dice_max: int = 1

    # Base Armor & Speed
    base_ac: int = 10
    speed_feet: int = 30
    inspiration: bool = False
    exhaustion_level: ExhaustionLevel = ExhaustionLevel.NONE

    # Proficiencies
    saving_throw_proficiencies: List[Ability] = field(default_factory=list)
    skill_proficiencies: List[Skill] = field(default_factory=list)

    # Equipment & Currency
    inventory: List[Item] = field(default_factory=list)
    gold: int = 10
    silver: int = 0
    copper: int = 0

    # Spells & Slots
    spell_slots: Dict[int, SpellSlotPool] = field(default_factory=dict)
    known_spells: List[str] = field(default_factory=list)
    prepared_spells: List[str] = field(default_factory=list)

    # Active Conditions & Death Saves
    active_conditions: List[str] = field(default_factory=list)
    death_saves: DeathSaveTracker = field(default_factory=DeathSaveTracker)

    @property
    def proficiency_bonus(self) -> int:
        return calculate_proficiency_bonus(self.level)

    def get_ability_score(self, ability: Ability) -> int:
        return getattr(self, ability.value)

    def get_ability_modifier(self, ability: Ability) -> int:
        return calculate_ability_modifier(self.get_ability_score(ability))

    def get_saving_throw_modifier(self, ability: Ability) -> int:
        base_mod = self.get_ability_modifier(ability)
        if ability in self.saving_throw_proficiencies:
            return base_mod + self.proficiency_bonus
        return base_mod

    def get_skill_modifier(self, skill: Skill) -> int:
        governing_ability = SKILL_GOVERNING_ABILITY[skill]
        base_mod = self.get_ability_modifier(governing_ability)
        if skill in self.skill_proficiencies:
            return base_mod + self.proficiency_bonus
        return base_mod

    def get_passive_skill(self, skill: Skill) -> int:
        mod = self.get_skill_modifier(skill)
        return 10 + mod

    @property
    def armor_class(self) -> int:
        dex_mod = self.get_ability_modifier(Ability.DEX)
        equipped_ac = 0
        has_armor = False

        for item in self.inventory:
            if item.is_equipped and item.armor_class_bonus > 0:
                equipped_ac += item.armor_class_bonus
                if "Shield" not in item.name:
                    has_armor = True

        if not has_armor:
            # Barbarian Unarmored Defense: 10 + DEX + CON
            if self.character_class == CharacterClass.BARBARIAN:
                con_mod = self.get_ability_modifier(Ability.CON)
                return 10 + dex_mod + con_mod + equipped_ac
            # Monk Unarmored Defense: 10 + DEX + WIS
            if self.character_class == CharacterClass.MONK:
                wis_mod = self.get_ability_modifier(Ability.WIS)
                return 10 + dex_mod + wis_mod + equipped_ac
            return 10 + dex_mod + equipped_ac

        return equipped_ac

    def apply_damage(self, damage: int) -> int:
        """Applies damage first to temp HP, then to current HP. Returns actual HP lost."""
        if damage <= 0:
            return 0

        effective_damage = damage
        if self.temp_hp > 0:
            absorbed = min(self.temp_hp, effective_damage)
            self.temp_hp -= absorbed
            effective_damage -= absorbed

        actual_hp_lost = min(self.hp_current, effective_damage)
        self.hp_current = max(0, self.hp_current - effective_damage)

        if self.hp_current == 0:
            self.death_saves = DeathSaveTracker()
            if "Unconscious" not in self.active_conditions:
                self.active_conditions.append("Unconscious")

        return actual_hp_lost

    def heal(self, amount: int) -> int:
        """Heals character up to maximum HP and removes Unconscious condition if at 0 HP."""
        if amount <= 0 or self.hp_current >= self.hp_max:
            return 0

        old_hp = self.hp_current
        self.hp_current = min(self.hp_max, self.hp_current + amount)
        actual_healed = self.hp_current - old_hp

        if old_hp == 0 and self.hp_current > 0:
            if "Unconscious" in self.active_conditions:
                self.active_conditions.remove("Unconscious")
            self.death_saves = DeathSaveTracker()

        return actual_healed

    def perform_short_rest(self, hit_dice_to_spend: int, dice_engine: DiceEngine) -> Dict[str, int]:
        """
        Executes a 5E Short Rest:
        Spends Hit Dice to heal (roll die + CON modifier per die).
        """
        if hit_dice_to_spend <= 0 or self.hit_dice_current <= 0:
            return {"hit_dice_spent": 0, "hp_healed": 0}

        to_spend = min(hit_dice_to_spend, self.hit_dice_current)
        sides = CLASS_HIT_DICE[self.character_class]
        con_mod = self.get_ability_modifier(Ability.CON)

        total_healed = 0
        for _ in range(to_spend):
            roll = dice_engine.roll_die(sides)
            heal_amount = max(1, roll + con_mod)
            total_healed += self.heal(heal_amount)
            self.hit_dice_current -= 1

        # Reset Warlock Pact Slots on Short Rest
        if self.character_class == CharacterClass.WARLOCK:
            for slot in self.spell_slots.values():
                slot.current = slot.maximum

        return {"hit_dice_spent": to_spend, "hp_healed": total_healed}

    def perform_long_rest(self) -> Dict[str, int]:
        """
        Executes an authentic 5E Long Rest:
        - Restore 100% current HP to max.
        - Regain Hit Dice equal to at least half total hit dice (floor(max / 2), min 1).
        - Reset all spell slots to maximum.
        - Clear temporary HP.
        - Reduce exhaustion level by 1.
        """
        old_hp = self.hp_current
        self.hp_current = self.hp_max
        healed = self.hp_max - old_hp
        self.temp_hp = 0

        recovered_dice = max(1, self.hit_dice_max // 2)
        old_hd = self.hit_dice_current
        self.hit_dice_current = min(self.hit_dice_max, self.hit_dice_current + recovered_dice)
        hd_regained = self.hit_dice_current - old_hd

        for slot in self.spell_slots.values():
            slot.current = slot.maximum

        if self.exhaustion_level > ExhaustionLevel.NONE:
            self.exhaustion_level = ExhaustionLevel(self.exhaustion_level - 1)

        if "Unconscious" in self.active_conditions:
            self.active_conditions.remove("Unconscious")
        self.death_saves = DeathSaveTracker()

        return {
            "hp_healed": healed,
            "hit_dice_recovered": hd_regained,
            "exhaustion_level": int(self.exhaustion_level)
        }

    def to_json(self) -> str:
        data = asdict(self)
        data["character_class"] = self.character_class.value
        data["exhaustion_level"] = int(self.exhaustion_level)
        data["saving_throw_proficiencies"] = [a.value for a in self.saving_throw_proficiencies]
        data["skill_proficiencies"] = [s.value for s in self.skill_proficiencies]
        return json.dumps(data, indent=2)


class CharacterBuilder:
    """Builder for authentic 5E Level 1 character creation."""

    @staticmethod
    def validate_point_buy(scores: Dict[Ability, int]) -> Tuple[bool, int, str]:
        """Validates a 27-point buy allocation."""
        total_cost = 0
        for ability, score in scores.items():
            if score < 8 or score > 15:
                return False, total_cost, f"{ability.value.capitalize()} score {score} is out of 8-15 range."
            total_cost += POINT_BUY_COSTS[score]

        if total_cost > 27:
            return False, total_cost, f"Spent {total_cost} points (maximum allowed is 27)."
        return True, total_cost, f"Valid point buy! {27 - total_cost} points remaining."

    @staticmethod
    def roll_4d6_drop_lowest(dice_engine: DiceEngine) -> int:
        """Classic 4d6 Drop Lowest ability score generation."""
        rolls = [dice_engine.roll_die(6) for _ in range(4)]
        rolls.sort()
        return sum(rolls[1:])  # Keep top 3

    @staticmethod
    def create_level_1(
        char_id: str,
        name: str,
        race: str,
        char_class: CharacterClass,
        scores: Dict[Ability, int],
        skill_choices: List[Skill],
        subclass: Optional[str] = None
    ) -> Character:
        hit_die = CLASS_HIT_DICE[char_class]
        con_mod = calculate_ability_modifier(scores[Ability.CON])
        level_1_hp = hit_die + con_mod

        saves = list(CLASS_SAVING_THROWS[char_class])

        char = Character(
            id=char_id,
            name=name,
            race=race,
            character_class=char_class,
            subclass=subclass or SRD_SUBCLASSES.get(char_class),
            level=1,
            strength=scores[Ability.STR],
            dexterity=scores[Ability.DEX],
            constitution=scores[Ability.CON],
            intelligence=scores[Ability.INT],
            wisdom=scores[Ability.WIS],
            charisma=scores[Ability.CHA],
            hp_current=level_1_hp,
            hp_max=level_1_hp,
            hit_dice_current=1,
            hit_dice_max=1,
            saving_throw_proficiencies=saves,
            skill_proficiencies=skill_choices,
        )

        # Standard Level 1 Spell Slot allocations for casters
        if char_class in [CharacterClass.WIZARD, CharacterClass.CLERIC, CharacterClass.DRUID,
                          CharacterClass.BARD, CharacterClass.SORCERER]:
            char.spell_slots[1] = SpellSlotPool(level=1, current=2, maximum=2)
        elif char_class == CharacterClass.WARLOCK:
            char.spell_slots[1] = SpellSlotPool(level=1, current=1, maximum=1)

        return char
