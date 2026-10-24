"""
Grimoire 5E - SRD 5.1 Data Ingestion Seed
Pre-populates the SQLite database with official SRD 5.1 Spells, Monsters, and Items.
"""

from typing import Dict, Any, List
from core_5e.database import DatabaseManager


SRD_SPELLS: List[Dict[str, Any]] = [
    # Cantrips
    {
        "id": "fire-bolt",
        "name": "Fire Bolt",
        "level": 0,
        "school": "Evocation",
        "casting_time": "1 action",
        "range_area": "120 feet",
        "components": "V, S",
        "duration": "Instantaneous",
        "concentration": False,
        "ritual": False,
        "description": "You hurl a mote of fire at a creature or object within range. Make a ranged spell attack. On a hit, the target takes 1d10 fire damage.",
        "damage_dice": "1d10",
        "saving_throw_type": None
    },
    {
        "id": "eldritch-blast",
        "name": "Eldritch Blast",
        "level": 0,
        "school": "Evocation",
        "casting_time": "1 action",
        "range_area": "120 feet",
        "components": "V, S",
        "duration": "Instantaneous",
        "concentration": False,
        "ritual": False,
        "description": "A beam of crackling energy streaks toward a creature within range. Make a ranged spell attack. On a hit, the target takes 1d10 force damage.",
        "damage_dice": "1d10",
        "saving_throw_type": None
    },
    {
        "id": "sacred-flame",
        "name": "Sacred Flame",
        "level": 0,
        "school": "Evocation",
        "casting_time": "1 action",
        "range_area": "60 feet",
        "components": "V, S",
        "duration": "Instantaneous",
        "concentration": False,
        "ritual": False,
        "description": "Flame-like radiance descends on a creature you see. The target must succeed on a DEX save or take 1d8 radiant damage. The target gains no benefit from cover.",
        "damage_dice": "1d8",
        "saving_throw_type": "dexterity"
    },
    {
        "id": "guidance",
        "name": "Guidance",
        "level": 0,
        "school": "Divination",
        "casting_time": "1 action",
        "range_area": "Touch",
        "components": "V, S",
        "duration": "Concentration, up to 1 minute",
        "concentration": True,
        "ritual": False,
        "description": "You touch one willing creature. Once before the spell ends, the target can roll 1d4 and add the number rolled to one ability check of its choice.",
        "damage_dice": None,
        "saving_throw_type": None
    },
    # 1st Level
    {
        "id": "magic-missile",
        "name": "Magic Missile",
        "level": 1,
        "school": "Evocation",
        "casting_time": "1 action",
        "range_area": "120 feet",
        "components": "V, S",
        "duration": "Instantaneous",
        "concentration": False,
        "ritual": False,
        "description": "You create three glowing darts of magical force. Each dart hits a creature of your choice that you can see within range. A dart deals 1d4 + 1 force damage to its target.",
        "damage_dice": "3d4+3",
        "saving_throw_type": None
    },
    {
        "id": "cure-wounds",
        "name": "Cure Wounds",
        "level": 1,
        "school": "Evocation",
        "casting_time": "1 action",
        "range_area": "Touch",
        "components": "V, S",
        "duration": "Instantaneous",
        "concentration": False,
        "ritual": False,
        "description": "A creature you touch regains hit points equal to 1d8 + your spellcasting ability modifier. This spell has no effect on undead or constructs.",
        "damage_dice": "1d8",
        "saving_throw_type": None
    },
    {
        "id": "healing-word",
        "name": "Healing Word",
        "level": 1,
        "school": "Evocation",
        "casting_time": "1 bonus action",
        "range_area": "60 feet",
        "components": "V",
        "duration": "Instantaneous",
        "concentration": False,
        "ritual": False,
        "description": "A creature of your choice that you can see within range regains hit points equal to 1d4 + your spellcasting ability modifier.",
        "damage_dice": "1d4",
        "saving_throw_type": None
    },
    {
        "id": "shield",
        "name": "Shield",
        "level": 1,
        "school": "Abjuration",
        "casting_time": "1 reaction",
        "range_area": "Self",
        "components": "V, S",
        "duration": "1 round",
        "concentration": False,
        "ritual": False,
        "description": "An invisible barrier of magical force appears and protects you. Until the start of your next turn, you have a +5 bonus to AC, and you take no damage from magic missile.",
        "damage_dice": None,
        "saving_throw_type": None
    },
    {
        "id": "bless",
        "name": "Bless",
        "level": 1,
        "school": "Enchantment",
        "casting_time": "1 action",
        "range_area": "30 feet",
        "components": "V, S, M",
        "duration": "Concentration, up to 1 minute",
        "concentration": True,
        "ritual": False,
        "description": "You bless up to three creatures. Whenever a target makes an attack roll or a saving throw, the target can roll 1d4 and add the number rolled to the attack roll or save.",
        "damage_dice": None,
        "saving_throw_type": None
    },
    # 2nd Level
    {
        "id": "misty-step",
        "name": "Misty Step",
        "level": 2,
        "school": "Conjuration",
        "casting_time": "1 bonus action",
        "range_area": "Self",
        "components": "V",
        "duration": "Instantaneous",
        "concentration": False,
        "ritual": False,
        "description": "Briefly surrounded by silvery mist, you teleport up to 30 feet to an unoccupied space that you can see.",
        "damage_dice": None,
        "saving_throw_type": None
    },
    {
        "id": "scorching-ray",
        "name": "Scorching Ray",
        "level": 2,
        "school": "Evocation",
        "casting_time": "1 action",
        "range_area": "120 feet",
        "components": "V, S",
        "duration": "Instantaneous",
        "concentration": False,
        "ritual": False,
        "description": "You create three rays of fire and hurl them at targets within range. Make a ranged spell attack for each ray. On a hit, the target takes 2d6 fire damage.",
        "damage_dice": "2d6",
        "saving_throw_type": None
    },
    # 3rd Level
    {
        "id": "fireball",
        "name": "Fireball",
        "level": 3,
        "school": "Evocation",
        "casting_time": "1 action",
        "range_area": "150 feet (20-foot radius)",
        "components": "V, S, M",
        "duration": "Instantaneous",
        "concentration": False,
        "ritual": False,
        "description": "A bright streak flashes from your pointing finger to a point you choose. Each creature in a 20-foot-radius sphere must make a DEX save. A target takes 8d6 fire damage on a failed save, or half as much on a successful one.",
        "damage_dice": "8d6",
        "saving_throw_type": "dexterity"
    }
]


