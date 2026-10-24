package com.grimoire.dnd.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EntityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: EntityRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<EntityRecord>)

    @Query("SELECT * FROM entities WHERE campaignId = :campaignId")
    suspend fun getAllForCampaign(campaignId: String): List<EntityRecord>

    @Query("SELECT * FROM entities WHERE campaignId = :campaignId AND isKnownToPlayer = 1")
    suspend fun getVisibleToPlayer(campaignId: String): List<EntityRecord>

    @Query("SELECT * FROM entities WHERE id = :id")
    suspend fun getById(id: String): EntityRecord?

    @Query("UPDATE entities SET isKnownToPlayer = 1 WHERE id = :id")
    suspend fun markDiscovered(id: String)
}

@Dao
interface RelationshipDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(rel: RelationshipRecord)

    @Query("SELECT * FROM relationships WHERE campaignId = :campaignId")
    suspend fun getAllForCampaign(campaignId: String): List<RelationshipRecord>

    @Query("SELECT * FROM relationships WHERE campaignId = :campaignId AND (sourceEntityId = :entityId OR targetEntityId = :entityId)")
    suspend fun getForEntity(campaignId: String, entityId: String): List<RelationshipRecord>

    @Query("UPDATE relationships SET affinityScore = :newScore, relationType = :newType WHERE id = :id")
    suspend fun updateAffinity(id: String, newScore: Int, newType: String)
}

@Dao
interface TimedClockDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(clock: TimedClockRecord)

    @Query("SELECT * FROM timed_clocks WHERE campaignId = :campaignId")
    suspend fun getAllForCampaign(campaignId: String): List<TimedClockRecord>

    @Query("SELECT * FROM timed_clocks WHERE campaignId = :campaignId AND isTriggered = 0")
    suspend fun getActiveClocks(campaignId: String): List<TimedClockRecord>

    @Query("SELECT * FROM timed_clocks WHERE id = :clockId")
    suspend fun getById(clockId: String): TimedClockRecord?

    @Query("UPDATE timed_clocks SET currentTicks = :ticks, isTriggered = :triggered WHERE id = :clockId")
    suspend fun updateTicks(clockId: String, ticks: Int, triggered: Boolean)
}

@Dao
interface DMSecretDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(secret: DMSecretRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(secrets: List<DMSecretRecord>)

    @Query("SELECT * FROM dm_secrets WHERE campaignId = :campaignId")
    suspend fun getAllForCampaign(campaignId: String): List<DMSecretRecord>

    @Query("SELECT * FROM dm_secrets WHERE campaignId = :campaignId AND revealedToPlayer = 0")
    suspend fun getUnrevealed(campaignId: String): List<DMSecretRecord>

    @Query("UPDATE dm_secrets SET revealedToPlayer = 1, revealedAtTurn = :turnIndex WHERE id = :secretId")
    suspend fun revealSecret(secretId: String, turnIndex: Int)
}

@Dao
interface EventLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun append(event: EventLogRecord): Long

    @Query("SELECT * FROM event_log WHERE campaignId = :campaignId ORDER BY turnIndex ASC, timestamp ASC")
    suspend fun getFullHistory(campaignId: String): List<EventLogRecord>

    @Query("SELECT * FROM event_log WHERE campaignId = :campaignId ORDER BY turnIndex DESC LIMIT :limit")
    suspend fun getRecentEvents(campaignId: String, limit: Int): List<EventLogRecord>

    @Query("SELECT MAX(turnIndex) FROM event_log WHERE campaignId = :campaignId")
    suspend fun getLatestTurnIndex(campaignId: String): Int?

    @Query("SELECT COUNT(*) FROM event_log WHERE campaignId = :campaignId")
    suspend fun count(campaignId: String): Int
}

@Dao
interface CanonLedgerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordAssertion(record: CanonLedgerRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordAll(records: List<CanonLedgerRecord>)

    @Query("SELECT * FROM canon_ledger WHERE campaignId = :campaignId ORDER BY turnIndex ASC")
    suspend fun getFullLedger(campaignId: String): List<CanonLedgerRecord>

    @Query("SELECT * FROM canon_ledger WHERE campaignId = :campaignId AND subject LIKE '%' || :query || '%'")
    suspend fun searchSubject(campaignId: String, query: String): List<CanonLedgerRecord>
}
