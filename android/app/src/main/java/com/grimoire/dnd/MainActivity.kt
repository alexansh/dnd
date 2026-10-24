package com.grimoire.dnd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.grimoire.dnd.audio.SoundManager
import com.grimoire.dnd.data.*
import com.grimoire.dnd.engine.*
import com.grimoire.dnd.ui.*
import com.grimoire.dnd.ui.components.TacticalCombatScreen
import com.grimoire.dnd.ui.theme.Grimoire5ETheme

enum class AppNavState {
    HOME,
    ADVENTURE_SELECT,
    CHARACTER_CREATION,
    GAME_PLAY,
    TACTICAL_COMBAT
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val saveManager = LocalSaveManager(this)
        val diceEngine = DiceEngine()

        setContent {
            val soundManager = remember { SoundManager.getInstance(this) }
            DisposableEffect(Unit) {
                soundManager.startAmbientMusic()
                onDispose {
                    soundManager.pauseAmbientMusic()
                }
            }

            Grimoire5ETheme {
                var navState by remember { mutableStateOf(AppNavState.HOME) }
                var selectedCampaign by remember { mutableStateOf(CampaignCatalog.campaigns.first()) }
                var activeCharacter by remember { mutableStateOf<Character?>(null) }
                var activeAdventureEngine by remember { mutableStateOf<AdventureEngine?>(null) }
                var activeCombatArena by remember { mutableStateOf<TacticalCombatArena?>(null) }
                var showGlobalDiceTray by remember { mutableStateOf(false) }

                fun launchCampaign(char: Character, campaign: CampaignInfo, startingNodeId: String = campaign.startingNodeId) {
                    val partyManager = PartyManager(char)
                    CompanionCatalog.createStarterCompanions().forEach { partyManager.companions.add(it) }

                    val engine = AdventureEngine(diceEngine, partyManager)
                    Campaign1Data.getNodes().forEach { engine.registerNode(it) }
                    Campaign2Data.getNodes().forEach { engine.registerNode(it) }
                    Campaign3Data.getNodes().forEach { engine.registerNode(it) }
                    InfiniteUnderdarkData.generateInitialNodes().forEach { engine.registerNode(it) }
                    engine.startAdventure(startingNodeId)

                    activeCharacter = char
                    activeAdventureEngine = engine
                    selectedCampaign = campaign

                    val initialSave = SaveGameData(
                        slotId = char.id,
                        slotName = "${char.name} - ${engine.getCurrentNode().title}",
                        character = char,
                        campaignId = campaign.id,
                        currentNodeId = startingNodeId,
                        visitedNodes = engine.visitedNodes.toList(),
                        questFlags = engine.questFlags.toMap()
                    )
                    saveManager.saveGame(initialSave)

                    navState = AppNavState.GAME_PLAY
                }

                fun resumeSavedGame(save: SaveGameData) {
                    val partyManager = PartyManager(save.character)
                    CompanionCatalog.createStarterCompanions().forEach { partyManager.companions.add(it) }

                    if (save.questFlags["recruited_elira"] == "true") {
                        partyManager.recruitCompanion("comp_elira")
                    }

                    val engine = AdventureEngine(diceEngine, partyManager)
                    Campaign1Data.getNodes().forEach { engine.registerNode(it) }
                    Campaign2Data.getNodes().forEach { engine.registerNode(it) }
                    Campaign3Data.getNodes().forEach { engine.registerNode(it) }
                    InfiniteUnderdarkData.generateInitialNodes().forEach { engine.registerNode(it) }

                    engine.visitedNodes.addAll(save.visitedNodes)
                    engine.questFlags.putAll(save.questFlags)
                    engine.startAdventure(save.currentNodeId)

                    activeCharacter = save.character
                    activeAdventureEngine = engine
                    selectedCampaign = CampaignCatalog.campaigns.firstOrNull { it.id == save.campaignId }
                        ?: CampaignCatalog.campaigns.first()

                    navState = AppNavState.GAME_PLAY
                }

                fun startTacticalCombat(enemies: List<String>) {
                    val engine = activeAdventureEngine ?: return
                    val party = engine.partyManager?.getActiveParty() ?: listOf(activeCharacter!!)
                    activeCombatArena = TacticalCombatArena(party, enemies, diceEngine)
                    navState = AppNavState.TACTICAL_COMBAT
                }

                when (navState) {
                    AppNavState.HOME -> {
                        HomeScreen(
                            saveManager = saveManager,
                            onNewAdventure = { navState = AppNavState.ADVENTURE_SELECT },
                            onContinueGame = { resumeSavedGame(it) },
                            onOpenDiceTray = { showGlobalDiceTray = true }
                        )
                    }

                    AppNavState.ADVENTURE_SELECT -> {
                        AdventureSelectScreen(
                            onSelectAdventure = { campaign ->
                                selectedCampaign = campaign
                                navState = AppNavState.CHARACTER_CREATION
                            },
                            onBack = { navState = AppNavState.HOME }
                        )
                    }

                    AppNavState.CHARACTER_CREATION -> {
                        CharacterCreationScreen(
                            campaign = selectedCampaign,
                            onCharacterCreated = { char ->
                                launchCampaign(char, selectedCampaign)
                            },
                            onBack = { navState = AppNavState.ADVENTURE_SELECT }
                        )
                    }

                    AppNavState.GAME_PLAY -> {
                        val char = activeCharacter
                        val engine = activeAdventureEngine
                        if (char != null && engine != null) {
                            val currentNode = engine.getCurrentNode()
                            // If encounter node and player chooses to enter combat:
                            GrimoireGameScreen(
                                character = char,
                                adventureEngine = engine,
                                diceEngine = diceEngine,
                                saveManager = saveManager,
                                campaignId = selectedCampaign.id,
                                onStartCombat = { enemies -> startTacticalCombat(enemies) },
                                onExitToHome = { navState = AppNavState.HOME }
                            )
                        } else {
                            navState = AppNavState.HOME
                        }
                    }

                    AppNavState.TACTICAL_COMBAT -> {
                        val arena = activeCombatArena
                        if (arena != null) {
                            TacticalCombatScreen(
                                arena = arena,
                                onCombatFinished = { isVictory ->
                                    if (isVictory) {
                                        val enemyCount = arena.combatants.count { !it.isPlayerParty }
                                        val xpBonus = maxOf(100, enemyCount * 120)
                                        activeCharacter?.xp = (activeCharacter?.xp ?: 0) + xpBonus
                                        activeCharacter?.gold = (activeCharacter?.gold ?: 0) + 20

                                        activeAdventureEngine?.let { eng ->
                                            val curr = eng.getCurrentNode()
                                            val combatChoice = curr.choices.firstOrNull { c ->
                                                c.id.contains("boss") || c.id.contains("fight") ||
                                                c.id.contains("battle") || c.id.contains("clash") ||
                                                c.id.contains("duel")
                                            } ?: curr.choices.firstOrNull()

                                            if (combatChoice != null) {
                                                eng.executeChoice(combatChoice.id, activeCharacter!!)
                                            }
                                        }
                                    }
                                    soundManager.startAmbientMusic()
                                    navState = AppNavState.GAME_PLAY
                                }
                            )
                        } else {
                            navState = AppNavState.GAME_PLAY
                        }
                    }
                }
            }
        }
    }
}
