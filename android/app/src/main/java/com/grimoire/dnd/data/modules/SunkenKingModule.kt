package com.grimoire.dnd.data.modules

import com.grimoire.dnd.data.Campaign1Data
import com.grimoire.dnd.data.db.DMSecretRecord
import com.grimoire.dnd.data.db.EntityRecord
import com.grimoire.dnd.data.db.TimedClockRecord
import com.grimoire.dnd.data.db.WorldStateRepository
import com.grimoire.dnd.engine.AdventureNode

object SunkenKingModule {
    const val CAMPAIGN_ID = "campaign_1"
    const val CAMPAIGN_TITLE = "The Sunken King's Legacy"

    fun getLegacyNodes(): List<AdventureNode> {
        return Campaign1Data.getNodes()
    }

    suspend fun seedToDatabase(repository: WorldStateRepository) {
        val entities = listOf(
            EntityRecord(
                id = "loc_c1_tavern",
                campaignId = CAMPAIGN_ID,
                entityType = "LOCATION",
                name = "The Drunken Dragon Tavern",
                description = "Warm taproom where townsfolk huddle from the storm.",
                isKnownToPlayer = true
            ),
            EntityRecord(
                id = "loc_c1_graveyard",
                campaignId = CAMPAIGN_ID,
                entityType = "LOCATION",
                name = "Riverfall Weeping Graveyard",
                description = "Mossy tombstones shrouded in thick marsh fog.",
                isKnownToPlayer = true
            ),
            EntityRecord(
                id = "loc_c1_crypt",
                campaignId = CAMPAIGN_ID,
                entityType = "LOCATION",
                name = "The Sunken Crypt Entrance",
                description = "Submerged granite stairs leaking brackish water.",
                isKnownToPlayer = false
            ),
            EntityRecord(
                id = "npc_judith",
                campaignId = CAMPAIGN_ID,
                entityType = "NPC",
                name = "Elder Judith",
                description = "Tavern elder who possesses the cemetery map.",
                isKnownToPlayer = true
            ),
            EntityRecord(
                id = "npc_kardrak",
                campaignId = CAMPAIGN_ID,
                entityType = "NPC",
                name = "King Kar-Drak",
                description = "Ancient drowned warlord sealed in bronze.",
                isKnownToPlayer = false
            )
        )

        val clocks = listOf(
            TimedClockRecord(
                id = "clock_marsh_flood",
                campaignId = CAMPAIGN_ID,
                name = "Rising Floodwaters",
                description = "Waters steadily engulfing the lower crypt chambers.",
                currentTicks = 1,
                maxTicks = 4,
                consequenceDescription = "Crypt becomes fully submerged."
            )
        )

        val secrets = listOf(
            DMSecretRecord(
                id = "sec_iron_key",
                campaignId = CAMPAIGN_ID,
                title = "Iron Skeleton Key Location",
                secretText = "The key is hidden in the stone sarcophagus of Mausoleum IV.",
                revealedToPlayer = false
            )
        )

        repository.seedInitialCampaignEntities(entities, secrets, clocks)
    }
}
