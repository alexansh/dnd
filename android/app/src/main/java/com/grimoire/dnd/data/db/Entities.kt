package com.grimoire.dnd.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "entities",
    indices = [
        Index("campaignId"),
        Index("entityType"),
        Index("isKnownToPlayer")
    ]
)
data class EntityRecord(
    @PrimaryKey val id: String,
    val campaignId: String,
    val entityType: String, // "NPC", "LOCATION", "FACTION", "ITEM", "QUEST"
    val name: String,
    val description: String,
    val attributesJson: String = "{}",
    val isKnownToPlayer: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "relationships",
    indices = [
        Index("campaignId"),
        Index(value = ["sourceEntityId", "targetEntityId"], unique = true)
    ]
)
data class RelationshipRecord(
    @PrimaryKey val id: String,
    val campaignId: String,
    val sourceEntityId: String,
    val targetEntityId: String,
    val relationType: String, // "FRIENDLY", "NEUTRAL", "HOSTILE", "ALLY", "DEBT", "MEMBER_OF"
    val affinityScore: Int = 0, // -100 to +100
    val notes: String = ""
)

@Entity(
    tableName = "timed_clocks",
    indices = [Index("campaignId")]
)
data class TimedClockRecord(
    @PrimaryKey val id: String,
    val campaignId: String,
    val name: String,
    val description: String,
    val currentTicks: Int = 0,
    val maxTicks: Int = 4,
    val isTriggered: Boolean = false,
    val consequenceDescription: String = ""
) {
    val progressFraction: Float
        get() = if (maxTicks > 0) (currentTicks.toFloat() / maxTicks.toFloat()).coerceIn(0f, 1f) else 0f
}

@Entity(
    tableName = "dm_secrets",
    indices = [
        Index("campaignId"),
        Index("revealedToPlayer")
    ]
)
data class DMSecretRecord(
    @PrimaryKey val id: String,
    val campaignId: String,
    val title: String,
    val secretText: String,
    val revealedToPlayer: Boolean = false,
    val triggerCondition: String? = null,
    val revealedAtTurn: Int? = null
)

@Entity(
    tableName = "event_log",
    indices = [
        Index("campaignId"),
        Index("turnIndex")
    ]
)
data class EventLogRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val campaignId: String,
    val turnIndex: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val playerAttemptText: String,
    val parsedIntentJson: String = "{}",
    val diceRollsJson: String = "[]",
    val engineStateDiffJson: String = "{}",
    val dmNarration: String,
    val isCombatTurn: Boolean = false
)

@Entity(
    tableName = "canon_ledger",
    indices = [
        Index("campaignId"),
        Index("subject")
    ]
)
data class CanonLedgerRecord(
    @PrimaryKey val id: String,
    val campaignId: String,
    val turnIndex: Int,
    val category: String, // "FACT", "NPC_LORE", "LOCATION_DETAIL", "HISTORICAL_EVENT", "PROMISE"
    val subject: String,
    val assertionText: String,
    val confidence: Float = 1.0f
)
