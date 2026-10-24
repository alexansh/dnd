package com.grimoire.dnd.data

import com.grimoire.dnd.engine.AdventureNode
import com.grimoire.dnd.engine.NodeChoice
import com.grimoire.dnd.engine.Skill
import com.grimoire.dnd.engine.SkillCheckRequirement
import kotlin.random.Random

object InfiniteUnderdarkData {
    var currentFloor: Int = 1

    fun generateInitialNodes(): List<AdventureNode> {
        val nodes = mutableListOf<AdventureNode>()
        nodes.add(createFloorNode(1))
        nodes.add(createFloorNode(2))
        nodes.add(createFloorNode(3))
        nodes.add(createFloorNode(4))
        nodes.add(createFloorNode(5, isBoss = true))
        return nodes
    }

    fun createFloorNode(floor: Int, isBoss: Boolean = false): AdventureNode {
        val nodeId = "inf_floor_$floor"
        val nextNodeId = "inf_floor_${floor + 1}"

        if (isBoss) {
            val bossName = when ((floor / 5) % 3) {
                1 -> "Grell Patriarch"
                2 -> "Chasm Umber Hulk"
                else -> "Underdark Shadow Drake"
            }
            return AdventureNode(
                nodeId = nodeId,
                campaignId = "campaign_infinite",
                title = "Depth Floor $floor: The Abyssal Nexus (BOSS)",
                sceneVisualTag = "sanctum",
                speakerName = bossName,
                narration = "The cavern ceiling arches into dizzying darkness. Bioluminescent purple fungus clings to jagged stalactites. At the center of a circular altar, a terrifying $bossName thrashes its tendrils, guarding the staircase descending to the deeper abyss!",
                monsterIds = listOf(bossName),
                rewardGold = 100 * floor,
                rewardXp = 300 * floor,
                isRestArea = true,
                choices = listOf(
                    NodeChoice(
                        id = "boss_clash_$floor",
                        text = "Engage the $bossName in mortal combat! (Tactical Combat)",
                        targetNodeId = nextNodeId,
                        moralTags = listOf("heroic")
                    ),
                    NodeChoice(
                        id = "boss_shrine_heal_$floor",
                        text = "Drink from the glowing abyssal font to restore vitality.",
                        targetNodeId = nodeId,
                        moralTags = listOf("mercy")
                    )
                )
            )
        }

        val roomType = (floor % 4)
        return when (roomType) {
            1 -> {
                // Monster Lair
                val monster = when {
                    floor <= 2 -> "Troglodyte Skirmisher"
                    floor <= 4 -> "Carrion Crawler"
                    else -> "Hook Horror"
                }
                AdventureNode(
                    nodeId = nodeId,
                    campaignId = "campaign_infinite",
                    title = "Depth Floor $floor: Echoing Chasm",
                    sceneVisualTag = "crypt",
                    narration = "Drip... drip... water echoes from miles above. Chittering claws scrape across the slate cavern floor. A prowling $monster emerges from a fissure, mandibles dripping with caustic bile!",
                    monsterIds = listOf(monster),
                    rewardGold = 15 * floor,
                    rewardXp = 80 * floor,
                    choices = listOf(
                        NodeChoice(
                            id = "fight_$floor",
                            text = "Strike down the $monster and advance down the shaft.",
                            targetNodeId = nextNodeId,
                            moralTags = listOf("heroic")
                        ),
                        NodeChoice(
                            id = "stealth_$floor",
                            text = "Slip through the upper stalactite ledges (Stealth DC ${10 + floor}).",
                            skillCheck = SkillCheckRequirement(
                                skill = Skill.STEALTH,
                                dc = 10 + floor,
                                successNodeId = nextNodeId,
                                failureNodeId = nextNodeId,
                                failureDamageFormula = "1d6",
                                successMessage = "You silently glide past the beast undetected!",
                                failureMessage = "Loose gravel clatters, and the beast snaps at your ankles as you plunge down the shaft!"
                            ),
                            moralTags = listOf("cunning")
                        )
                    )
                )
            }
            2 -> {
                // Treasure Vault / Trap Room
                AdventureNode(
                    nodeId = nodeId,
                    campaignId = "campaign_infinite",
                    title = "Depth Floor $floor: Forgotten Crypt of the Drow",
                    sceneVisualTag = "crypt",
                    narration = "Carved obsidian pillars flank an ancient drow sarcophagus covered in web strands. The stone chest glints with silver filigree, but glyphs of warding crackle around the perimeter.",
                    rewardGold = 35 * floor,
                    rewardXp = 100 * floor,
                    choices = listOf(
                        NodeChoice(
                            id = "disarm_$floor",
                            text = "Disarm the magical glyphs (Investigation DC ${11 + floor}).",
                            skillCheck = SkillCheckRequirement(
                                skill = Skill.INVESTIGATION,
                                dc = 11 + floor,
                                successNodeId = nextNodeId,
                                failureNodeId = nextNodeId,
                                failureDamageFormula = "1d8",
                                successMessage = "You ground the runes safely and loot the drow vault!",
                                failureMessage = "The glyph detonates in a flash of violet force!"
                            ),
                            moralTags = listOf("cunning")
                        ),
                        NodeChoice(
                            id = "bypass_chest_$floor",
                            text = "Ignore the trapped sarcophagus and proceed down the stairwell.",
                            targetNodeId = nextNodeId
                        )
                    )
                )
            }
            3 -> {
                // Hazard / Natural Obstacle
                AdventureNode(
                    nodeId = nodeId,
                    campaignId = "campaign_infinite",
                    title = "Depth Floor $floor: The Magma Fissure",
                    sceneVisualTag = "graveyard",
                    narration = "A yawning rift divides the cavern. A river of subterranean magma bubbles below. A crumbling limestone bridge spans the boiling gap.",
                    choices = listOf(
                        NodeChoice(
                            id = "cross_bridge_$floor",
                            text = "Leap across the collapsing limestone pillars (Acrobatics DC ${11 + floor}).",
                            skillCheck = SkillCheckRequirement(
                                skill = Skill.ACROBATICS,
                                dc = 11 + floor,
                                successNodeId = nextNodeId,
                                failureNodeId = nextNodeId,
                                failureDamageFormula = "1d6",
                                successMessage = "Gracefully vaulting from pillar to pillar, you reach the far landing!",
                                failureMessage = "A stone crumbles beneath your foot, singing your armor on the heat below!"
                            )
                        ),
                        NodeChoice(
                            id = "anchor_grapple_$floor",
                            text = "Anchor a piton and swing across (Athletics DC ${12 + floor}).",
                            skillCheck = SkillCheckRequirement(
                                skill = Skill.ATHLETICS,
                                dc = 12 + floor,
                                successNodeId = nextNodeId,
                                failureNodeId = nextNodeId,
                                successMessage = "Your piton holds firm as you swing safely to the far passage!",
                                failureMessage = "The rope jerks violently, jarring your shoulder!"
                            )
                        )
                    )
                )
            }
            else -> {
                // Resting Sanctuary / Sacred Spring
                AdventureNode(
                    nodeId = nodeId,
                    campaignId = "campaign_infinite",
                    title = "Depth Floor $floor: Bioluminescent Grove",
                    sceneVisualTag = "tavern",
                    narration = "Gentle azure moss carpets the walls, bathing this peaceful cavern in cool tranquil light. Pure subterranean spring water pools in a carved basin. The air is calm and free of monsters.",
                    isRestArea = true,
                    rewardGold = 10,
                    rewardXp = 50,
                    choices = listOf(
                        NodeChoice(
                            id = "rest_spring_$floor",
                            text = "Rest by the tranquil pool and prepare your spells.",
                            targetNodeId = nextNodeId,
                            moralTags = listOf("mercy")
                        ),
                        NodeChoice(
                            id = "dive_deeper_$floor",
                            text = "March onward toward the lower depths.",
                            targetNodeId = nextNodeId
                        )
                    )
                )
            }
        }
    }
}
