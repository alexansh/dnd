package com.grimoire.dnd.data

import com.grimoire.dnd.engine.AdventureNode
import com.grimoire.dnd.engine.NodeChoice
import com.grimoire.dnd.engine.Skill
import com.grimoire.dnd.engine.SkillCheckRequirement

object Campaign2Data {
    fun getNodes(): List<AdventureNode> = listOf(
        // Node 1: The Frigid Pass
        AdventureNode(
            nodeId = "c2_mountain_pass",
            campaignId = "campaign_2",
            title = "The Jagged Peak Pass",
            sceneVisualTag = "graveyard",
            narration = "A biting blizzard howls across the crags of Mount Kazad. Smoke and sulfur billow from the great smokestacks of Ironhold ahead. Black-iron banners of the Duergar warband flap violently in the freezing gale.",
            returnNarration = "You stand again upon the windswept pass. The molten glow of Ironhold flickers against the snowy peaks.",
            choices = listOf(
                NodeChoice(
                    id = "c2_scout_ridge",
                    text = "Scout the ridge for Duergar snipers (Perception DC 12).",
                    skillCheck = SkillCheckRequirement(
                        skill = Skill.PERCEPTION,
                        dc = 12,
                        successNodeId = "c2_ridge_ambush_thwarted",
                        failureNodeId = "c2_ridge_ambushed",
                        failureDamageFormula = "1d6",
                        successMessage = "You spot crosshairs glinting in the snow and flank the snipers!",
                        failureMessage = "A barbed heavy crossbow bolt grazes your shoulder from the cliffs!"
                    )
                ),
                NodeChoice(
                    id = "c2_march_outer_gate",
                    text = "March directly toward the Adamantine Portcullis.",
                    targetNodeId = "c2_outer_gate"
                )
            )
        ),

        // Node 1a: Sniper Ambush Thwarted
        AdventureNode(
            nodeId = "c2_ridge_ambush_thwarted",
            campaignId = "campaign_2",
            title = "Clifftop Vantage",
            sceneVisualTag = "sanctum",
            narration = "You surprise two Duergar scouts behind their snow barricade. With swift steel, you neutralize them before they can trigger the alarm horn. On their bodies, you find a heavy iron winch lever and a detailed map of the subterranean ventilation aqueducts.",
            rewardGold = 25,
            rewardXp = 100,
            choices = listOf(
                NodeChoice(
                    id = "c2_infiltrate_aqueduct",
                    text = "Use the aqueduct map to slip through the ventilation conduits.",
                    targetNodeId = "c2_aqueduct_infiltration",
                    setFlagOnSelect = "knows_aqueduct_route" to "true",
                    moralTags = listOf("cunning", "shadow")
                ),
                NodeChoice(
                    id = "c2_proceed_gates",
                    text = "Descend to the main fortress gates.",
                    targetNodeId = "c2_outer_gate"
                )
            )
        ),

        // Node 1b: Sniper Ambushed
        AdventureNode(
            nodeId = "c2_ridge_ambushed",
            campaignId = "campaign_2",
            title = "Pinned Under Crossfire",
            sceneVisualTag = "graveyard",
            narration = "Crossbow bolts strike the stone around your boots as Duergar sentries shout from the upper parapets. You dive behind an icy boulder as a warning warhorn blares into the mountain canyon!",
            choices = listOf(
                NodeChoice(
                    id = "c2_dash_gate",
                    text = "Dash under shield cover toward the outer gates (Athletics DC 13).",
                    skillCheck = SkillCheckRequirement(
                        skill = Skill.ATHLETICS,
                        dc = 13,
                        successNodeId = "c2_outer_gate",
                        failureNodeId = "c2_outer_gate",
                        failureDamageFormula = "1d4",
                        successMessage = "You power through the crossfire and smash through the perimeter barricade!",
                        failureMessage = "You stumble over jagged ice while taking suppression fire!"
                    )
                )
            )
        ),

        // Node 2: Outer Gate
        AdventureNode(
            nodeId = "c2_outer_gate",
            campaignId = "campaign_2",
            title = "The Adamantine Portcullis",
            sceneVisualTag = "crypt",
            narration = "Massive runic iron gates seal the fortress entrance. Below the gatehouse, magma channels hiss with white-hot slag. A squad of armored Duergar shock troopers and war hounds guard the lever platform.",
            monsterIds = listOf("Duergar Shock Trooper", "Infernal War Hound"),
            choices = listOf(
                NodeChoice(
                    id = "c2_fight_gate_guards",
                    text = "Draw steel and breach the gatehouse guard (Tactical Combat).",
                    targetNodeId = "c2_smelter_tunnels",
                    moralTags = listOf("heroic")
                ),
                NodeChoice(
                    id = "c2_bypass_lockpick",
                    text = "Pick the emergency drainage sluice gate (Sleight of Hand DC 14).",
                    skillCheck = SkillCheckRequirement(
                        skill = Skill.SLEIGHT_OF_HAND,
                        dc = 14,
                        successNodeId = "c2_smelter_tunnels",
                        failureNodeId = "c2_outer_gate",
                        failureDamageFormula = "1d6",
                        successMessage = "With a crisp click, the heavy counterweight snaps open!",
                        failureMessage = "A scalding steam valve vents directly across your hands!"
                    ),
                    moralTags = listOf("cunning")
                )
            )
        ),

        // Node 2b: Aqueduct Infiltration
        AdventureNode(
            nodeId = "c2_aqueduct_infiltration",
            campaignId = "campaign_2",
            title = "The Subterranean Flumes",
            sceneVisualTag = "crypt",
            narration = "Cool runoff water gushes beneath ancient dwarven arches. Following your acquired schematics, you bypass the fortified perimeter walls completely and emerge inside the bustling smelter foundry.",
            rewardXp = 150,
            choices = listOf(
                NodeChoice(
                    id = "c2_enter_smelter_stealth",
                    text = "Slip into the foundry shadows.",
                    targetNodeId = "c2_smelter_tunnels"
                )
            )
        ),

        // Node 3: The Smelter Tunnels
        AdventureNode(
            nodeId = "c2_smelter_tunnels",
            campaignId = "campaign_2",
            title = "The Foundry of Cinders",
            sceneVisualTag = "crypt",
            narration = "Immense stone crucibles pour liquid iron into casting trenches. Roaring furnaces drown out all whispers. Ahead, you spot iron gibbet cages suspended above magma pits: dwarven clan craftsmen are imprisoned here, forced into slave labor.",
            choices = listOf(
                NodeChoice(
                    id = "c2_free_prisoners",
                    text = "Sever the chains and liberate the dwarven smiths (Athletics DC 13).",
                    skillCheck = SkillCheckRequirement(
                        skill = Skill.ATHLETICS,
                        dc = 13,
                        successNodeId = "c2_freed_smiths",
                        failureNodeId = "c2_freed_smiths_alarm",
                        failureDamageFormula = "1d6",
                        successMessage = "You sunder the iron links! The grateful dwarves arm themselves with sledgehammers!",
                        failureMessage = "The chain snaps loudly, alerting an overseer patrol before the cages open!"
                    ),
                    moralTags = listOf("heroic", "mercy")
                ),
                NodeChoice(
                    id = "c2_bypass_to_caldera",
                    text = "Leave the prisoners and press forward directly to the Caldera Forge.",
                    targetNodeId = "c2_caldera_forge",
                    moralTags = listOf("ruthless")
                )
            )
        ),

        // Node 3a: Freed Smiths
        AdventureNode(
            nodeId = "c2_freed_smiths",
            campaignId = "campaign_2",
            title = "The Uprising of Mount Kazad",
            sceneVisualTag = "tavern",
            speakerName = "Master Smith Harek",
            narration = "'By Torag's beard, you came!' bellows the master smith. 'Take this rune-forged shield and dwarven draughts. We will hold the smelter rear! Warlord Grak'nar has chained a young red dragon in the main caldera to superheat his dark weapons!'",
            rewardGold = 75,
            rewardXp = 250,
            choices = listOf(
                NodeChoice(
                    id = "c2_march_caldera_with_buff",
                    text = "Rally your strength and breach the Caldera Forge.",
                    targetNodeId = "c2_caldera_forge",
                    setFlagOnSelect = "smiths_reinforcing" to "true",
                    moralTags = listOf("heroic")
                )
            )
        ),

        // Node 3b: Freed Smiths Alarm
        AdventureNode(
            nodeId = "c2_freed_smiths_alarm",
            campaignId = "campaign_2",
            title = "Skirmish on the Magma Grate",
            sceneVisualTag = "crypt",
            narration = "Duergar overseers charge brandishing molten tongs! With the help of the freed dwarves, you beat them back into the slag pits, but the fortress bells are now tolling for war.",
            rewardXp = 150,
            choices = listOf(
                NodeChoice(
                    id = "c2_rush_caldera_alarmed",
                    text = "Charge into the Caldera Forge before reinforcements arrive!",
                    targetNodeId = "c2_caldera_forge"
                )
            )
        ),

        // Node 4: The Caldera Forge (Climax)
        AdventureNode(
            nodeId = "c2_caldera_forge",
            campaignId = "campaign_2",
            title = "The Heart of the Volcano",
            sceneVisualTag = "sanctum",
            speakerName = "Warlord Grak'nar",
            narration = "At the center of a lake of boiling magma stands a massive obsidian anvil. Chained to adamantine pillars is Cinder-Scale, a raging Young Red Dragon wyrmling breathing plumes of fire. Warlord Grak'nar stands atop the platform clad in spiked black plate: 'You are too late, surfacers! This mountain belongs to the deep!'",
            monsterIds = listOf("Warlord Grak'nar", "Young Red Dragon Wyrmling"),
            choices = listOf(
                NodeChoice(
                    id = "c2_sever_dragon_chains",
                    text = "Target the dragon's collar to set the beast against Grak'nar! (Sleight of Hand DC 15)",
                    skillCheck = SkillCheckRequirement(
                        skill = Skill.SLEIGHT_OF_HAND,
                        dc = 15,
                        successNodeId = "c2_dragon_unleashed",
                        failureNodeId = "c2_dragon_fury_fail",
                        failureDamageFormula = "2d6",
                        successMessage = "Your strike shatters the locking ring! The enraged dragon roars and turns upon its captor!",
                        failureMessage = "A blast of fiery breath scorches you as the collar deflects your blade!"
                    ),
                    moralTags = listOf("cunning", "chaos")
                ),
                NodeChoice(
                    id = "c2_duel_grak_nar",
                    text = "Challenge Warlord Grak'nar in glorious single combat! (Tactical Combat)",
                    targetNodeId = "c2_victory_bastion",
                    moralTags = listOf("heroic")
                ),
                NodeChoice(
                    id = "c2_overload_valves",
                    text = "Overload the geothermal pressure valves to flood the platform! (Investigation DC 14)",
                    skillCheck = SkillCheckRequirement(
                        skill = Skill.INVESTIGATION,
                        dc = 14,
                        successNodeId = "c2_victory_bastion",
                        failureNodeId = "c2_victory_bastion",
                        failureDamageFormula = "1d8",
                        successMessage = "Superheated steam blinds the warlord and throws him into the magma pit!",
                        failureMessage = "The valves rupture violently, blasting steam across the entire chamber!"
                    ),
                    moralTags = listOf("ruthless")
                )
            )
        ),

        // Node 4a: Dragon Unleashed
        AdventureNode(
            nodeId = "c2_dragon_unleashed",
            campaignId = "campaign_2",
            title = "Wrath of Cinder-Scale",
            sceneVisualTag = "sanctum",
            narration = "Free of its shackles, the young dragon incinerates Grak'nar's guard retinue with a single fiery roar. It locks eyes with you for one tense heartbeat, spreads its crimson wings, and blasts upward through the caldera crater into the open sky, leaving the fortress in your hands.",
            rewardGold = 200,
            rewardXp = 600,
            choices = listOf(
                NodeChoice(
                    id = "c2_claim_victory_dragon",
                    text = "Claim the fortress forge and inspect the armory.",
                    targetNodeId = "c2_victory_bastion"
                )
            )
        ),

        // Node 4b: Dragon Fury Fail
        AdventureNode(
            nodeId = "c2_dragon_fury_fail",
            campaignId = "campaign_2",
            title = "A Desperate Stand in the Ash",
            sceneVisualTag = "sanctum",
            narration = "Burned and gasping for breath amidst smoke, you stand your ground against Grak'nar as he draws his flaming greathammer.",
            choices = listOf(
                NodeChoice(
                    id = "c2_final_clash",
                    text = "Channel your resolve and strike Grak'nar down!",
                    targetNodeId = "c2_victory_bastion",
                    moralTags = listOf("heroic")
                )
            )
        ),

        // Node 5: Victory Epilogue
        AdventureNode(
            nodeId = "c2_victory_bastion",
            campaignId = "campaign_2",
            title = "Liberation of the Iron Bastion",
            sceneVisualTag = "tavern",
            speakerName = "Narrator",
            narration = "The Duergar banner falls into the volcanic pit. The dwarves of Ironhold reclaim their sacred ancestral anvils, singing ancient war hymns that echo through the stone halls. You are hailed as the Savior of Mount Kazad, bestowed with masterwork dwarven armor and ancient gold.",
            rewardGold = 350,
            rewardXp = 800,
            isRestArea = true,
            choices = listOf(
                NodeChoice(
                    id = "c2_return_pass",
                    text = "Rest by the warm forge hearth and celebrate your victory.",
                    targetNodeId = "c2_mountain_pass"
                )
            )
        )
    )
}
