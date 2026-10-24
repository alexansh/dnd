"""
Golden Vector Generator for Grimoire 5E
Uses core_5e as the authoritative test oracle to export deterministic test vectors for Kotlin engine verification.
"""

import json
import os
import sys

# Ensure repository root is on sys.path
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from typing import Dict, Any, List

from core_5e.mechanics import (
    calculate_ability_modifier, calculate_proficiency_bonus,
    calculate_spell_save_dc, calculate_spell_attack_modifier,
    calculate_passive_score, calculate_concentration_dc,
    Ability, Skill, SKILL_GOVERNING_ABILITY, DeathSaveTracker
)
from core_5e.character import (
    CharacterClass, CLASS_HIT_DICE, CLASS_SAVING_THROWS,
    SRD_SUBCLASSES, POINT_BUY_COSTS, STANDARD_ARRAY
)
from core_5e.dice import DiceEngine, RollType


def generate_all_vectors() -> Dict[str, Any]:
    vectors = {}

    # 1. Ability Modifiers (Score 1 to 30)
    vectors["ability_modifiers"] = [
        {"score": score, "expected_modifier": calculate_ability_modifier(score)}
        for score in range(1, 31)
    ]

    # 2. Proficiency Bonus (Level 1 to 20)
    vectors["proficiency_bonuses"] = [
        {"level": lvl, "expected_proficiency": calculate_proficiency_bonus(lvl)}
        for lvl in range(1, 21)
    ]

    # 3. Spell Save DC & Attack Modifiers
    spell_dcs = []
    spell_attacks = []
    for prof in range(2, 7):
        for casting_mod in range(-2, 6):
            spell_dcs.append({
                "proficiency_bonus": prof,
                "casting_mod": casting_mod,
                "expected_dc": calculate_spell_save_dc(prof, casting_mod)
            })
            spell_attacks.append({
                "proficiency_bonus": prof,
                "casting_mod": casting_mod,
                "expected_attack_mod": calculate_spell_attack_modifier(prof, casting_mod)
            })
    vectors["spell_save_dcs"] = spell_dcs
    vectors["spell_attack_mods"] = spell_attacks

    # 4. Passive Scores
    passives = []
    for mod in range(-2, 6):
        for is_prof in [False, True]:
            for prof in [2, 3, 4, 5, 6]:
                passives.append({
                    "ability_mod": mod,
                    "is_proficient": is_prof,
                    "proficiency_bonus": prof,
                    "expected_passive": calculate_passive_score(mod, is_prof, prof)
                })
    vectors["passive_scores"] = passives

    # 5. Concentration DCs (Damage 1 to 60)
    vectors["concentration_dcs"] = [
        {"damage": dmg, "expected_dc": calculate_concentration_dc(dmg)}
        for dmg in range(1, 61)
    ]

    # 6. Death Save Outcomes (Roll 1 to 20)
    death_saves = []
    for roll in range(1, 21):
        tracker = DeathSaveTracker()
        success, desc = tracker.record_roll(roll)
        death_saves.append({
            "roll": roll,
            "success": success,
            "successes_count": tracker.successes,
            "failures_count": tracker.failures,
            "is_stabilized": tracker.is_stabilized,
            "is_dead": tracker.is_dead,
            "hp_regained": tracker.hp_regained
        })
    vectors["death_save_outcomes"] = death_saves

    # 7. Class SRD Metadata
    vectors["class_hit_dice"] = {
        cls.value: CLASS_HIT_DICE[cls] for cls in CharacterClass
    }
    vectors["class_saving_throws"] = {
        cls.value: [a.value for a in CLASS_SAVING_THROWS[cls]] for cls in CharacterClass
    }
    vectors["srd_subclasses"] = {
        cls.value: SRD_SUBCLASSES[cls] for cls in CharacterClass
    }
    vectors["point_buy_costs"] = POINT_BUY_COSTS
    vectors["standard_array"] = STANDARD_ARRAY

    # 8. Deterministic Dice Engine Roll Samples (Seed 42)
    dice_samples = []
    engine = DiceEngine(rng_seed=42)
    for sides in [4, 6, 8, 10, 12, 20, 100]:
        rolls = [engine.roll_die(sides) for _ in range(10)]
        dice_samples.append({
            "die_sides": sides,
            "rolls": rolls
        })
    vectors["dice_rolls_seed_42"] = dice_samples

    # Advantage / Disadvantage samples
    engine_adv = DiceEngine(rng_seed=1337)
    d20_adv_samples = []
    for roll_type in [RollType.NORMAL, RollType.ADVANTAGE, RollType.DISADVANTAGE]:
        for mod in [0, 3, -1]:
            outcome = engine_adv.roll_d20(modifier=mod, roll_type=roll_type)
            d20_adv_samples.append({
                "roll_type": roll_type.value,
                "modifier": mod,
                "total": outcome.total,
                "is_critical_hit": outcome.is_critical_hit,
                "is_critical_miss": outcome.is_critical_miss,
                "dice_values": [d.value for d in outcome.dice_results],
                "discarded_values": [d.value for d in outcome.dice_results if d.is_discarded]
            })
    vectors["d20_outcomes_seed_1337"] = d20_adv_samples

    return vectors


if __name__ == "__main__":
    data = generate_all_vectors()
    
    # Save to tests directory
    out_path1 = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "tests", "golden_vectors_5e.json")
    with open(out_path1, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2)
    print(f"Exported {len(data)} test vector categories to {out_path1}")

    # Also save to Android test resources
    android_res_dir = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "android", "app", "src", "test", "resources")
    os.makedirs(android_res_dir, exist_ok=True)
    out_path2 = os.path.join(android_res_dir, "golden_vectors_5e.json")
    with open(out_path2, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2)
    print(f"Exported to Android test resources: {out_path2}")
