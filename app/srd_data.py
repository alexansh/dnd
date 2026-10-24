from typing import Dict, Any, List

SKILLS_MAP = {
    "Acrobatics": "dex",
    "Animal Handling": "wis",
    "Arcana": "int",
    "Athletics": "str",
    "Deception": "cha",
    "History": "int",
    "Insight": "wis",
    "Intimidation": "cha",
    "Investigation": "int",
    "Medicine": "wis",
    "Nature": "int",
    "Perception": "wis",
    "Performance": "cha",
    "Persuasion": "cha",
    "Religion": "int",
    "Sleight of Hand": "dex",
    "Stealth": "dex",
    "Survival": "wis"
}

CHARACTER_PRESETS: List[Dict[str, Any]] = [
    {
        "id": "valen-paladin",
        "name": "Sir Valen of Silverpeak",
        "race": "Human",
        "class_name": "Paladin",
        "level": 1,
        "background": "Noble",
        "alignment": "Lawful Good",
        "ability_scores": {
            "str": 16,
            "dex": 10,
            "con": 14,
            "int": 9,
            "wis": 12,
            "cha": 15
        },
        "hp_current": 12,
        "hp_max": 12,
        "armor_class": 18, # Chain mail (16) + Shield (2)
        "speed": 30,
        "initiative_bonus": 0,
        "proficiency_bonus": 2,
        "proficient_skills": ["Athletics", "Persuasion", "Religion", "Insight"],
        "saving_throws": ["wis", "cha"],
        "features_and_traits": [
            "Divine Sense (detect celestials, fiends, undead)",
            "Lay on Hands (5 HP pool)",
            "Fighting Style: Defense (+1 AC included)"
        ],
        "inventory": [
            {"name": "Longsword (1d8 slashing, versatile 1d10)", "quantity": 1, "description": "Ancestral silver-pommeled blade", "equipped": True},
            {"name": "Shield (+2 AC)", "quantity": 1, "description": "Steel shield bearing the sunburst emblem", "equipped": True},
            {"name": "Chain Mail", "quantity": 1, "description": "Interlocking metal rings", "equipped": True},
            {"name": "Javelins", "quantity": 5, "description": "Thrown spears (1d6 piercing, 30/120 ft)", "equipped": False},
            {"name": "Explorer's Pack & Rations", "quantity": 1, "description": "Bedroll, mess kit, tinderbox, 10 torches, 10 days rations, waterskin", "equipped": False},
            {"name": "Holy Symbol Amulet", "quantity": 1, "description": "Silver sunburst amulet", "equipped": True}
        ],
        "currency": {"cp": 0, "sp": 5, "ep": 0, "gp": 25, "pp": 0},
        "spellcasting_ability": "cha",
        "spell_save_dc": 12,
        "spell_attack_bonus": 4,
        "spell_slots": {"1": {"total": 0, "used": 0}},
        "known_spells": [],
        "backstory": "Sworn to protect the innocent and cleanse corruption from the realm."
    },
    {
        "id": "elira-rogue",
        "name": "Elira Moonshadow",
        "race": "Elf (Wood)",
        "class_name": "Rogue",
        "level": 1,
        "background": "Criminal / Spy",
        "alignment": "Chaotic Neutral",
        "ability_scores": {
            "str": 10,
            "dex": 17,
            "con": 13,
            "int": 12,
            "wis": 14,
            "cha": 12
        },
        "hp_current": 9,
        "hp_max": 9,
        "armor_class": 14, # Leather armor + Dex (+3)
        "speed": 35,
        "initiative_bonus": 3,
        "proficiency_bonus": 2,
        "proficient_skills": ["Stealth", "Acrobatics", "Deception", "Perception", "Sleight of Hand", "Investigation"],
        "saving_throws": ["dex", "int"],
        "features_and_traits": [
            "Sneak Attack (+1d6 damage when having advantage or adjacent ally)",
            "Expertise: Stealth, Thieves' Tools",
            "Fey Ancestry (Advantage vs charm, magic can't put you to sleep)",
            "Darkvision 60ft"
        ],
        "inventory": [
            {"name": "Rapier (1d8 piercing, finesse)", "quantity": 1, "description": "Slender, deadly steel", "equipped": True},
            {"name": "Shortbow (1d6 piercing, 80/320 ft)", "quantity": 1, "description": "Elven yew bow", "equipped": True},
            {"name": "Quiver with Arrows", "quantity": 20, "description": "Fletched hunting arrows", "equipped": True},
            {"name": "Leather Armor", "quantity": 1, "description": "Supple dyed leather", "equipped": True},
            {"name": "Daggers", "quantity": 2, "description": "Concealed 1d4 piercing blades", "equipped": False},
            {"name": "Thieves' Tools", "quantity": 1, "description": "Lockpicks and wire probes", "equipped": False},
            {"name": "Burglar's Pack", "quantity": 1, "description": "Rope, pitons, crowbar, hooded lantern", "equipped": False}
        ],
        "currency": {"cp": 0, "sp": 12, "ep": 0, "gp": 15, "pp": 0},
        "spellcasting_ability": None,
        "spell_slots": {},
        "known_spells": [],
        "backstory": "A shadow in the city alleyways who took a dangerous job to clear a debt."
    },
    {
        "id": "zephyr-wizard",
        "name": "Zephyr of the Azure Spire",
        "race": "Elf (High)",
        "class_name": "Wizard",
        "level": 1,
        "background": "Sage",
        "alignment": "Neutral",
        "ability_scores": {
            "str": 8,
            "dex": 14,
            "con": 13,
            "int": 16,
            "wis": 13,
            "cha": 11
        },
        "hp_current": 7,
        "hp_max": 7,
        "armor_class": 12,
        "speed": 30,
        "initiative_bonus": 2,
        "proficiency_bonus": 2,
        "proficient_skills": ["Arcana", "History", "Investigation", "Insight"],
        "saving_throws": ["int", "wis"],
        "features_and_traits": [
            "Arcane Recovery (recover 1 spell slot on short rest)",
            "Spellcasting (Intelligence)",
            "Fey Ancestry",
            "Darkvision 60ft"
        ],
        "inventory": [
            {"name": "Quarterstaff (1d6 bludgeoning)", "quantity": 1, "description": "Carved with runic patterns", "equipped": True},
            {"name": "Arcane Focus (Crystal Orb)", "quantity": 1, "description": "Glowing faint blue crystal", "equipped": True},
            {"name": "Spellbook", "quantity": 1, "description": "Leather-bound tome containing arcane formulas", "equipped": True},
            {"name": "Scholar's Pack", "quantity": 1, "description": "Ink, quill, parchment, books, candle", "equipped": False}
        ],
        "currency": {"cp": 0, "sp": 0, "ep": 0, "gp": 30, "pp": 0},
        "spellcasting_ability": "int",
        "spell_save_dc": 13,
        "spell_attack_bonus": 5,
        "spell_slots": {"1": {"total": 2, "used": 0}},
        "known_spells": [
            {"name": "Fire Bolt", "level": 0, "school": "Evocation", "description": "1d10 fire damage at 120ft range"},
            {"name": "Mage Hand", "level": 0, "school": "Conjuration", "description": "Spectral hand moves objects up to 10 lbs"},
            {"name": "Prestidigitation", "level": 0, "school": "Transmutation", "description": "Minor sensory effects, lighting fires, cleaning"},
            {"name": "Magic Missile", "level": 1, "school": "Evocation", "description": "Three darts dealing 1d4+1 force damage each, auto-hit"},
            {"name": "Shield", "level": 1, "school": "Abjuration", "description": "Reaction: +5 to AC until start of next turn"},
            {"name": "Burning Hands", "level": 1, "school": "Evocation", "description": "15ft cone, 3d6 fire damage (Dex save for half)"},
            {"name": "Detect Magic", "level": 1, "school": "Divination", "description": "Sense presence of magic within 30ft"}
        ],
        "backstory": "Seeking ancient arcane secrets and forgotten lore in uncharted ruins."
    },
    {
        "id": "thorgar-barbarian",
        "name": "Thorgar Ironbreaker",
        "race": "Dwarf (Mountain)",
        "class_name": "Barbarian",
        "level": 1,
        "background": "Outlander",
        "alignment": "Chaotic Good",
        "ability_scores": {
            "str": 16,
            "dex": 13,
            "con": 16,
            "int": 8,
            "wis": 12,
            "cha": 9
        },
        "hp_current": 15,
        "hp_max": 15,
        "armor_class": 14, # Unarmored Defense: 10 + Dex (+1) + Con (+3)
        "speed": 25,
        "initiative_bonus": 1,
        "proficiency_bonus": 2,
        "proficient_skills": ["Athletics", "Intimidation", "Survival", "Perception"],
        "saving_throws": ["str", "con"],
        "features_and_traits": [
            "Rage (2/day: +2 melee damage, resistance to bludgeoning/piercing/slashing)",
            "Unarmored Defense (AC = 10 + DEX + CON)",
            "Dwarven Resilience (Advantage on saves vs poison, resistance to poison damage)",
            "Darkvision 60ft"
        ],
        "inventory": [
            {"name": "Greataxe (1d12 slashing, heavy, two-handed)", "quantity": 1, "description": "Massive battle-worn dwarven axe", "equipped": True},
            {"name": "Handaxes", "quantity": 2, "description": "1d6 slashing, thrown (20/60 ft)", "equipped": False},
            {"name": "Javelins", "quantity": 4, "description": "1d6 piercing, thrown", "equipped": False},
            {"name": "Explorer's Pack", "quantity": 1, "description": "Rations, bedroll, torches, rope", "equipped": False}
        ],
        "currency": {"cp": 0, "sp": 8, "ep": 0, "gp": 10, "pp": 0},
        "spellcasting_ability": None,
        "spell_slots": {},
        "known_spells": [],
        "backstory": "Exiled from his mountain clan, he carves his own legend across the wild frontiers."
    }
]

