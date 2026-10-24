package com.grimoire.dnd.dm.campaign

import com.grimoire.dnd.data.db.DMSecretRecord
import com.grimoire.dnd.data.db.EntityRecord
import com.grimoire.dnd.data.db.TimedClockRecord
import java.util.UUID

data class CampaignSkeleton(
    val campaignId: String,
    val title: String,
    val premise: String,
    val factions: List<EntityRecord>,
    val keyNpcs: List<EntityRecord>,
    val clocks: List<TimedClockRecord>,
    val secrets: List<DMSecretRecord>,
    val startingLocation: EntityRecord
)

class CampaignGenerator {

    /**
     * Generates a complete 3-act campaign skeleton with 3 factions, 4 timed clocks,
     * 6 core NPCs, and 3 secrets, usable 100% offline or as a starting scaffold.
     */
    fun generateProceduralSkeleton(theme: String = "Gothic Marsh"): CampaignSkeleton {
        val campId = "camp_${UUID.randomUUID().toString().take(8)}"

        val startingLocation = EntityRecord(
            id = "loc_tavern_$campId",
            campaignId = campId,
            entityType = "LOCATION",
            name = "The Drunken Dragon Inn",
            description = "A warm, timber-framed sanctuary surrounded by misty swampland.",
            isKnownToPlayer = true
        )

        val factions = listOf(
            EntityRecord(
                id = "fac_elders_$campId",
                campaignId = campId,
                entityType = "FACTION",
                name = "Riverfall Elders Council",
                description = "Guards of ancient lineage sworn to prevent the tomb's opening.",
                isKnownToPlayer = true
            ),
            EntityRecord(
                id = "fac_drowned_$campId",
                campaignId = campId,
                entityType = "FACTION",
                name = "The Drowned Legion",
                description = "Cursed undead warriors rising from the brackish depths.",
                isKnownToPlayer = false
            ),
            EntityRecord(
                id = "fac_smugglers_$campId",
                campaignId = campId,
                entityType = "FACTION",
                name = "Blackwater Smugglers",
                description = "Opportunistic thieves plundering relics from the flooded ruins.",
                isKnownToPlayer = false
            )
        )

        val keyNpcs = listOf(
            EntityRecord(
                id = "npc_judith_$campId",
                campaignId = campId,
                entityType = "NPC",
                name = "Elder Judith",
                description = "Matriarch of Riverfall who clutches an ancestral key map.",
                isKnownToPlayer = true
            ),
            EntityRecord(
                id = "npc_kardrak_$campId",
                campaignId = campId,
                entityType = "NPC",
                name = "King Kar-Drak",
                description = "The ancient slumbering warlord in the bronze sarcophagus.",
                isKnownToPlayer = false
            ),
            EntityRecord(
                id = "npc_slink_$campId",
                campaignId = campId,
                entityType = "NPC",
                name = "Slink the Smuggler",
                description = "A rat-faced informant offering to sell a secret passage map.",
                isKnownToPlayer = false
            ),
            EntityRecord(
                id = "npc_elira_$campId",
                campaignId = campId,
                entityType = "NPC",
                name = "Elira Moonshadow",
                description = "A hooded elf scout tracking dark omens in the marsh.",
                isKnownToPlayer = true
            ),
            EntityRecord(
                id = "npc_thorgar_$campId",
                campaignId = campId,
                entityType = "NPC",
                name = "Thorgar Ironbreaker",
                description = "A grizzled dwarven warrior seeking vengeance for fallen kin.",
                isKnownToPlayer = true
            ),
            EntityRecord(
                id = "npc_vespera_$campId",
                campaignId = campId,
                entityType = "NPC",
                name = "Vespera of the Eclipse",
                description = "A tiefling mystic seeking to banish necromantic corruption.",
                isKnownToPlayer = true
            )
        )

        val clocks = listOf(
            TimedClockRecord(
                id = "clock_flood_$campId",
                campaignId = campId,
                name = "Marsh Flood Surge",
                description = "Brackish floodwaters rising to submerge lower crypt tunnels.",
                currentTicks = 1,
                maxTicks = 4,
                consequenceDescription = "Lower passages become fully underwater, requiring swim checks or water breathing."
            ),
            TimedClockRecord(
                id = "clock_ritual_$campId",
                campaignId = campId,
                name = "The Kar-Drak Awakening",
                description = "Necromantic energy gathering in the central sanctum.",
                currentTicks = 0,
                maxTicks = 6,
                consequenceDescription = "King Kar-Drak rises from the sarcophagus empowered by shadow water."
            ),
            TimedClockRecord(
                id = "clock_town_defense_$campId",
                campaignId = campId,
                name = "Riverfall Palisade Defense",
                description = "Villagers fortifying the village walls against ghoul raids.",
                currentTicks = 2,
                maxTicks = 4,
                consequenceDescription = "The palisade holds firm, securing a safe resting haven."
            ),
            TimedClockRecord(
                id = "clock_smuggler_heist_$campId",
                campaignId = campId,
                name = "Smuggler Vault Breach",
                description = "Blackwater thieves blasting open the ancestral treasury.",
                currentTicks = 0,
                maxTicks = 4,
                consequenceDescription = "The treasury is ransacked unless adventurers intervene first."
            )
        )

        val secrets = listOf(
            DMSecretRecord(
                id = "sec_bloodline_$campId",
                campaignId = campId,
                title = "The Cursed Bloodline",
                secretText = "Elder Judith's grandfather was the high priest who originally bound King Kar-Drak to water.",
                revealedToPlayer = false
            ),
            DMSecretRecord(
                id = "sec_weakness_$campId",
                campaignId = campId,
                title = "The Warlord's Vulnerability",
                secretText = "Kar-Drak is invulnerable while submerged, but takes double radiant damage on dry consecrated ground.",
                revealedToPlayer = false
            ),
            DMSecretRecord(
                id = "sec_hidden_vault_$campId",
                campaignId = campId,
                title = "The Flooded Mausoleum Key",
                secretText = "The bronze key is hidden inside the jawbone of the weeping gargoyle in the cemetery.",
                revealedToPlayer = false
            )
        )

        return CampaignSkeleton(
            campaignId = campId,
            title = "The Sunken King: $theme",
            premise = "Ancient floodwaters threaten Riverfall as the drowned warlord stirs beneath the cemetery.",
            factions = factions,
            keyNpcs = keyNpcs,
            clocks = clocks,
            secrets = secrets,
            startingLocation = startingLocation
        )
    }
}
