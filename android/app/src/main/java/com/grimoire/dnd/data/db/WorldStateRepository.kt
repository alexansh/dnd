package com.grimoire.dnd.data.db

import androidx.room.withTransaction

data class TurnContext(
    val visibleEntities: List<EntityRecord>,
    val relationships: List<RelationshipRecord>,
    val activeClocks: List<TimedClockRecord>,
    val unrevealedSecrets: List<DMSecretRecord>,
    val recentEvents: List<EventLogRecord>,
    val canonFacts: List<CanonLedgerRecord>
)

class WorldStateRepository(
    private val database: GrimoireDatabase
) {
    private val entityDao = database.entityDao()
    private val relationshipDao = database.relationshipDao()
    private val clockDao = database.timedClockDao()
    private val secretDao = database.dmSecretDao()
    private val eventLogDao = database.eventLogDao()
    private val canonDao = database.canonLedgerDao()

    suspend fun getTurnContext(campaignId: String, recentLimit: Int = 5): TurnContext {
        val visibleEntities = entityDao.getVisibleToPlayer(campaignId)
        val relationships = relationshipDao.getAllForCampaign(campaignId)
        val activeClocks = clockDao.getActiveClocks(campaignId)
        val unrevealedSecrets = secretDao.getUnrevealed(campaignId)
        val recentEvents = eventLogDao.getRecentEvents(campaignId, recentLimit)
        val canonFacts = canonDao.getFullLedger(campaignId)

        return TurnContext(
            visibleEntities = visibleEntities,
            relationships = relationships,
            activeClocks = activeClocks,
            unrevealedSecrets = unrevealedSecrets,
            recentEvents = recentEvents,
            canonFacts = canonFacts
        )
    }

    suspend fun recordTurn(
        campaignId: String,
        turnIndex: Int,
        playerAttempt: String,
        parsedIntentJson: String,
        diceRollsJson: String,
        stateDiffJson: String,
        dmNarration: String,
        isCombat: Boolean = false,
        newCanonFacts: List<CanonLedgerRecord> = emptyList(),
        clocksToAdvance: List<Pair<String, Int>> = emptyList(),
        revealedSecretIds: List<String> = emptyList()
    ): Long {
        return database.withTransaction {
            // 1. Append event record
            val event = EventLogRecord(
                campaignId = campaignId,
                turnIndex = turnIndex,
                playerAttemptText = playerAttempt,
                parsedIntentJson = parsedIntentJson,
                diceRollsJson = diceRollsJson,
                engineStateDiffJson = stateDiffJson,
                dmNarration = dmNarration,
                isCombatTurn = isCombat
            )
            val eventId = eventLogDao.append(event)

            // 2. Commit new canon facts
            if (newCanonFacts.isNotEmpty()) {
                canonDao.recordAll(newCanonFacts)
            }

            // 3. Advance progress clocks
            for ((clockId, ticksToAdd) in clocksToAdvance) {
                val existing = clockDao.getById(clockId)
                if (existing != null) {
                    val newTicks = (existing.currentTicks + ticksToAdd).coerceAtMost(existing.maxTicks)
                    val triggered = newTicks >= existing.maxTicks
                    clockDao.updateTicks(clockId, newTicks, triggered)
                }
            }

            // 4. Reveal secrets if any
            for (secretId in revealedSecretIds) {
                secretDao.revealSecret(secretId, turnIndex)
            }

            eventId
        }
    }

    suspend fun getEventHistory(campaignId: String): List<EventLogRecord> {
        return eventLogDao.getFullHistory(campaignId)
    }

    suspend fun getLatestTurnIndex(campaignId: String): Int {
        return eventLogDao.getLatestTurnIndex(campaignId) ?: 0
    }

    suspend fun seedInitialCampaignEntities(
        entities: List<EntityRecord>,
        secrets: List<DMSecretRecord>,
        clocks: List<TimedClockRecord>
    ) {
        database.withTransaction {
            entityDao.insertAll(entities)
            secretDao.insertAll(secrets)
            clocks.forEach { clockDao.insertOrUpdate(it) }
        }
    }
}
