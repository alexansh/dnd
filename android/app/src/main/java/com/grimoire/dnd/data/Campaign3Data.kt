package com.grimoire.dnd.data

import com.grimoire.dnd.engine.AdventureNode
import com.grimoire.dnd.engine.NodeChoice
import com.grimoire.dnd.engine.Skill
import com.grimoire.dnd.engine.SkillCheckRequirement

object Campaign3Data {
    fun getNodes(): List<AdventureNode> = listOf(
        // Node 1: The Locked Cathedral Crime Scene
        AdventureNode(
            nodeId = "c3_cathedral_crime_scene",
            campaignId = "campaign_3",
            title = "High Sanctum of St. Jude",
            sceneVisualTag = "sanctum",
            speakerName = "Constable Thorne",
            narration = "Rain lashes against stained-glass saints. Lord Valerius lies sprawled across the altar steps, his velvet doublet soaked in crimson. Heavy iron bolts seal the doors from the inside. Constable Thorne turns to you: 'A locked room. No broken windows. Yet his throat was torn by something with savage claws.'",
            returnNarration = "You stand once more in the echoing sanctum of St. Jude. The chalk outline of Lord Valerius marks the cold marble floor.",
            choices = listOf(
                NodeChoice(
                    id = "c3_inspect_corpse",
                    text = "Examine the wounds for chemical or supernatural traces (Investigation DC 12).",
                    skillCheck = SkillCheckRequirement(
                        skill = Skill.INVESTIGATION,
                        dc = 12,
                        successNodeId = "c3_evidence_found",
                        failureNodeId = "c3_evidence_missed",
                        successMessage = "You find traces of wolfsbane pollen and a severed sewer grate key clutched in his dead grip!",
                        failureMessage = "The gruesome claw marks yield few answers other than sheer brutality."
                    ),
                    moralTags = listOf("cunning")
                ),
                NodeChoice(
                    id = "c3_interrogate_priest",
                    text = "Question the trembling altar acolyte (Insight DC 11).",
                    skillCheck = SkillCheckRequirement(
                        skill = Skill.INSIGHT,
                        dc = 11,
                        successNodeId = "c3_acolyte_confession",
                        failureNodeId = "c3_foggy_streets",
                        successMessage = "The acolyte breaks: 'Lord Valerius met Madame Morgana at the Velvet Raven before he died!'",
                        failureMessage = "The acolyte is paralyzed with fright and stammers incoherently."
                    )
                ),
                NodeChoice(
                    id = "c3_leave_for_streets",
                    text = "Step out into the rainy cobblestone alleys of Oakhaven.",
                    targetNodeId = "c3_foggy_streets"
                )
            )
        ),

        // Node 1a: Evidence Discovered
        AdventureNode(
            nodeId = "c3_evidence_found",
            campaignId = "campaign_3",
            title = "The Sewer Seal Clue",
            sceneVisualTag = "sanctum",
            narration = "Beneath the bloodstained collar, you uncover a bronze seal bearing the crest of the Oakhaven Aqueduct Guild, accompanied by a packet of distilled aconite poison. The killer did not enter through doors; they came from the drainage conduits beneath the altar.",
            rewardXp = 120,
            choices = listOf(
                NodeChoice(
                    id = "c3_enter_sewers_direct",
                    text = "Unscrew the altar drainage grate and descend into the city sewers.",
                    targetNodeId = "c3_sewer_canals",
                    setFlagOnSelect = "has_sewer_key" to "true",
                    moralTags = listOf("cunning")
                ),
                NodeChoice(
                    id = "c3_visit_raven_with_clue",
                    text = "Visit the Velvet Raven Parlor to confront Madame Morgana.",
                    targetNodeId = "c3_velvet_raven"
                )
            )
        ),

        // Node 1b: Acolyte Confession
        AdventureNode(
            nodeId = "c3_acolyte_confession",
            campaignId = "campaign_3",
            title = "Whispered Suspicions",
            sceneVisualTag = "sanctum",
            speakerName = "Brother Paul",
            narration = "'Lord Valerius was paying hush money to the shadow guilds,' whispers Brother Paul. 'He believed the city watch was infiltrated by lycanthropes. Go to the docks, find the Velvet Raven!'",
            rewardXp = 80,
            choices = listOf(
                NodeChoice(
                    id = "c3_go_velvet_raven",
                    text = "Head to the Velvet Raven in the dockside district.",
                    targetNodeId = "c3_velvet_raven"
                )
            )
        ),

        // Node 1c: Failed check leads to streets
        AdventureNode(
            nodeId = "c3_evidence_missed",
            campaignId = "campaign_3",
            title = "Baffled Watch",
            sceneVisualTag = "sanctum",
            narration = "The wounds are ragged and inconclusive. Constable Thorne shakes his head: 'We must canvas the streets before panic grips Oakhaven.'",
            choices = listOf(
                NodeChoice(
                    id = "c3_canvas_streets",
                    text = "Step out into the rain and search the dockside tavern district.",
                    targetNodeId = "c3_foggy_streets"
                )
            )
        ),

        // Node 2: Foggy Streets
        AdventureNode(
            nodeId = "c3_foggy_streets",
            campaignId = "campaign_3",
            title = "The Fog-Veiled Docks",
            sceneVisualTag = "graveyard",
            narration = "Gas lamps cast sickly yellow rings into the swirling river fog. Shadowy figures dart between dripping timber eaves. You hear the clinking of tankards from the Velvet Raven Parlor to the east, while a stench of stagnant river tide rises from open storm drains to the west.",
            choices = listOf(
                NodeChoice(
                    id = "c3_enter_raven",
                    text = "Enter the Velvet Raven Parlor.",
                    targetNodeId = "c3_velvet_raven"
                ),
                NodeChoice(
                    id = "c3_pry_storm_drain",
                    text = "Pry open the rusted storm drain (Athletics DC 12).",
                    skillCheck = SkillCheckRequirement(
                        skill = Skill.ATHLETICS,
                        dc = 12,
                        successNodeId = "c3_sewer_canals",
                        failureNodeId = "c3_foggy_streets",
                        failureDamageFormula = "1d4",
                        successMessage = "The heavy iron grate yields, revealing mossy stone stairs descending into the dark.",
                        failureMessage = "The crowbar slips, bruising your hand against the iron bars."
                    )
                )
            )
        ),

        // Node 3: The Velvet Raven Parlor
        AdventureNode(
            nodeId = "c3_velvet_raven",
            campaignId = "campaign_3",
            title = "The Velvet Raven Parlor",
            sceneVisualTag = "tavern",
            speakerName = "Madame Morgana",
            narration = "Velvet drapes muffle the clatter of dice and pipe smoke. Behind a lacquered bar stands Madame Morgana, smoking an ebony cheroot. 'You smell of cathedral incense and fresh blood, stranger. Are you here to ask about poor Valerius?'",
            choices = listOf(
                NodeChoice(
                    id = "c3_persuade_morgana",
                    text = "'Tell me who ordered his death, or the City Watch shuts this den down.' (Intimidation DC 13)",
                    skillCheck = SkillCheckRequirement(
                        skill = Skill.INTIMIDATION,
                        dc = 13,
                        successNodeId = "c3_morgana_reveals",
                        failureNodeId = "c3_morgana_bribed",
                        successMessage = "Morgana's eyes narrow. 'Watch Captain Drake and Guildmaster Slink orchestrated it from the sewer cistern!'",
                        failureMessage = "Morgana laughs smoothly. 'Watch threats don't frighten me, dear. Gold does.'"
                    ),
                    moralTags = listOf("ruthless")
                ),
                NodeChoice(
                    id = "c3_bribe_morgana",
                    text = "Slide 50 Gold Pieces across the mahogany counter.",
                    targetNodeId = "c3_morgana_reveals",
                    moralTags = listOf("cunning")
                )
            )
        ),

        // Node 3a: Morgana Reveals
        AdventureNode(
            nodeId = "c3_morgana_reveals",
            campaignId = "campaign_3",
            title = "A Conspiracy of Rats",
            sceneVisualTag = "tavern",
            speakerName = "Madame Morgana",
            narration = "'Valerius discovered that Watch Captain Drake was taking cuts from Guildmaster Slink's wererat syndicate,' Morgana whispers. 'They meet tonight in the Sunken Cistern beneath the Grand Bazaar to divide the stolen treasury. Take this silvered dagger—you will need it against Slink.'",
            rewardGold = 25,
            rewardXp = 200,
            choices = listOf(
                NodeChoice(
                    id = "c3_infiltrate_cistern",
                    text = "Descend into the Sunken Cistern to confront the conspirators.",
                    targetNodeId = "c3_sewer_canals",
                    setFlagOnSelect = "knows_conspiracy" to "true",
                    moralTags = listOf("heroic")
                )
            )
        ),

        // Node 3b: Morgana Bribed fallback
        AdventureNode(
            nodeId = "c3_morgana_bribed",
            campaignId = "campaign_3",
            title = "Costly Information",
            sceneVisualTag = "tavern",
            speakerName = "Madame Morgana",
            narration = "'Check the Sunken Cistern beneath the Grand Bazaar,' she says quietly. 'And watch the shadows. Slink doesn't fight fair.'",
            choices = listOf(
                NodeChoice(
                    id = "c3_go_sewer_from_bribe",
                    text = "Head to the sewer cistern.",
                    targetNodeId = "c3_sewer_canals"
                )
            )
        ),

        // Node 4: The Sewer Canals
        AdventureNode(
            nodeId = "c3_sewer_canals",
            campaignId = "campaign_3",
            title = "The Sunken Cistern",
            sceneVisualTag = "crypt",
            narration = "Black runoff sloshes against slime-coated bricks. Red eyes gleam from every alcove. On a central dry stone pier, Guildmaster Slink—a hunched figure with twitching whiskers and twin poisoned kukris—stands beside the armored Watch Captain Drake, counting chests of gold.",
            monsterIds = listOf("Guildmaster Slink (Wererat)", "Corrupted Watch Guard"),
            choices = listOf(
                NodeChoice(
                    id = "c3_expose_drake",
                    text = "Expose Captain Drake's treachery to his guards! (Persuasion DC 14)",
                    skillCheck = SkillCheckRequirement(
                        skill = Skill.PERSUASION,
                        dc = 14,
                        successNodeId = "c3_guards_turn",
                        failureNodeId = "c3_battle_conspirators",
                        successMessage = "The city guards lower their halberds in shock and turn upon Captain Drake!",
                        failureMessage = "Drake sneers: 'Kill the intruder! No witnesses leave these tunnels!'"
                    ),
                    moralTags = listOf("heroic")
                ),
                NodeChoice(
                    id = "c3_ambush_slink",
                    text = "Strike Guildmaster Slink from the shadows with your silvered blade! (Stealth DC 13)",
                    skillCheck = SkillCheckRequirement(
                        skill = Skill.STEALTH,
                        dc = 13,
                        successNodeId = "c3_slink_backstabbed",
                        failureNodeId = "c3_battle_conspirators",
                        failureDamageFormula = "1d6",
                        successMessage = "Your blade bites deep before the wererat can shift form!",
                        failureMessage = "Slink's keen ears catch your boots splashing in the runoff!"
                    ),
                    moralTags = listOf("cunning")
                ),
                NodeChoice(
                    id = "c3_battle_all",
                    text = "Draw your weapons and attack both traitors! (Tactical Combat)",
                    targetNodeId = "c3_battle_conspirators",
                    moralTags = listOf("ruthless")
                )
            )
        ),

        // Node 4a: Guards Turn
        AdventureNode(
            nodeId = "c3_guards_turn",
            campaignId = "campaign_3",
            title = "Mutiny in the Murk",
            sceneVisualTag = "crypt",
            narration = "Shamed by their captain's villainy, the watchmen disarm Captain Drake. Slink shrieks, hissing in fury as he scampers toward the drainage pipe, but your arrow pins him to the stone!",
            rewardGold = 250,
            rewardXp = 500,
            choices = listOf(
                NodeChoice(
                    id = "c3_claim_justice",
                    text = "Arrest the traitors and secure the city.",
                    targetNodeId = "c3_epilogue_justice"
                )
            )
        ),

        // Node 4b: Slink Backstabbed
        AdventureNode(
            nodeId = "c3_slink_backstabbed",
            campaignId = "campaign_3",
            title = "A Throat Cut in the Dark",
            sceneVisualTag = "crypt",
            narration = "Silver pierces the wererat's heart. Slink collapses into the canal with a gurgle. Seeing his ally dead, Captain Drake drops his sword and falls to his knees, begging for mercy.",
            rewardGold = 200,
            rewardXp = 450,
            choices = listOf(
                NodeChoice(
                    id = "c3_spare_or_slay_drake",
                    text = "Turn Drake over to the High Council for public trial.",
                    targetNodeId = "c3_epilogue_justice",
                    moralTags = listOf("mercy", "heroic")
                )
            )
        ),

        // Node 4c: Battle Conspirators
        AdventureNode(
            nodeId = "c3_battle_conspirators",
            campaignId = "campaign_3",
            title = "The Clash of Oakhaven",
            sceneVisualTag = "crypt",
            narration = "Steel rings against the sewer ceiling! In a desperate flurry of blades, you overcome the corrupt captain and drive the wererat syndicate from their subterranean lair.",
            rewardGold = 180,
            rewardXp = 400,
            choices = listOf(
                NodeChoice(
                    id = "c3_wrap_investigation",
                    text = "Emerge into the dawn light as the savior of Oakhaven.",
                    targetNodeId = "c3_epilogue_justice"
                )
            )
        ),

        // Node 5: Epilogue Justice
        AdventureNode(
            nodeId = "c3_epilogue_justice",
            campaignId = "campaign_3",
            title = "Dawn Over Oakhaven",
            sceneVisualTag = "tavern",
            speakerName = "High Magistrate",
            narration = "Sunlight breaks through the coastal fog, illuminating the towers of Oakhaven. The High Council clears Lord Valerius's name, dismantles the wererat guild, and presents you with the Golden Medallion of the City Guard along with a chest of guild rubies.",
            rewardGold = 400,
            rewardXp = 750,
            isRestArea = true,
            choices = listOf(
                NodeChoice(
                    id = "c3_return_cathedral",
                    text = "Return to St. Jude's Cathedral to pay respects to the fallen.",
                    targetNodeId = "c3_cathedral_crime_scene"
                )
            )
        )
    )
}
