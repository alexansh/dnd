"""
Grimoire 5E - SQLite Local Database Layer
Provides schema migration, persistence for characters, save states,
and ingestion for SRD 5.1 spells, monsters, items, and campaign nodes.
"""

from contextlib import contextmanager
import json
import os
import sqlite3
from typing import Any, Dict, List, Optional, Tuple

from core_5e.character import Character, CharacterClass, Item, SpellSlotPool
from core_5e.mechanics import Ability, Skill, ExhaustionLevel, DeathSaveTracker


SCHEMA_SQL = """
PRAGMA journal_mode = WAL;
PRAGMA foreign_keys = ON;

-- 1. Characters Table
CREATE TABLE IF NOT EXISTS characters (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    race TEXT NOT NULL,
    character_class TEXT NOT NULL,
    subclass TEXT,
    level INTEGER NOT NULL DEFAULT 1,
    xp INTEGER NOT NULL DEFAULT 0,
    strength INTEGER NOT NULL,
    dexterity INTEGER NOT NULL,
    constitution INTEGER NOT NULL,
    intelligence INTEGER NOT NULL,
    wisdom INTEGER NOT NULL,
    charisma INTEGER NOT NULL,
    hp_current INTEGER NOT NULL,
    hp_max INTEGER NOT NULL,
    temp_hp INTEGER NOT NULL DEFAULT 0,
    hit_dice_current INTEGER NOT NULL DEFAULT 1,
    hit_dice_max INTEGER NOT NULL DEFAULT 1,
    base_ac INTEGER NOT NULL DEFAULT 10,
    speed_feet INTEGER NOT NULL DEFAULT 30,
    inspiration BOOLEAN NOT NULL DEFAULT 0,
    exhaustion_level INTEGER NOT NULL DEFAULT 0,
    saving_throws_json TEXT NOT NULL,
    skills_json TEXT NOT NULL,
    gold INTEGER NOT NULL DEFAULT 0,
    silver INTEGER NOT NULL DEFAULT 0,
    copper INTEGER NOT NULL DEFAULT 0,
    spell_slots_json TEXT NOT NULL DEFAULT '{}',
    known_spells_json TEXT NOT NULL DEFAULT '[]',
    prepared_spells_json TEXT NOT NULL DEFAULT '[]',
    inventory_json TEXT NOT NULL DEFAULT '[]',
    active_conditions_json TEXT NOT NULL DEFAULT '[]',
    death_saves_json TEXT NOT NULL DEFAULT '{}',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Spells Catalog (SRD 5.1)
CREATE TABLE IF NOT EXISTS spells (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL UNIQUE,
    level INTEGER NOT NULL,
    school TEXT NOT NULL,
    casting_time TEXT NOT NULL,
    range_area TEXT NOT NULL,
    components TEXT NOT NULL,
    duration TEXT NOT NULL,
    concentration BOOLEAN NOT NULL DEFAULT 0,
    ritual BOOLEAN NOT NULL DEFAULT 0,
    description TEXT NOT NULL,
    damage_dice TEXT,
    saving_throw_type TEXT
);

-- 3. Monsters Catalog (SRD 5.1)
CREATE TABLE IF NOT EXISTS monsters (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL UNIQUE,
    size TEXT NOT NULL,
    monster_type TEXT NOT NULL,
    alignment TEXT NOT NULL,
    armor_class INTEGER NOT NULL,
    hit_points INTEGER NOT NULL,
    hit_dice TEXT NOT NULL,
    speed_feet INTEGER NOT NULL,
    strength INTEGER NOT NULL,
    dexterity INTEGER NOT NULL,
    constitution INTEGER NOT NULL,
    intelligence INTEGER NOT NULL,
    wisdom INTEGER NOT NULL,
    charisma INTEGER NOT NULL,
    challenge_rating REAL NOT NULL,
    xp_value INTEGER NOT NULL,
    traits_json TEXT NOT NULL DEFAULT '[]',
    actions_json TEXT NOT NULL DEFAULT '[]'
);

-- 4. Equipment and Items Catalog (SRD 5.1)
CREATE TABLE IF NOT EXISTS items (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL UNIQUE,
    category TEXT NOT NULL, -- weapon, armor, potion, gear
    cost_gp REAL NOT NULL DEFAULT 0,
    weight_lbs REAL NOT NULL DEFAULT 0,
    armor_class_bonus INTEGER NOT NULL DEFAULT 0,
    damage_formula TEXT,
    damage_type TEXT,
    properties_json TEXT NOT NULL DEFAULT '[]',
    description TEXT NOT NULL
);

-- 5. Campaign & Adventure Nodes
CREATE TABLE IF NOT EXISTS adventure_nodes (
    node_id TEXT PRIMARY KEY,
    campaign_id TEXT NOT NULL,
    chapter_index INTEGER NOT NULL DEFAULT 1,
    title TEXT NOT NULL,
    room_illustration_key TEXT,
    ambient_audio_track TEXT,
    narration_template TEXT NOT NULL,
    return_narration_template TEXT,
    choices_json TEXT NOT NULL DEFAULT '[]',
    encounter_json TEXT,
    is_rest_area BOOLEAN NOT NULL DEFAULT 0
);

-- 6. Saved Game States & World Flags
CREATE TABLE IF NOT EXISTS quest_flags (
    save_id TEXT NOT NULL,
    flag_key TEXT NOT NULL,
    flag_value TEXT NOT NULL,
    PRIMARY KEY (save_id, flag_key)
);

-- 7. Mid-Combat State Snapshots
CREATE TABLE IF NOT EXISTS combat_snapshots (
    save_id TEXT PRIMARY KEY,
    encounter_id TEXT NOT NULL,
    round_number INTEGER NOT NULL DEFAULT 1,
    current_turn_index INTEGER NOT NULL DEFAULT 0,
    combatants_json TEXT NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_spells_level ON spells(level);
CREATE INDEX IF NOT EXISTS idx_monsters_cr ON monsters(challenge_rating);
CREATE INDEX IF NOT EXISTS idx_nodes_campaign ON adventure_nodes(campaign_id);
"""