SRD_MONSTERS: List[Dict[str, Any]] = [
    {
        "id": "goblin",
        "name": "Goblin",
        "size": "Small",
        "monster_type": "Humanoid (goblinoid)",
        "alignment": "Neutral Evil",
        "armor_class": 15,
        "hit_points": 7,
        "hit_dice": "2d6",
        "speed_feet": 30,
        "strength": 8,
        "dexterity": 14,
        "constitution": 10,
        "intelligence": 10,
        "wisdom": 8,
        "charisma": 8,
        "challenge_rating": 0.25,
        "xp_value": 50,
        "traits": [
            {"name": "Nimble Escape", "description": "The goblin can take the Disengage or Hide action as a bonus action on each of its turns."}
        ],
        "actions": [
            {"name": "Scimitar", "type": "melee", "to_hit": 4, "reach": "5 ft", "damage": "1d6 + 2", "damage_type": "slashing"},
            {"name": "Shortbow", "type": "ranged", "to_hit": 4, "range": "80/320 ft", "damage": "1d6 + 2", "damage_type": "piercing"}
        ]
    },
    {
        "id": "goblin-boss",
        "name": "Goblin Boss",
        "size": "Small",
        "monster_type": "Humanoid (goblinoid)",
        "alignment": "Neutral Evil",
        "armor_class": 17,
        "hit_points": 21,
        "hit_dice": "6d6",
        "speed_feet": 30,
        "strength": 10,
        "dexterity": 14,
        "constitution": 10,
        "intelligence": 10,
        "wisdom": 8,
        "charisma": 10,
        "challenge_rating": 1.0,
        "xp_value": 200,
        "traits": [
            {"name": "Nimble Escape", "description": "Bonus action Disengage or Hide."}
        ],
        "actions": [
            {"name": "Multiattack", "description": "Makes two attacks with its scimitar."},
            {"name": "Scimitar", "type": "melee", "to_hit": 4, "reach": "5 ft", "damage": "1d6 + 2", "damage_type": "slashing"}
        ]
    },
    {
        "id": "skeleton",
        "name": "Skeleton",
        "size": "Medium",
        "monster_type": "Undead",
        "alignment": "Lawful Evil",
        "armor_class": 13,
        "hit_points": 13,
        "hit_dice": "2d8+4",
        "speed_feet": 30,
        "strength": 10,
        "dexterity": 14,
        "constitution": 15,
        "intelligence": 6,
        "wisdom": 8,
        "charisma": 5,
        "challenge_rating": 0.25,
        "xp_value": 50,
        "traits": [
            {"name": "Damage Vulnerability", "description": "Vulnerable to bludgeoning damage."}
        ],
        "actions": [
            {"name": "Shortsword", "type": "melee", "to_hit": 4, "reach": "5 ft", "damage": "1d6 + 2", "damage_type": "piercing"},
            {"name": "Shortbow", "type": "ranged", "to_hit": 4, "range": "80/320 ft", "damage": "1d6 + 2", "damage_type": "piercing"}
        ]
    },
    {
        "id": "wight",
        "name": "Wight",
        "size": "Medium",
        "monster_type": "Undead",
        "alignment": "Neutral Evil",
        "armor_class": 14,
        "hit_points": 45,
        "hit_dice": "6d8+18",
        "speed_feet": 30,
        "strength": 16,
        "dexterity": 14,
        "constitution": 16,
        "intelligence": 10,
        "wisdom": 13,
        "charisma": 15,
        "challenge_rating": 3.0,
        "xp_value": 700,
        "traits": [
            {"name": "Sunlight Sensitivity", "description": "Disadvantage on attack rolls and Perception checks in direct sunlight."}
        ],
        "actions": [
            {"name": "Multiattack", "description": "Makes two longsword attacks or two longbow attacks. It can use its Life Drain in place of one longsword attack."},
            {"name": "Life Drain", "type": "melee", "to_hit": 4, "damage": "1d6 + 2 necrotic", "description": "Target must succeed on a DC 13 CON save or its hit point maximum is reduced by an amount equal to the damage taken."},
            {"name": "Longsword", "type": "melee", "to_hit": 5, "reach": "5 ft", "damage": "1d8 + 3", "damage_type": "slashing"}
        ]
    }
]


