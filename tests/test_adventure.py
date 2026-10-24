"""
Unit Tests for Grimoire 5E Adventure Engine & State Machine
"""

import os
import tempfile
import unittest

from core_5e.adventure import AdventureEngine
from core_5e.campaign_data import build_campaign_1_nodes
from core_5e.character import CharacterBuilder, CharacterClass
from core_5e.database import DatabaseManager
from core_5e.dice import DiceEngine
from core_5e.mechanics import Ability, Skill


class TestAdventureEngine(unittest.TestCase):
    def setUp(self):
        self.temp_file = tempfile.NamedTemporaryFile(delete=False, suffix=".db")
        self.temp_file.close()
        self.db = DatabaseManager(self.temp_file.name)
        # Fixed seed for predictable dice rolls during tests
        self.dice = DiceEngine(rng_seed=999)
        self.engine = AdventureEngine(self.db, self.dice)

        for node in build_campaign_1_nodes():
            self.engine.register_node(node)

        scores = {
            Ability.STR: 16,
            Ability.DEX: 14,
            Ability.CON: 14,
            Ability.INT: 10,
            Ability.WIS: 12,
            Ability.CHA: 10
        }
        self.character = CharacterBuilder.create_level_1(
            char_id="hero-test-1",
            name="Sir Valen",
            race="Human",
            char_class=CharacterClass.FIGHTER,
            scores=scores,
            skill_choices=[Skill.ATHLETICS, Skill.PERCEPTION, Skill.INVESTIGATION]
        )
        self.db.save_character(self.character)
        self.save_id = "test-save-slot"

    def tearDown(self):
        if os.path.exists(self.temp_file.name):
            try:
                os.remove(self.temp_file.name)
            except PermissionError:
                pass

    def test_adventure_start_and_narration(self):
        node = self.engine.start_adventure(self.save_id, "c1_tavern")
        self.assertEqual(node.node_id, "c1_tavern")
        self.assertEqual(node.title, "The Drunken Dragon Tavern")
        narration = self.engine.get_current_narration()
        self.assertIn("A crackling hearth warms", narration)

    def test_direct_transition_and_backtracking(self):
        self.engine.start_adventure(self.save_id, "c1_tavern")
        
        # Advance: Tavern -> Graveyard
        res = self.engine.execute_choice(self.save_id, "depart_graveyard", self.character)
        self.assertEqual(res.next_node_id, "c1_graveyard")
        self.assertEqual(self.engine.current_node_id, "c1_graveyard")

        # Backtrack: Graveyard -> Tavern
        res2 = self.engine.execute_choice(self.save_id, "backtrack_to_tavern", self.character)
        self.assertEqual(res2.next_node_id, "c1_tavern")
        self.assertEqual(self.engine.current_node_id, "c1_tavern")

        # Verify re-entrant room text is used when backtracking!
        revisited_text = self.engine.get_current_narration()
        self.assertIn("You step back into the warmth of the Drunken Dragon Tavern", revisited_text)

    def test_flag_gated_choices(self):
        self.engine.start_adventure(self.save_id, "c1_crypt_doors")

        # Initially, player does NOT have crypt key
        choices_initial = self.engine.get_available_choices(self.save_id)
        choice_ids = [c.id for c in choices_initial]
        self.assertNotIn("use_key", choice_ids)
        self.assertIn("pick_lock", choice_ids)
        self.assertIn("force_doors", choice_ids)

        # Set the flag as if player found the key
        self.db.set_quest_flag(self.save_id, "has_crypt_key", "true")

        # Now "use_key" must be available
        choices_after = self.engine.get_available_choices(self.save_id)
        choice_ids_after = [c.id for c in choices_after]
        self.assertIn("use_key", choice_ids_after)

    def test_combat_trigger(self):
        self.engine.start_adventure(self.save_id, "c1_crypt_foyer")
        
        # Move into the sanctum
        self.engine.current_node_id = "c1_crypt_foyer"
        # We can simulate stepping into sanctum
        node = self.engine.nodes["c1_sanctum_entrance"]
        self.assertTrue(bool(node.monster_ids))
        self.assertIn("skeleton", node.monster_ids)
        self.assertIn("wight", node.monster_ids)


if __name__ == "__main__":
    unittest.main()
