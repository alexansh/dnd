"""
Unit Tests for Grimoire 5E SQLite Database Layer & SRD Ingestion
"""

import os
import tempfile
import unittest

from core_5e.database import DatabaseManager
from core_5e.srd_seed import seed_database
from core_5e.character import (
    CharacterBuilder, CharacterClass, Item, SpellSlotPool
)
from core_5e.mechanics import Ability, Skill, ExhaustionLevel


class TestDatabaseManager(unittest.TestCase):
    def setUp(self):
        # Create a temporary database file for each test
        self.temp_file = tempfile.NamedTemporaryFile(delete=False, suffix=".db")
        self.temp_file.close()
        self.db = DatabaseManager(self.temp_file.name)

    def tearDown(self):
        if os.path.exists(self.temp_file.name):
            try:
                os.remove(self.temp_file.name)
            except PermissionError:
                pass

    def test_srd_seeding_and_retrieval(self):
        stats = seed_database(self.db)
        self.assertGreaterEqual(stats["spells_ingested"], 10)
        self.assertGreaterEqual(stats["monsters_ingested"], 4)
        self.assertGreaterEqual(stats["items_ingested"], 8)

        # Check Fireball retrieval
        fireball = self.db.get_spell("fireball")
        self.assertIsNotNone(fireball)
        self.assertEqual(fireball["name"], "Fireball")
        self.assertEqual(fireball["level"], 3)
        self.assertEqual(fireball["damage_dice"], "8d6")

        # Check Goblin statblock
        goblin = self.db.get_monster("goblin")
        self.assertIsNotNone(goblin)
        self.assertEqual(goblin["name"], "Goblin")
        self.assertEqual(goblin["armor_class"], 15)
        self.assertEqual(goblin["actions"][0]["name"], "Scimitar")

        # Check Item retrieval
        longsword = self.db.get_item("longsword")
        self.assertIsNotNone(longsword)
        self.assertEqual(longsword["damage_formula"], "1d8")

    def test_character_persistence_roundtrip(self):
        scores = {
            Ability.STR: 16,
            Ability.DEX: 12,
            Ability.CON: 14,
            Ability.INT: 8,
            Ability.WIS: 12,
            Ability.CHA: 14
        }
        paladin = CharacterBuilder.create_level_1(
            char_id="char-valen-001",
            name="Sir Valen",
            race="Human",
            char_class=CharacterClass.PALADIN,
            scores=scores,
            skill_choices=[Skill.ATHLETICS, Skill.PERSUASION]
        )
        paladin.gold = 35
        paladin.silver = 14
        paladin.exhaustion_level = ExhaustionLevel.LEVEL_1
        paladin.inventory.append(
            Item(id="sword-1", name="Ancestral Longsword", quantity=1, is_equipped=True, damage_formula="1d8+3")
        )

        # Save to SQLite
        self.db.save_character(paladin)

        # Retrieve from SQLite
        loaded = self.db.load_character("char-valen-001")
        self.assertIsNotNone(loaded)
        self.assertEqual(loaded.name, "Sir Valen")
        self.assertEqual(loaded.character_class, CharacterClass.PALADIN)
        self.assertEqual(loaded.level, 1)
        self.assertEqual(loaded.hp_current, 12)
        self.assertEqual(loaded.gold, 35)
        self.assertEqual(loaded.exhaustion_level, ExhaustionLevel.LEVEL_1)
        self.assertEqual(len(loaded.inventory), 1)
        self.assertEqual(loaded.inventory[0].name, "Ancestral Longsword")
        self.assertTrue(loaded.inventory[0].is_equipped)
        self.assertIn(Skill.ATHLETICS, loaded.skill_proficiencies)
        self.assertIn(Ability.WIS, loaded.saving_throw_proficiencies)

    def test_quest_flags(self):
        save_id = "save-slot-alpha"
        self.db.set_quest_flag(save_id, "crypt_door_unlocked", "true")
        self.db.set_quest_flag(save_id, "gold_paid_to_innkeeper", "5")

        flag1 = self.db.get_quest_flag(save_id, "crypt_door_unlocked")
        self.assertEqual(flag1, "true")

        # Overwrite flag
        self.db.set_quest_flag(save_id, "gold_paid_to_innkeeper", "10")
        flag2 = self.db.get_quest_flag(save_id, "gold_paid_to_innkeeper")
        self.assertEqual(flag2, "10")

        all_flags = self.db.get_all_flags(save_id)
        self.assertEqual(len(all_flags), 2)
        self.assertEqual(all_flags["crypt_door_unlocked"], "true")


if __name__ == "__main__":
    unittest.main()