STARTER_SCENARIOS = [
    {
        "id": "ambush-road",
        "title": "The Ambush on the King's Road",
        "description": "You were hired by a dwarven merchant named Gundren to guard a supply wagon bound for the frontier settlement of Phandalin. Rounding a bend in the High Road, you spot two dead horses blocking the narrow trail, arrows protruding from their ribs, and ominous rustling in the dense briars.",
        "location": "The King's High Road - Triboar Trail",
        "initial_quest": {
            "title": "Investigate the Wagon & Trail",
            "description": "Approach cautiously, inspect the dead horses, or prepare for an imminent ambush from the brush."
        }
    },
    {
        "id": "catacombs",
        "title": "The Whispering Crypt of Oakhaven",
        "description": "Underneath the weeping willows of Oakhaven's ancient cemetery, a stone mausoleum door has been blasted inward with necrotic force. The local parish priest begged for your aid: cold blue flames flicker below, and hollow wailing echoes from the crypt depths.",
        "location": "Oakhaven Parish Cemetery - Crypt Entrance",
        "initial_quest": {
            "title": "Cleanse the Crypt",
            "description": "Descend into the ancient stone crypt and discover the source of the dark disturbance."
        }
    },
    {
        "id": "tavern-mystery",
        "title": "Trouble at the Dancing Boar Tavern",
        "description": "Rain lashes against the stained-glass windows of the cozy tavern. Suddenly, the heavy oak door bangs open: a mud-spattered courier collapses onto the hearth rug clutching a wax-sealed missive, whispering of bandits at the old watchtower before losing consciousness.",
        "location": "The Dancing Boar Tavern - Hearth Room",
        "initial_quest": {
            "title": "The Courier's Message",
            "description": "Examine the wounded courier, read the sealed letter, or speak with the startled tavern patrons."
        }
    }
]
