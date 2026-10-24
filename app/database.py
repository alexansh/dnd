import os
import json
import sqlite3
import aiosqlite
from typing import Dict, Any, List, Optional
from app.models import Character, Campaign, ChatMessage

DB_FILE = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "dnd_campaigns.db")

async def init_db():
    async with aiosqlite.connect(DB_FILE) as db:
        await db.execute("""
            CREATE TABLE IF NOT EXISTS settings (
                key TEXT PRIMARY KEY,
                value TEXT
            )
        """)
        await db.execute("""
            CREATE TABLE IF NOT EXISTS characters (
                id TEXT PRIMARY KEY,
                name TEXT,
                data TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """)
        await db.execute("""
            CREATE TABLE IF NOT EXISTS campaigns (
                id TEXT PRIMARY KEY,
                title TEXT,
                description TEXT,
                character_id TEXT,
                current_location TEXT,
                combat_active INTEGER DEFAULT 0,
                combat_round INTEGER DEFAULT 0,
                data TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (character_id) REFERENCES characters (id)
            )
        """)
        await db.execute("""
            CREATE TABLE IF NOT EXISTS messages (
                id TEXT PRIMARY KEY,
                campaign_id TEXT,
                role TEXT,
                content TEXT,
                dice_roll TEXT,
                suggested_actions TEXT,
                required_check TEXT,
                state_updates TEXT,
                timestamp TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (campaign_id) REFERENCES campaigns (id)
            )
        """)
        await db.commit()

async def get_setting(key: str, default: Optional[str] = None) -> Optional[str]:
    async with aiosqlite.connect(DB_FILE) as db:
        async with db.execute("SELECT value FROM settings WHERE key = ?", (key,)) as cursor:
            row = await cursor.fetchone()
            if row:
                return row[0]
            return default

async def set_setting(key: str, value: str):
    async with aiosqlite.connect(DB_FILE) as db:
        await db.execute("INSERT OR REPLACE INTO settings (key, value) VALUES (?, ?)", (key, value))
        await db.commit()

async def save_character(character: Character):
    async with aiosqlite.connect(DB_FILE) as db:
        await db.execute(
            "INSERT OR REPLACE INTO characters (id, name, data) VALUES (?, ?, ?)",
            (character.id, character.name, json.dumps(character.model_dump()))
        )
        await db.commit()

async def get_character(character_id: str) -> Optional[Character]:
    async with aiosqlite.connect(DB_FILE) as db:
        async with db.execute("SELECT data FROM characters WHERE id = ?", (character_id,)) as cursor:
            row = await cursor.fetchone()
            if row:
                data = json.loads(row[0])
                return Character(**data)
            return None

async def list_characters() -> List[Dict[str, Any]]:
    async with aiosqlite.connect(DB_FILE) as db:
        async with db.execute("SELECT id, name, data FROM characters ORDER BY created_at DESC") as cursor:
            rows = await cursor.fetchall()
            return [{"id": r[0], "name": r[1], **json.loads(r[2])} for r in rows]

async def save_campaign(campaign: Campaign):
    async with aiosqlite.connect(DB_FILE) as db:
        payload = campaign.model_dump()
        await db.execute(
            """INSERT OR REPLACE INTO campaigns 
               (id, title, description, character_id, current_location, combat_active, combat_round, data, created_at, updated_at) 
               VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""",
            (
                campaign.id,
                campaign.title,
                campaign.description,
                campaign.character_id,
                campaign.current_location,
                1 if campaign.combat_active else 0,
                campaign.combat_round,
                json.dumps(payload),
                campaign.created_at,
                campaign.updated_at
            )
        )
        await db.commit()

async def get_campaign(campaign_id: str) -> Optional[Campaign]:
    async with aiosqlite.connect(DB_FILE) as db:
        async with db.execute("SELECT data FROM campaigns WHERE id = ?", (campaign_id,)) as cursor:
            row = await cursor.fetchone()
            if row:
                data = json.loads(row[0])
                return Campaign(**data)
            return None

async def list_campaigns() -> List[Dict[str, Any]]:
    async with aiosqlite.connect(DB_FILE) as db:
        query = """
            SELECT c.id, c.title, c.description, c.character_id, c.current_location, c.updated_at, c.data, ch.name, ch.data
            FROM campaigns c
            LEFT JOIN characters ch ON c.character_id = ch.id
            ORDER BY c.updated_at DESC
        """
        async with db.execute(query) as cursor:
            rows = await cursor.fetchall()
            results = []
            for r in rows:
                c_data = json.loads(r[6]) if r[6] else {}
                ch_data = json.loads(r[8]) if r[8] else {}
                results.append({
                    "id": r[0],
                    "title": r[1],
                    "description": r[2],
                    "character_id": r[3],
                    "current_location": r[4],
                    "updated_at": r[5],
                    "quests_count": len(c_data.get("quests", [])),
                    "character_name": ch_data.get("name", "Unknown Hero"),
                    "character_class": ch_data.get("class_name", "Adventurer"),
                    "character_race": ch_data.get("race", "Human"),
                    "character_level": ch_data.get("level", 1)
                })
            return results

async def delete_campaign(campaign_id: str):
    async with aiosqlite.connect(DB_FILE) as db:
        await db.execute("DELETE FROM messages WHERE campaign_id = ?", (campaign_id,))
        await db.execute("DELETE FROM campaigns WHERE id = ?", (campaign_id,))
        await db.commit()

async def save_message(msg: ChatMessage):
    async with aiosqlite.connect(DB_FILE) as db:
        await db.execute(
            """INSERT INTO messages 
               (id, campaign_id, role, content, dice_roll, suggested_actions, required_check, state_updates, timestamp) 
               VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)""",
            (
                msg.id,
                msg.campaign_id,
                msg.role,
                msg.content,
                json.dumps(msg.dice_roll.model_dump()) if msg.dice_roll else None,
                json.dumps(msg.suggested_actions) if msg.suggested_actions else None,
                json.dumps(msg.required_check) if msg.required_check else None,
                json.dumps(msg.state_updates) if msg.state_updates else None,
                msg.timestamp
            )
        )
        await db.commit()

async def get_messages(campaign_id: str, limit: int = 50) -> List[ChatMessage]:
    async with aiosqlite.connect(DB_FILE) as db:
        async with db.execute(
            "SELECT id, campaign_id, role, content, dice_roll, suggested_actions, required_check, state_updates, timestamp FROM messages WHERE campaign_id = ? ORDER BY rowid ASC",
            (campaign_id,)
        ) as cursor:
            rows = await cursor.fetchall()
            messages = []
            for r in rows:
                messages.append(ChatMessage(
                    id=r[0],
                    campaign_id=r[1],
                    role=r[2],
                    content=r[3],
                    dice_roll=json.loads(r[4]) if r[4] else None,
                    suggested_actions=json.loads(r[5]) if r[5] else None,
                    required_check=json.loads(r[6]) if r[6] else None,
                    state_updates=json.loads(r[7]) if r[7] else None,
                    timestamp=r[8]
                ))
            return messages
