"""
Grimoire 5E - Interactive Offline Adventure Runner
Play through Campaign 1 (The Sunken King's Legacy) right in your terminal.
Uses the 100% offline, deterministic 5E Rules and Adventure Engine.
"""

import sys
import time

# Ensure UTF-8 output on Windows consoles
if hasattr(sys.stdout, "reconfigure"):
    try:
        sys.stdout.reconfigure(encoding="utf-8")
    except Exception:
        pass

from core_5e.adventure import AdventureEngine
from core_5e.campaign_data import build_campaign_1_nodes
from core_5e.character import CharacterBuilder, CharacterClass, Item
from core_5e.database import DatabaseManager
from core_5e.dice import DiceEngine
from core_5e.mechanics import Ability, Skill


def print_banner():
    print("=" * 65)
    print("      [+] GRIMOIRE 5E: THE TABLETOP CHRONICLE [+]")
    print("   100% Offline D&D 5th Edition Engine & Adventure Runner")
    print("=" * 65)
    print()


def create_default_character(db: DatabaseManager) -> str:
    print("[1] Sir Valen of Silverpeak (Paladin Lv.1 - Tank & Holy Smite)")
    print("[2] Elira Moonshadow (Rogue Lv.1 - Stealth & Locks)")
    print("[3] Zephyr of the Spire (Wizard Lv.1 - Spells & Arcana)")
    choice = input("\nChoose your adventurer (1-3, default 1): ").strip()

    if choice == "2":
        scores = {Ability.STR: 10, Ability.DEX: 16, Ability.CON: 14, Ability.INT: 12, Ability.WIS: 12, Ability.CHA: 10}
        char = CharacterBuilder.create_level_1(
            "hero_elira", "Elira Moonshadow", "Elf", CharacterClass.ROGUE,
            scores, [Skill.STEALTH, Skill.SLEIGHT_OF_HAND, Skill.ACROBATICS, Skill.PERCEPTION]
        )
        char.inventory.append(Item("shortsword", "Shortsword", 1, is_equipped=True, damage_formula="1d6+3"))
        char.inventory.append(Item("leather", "Leather Armor", 1, is_equipped=True, armor_class_bonus=11))
    elif choice == "3":
        scores = {Ability.STR: 8, Ability.DEX: 14, Ability.CON: 14, Ability.INT: 16, Ability.WIS: 12, Ability.CHA: 10}
        char = CharacterBuilder.create_level_1(
            "hero_zephyr", "Zephyr of the Spire", "High Elf", CharacterClass.WIZARD,
            scores, [Skill.ARCANA, Skill.INVESTIGATION, Skill.HISTORY]
        )
        char.inventory.append(Item("quarterstaff", "Quarterstaff", 1, is_equipped=True, damage_formula="1d6"))
    else:
        scores = {Ability.STR: 16, Ability.DEX: 10, Ability.CON: 14, Ability.INT: 9, Ability.WIS: 12, Ability.CHA: 15}
        char = CharacterBuilder.create_level_1(
            "hero_valen", "Sir Valen", "Human", CharacterClass.PALADIN,
            scores, [Skill.ATHLETICS, Skill.PERSUASION, Skill.RELIGION]
        )
        char.inventory.append(Item("longsword", "Ancestral Longsword", 1, is_equipped=True, damage_formula="1d8+3"))
        char.inventory.append(Item("chain_mail", "Chain Mail", 1, is_equipped=True, armor_class_bonus=16))
        char.inventory.append(Item("shield", "Shield", 1, is_equipped=True, armor_class_bonus=2))

    db.save_character(char)
    print(f"\n[Created] {char.name} ({char.character_class.value} Lv.{char.level})")
    print(f"HP: {char.hp_current}/{char.hp_max} | AC: {char.armor_class} | Gold: {char.gold}gp")
    return char.id


def main():
    print_banner()
    db = DatabaseManager("grimoire_5e.db")
    dice = DiceEngine()
    engine = AdventureEngine(db, dice)

    for node in build_campaign_1_nodes():
        engine.register_node(node)

    char_id = create_default_character(db)
    character = db.load_character(char_id)

    save_id = f"save_{char_id}"
    engine.start_adventure(save_id, "c1_tavern")

    print("\n" + "~" * 65)
    print("Campaign 1: The Sunken King's Legacy (Act I: The Drowned Crypt)")
    print("~" * 65 + "\n")

    while True:
        node = engine.get_current_node()
        narration = engine.get_current_narration()

        print(f"\n>> LOCATION: {node.title.upper()}")
        print("-" * 50)
        print(f"HP: {character.hp_current}/{character.hp_max} | AC: {character.armor_class} | Gold: {character.gold} GP")
        print("-" * 50)
        print(f"\n[DM] {narration}\n")

        # Check combat trigger
        if node.monster_ids:
            print("[!] ENCOUNTER DETECTED! Hostiles present:")
            for mid in node.monster_ids:
                m = db.get_monster(mid)
                m_name = m['name'] if m else mid
                m_hp = m['hit_points'] if m else '?'
                m_ac = m['armor_class'] if m else '?'
                print(f"   * {m_name} (HP: {m_hp}, AC: {m_ac})")
            print()

        # Display Available Choices
        choices = engine.get_available_choices(save_id)
        if not choices:
            print("You have reached the end of this chapter.")
            break

        print("WHAT WOULD YOU LIKE TO DO?")
        for idx, c in enumerate(choices, 1):
            dc_tag = f" [{c.skill_check.skill.value} DC {c.skill_check.dc}]" if c.skill_check else ""
            print(f"  [{idx}] {c.text}{dc_tag}")

        print("  [Q] Save & Quit Game")

        user_input = input("\nEnter choice: ").strip()
        if user_input.lower() == "q":
            print("\nProgress saved locally in SQLite. Fare thee well, adventurer!")
            break

        try:
            choice_idx = int(user_input) - 1
            if 0 <= choice_idx < len(choices):
                selected = choices[choice_idx]
                result = engine.execute_choice(save_id, selected.id, character)
                if result.narrative_log:
                    print(f"\n[DICE] {result.narrative_log}")
                    time.sleep(0.3)
            else:
                print("Invalid choice number.")
        except ValueError:
            print("Please enter a valid choice number.")


if __name__ == "__main__":
    main()