class DatabaseManager:
    """Manages SQLite connection, schema migrations, and relational queries."""

    def __init__(self, db_path: str = "grimoire_5e.db"):
        self.db_path = db_path
        self._init_db()

    @contextmanager
    def get_connection(self):
        conn = sqlite3.connect(self.db_path)
        conn.row_factory = sqlite3.Row
        try:
            yield conn
            conn.commit()
        except Exception:
            conn.rollback()
            raise
        finally:
            conn.close()

    def _init_db(self):
        with self.get_connection() as conn:
            conn.executescript(SCHEMA_SQL)

    # -------------------------------------------------------------
    # Character Persistence
    # -------------------------------------------------------------
    def save_character(self, char: Character) -> None:
        """Serializes and saves or updates a Character in SQLite."""
        slots_dict = {
            lvl: {"level": p.level, "current": p.current, "maximum": p.maximum}
            for lvl, p in char.spell_slots.items()
        }
        inv_list = [
            {
                "id": it.id, "name": it.name, "quantity": it.quantity,
                "weight_lbs": it.weight_lbs, "is_equipped": it.is_equipped,
                "is_attuned": it.is_attuned, "armor_class_bonus": it.armor_class_bonus,
                "damage_formula": it.damage_formula, "description": it.description
            }
            for it in char.inventory
        ]
        death_saves_dict = {
            "successes": char.death_saves.successes,
            "failures": char.death_saves.failures,
            "is_stabilized": char.death_saves.is_stabilized,
            "is_dead": char.death_saves.is_dead
        }

        sql = """
        INSERT INTO characters (
            id, name, race, character_class, subclass, level, xp,
            strength, dexterity, constitution, intelligence, wisdom, charisma,
            hp_current, hp_max, temp_hp, hit_dice_current, hit_dice_max,
            base_ac, speed_feet, inspiration, exhaustion_level,
            saving_throws_json, skills_json, gold, silver, copper,
            spell_slots_json, known_spells_json, prepared_spells_json,
            inventory_json, active_conditions_json, death_saves_json, updated_at
        ) VALUES (
            ?, ?, ?, ?, ?, ?, ?,
            ?, ?, ?, ?, ?, ?,
            ?, ?, ?, ?, ?,
            ?, ?, ?, ?,
            ?, ?, ?, ?, ?,
            ?, ?, ?,
            ?, ?, ?, CURRENT_TIMESTAMP
        )
        ON CONFLICT(id) DO UPDATE SET
            name=excluded.name, race=excluded.race, character_class=excluded.character_class,
            subclass=excluded.subclass, level=excluded.level, xp=excluded.xp,
            strength=excluded.strength, dexterity=excluded.dexterity, constitution=excluded.constitution,
            intelligence=excluded.intelligence, wisdom=excluded.wisdom, charisma=excluded.charisma,
            hp_current=excluded.hp_current, hp_max=excluded.hp_max, temp_hp=excluded.temp_hp,
            hit_dice_current=excluded.hit_dice_current, hit_dice_max=excluded.hit_dice_max,
            base_ac=excluded.base_ac, speed_feet=excluded.speed_feet,
            inspiration=excluded.inspiration, exhaustion_level=excluded.exhaustion_level,
            saving_throws_json=excluded.saving_throws_json, skills_json=excluded.skills_json,
            gold=excluded.gold, silver=excluded.silver, copper=excluded.copper,
            spell_slots_json=excluded.spell_slots_json, known_spells_json=excluded.known_spells_json,
            prepared_spells_json=excluded.prepared_spells_json, inventory_json=excluded.inventory_json,
            active_conditions_json=excluded.active_conditions_json,
            death_saves_json=excluded.death_saves_json, updated_at=CURRENT_TIMESTAMP;
        """

        with self.get_connection() as conn:
            conn.execute(sql, (
                char.id, char.name, char.race, char.character_class.value, char.subclass,
                char.level, char.xp, char.strength, char.dexterity, char.constitution,
                char.intelligence, char.wisdom, char.charisma,
                char.hp_current, char.hp_max, char.temp_hp,
                char.hit_dice_current, char.hit_dice_max,
                char.base_ac, char.speed_feet, 1 if char.inspiration else 0,
                int(char.exhaustion_level),
                json.dumps([a.value for a in char.saving_throw_proficiencies]),
                json.dumps([s.value for s in char.skill_proficiencies]),
                char.gold, char.silver, char.copper,
                json.dumps(slots_dict),
                json.dumps(char.known_spells),
                json.dumps(char.prepared_spells),
                json.dumps(inv_list),
                json.dumps(char.active_conditions),
                json.dumps(death_saves_dict)
            ))

    def load_character(self, char_id: str) -> Optional[Character]:
        """Loads and reconstructs a Character instance from SQLite."""
        sql = "SELECT * FROM characters WHERE id = ?"
        with self.get_connection() as conn:
            row = conn.execute(sql, (char_id,)).fetchone()
            if not row:
                return None

            saves = [Ability(a) for a in json.loads(row["saving_throws_json"])]
            skills = [Skill(s) for s in json.loads(row["skills_json"])]

            char = Character(
                id=row["id"],
                name=row["name"],
                race=row["race"],
                character_class=CharacterClass(row["character_class"]),
                subclass=row["subclass"],
                level=row["level"],
                xp=row["xp"],
                strength=row["strength"],
                dexterity=row["dexterity"],
                constitution=row["constitution"],
                intelligence=row["intelligence"],
                wisdom=row["wisdom"],
                charisma=row["charisma"],
                hp_current=row["hp_current"],
                hp_max=row["hp_max"],
                temp_hp=row["temp_hp"],
                hit_dice_current=row["hit_dice_current"],
                hit_dice_max=row["hit_dice_max"],
                base_ac=row["base_ac"],
                speed_feet=row["speed_feet"],
                inspiration=bool(row["inspiration"]),
                exhaustion_level=ExhaustionLevel(row["exhaustion_level"]),
                saving_throw_proficiencies=saves,
                skill_proficiencies=skills,
                gold=row["gold"],
                silver=row["silver"],
                copper=row["copper"],
                known_spells=json.loads(row["known_spells_json"]),
                prepared_spells=json.loads(row["prepared_spells_json"]),
                active_conditions=json.loads(row["active_conditions_json"]),
            )

            # Reconstruct spell slots
            raw_slots = json.loads(row["spell_slots_json"])
            for lvl_str, s in raw_slots.items():
                char.spell_slots[int(lvl_str)] = SpellSlotPool(
                    level=s["level"], current=s["current"], maximum=s["maximum"]
                )

            # Reconstruct inventory
            raw_inv = json.loads(row["inventory_json"])
            char.inventory = [
                Item(
                    id=it["id"], name=it["name"], quantity=it["quantity"],
                    weight_lbs=it["weight_lbs"], is_equipped=it["is_equipped"],
                    is_attuned=it["is_attuned"], armor_class_bonus=it["armor_class_bonus"],
                    damage_formula=it.get("damage_formula"), description=it["description"]
                )
                for it in raw_inv
            ]

            # Reconstruct death saves
            ds_raw = json.loads(row["death_saves_json"])
            char.death_saves = DeathSaveTracker(
                successes=ds_raw.get("successes", 0),
                failures=ds_raw.get("failures", 0),
                is_stabilized=ds_raw.get("is_stabilized", False),
                is_dead=ds_raw.get("is_dead", False)
            )

            return char

    # -------------------------------------------------------------
    # SRD Ingestion & Retrieval
    # -------------------------------------------------------------
    def insert_spell(self, spell_data: Dict[str, Any]) -> None:
        sql = """
        INSERT INTO spells (
            id, name, level, school, casting_time, range_area,
            components, duration, concentration, ritual, description,
            damage_dice, saving_throw_type
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        ON CONFLICT(id) DO UPDATE SET
            name=excluded.name, level=excluded.level, school=excluded.school,
            casting_time=excluded.casting_time, range_area=excluded.range_area,
            components=excluded.components, duration=excluded.duration,
            concentration=excluded.concentration, ritual=excluded.ritual,
            description=excluded.description, damage_dice=excluded.damage_dice,
            saving_throw_type=excluded.saving_throw_type;
        """
        with self.get_connection() as conn:
            conn.execute(sql, (
                spell_data["id"], spell_data["name"], spell_data["level"],
                spell_data["school"], spell_data["casting_time"], spell_data["range_area"],
                spell_data["components"], spell_data["duration"],
                1 if spell_data.get("concentration") else 0,
                1 if spell_data.get("ritual") else 0,
                spell_data["description"], spell_data.get("damage_dice"),
                spell_data.get("saving_throw_type")
            ))

    def get_spell(self, spell_id: str) -> Optional[Dict[str, Any]]:
        with self.get_connection() as conn:
            row = conn.execute("SELECT * FROM spells WHERE id = ?", (spell_id,)).fetchone()
            return dict(row) if row else None

    def get_spells_by_level(self, level: int) -> List[Dict[str, Any]]:
        with self.get_connection() as conn:
            rows = conn.execute("SELECT * FROM spells WHERE level = ? ORDER BY name ASC", (level,)).fetchall()
            return [dict(r) for r in rows]

    def insert_monster(self, m: Dict[str, Any]) -> None:
        sql = """
        INSERT INTO monsters (
            id, name, size, monster_type, alignment, armor_class, hit_points,
            hit_dice, speed_feet, strength, dexterity, constitution,
            intelligence, wisdom, charisma, challenge_rating, xp_value,
            traits_json, actions_json
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        ON CONFLICT(id) DO UPDATE SET
            name=excluded.name, armor_class=excluded.armor_class,
            hit_points=excluded.hit_points, actions_json=excluded.actions_json;
        """
        with self.get_connection() as conn:
            conn.execute(sql, (
                m["id"], m["name"], m["size"], m["monster_type"], m["alignment"],
                m["armor_class"], m["hit_points"], m["hit_dice"], m["speed_feet"],
                m["strength"], m["dexterity"], m["constitution"],
                m["intelligence"], m["wisdom"], m["charisma"],
                m["challenge_rating"], m["xp_value"],
                json.dumps(m.get("traits", [])),
                json.dumps(m.get("actions", []))
            ))

    def get_monster(self, monster_id: str) -> Optional[Dict[str, Any]]:
        with self.get_connection() as conn:
            row = conn.execute("SELECT * FROM monsters WHERE id = ?", (monster_id,)).fetchone()
            if not row:
                return None
            res = dict(row)
            res["traits"] = json.loads(res["traits_json"])
            res["actions"] = json.loads(res["actions_json"])
            return res

    def insert_item(self, item_data: Dict[str, Any]) -> None:
        sql = """
        INSERT INTO items (
            id, name, category, cost_gp, weight_lbs, armor_class_bonus,
            damage_formula, damage_type, properties_json, description
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        ON CONFLICT(id) DO UPDATE SET
            name=excluded.name, cost_gp=excluded.cost_gp, description=excluded.description;
        """
        with self.get_connection() as conn:
            conn.execute(sql, (
                item_data["id"], item_data["name"], item_data["category"],
                item_data.get("cost_gp", 0.0), item_data.get("weight_lbs", 0.0),
                item_data.get("armor_class_bonus", 0), item_data.get("damage_formula"),
                item_data.get("damage_type"),
                json.dumps(item_data.get("properties", [])),
                item_data["description"]
            ))

    def get_item(self, item_id: str) -> Optional[Dict[str, Any]]:
        with self.get_connection() as conn:
            row = conn.execute("SELECT * FROM items WHERE id = ?", (item_id,)).fetchone()
            if not row:
                return None
            res = dict(row)
            res["properties"] = json.loads(res["properties_json"])
            return res

    # -------------------------------------------------------------
    # Quest Flags & State Management
    # -------------------------------------------------------------
    def set_quest_flag(self, save_id: str, flag_key: str, flag_value: str) -> None:
        sql = """
        INSERT INTO quest_flags (save_id, flag_key, flag_value)
        VALUES (?, ?, ?)
        ON CONFLICT(save_id, flag_key) DO UPDATE SET flag_value=excluded.flag_value;
        """
        with self.get_connection() as conn:
            conn.execute(sql, (save_id, flag_key, str(flag_value)))

    def get_quest_flag(self, save_id: str, flag_key: str, default: Optional[str] = None) -> Optional[str]:
        sql = "SELECT flag_value FROM quest_flags WHERE save_id = ? AND flag_key = ?"
        with self.get_connection() as conn:
            row = conn.execute(sql, (save_id, flag_key)).fetchone()
            return row["flag_value"] if row else default

    def get_all_flags(self, save_id: str) -> Dict[str, str]:
        sql = "SELECT flag_key, flag_value FROM quest_flags WHERE save_id = ?"
        with self.get_connection() as conn:
            rows = conn.execute(sql, (save_id,)).fetchall()
            return {r["flag_key"]: r["flag_value"] for r in rows}
