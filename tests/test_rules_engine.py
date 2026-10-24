"""
Unit Tests for Grimoire 5E Core Rules & Adjudication Engine
"""

import unittest
from core_5e import (
    DiceEngine, RollType,
    Ability, Skill, Cover, ExhaustionLevel, DeathSaveTracker,
    calculate_ability_modifier, calculate_proficiency_bonus,
    calculate_spell_save_dc, calculate_spell_attack_modifier,
    calculate_passive_score, calculate_concentration_dc,
    Character, CharacterBuilder, CharacterClass, Item, SpellSlotPool,
    POINT_BUY_COSTS, STANDARD_ARRAY
)


class TestAbilityAndProficiencyFormulas(unittest.TestCase):
    def test_ability_modifiers(self):
        # 5e table checks
        self.assertEqual(calculate_ability_modifier(1), -5)
        self.assertEqual(calculate_ability_modifier(8), -1)
        self.assertEqual(calculate_ability_modifier(9), -1)
        self.assertEqual(calculate_ability_modifier(10), 0)
        self.assertEqual(calculate_ability_modifier(11), 0)
        self.assertEqual(calculate_ability_modifier(12), 1)
        self.assertEqual(calculate_ability_modifier(14), 2)
        self.assertEqual(calculate_ability_modifier(15), 2)
        self.assertEqual(calculate_ability_modifier(18), 4)
        self.assertEqual(calculate_ability_modifier(20), 5)
        self.assertEqual(calculate_ability_modifier(30), 10)

    def test_proficiency_bonus_progression(self):
        # Levels 1-4: +2
        for lvl in range(1, 5):
            self.assertEqual(calculate_proficiency_bonus(lvl), 2)
        # Levels 5-8: +3
        for lvl in range(5, 9):
            self.assertEqual(calculate_proficiency_bonus(lvl), 3)
        # Levels 9-12: +4
        for lvl in range(9, 13):
            self.assertEqual(calculate_proficiency_bonus(lvl), 4)
        # Levels 13-16: +5
        for lvl in range(13, 17):
            self.assertEqual(calculate_proficiency_bonus(lvl), 5)
        # Levels 17-20: +6
        for lvl in range(17, 21):
            self.assertEqual(calculate_proficiency_bonus(lvl), 6)

    def test_spell_save_dc_and_attack_mod(self):
        # Lvl 1 Cleric: Prof +2, WIS 16 (+3)
        dc = calculate_spell_save_dc(proficiency_bonus=2, spellcasting_ability_mod=3)
        self.assertEqual(dc, 13)

        attack = calculate_spell_attack_modifier(proficiency_bonus=2, spellcasting_ability_mod=3)
        self.assertEqual(attack, 5)

    def test_passive_scores(self):
        # Lvl 1 Rogue with WIS 12 (+1), Proficient in Perception (+2)
        passive_perception = calculate_passive_score(ability_mod=1, is_proficient=True, proficiency_bonus=2)
        self.assertEqual(passive_perception, 13)

        # Non-proficient
        passive_not_prof = calculate_passive_score(ability_mod=1, is_proficient=False, proficiency_bonus=2)
        self.assertEqual(passive_not_prof, 11)

    def test_concentration_dc(self):
        self.assertEqual(calculate_concentration_dc(4), 10)   # 4 // 2 = 2, max(10, 2) = 10
        self.assertEqual(calculate_concentration_dc(20), 10)  # 20 // 2 = 10, max(10, 10) = 10
        self.assertEqual(calculate_concentration_dc(26), 13)  # 26 // 2 = 13, max(10, 13) = 13
        self.assertEqual(calculate_concentration_dc(45), 22)


class TestDiceEngine(unittest.TestCase):
    def setUp(self):
        self.dice = DiceEngine(rng_seed=42)

    def test_polyhedral_die_ranges(self):
        for sides in [4, 6, 8, 10, 12, 20, 100]:
            for _ in range(50):
                val = self.dice.roll_die(sides)
                self.assertTrue(1 <= val <= sides)

    def test_advantage_and_disadvantage(self):
        adv_roll = self.dice.roll_d20(modifier=3, roll_type=RollType.ADVANTAGE)
        self.assertEqual(len(adv_roll.dice_results), 2)
        kept = [d.value for d in adv_roll.dice_results if not d.is_discarded][0]
        discarded = [d.value for d in adv_roll.dice_results if d.is_discarded][0]
        self.assertGreaterEqual(kept, discarded)
        self.assertEqual(adv_roll.total, kept + 3)

        dis_roll = self.dice.roll_d20(modifier=2, roll_type=RollType.DISADVANTAGE)
        kept_dis = [d.value for d in dis_roll.dice_results if not d.is_discarded][0]
        discarded_dis = [d.value for d in dis_roll.dice_results if d.is_discarded][0]
        self.assertLessEqual(kept_dis, discarded_dis)
        self.assertEqual(dis_roll.total, kept_dis + 2)

    def test_damage_critical_doubling(self):
        # Critical doubles dice count
        normal_dmg = self.dice.roll_damage(dice_count=2, sides=6, modifier=3, is_critical=False)
        self.assertEqual(len(normal_dmg.dice_results), 2)

        crit_dmg = self.dice.roll_damage(dice_count=2, sides=6, modifier=3, is_critical=True)
        self.assertEqual(len(crit_dmg.dice_results), 4)

    def test_formula_parser(self):
        res = self.dice.parse_and_roll("2d6 + 3 + 1d4")
        self.assertEqual(len(res.dice_results), 3)  # 2 d6s + 1 d4
        self.assertEqual(res.modifier, 3)

        res_minus = self.dice.parse_and_roll("1d20 - 2")
        self.assertEqual(len(res_minus.dice_results), 1)
        self.assertEqual(res_minus.modifier, -2)


