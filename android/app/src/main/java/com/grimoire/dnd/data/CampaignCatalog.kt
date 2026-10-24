package com.grimoire.dnd.data

data class CampaignInfo(
    val id: String,
    val title: String,
    val subtitle: String,
    val recommendedLevels: String,
    val estimatedLength: String,
    val description: String,
    val isPlayable: Boolean = true,
    val startingNodeId: String = "c1_tavern"
)

object CampaignCatalog {
    val campaigns = listOf(
        CampaignInfo(
            id = "campaign_1",
            title = "The Sunken King's Legacy",
            subtitle = "Act I: The Drowned Crypt",
            recommendedLevels = "Level 1 – 3",
            estimatedLength = "2–3 Hours",
            description = "Ancient catacombs beneath the marshland have begun leaking black waters and awakening dormant undead. Venture into the submerged tomb of King Kar-Drak to recover the ancient relic.",
            isPlayable = true,
            startingNodeId = "c1_tavern"
        ),
        CampaignInfo(
            id = "campaign_2",
            title = "Siege of the Iron Bastion",
            subtitle = "Act II: The Mountain Pass",
            recommendedLevels = "Level 3 – 5",
            estimatedLength = "4–5 Hours",
            description = "Duergar warbands have seized the dwarven smelters of Ironhold. Infiltrate the molten minecart tunnels, disable the siege ballistas, and confront the Young Dragon in the caldera.",
            isPlayable = true,
            startingNodeId = "c2_mountain_pass"
        ),
        CampaignInfo(
            id = "campaign_3",
            title = "Shadows Over Oakhaven",
            subtitle = "Act III: The City of Daggers",
            recommendedLevels = "Level 1 – 4",
            estimatedLength = "3–4 Hours",
            description = "A noble is assassinated inside a locked cathedral. Navigate the foggy cobblestones, interrogate shady tavern barkeeps, and infiltrate the underground sewer guild.",
            isPlayable = true,
            startingNodeId = "c3_cathedral_crime_scene"
        ),
        CampaignInfo(
            id = "campaign_infinite",
            title = "The Infinite Underdark",
            subtitle = "Rogue-Lite Delve Mode",
            recommendedLevels = "Level 1 – 20",
            estimatedLength = "Endless",
            description = "A procedural descent into the subterranean abyss. Test your tactical prowess across infinite floors of randomized monsters, holy shrines, and ancestral loot.",
            isPlayable = true,
            startingNodeId = "inf_floor_1"
        )
    )
}
