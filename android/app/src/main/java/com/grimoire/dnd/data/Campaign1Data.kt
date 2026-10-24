package com.grimoire.dnd.data

import com.grimoire.dnd.engine.AdventureNode
import com.grimoire.dnd.engine.NodeChoice
import com.grimoire.dnd.engine.Skill
import com.grimoire.dnd.engine.SkillCheckRequirement

object Campaign1Data {
    fun getNodes(): List<AdventureNode> = listOf(
        // Node 1: The Drunken Dragon Tavern
        AdventureNode(
            nodeId = "c1_tavern",
            campaignId = "campaign_1",
            title = "The Drunken Dragon Tavern",
            sceneVisualTag = "tavern",
            narration = "A crackling hearth warms the smoke-filled taproom of Riverfall. Townsfolk whisper anxiously in shadowed booths. At the corner table sits Elder Judith with a worn map. Nearby, a cloaked elven rogue sips spiced wine, watching you intently.",
            returnNarration = "You step back into the warmth of the Drunken Dragon Tavern. The scent of roasted ale and pine smoke offers a welcome respite from the damp marsh outside.",
            isRestArea = true,
            choices = listOf(
                NodeChoice(
                    id = "talk_judith",
                    text = "Speak with Elder Judith about the flooded crypt.",
                    targetNodeId = "c1_tavern_judith",
                    setFlagOnSelect = "knows_crypt_lore" to "true",
                    moralTags = listOf("heroic")
                ),
                NodeChoice(
                    id = "approach_elira",
                    text = "Approach the cloaked elf in the shadows (Recruit Elira).",
                    targetNodeId = "c1_tavern_elira"
                ),
                NodeChoice(
                    id = "depart_graveyard",
                    text = "Leave the tavern and march toward Riverfall Cemetery.",
                    targetNodeId = "c1_graveyard"
                )
            )
        ),

        // Node 1b: Talking to Elira (Recruitment Event!)
        AdventureNode(
            nodeId = "c1_tavern_elira",
            campaignId = "campaign_1",
            title = "The Shadows of the Taproom",
            sceneVisualTag = "tavern",
            speakerName = "Elira Moonshadow",
            narration = "The elf looks up, golden eyes gleaming beneath her cowl. 'You have the posture of someone marching straight into King Kar-Drak's tomb. That place is rigged with pressure plates and locks older than the Empire. You'll need someone who knows how to open doors without losing their hands. Shall we partner up?'",
            choices = listOf(
                NodeChoice(
                    id = "recruit_elira_accept",
                    text = "'Your daggers and lockpicks are welcome, Elira.' (Recruit Companion)",
                    targetNodeId = "c1_tavern",
                    setFlagOnSelect = "recruited_elira" to "true",
                    recruitCompanionId = "comp_elira",
                    moralTags = listOf("mercy", "heroic")
                ),
                NodeChoice(
                    id = "recruit_elira_decline",
                    text = "'I walk alone, thief.'",
                    targetNodeId = "c1_tavern",
                    moralTags = listOf("ruthless")
                )
            )
        ),

        // Node 1c: Talking to Judith
        AdventureNode(
            nodeId = "c1_tavern_judith",
            campaignId = "campaign_1",
            title = "A Dire Warning from Elder Judith",
            sceneVisualTag = "tavern",
            speakerName = "Elder Judith",
            narration = "Elder Judith leans in, her voice hushed: 'The tomb belongs to King Kar-Drak, an ancient warlord who bargained with dark waters. A heavy bronze gate seals the inner crypt. Our ancestors hid an iron skeleton key somewhere in the cemetery mausoleums. Find it, or you'll have to break your way inside.'",
            returnNarration = "Elder Judith nods solemnly at your return. 'May the light guide your blade, adventurer.'",
            isRestArea = true,
            choices = listOf(
                NodeChoice(
                    id = "leave_for_graveyard",
                    text = "Take your leave and head to Riverfall Cemetery.",
                    targetNodeId = "c1_graveyard"
                ),
                NodeChoice(
                    id = "return_tavern_floor",
                    text = "Step back to the main taproom.",
                    targetNodeId = "c1_tavern"
                )
            )
        ),

        // Node 2: Riverfall Graveyard
        AdventureNode(
            nodeId = "c1_graveyard",
            campaignId = "campaign_1",
            title = "Riverfall Weeping Graveyard",
            sceneVisualTag = "graveyard",
            narration = "Fog blankets the moss-eaten tombstones. Ravens caw from weeping willows. At the center of the graveyard stands a monumental granite archway framing the submerged stairs that lead down into the sunken crypt.",
            returnNarration = "You return to the damp graveyard. The fog swirls across the silent headstones.",
            choices = listOf(
                NodeChoice(
                    id = "approach_crypt_doors",
                    text = "Descend the stone stairs toward the heavy crypt doors.",
                    targetNodeId = "c1_crypt_doors"
                ),
                NodeChoice(
                    id = "search_mausoleums",
                    text = "Search the ancient family mausoleums for the hidden key (Investigation DC 12).",
                    skillCheck = SkillCheckRequirement(
                        skill = Skill.INVESTIGATION,
                        dc = 12,
                        successNodeId = "c1_found_key",
                        failureNodeId = "c1_graveyard_failed_search",
                        successMessage = "You discover an iron key tucked behind a weathered stone gargoyle!",
                        failureMessage = "You find only rotted moss and mud among the eroded inscriptions."
                    )
                ),
                NodeChoice(
                    id = "backtrack_to_tavern",
                    text = "Retreat back to the warmth of the Drunken Dragon Tavern.",
                    targetNodeId = "c1_tavern"
                )
            )
        ),

        // Node 2b: Found the Key!
        AdventureNode(
            nodeId = "c1_found_key",
            campaignId = "campaign_1",
            title = "Ancient Bronze Key Discovered",
            sceneVisualTag = "graveyard",
            narration = "Behind the crumbling stonework of the founders' tomb, your fingers brush cold metal. You pull free a heavy iron skeleton key inscribed with an archaic trident crest.",
            choices = listOf(
                NodeChoice(
                    id = "go_to_doors_with_key",
                    text = "Head down to the crypt doors with your newly found key.",
                    targetNodeId = "c1_crypt_doors",
                    setFlagOnSelect = "has_crypt_key" to "true"
                )
            )
        ),

        // Node 2c: Failed search
        AdventureNode(
            nodeId = "c1_graveyard_failed_search",
            campaignId = "campaign_1",
            title = "Empty Mausoleum",
            sceneVisualTag = "graveyard",
            narration = "You scrape your knuckles against cold granite, finding nothing of note. A chilly wind moans through the willow branches.",
            choices = listOf(
                NodeChoice(
                    id = "return_graveyard",
                    text = "Look around the cemetery grounds again.",
                    targetNodeId = "c1_graveyard"
                )
            )
        ),

        // Node 3: The Crypt Doors
        AdventureNode(
            nodeId = "c1_crypt_doors",
            campaignId = "campaign_1",
            title = "The Sunken Bronze Doors",
            sceneVisualTag = "crypt",
            narration = "At the bottom of the wet stairwell, a massive pair of bronze-banded doors blocks your way. Murky water pools around your boots. A heavy iron padlock holds the rusted chain fast.",
            returnNarration = "You stand before the heavy bronze crypt doors.",
            choices = listOf(
                NodeChoice(
                    id = "use_key",
                    text = "[Key] Unlock the padlock using the tarnished iron key.",
                    targetNodeId = "c1_crypt_foyer",
                    requiredFlag = "has_crypt_key" to "true",
                    setFlagOnSelect = "crypt_unlocked" to "true"
                ),
                NodeChoice(
                    id = "pick_lock",
                    text = "Pick the lock using Thieves' Tools (Sleight of Hand DC 12).",
                    skillCheck = SkillCheckRequirement(
                        skill = Skill.SLEIGHT_OF_HAND,
                        dc = 12,
                        successNodeId = "c1_crypt_foyer",
                        failureNodeId = "c1_crypt_doors_failed_pick",
                        successMessage = "With a sharp click, the ancient tumblers align and the chain drops free!",
                        failureMessage = "Your pick slips on the rusted tumbler with an agonizing metallic screech."
                    )
                ),
                NodeChoice(
                    id = "force_doors",
                    text = "Batter the rusted gate open with sheer force (Athletics DC 14).",
                    skillCheck = SkillCheckRequirement(
                        skill = Skill.ATHLETICS,
                        dc = 14,
                        successNodeId = "c1_crypt_foyer",
                        failureNodeId = "c1_crypt_doors_failed_force",
                        failureDamageFormula = "1d4",
                        successMessage = "You slam your shoulder against the seam—the rusted hinges tear from the stone wall!",
                        failureMessage = "The bronze barrier reverberates painfully through your shoulder."
                    )
                ),
                NodeChoice(
                    id = "backtrack_graveyard",
                    text = "Climb back up the stairs to the cemetery.",
                    targetNodeId = "c1_graveyard"
                )
            )
        ),

        // Node 3b: Failed pick
        AdventureNode(
            nodeId = "c1_crypt_doors_failed_pick",
            campaignId = "campaign_1",
            title = "Stubborn Lock",
            sceneVisualTag = "crypt",
            narration = "The corroded padlock resists your pick. It will take another attempt or brute force to open.",
            choices = listOf(
                NodeChoice(id = "retry_doors", text = "Step back to reassess the entrance.", targetNodeId = "c1_crypt_doors")
            )
        ),

        // Node 3c: Failed force
        AdventureNode(
            nodeId = "c1_crypt_doors_failed_force",
            campaignId = "campaign_1",
            title = "Bruised Shoulder",
            sceneVisualTag = "crypt",
            narration = "Pain shoots down your arm as the bronze slab refuses to yield. Murky water ripples around your knees.",
            choices = listOf(
                NodeChoice(id = "retry_doors", text = "Catch your breath and try another approach.", targetNodeId = "c1_crypt_doors")
            )
        ),

        // Node 4: The Crypt Foyer
        AdventureNode(
            nodeId = "c1_crypt_foyer",
            campaignId = "campaign_1",
            title = "The Drowned Antechamber",
            sceneVisualTag = "crypt",
            narration = "Beyond the heavy doors lies an arched subterranean gallery. Cold water sloshes knee-deep. Statues of forgotten sea-priests line the walls. Ahead, the water deepens into the burial altar.",
            returnNarration = "You wade back into the arched entrance corridor. The cold water ripples around the stone statues.",
            choices = listOf(
                NodeChoice(
                    id = "wade_cautiously",
                    text = "Search for submerged traps as you advance (Perception DC 12).",
                    skillCheck = SkillCheckRequirement(
                        skill = Skill.PERCEPTION,
                        dc = 12,
                        successNodeId = "c1_sanctum_entrance",
                        failureNodeId = "c1_triggered_dart_trap",
                        failureDamageFormula = "1d6",
                        successMessage = "You spot a taut wire under the murky waterline and step carefully over it!",
                        failureMessage = "Your boot catches a hidden tripwire! A volley of rusted darts fires from the wall!"
                    )
                ),
                NodeChoice(
                    id = "backtrack_to_entrance",
                    text = "Backtrack out of the crypt to the surface stairs.",
                    targetNodeId = "c1_crypt_doors"
                )
            )
        ),

        // Node 4b: Triggered Trap
        AdventureNode(
            nodeId = "c1_triggered_dart_trap",
            campaignId = "campaign_1",
            title = "Dart Trap Sprung!",
            sceneVisualTag = "crypt",
            narration = "You wrench the rusty darts from your armor as water splashes around you. Ahead, the corridor opens into the boss sanctum.",
            choices = listOf(
                NodeChoice(id = "enter_sanctum", text = "Press forward into the inner tomb.", targetNodeId = "c1_sanctum_entrance")
            )
        ),

        // Node 5: The Altar Sanctum (Boss Encounter)
        AdventureNode(
            nodeId = "c1_sanctum_entrance",
            campaignId = "campaign_1",
            title = "The Altar of the Sunken King",
            sceneVisualTag = "sanctum",
            speakerName = "King Kar-Drak",
            narration = "You step onto a dry dais in a grand vaulted hall. Atop an ornate sarcophagus sits the dried, armored corpse of King Kar-Drak—a Wight! Two skeleton guards raise rusty scimitars as unholy blue fire ignites within their eye sockets! 'Mortals... your flesh shall join the black tides!'",
            monsterIds = listOf("Skeleton Guard", "King Kar-Drak (Wight Lord)"),
            choices = listOf(
                NodeChoice(
                    id = "engage_boss",
                    text = "Draw your weapons and strike down the Wight Lord! (Frontal Attack)",
                    targetNodeId = "c1_boss_victory",
                    moralTags = listOf("heroic")
                ),
                NodeChoice(
                    id = "diplomacy_boss",
                    text = "'We seek only the relic, King Kar-Drak. Grant us passage and we shall leave your resting place intact!' (Persuasion DC 15)",
                    skillCheck = SkillCheckRequirement(
                        skill = Skill.PERSUASION,
                        dc = 15,
                        successNodeId = "c1_boss_diplomacy_victory",
                        failureNodeId = "c1_boss_victory",
                        successMessage = "The Wight halts, his ancient memories stirring. He tosses the relic toward you in exchange for your vow of non-desecration!",
                        failureMessage = "'Insolent whelps! The drowned dead strike no bargains!'"
                    ),
                    moralTags = listOf("mercy")
                ),
                NodeChoice(
                    id = "retreat_foyer",
                    text = "Make a fighting retreat back down the flooded corridor.",
                    targetNodeId = "c1_crypt_foyer"
                )
            )
        ),

        // Node 6a: Boss Combat Victory
        AdventureNode(
            nodeId = "c1_boss_victory",
            campaignId = "campaign_1",
            title = "Victory in the Drowned Crypt",
            sceneVisualTag = "sanctum",
            narration = "With a final, desperate strike, the Wight Lord collapses into ancient dust! The unholy fire leaves the skeletons, and their bones clatter harmlessly into the water. Inside the sarcophagus you discover 150 gold coins, an ancestral Silver Longsword, and the Sacred Sigil of Riverfall!",
            rewardXp = 500,
            rewardGold = 150,
            isRestArea = true,
            choices = listOf(
                NodeChoice(
                    id = "return_triumphant",
                    text = "Gather the relics and return triumphant to Riverfall Tavern.",
                    targetNodeId = "c1_tavern",
                    setFlagOnSelect = "campaign_1_complete" to "true"
                )
            )
        ),

        // Node 6b: Boss Diplomatic Victory
        AdventureNode(
            nodeId = "c1_boss_diplomacy_victory",
            campaignId = "campaign_1",
            title = "A Truce with the Drowned King",
            sceneVisualTag = "sanctum",
            speakerName = "King Kar-Drak",
            narration = "The ancient Wight Lord bows his crowned skull. 'Take the Sigil of Riverfall, mortal. Let your elders remember the drowned pact.' You secured the holy relic without losing a single drop of your party's blood!",
            rewardXp = 650,
            rewardGold = 100,
            isRestArea = true,
            choices = listOf(
                NodeChoice(
                    id = "return_peace_triumphant",
                    text = "Return to Riverfall with the sacred relic.",
                    targetNodeId = "c1_tavern",
                    setFlagOnSelect = "campaign_1_complete" to "true"
                )
            )
        )
    )
}