SRD_ITEMS: List[Dict[str, Any]] = [
    # Weapons
    {"id": "longsword", "name": "Longsword", "category": "weapon", "cost_gp": 15, "weight_lbs": 3, "damage_formula": "1d8", "damage_type": "slashing", "properties": ["versatile (1d10)"], "description": "A classic martial blade favored by knights and paladins."},
    {"id": "shortsword", "name": "Shortsword", "category": "weapon", "cost_gp": 10, "weight_lbs": 2, "damage_formula": "1d6", "damage_type": "piercing", "properties": ["finesse", "light"], "description": "A nimble, thrusting blade ideal for rogues and skirmishers."},
    {"id": "greatsword", "name": "Greatsword", "category": "weapon", "cost_gp": 50, "weight_lbs": 6, "damage_formula": "2d6", "damage_type": "slashing", "properties": ["heavy", "two-handed"], "description": "A massive two-handed weapon designed for devastating cleaving strikes."},
    {"id": "shortbow", "name": "Shortbow", "category": "weapon", "cost_gp": 25, "weight_lbs": 2, "damage_formula": "1d6", "damage_type": "piercing", "properties": ["ammunition (80/320)", "two-handed"], "description": "A light wooden recurve bow."},
    # Armors
    {"id": "leather-armor", "name": "Leather Armor", "category": "armor", "cost_gp": 10, "weight_lbs": 10, "armor_class_bonus": 11, "properties": ["light armor (11 + DEX)"], "description": "Boiled and hardened cuirass that provides basic protection."},
    {"id": "chain-mail", "name": "Chain Mail", "category": "armor", "cost_gp": 75, "weight_lbs": 55, "armor_class_bonus": 16, "properties": ["heavy armor (16 AC)", "stealth disadvantage", "requires STR 13"], "description": "Interlocking steel rings covering the entire body."},
    {"id": "plate-armor", "name": "Plate Armor", "category": "armor", "cost_gp": 1500, "weight_lbs": 65, "armor_class_bonus": 18, "properties": ["heavy armor (18 AC)", "stealth disadvantage", "requires STR 15"], "description": "Interlocking fitted metal plates offering supreme protection."},
    {"id": "shield", "name": "Shield", "category": "armor", "cost_gp": 10, "weight_lbs": 6, "armor_class_bonus": 2, "properties": ["+2 AC"], "description": "A sturdy wooden and steel round shield strapped to the forearm."},
    # Consumables & Gear
    {"id": "potion-of-healing", "name": "Potion of Healing", "category": "potion", "cost_gp": 50, "weight_lbs": 0.5, "damage_formula": "2d4+2", "description": "A shimmering red liquid. Drinking it restores 2d4 + 2 hit points."},
    {"id": "thieves-tools", "name": "Thieves' Tools", "category": "gear", "cost_gp": 25, "weight_lbs": 1.0, "description": "A set of lockpicks, small files, and miniature mirrors for picking locks and disarming traps."}
]


def seed_database(db: DatabaseManager) -> Dict[str, int]:
    """Ingests all SRD catalogs into SQLite."""
    spells_count = 0
    monsters_count = 0
    items_count = 0

    for s in SRD_SPELLS:
        db.insert_spell(s)
        spells_count += 1

    for m in SRD_MONSTERS:
        db.insert_monster(m)
        monsters_count += 1

    for i in SRD_ITEMS:
        db.insert_item(i)
        items_count += 1

    return {
        "spells_ingested": spells_count,
        "monsters_ingested": monsters_count,
        "items_ingested": items_count
    }