class TestDeathSaves(unittest.TestCase):
    def test_stabilization_at_three_successes(self):
        tracker = DeathSaveTracker()
        tracker.record_roll(12)
        tracker.record_roll(15)
        done, msg = tracker.record_roll(18)
        self.assertTrue(done)
        self.assertTrue(tracker.is_stabilized)
        self.assertFalse(tracker.is_dead)

    def test_death_at_three_failures(self):
        tracker = DeathSaveTracker()
        tracker.record_roll(8)
        tracker.record_roll(5)
        done, msg = tracker.record_roll(9)
        self.assertTrue(done)
        self.assertTrue(tracker.is_dead)

    def test_natural_20_instant_wake(self):
        tracker = DeathSaveTracker()
        tracker.record_roll(5)
        done, msg = tracker.record_roll(20)
        self.assertTrue(done)
        self.assertTrue(tracker.is_stabilized)
        self.assertEqual(tracker.hp_regained, 1)

    def test_natural_1_two_failures(self):
        tracker = DeathSaveTracker()
        tracker.record_roll(1)
        self.assertEqual(tracker.failures, 2)
        done, msg = tracker.record_roll(8)
        self.assertTrue(done)
        self.assertTrue(tracker.is_dead)


class TestCharacterBuilderAndResting(unittest.TestCase):
    def setUp(self):
        self.dice = DiceEngine(rng_seed=123)

    def test_point_buy_validation(self):
        # 15, 15, 15, 8, 8, 8 = 9 + 9 + 9 = 27 points
        valid_scores = {
            Ability.STR: 15,
            Ability.DEX: 15,
            Ability.CON: 15,
            Ability.INT: 8,
            Ability.WIS: 8,
            Ability.CHA: 8
        }
        is_valid, cost, _ = CharacterBuilder.validate_point_buy(valid_scores)
        self.assertTrue(is_valid)
        self.assertEqual(cost, 27)

        # Illegal score 16 (point buy max is 15)
        invalid_scores = valid_scores.copy()
        invalid_scores[Ability.STR] = 16
        is_valid, _, msg = CharacterBuilder.validate_point_buy(invalid_scores)
        self.assertFalse(is_valid)

    def test_character_creation_and_ac(self):
        scores = {
            Ability.STR: 16,
            Ability.DEX: 10,
            Ability.CON: 14,
            Ability.INT: 10,
            Ability.WIS: 12,
            Ability.CHA: 14
        }
        paladin = CharacterBuilder.create_level_1(
            char_id="valen-1",
            name="Sir Valen",
            race="Human",
            char_class=CharacterClass.PALADIN,
            scores=scores,
            skill_choices=[Skill.ATHLETICS, Skill.PERSUASION]
        )
        # Lvl 1 Paladin HP = 10 (hit die) + 2 (CON mod) = 12
        self.assertEqual(paladin.hp_max, 12)
        self.assertEqual(paladin.hp_current, 12)
        self.assertEqual(paladin.proficiency_bonus, 2)

        # Equipping Chain Mail (16 AC) and Shield (+2 AC)
        chain_mail = Item(id="chain", name="Chain Mail", is_equipped=True, armor_class_bonus=16)
        shield = Item(id="shield", name="Shield", is_equipped=True, armor_class_bonus=2)
        paladin.inventory.extend([chain_mail, shield])

        self.assertEqual(paladin.armor_class, 18)

    def test_unarmored_defense(self):
        barb_scores = {
            Ability.STR: 16,
            Ability.DEX: 14,  # +2
            Ability.CON: 16,  # +3
            Ability.INT: 8,
            Ability.WIS: 12,
            Ability.CHA: 8
        }
        barb = CharacterBuilder.create_level_1(
            char_id="barb-1",
            name="Thorgar",
            race="Mountain Dwarf",
            char_class=CharacterClass.BARBARIAN,
            scores=barb_scores,
            skill_choices=[Skill.ATHLETICS, Skill.INTIMIDATION]
        )
        # Barbarian Unarmored AC = 10 + DEX(+2) + CON(+3) = 15
        self.assertEqual(barb.armor_class, 15)

    def test_short_and_long_rest(self):
        scores = {
            Ability.STR: 10,
            Ability.DEX: 14,
            Ability.CON: 14,  # +2
            Ability.INT: 16,
            Ability.WIS: 12,
            Ability.CHA: 8
        }
        wizard = CharacterBuilder.create_level_1(
            char_id="wiz-1",
            name="Zephyr",
            race="Elf",
            char_class=CharacterClass.WIZARD,
            scores=scores,
            skill_choices=[Skill.ARCANA, Skill.HISTORY]
        )
        wizard.hit_dice_max = 4
        wizard.hit_dice_current = 4
        wizard.hp_max = 26
        wizard.hp_current = 6

        # Consume spell slots
        wizard.spell_slots[1].current = 0

        # Short rest: spend 1 hit die
        res = wizard.perform_short_rest(1, self.dice)
        self.assertEqual(res["hit_dice_spent"], 1)
        self.assertEqual(wizard.hit_dice_current, 3)
        self.assertGreater(wizard.hp_current, 6)

        # Long rest: restores to full HP and recovers hit dice
        wizard.exhaustion_level = ExhaustionLevel.LEVEL_2
        long_res = wizard.perform_long_rest()
        self.assertEqual(wizard.hp_current, wizard.hp_max)
        self.assertEqual(wizard.spell_slots[1].current, wizard.spell_slots[1].maximum)
        self.assertEqual(wizard.exhaustion_level, ExhaustionLevel.LEVEL_1)
        self.assertEqual(wizard.hit_dice_current, 4)


if __name__ == "__main__":
    unittest.main()
