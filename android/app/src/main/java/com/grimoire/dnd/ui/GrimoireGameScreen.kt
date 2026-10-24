package com.grimoire.dnd.ui

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grimoire.dnd.R
import com.grimoire.dnd.audio.SoundManager
import com.grimoire.dnd.data.LocalSaveManager
import com.grimoire.dnd.data.SaveGameData
import com.grimoire.dnd.data.db.GrimoireDatabase
import com.grimoire.dnd.data.db.WorldStateRepository
import com.grimoire.dnd.dm.pipeline.ContextBuilder
import com.grimoire.dnd.dm.pipeline.TurnPipelineCoordinator
import com.grimoire.dnd.dm.provider.DeterministicFallbackProvider
import com.grimoire.dnd.engine.*
import com.grimoire.dnd.ui.components.InventoryModal
import com.grimoire.dnd.ui.components.LevelUpDialog
import com.grimoire.dnd.ui.components.PartyDeckBar
import com.grimoire.dnd.ui.components.VisualSceneCard
import com.grimoire.dnd.ui.theme.*
import kotlinx.coroutines.launch

fun triggerHapticFeedback(context: Context, isCritical: Boolean = false) {
    try {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (isCritical) {
                    val timings = longArrayOf(0, 80, 50, 150)
                    val amplitudes = intArrayOf(0, 255, 0, 255)
                    it.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                } else {
                    it.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE))
                }
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(if (isCritical) 200 else 40)
            }
        }
    } catch (_: Exception) {
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrimoireGameScreen(
    character: Character,
    adventureEngine: AdventureEngine,
    diceEngine: DiceEngine,
    saveManager: LocalSaveManager,
    campaignId: String = "campaign_1",
    onStartCombat: ((List<String>) -> Unit)? = null,
    onExitToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val soundManager = remember { SoundManager.getInstance(context) }
    val dmEngine = remember { DMEngine(diceEngine) }
    var isMuted by remember { mutableStateOf(!soundManager.isMusicEnabled) }
    var showDiceTray by remember { mutableStateOf(false) }
    var showInventoryModal by remember { mutableStateOf(false) }
    var showLevelUpDialog by remember { mutableStateOf(false) }
    var activeRuleExplanation by remember { mutableStateOf<RuleExplanation?>(null) }
    var lastDiceLog by remember { mutableStateOf<String?>(null) }
    var companionReactionLog by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedRollType by remember { mutableStateOf(RollType.NORMAL) }
    var manualDiceResult by remember { mutableStateOf<RollOutcome?>(null) }

    var refreshKey by remember { mutableStateOf(0) }
    val currentNode = remember(refreshKey) { adventureEngine.getCurrentNode() }
    val currentNarration = remember(refreshKey) { adventureEngine.getCurrentNarration() }
    val availableChoices = remember(refreshKey) { adventureEngine.getAvailableChoices() }
    val currentWhisper = remember(refreshKey, currentNode.nodeId) { dmEngine.generateDMWhisper(currentNode, character) }
    val currentTension = remember(refreshKey, currentNode.nodeId, character.hpCurrent) { dmEngine.calculateTensionLevel(character, currentNode) }

    val scope = rememberCoroutineScope()
    val worldStateRepo = remember {
        val db = GrimoireDatabase.getInstance(context)
        WorldStateRepository(db)
    }
    val pipelineCoordinator = remember {
        val provider = DeterministicFallbackProvider()
        val contextBuilder = ContextBuilder(worldStateRepo)
        TurnPipelineCoordinator(
            llmProvider = provider,
            contextBuilder = contextBuilder,
            diceEngine = diceEngine,
            worldStateRepository = worldStateRepo
        )
    }
    var playerFreeTextInput by remember { mutableStateOf("") }
    var isProcessingTurn by remember { mutableStateOf(false) }
    var liveDmNarration by remember { mutableStateOf<String?>(null) }
    var dynamicSuggestedActions by remember { mutableStateOf<List<String>>(emptyList()) }

    fun autoSave() {
        val save = SaveGameData(
            slotId = character.id,
            slotName = "${character.name} - ${currentNode.title}",
            character = character,
            campaignId = campaignId,
            currentNodeId = currentNode.nodeId,
            visitedNodes = adventureEngine.visitedNodes.toList(),
            questFlags = adventureEngine.questFlags.toMap()
        )
        saveManager.saveGame(save)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(GoldAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = character.name.take(1),
                                color = TextDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = character.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextParchment
                            )
                            Text(
                                text = "${character.race} ${character.characterClass.label} Lv.${character.level}",
                                fontSize = 12.sp,
                                color = GoldAccent
                            )
                        }
                    }
                },
                actions = {
                    Surface(
                        color = DarkCardSurface,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, GoldMuted)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_coins),
                                contentDescription = "Gold",
                                tint = GoldAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("${character.gold} GP", fontSize = 13.sp, color = GoldAccent, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(onClick = { showInventoryModal = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_scroll),
                            contentDescription = "Inventory Satchel",
                            tint = GoldAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(2.dp))

                    IconButton(onClick = {
                        isMuted = !isMuted
                        soundManager.isMusicEnabled = !isMuted
                        soundManager.isSfxEnabled = !isMuted
                        if (isMuted) {
                            soundManager.pauseAmbientMusic()
                        } else {
                            soundManager.startAmbientMusic()
                        }
                    }) {
                        Text(if (isMuted) "🔇" else "🔊", fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.width(2.dp))

                    IconButton(
                        onClick = {
                            autoSave()
                            onExitToHome()
                        }
                    ) {
                        Text("💾", fontSize = 18.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        bottomBar = {
            Surface(
                color = DarkSurface,
                border = BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val roll = diceEngine.rollD20(character.getAbilityModifier(Ability.STR), selectedRollType)
                            triggerHapticFeedback(context, roll.isCriticalHit)
                            if (roll.isCriticalHit) {
                                soundManager.playDiceCrit()
                            } else {
                                soundManager.playDiceRoll()
                            }
                            lastDiceLog = "d20 Roll: ${roll.summary()}"
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkCardSurface),
                        border = BorderStroke(1.dp, GoldMuted)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_d20),
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Roll d20", color = TextParchment, fontSize = 14.sp)
                    }

                    Button(
                        onClick = { showDiceTray = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_d20),
                            contentDescription = null,
                            tint = TextDark,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Dice Tray", color = TextDark, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // 1. High-Fidelity 2D Visual Scene Card
            item {
                VisualSceneCard(
                    visualTag = currentNode.sceneVisualTag,
                    locationTitle = currentNode.title,
                    speakerName = currentNode.speakerName
                )
            }

            // 2. Active Party & Companion Approval Deck
            if (adventureEngine.partyManager != null) {
                item {
                    PartyDeckBar(partyManager = adventureEngine.partyManager)
                }
            }

            // 3. Companion Reactions Box (e.g. "Elira approves (+5)")
            if (companionReactionLog.isNotEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = BorderStroke(1.dp, GoldAccent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            companionReactionLog.forEach { reaction ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("💬", fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = reaction,
                                        fontSize = 12.sp,
                                        color = GoldAccent,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Level Up Ready Banner
            if (character.canLevelUp()) {
                item {
                    Card(
                        onClick = { showLevelUpDialog = true },
                        colors = CardDefaults.cardColors(containerColor = GoldAccent),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_dragon),
                                    contentDescription = null,
                                    tint = TextDark,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "LEVEL UP READY!",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TextDark,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "${character.xp} / ${character.xpForNextLevel} XP — Tap to Ascend",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextDark.copy(alpha = 0.85f)
                                    )
                                }
                            }
                            Text("ASCEND ➔", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
                        }
                    }
                }
            }

            // 4. Vitals Card: HP, Armor Class, Hit Dice
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
                    border = BorderStroke(1.dp, DarkBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "HIT POINTS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.sp
                            )
                            Text(
                                "${character.hpCurrent} / ${character.hpMax} HP",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (character.hpCurrent < character.hpMax / 3) HealthRed else HealthGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        val hpPercent = (character.hpCurrent.toFloat() / character.hpMax.toFloat()).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { hpPercent },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (hpPercent < 0.35f) HealthRed else HealthGreen,
                            trackColor = DarkSurface
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // XP Tracker
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "EXPERIENCE POINTS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.sp
                            )
                            Text(
                                "${character.xp} / ${character.xpForNextLevel} XP",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        val xpPercent = (character.xp.toFloat() / character.xpForNextLevel.toFloat()).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { xpPercent },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = GoldAccent,
                            trackColor = DarkSurface
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_shield),
                                    contentDescription = null,
                                    tint = GoldAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${character.armorClass} AC", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextParchment)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_sword),
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${character.speedFeet} ft", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextParchment)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_potion),
                                    contentDescription = null,
                                    tint = GoldAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("+${character.proficiencyBonus}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextParchment)
                            }
                        }
                    }
                }
            }

            // The Dungeon Master's Whisper Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, GoldMuted),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_scroll),
                                    contentDescription = null,
                                    tint = GoldAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "THE DM WHISPERS • ${currentWhisper.insightType.uppercase()}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldAccent,
                                    letterSpacing = 1.sp
                                )
                            }
                            Surface(
                                color = if (currentTension.contains("DANGER")) Crimson.copy(alpha = 0.2f) else GoldAccent.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = if (currentTension.contains("DANGER")) "CRITICAL" else "EXPLORING",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentTension.contains("DANGER")) Crimson else GoldAccent,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = currentWhisper.message,
                            fontSize = 12.sp,
                            color = TextParchment,
                            lineHeight = 18.sp,
                            fontFamily = FontFamily.Serif
                        )
                    }
                }
            }

            // 5. Dice Roll Alert Box (if roll happened)
            if (lastDiceLog != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = BorderStroke(1.dp, GoldAccent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_d20),
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = lastDiceLog ?: "",
                                fontSize = 13.sp,
                                color = TextParchment,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // 6. Narrative DM Dialogue Box
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
                    border = BorderStroke(1.dp, DarkBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_tombstone),
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (currentNode.speakerName != null) "${currentNode.speakerName?.uppercase()}" else currentNode.title.uppercase(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent,
                                letterSpacing = 1.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = liveDmNarration ?: currentNarration,
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            color = TextParchment,
                            fontFamily = FontFamily.Serif
                        )

                        if (currentNode.monsterIds.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                color = Crimson.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, Crimson),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_dragon),
                                            contentDescription = null,
                                            tint = Crimson,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "HOSTILES: ${currentNode.monsterIds.joinToString(", ")}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextParchment
                                        )
                                    }
                                    if (onStartCombat != null) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Button(
                                            onClick = {
                                                autoSave()
                                                onStartCombat(currentNode.monsterIds)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Crimson),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_sword),
                                                contentDescription = null,
                                                tint = TextParchment,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "ENTER TACTICAL COMBAT",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextParchment
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 7. Free-Text Player Action Attempt Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, GoldAccent),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⚔️", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "WHAT DO YOU ATTEMPT?",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldAccent,
                                    letterSpacing = 1.sp
                                )
                            }
                            if (isProcessingTurn) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = GoldAccent,
                                    strokeWidth = 2.dp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = playerFreeTextInput,
                                onValueChange = { playerFreeTextInput = it },
                                placeholder = {
                                    Text(
                                        "Say what you attempt... e.g. 'I inspect the runes', 'I drink a potion'",
                                        fontSize = 12.sp,
                                        color = TextMuted
                                    )
                                },
                                singleLine = true,
                                enabled = !isProcessingTurn,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldAccent,
                                    unfocusedBorderColor = DarkBorder,
                                    focusedTextColor = TextParchment,
                                    unfocusedTextColor = TextParchment
                                )
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = {
                                    if (playerFreeTextInput.isNotBlank()) {
                                        val attemptText = playerFreeTextInput
                                        playerFreeTextInput = ""
                                        scope.launch {
                                            isProcessingTurn = true
                                            try {
                                                val result = pipelineCoordinator.executeTurn(
                                                    campaignId = campaignId,
                                                    character = character,
                                                    playerFreeText = attemptText,
                                                    locationTitle = currentNode.title
                                                )
                                                liveDmNarration = result.dmNarration
                                                dynamicSuggestedActions = result.suggestedActions
                                                if (result.ruleExplanation != null) {
                                                    activeRuleExplanation = result.ruleExplanation
                                                    lastDiceLog = result.ruleExplanation.breakdownText
                                                }
                                                if (result.diceRollOutcome != null) {
                                                    if (result.diceRollOutcome.isCriticalHit) soundManager.playDiceCrit()
                                                    else soundManager.playDiceRoll()
                                                }
                                                if (result.isCombatTriggered && onStartCombat != null) {
                                                    onStartCombat(result.combatEnemyIds.ifEmpty { currentNode.monsterIds })
                                                }
                                                refreshKey++
                                                autoSave()
                                            } finally {
                                                isProcessingTurn = false
                                            }
                                        }
                                    }
                                },
                                enabled = !isProcessingTurn && playerFreeTextInput.isNotBlank(),
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(if (playerFreeTextInput.isNotBlank()) GoldAccent else DarkCardSurface, RoundedCornerShape(8.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Submit Attempt",
                                    tint = if (playerFreeTextInput.isNotBlank()) TextDark else TextMuted
                                )
                            }
                        }

                        // Dynamic DM Suggestion Pills
                        if (dynamicSuggestedActions.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                "DM SUGGESTED ACTIONS:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                dynamicSuggestedActions.take(3).forEach { suggestion ->
                                    Surface(
                                        onClick = {
                                            playerFreeTextInput = suggestion
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        color = DarkCardSurface,
                                        border = BorderStroke(1.dp, GoldMuted)
                                    ) {
                                        Text(
                                            text = suggestion,
                                            fontSize = 11.sp,
                                            color = GoldAccent,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 8. Section Label
            item {
                Text(
                    text = "AVAILABLE ACTIONS & DIALOGUE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // 8. Choices List (with BG3-style moral feedback)
            items(availableChoices) { choice ->
                Card(
                    onClick = {
                        val result = adventureEngine.executeChoice(choice.id, character)
                        triggerHapticFeedback(context, result.rollOutcome?.isCriticalHit == true)
                        if (result.rollOutcome != null) {
                            if (result.rollOutcome.isCriticalHit) {
                                soundManager.playDiceCrit()
                            } else {
                                soundManager.playDiceRoll()
                            }
                        }
                        if (choice.skillCheck != null) {
                            val (_, explanation) = dmEngine.evaluateSkillCheck(character, choice.skillCheck.skill, choice.skillCheck.dc, selectedRollType)
                            activeRuleExplanation = explanation
                        }
                        if (result.narrativeLog.isNotEmpty()) {
                            lastDiceLog = result.narrativeLog
                        }
                        if (result.companionReactions.isNotEmpty()) {
                            companionReactionLog = result.companionReactions
                        }
                        refreshKey++
                        autoSave()
                    },
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, if (choice.skillCheck != null) GoldMuted else DarkBorder),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = choice.text,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextParchment
                            )
                            if (choice.skillCheck != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    color = GoldAccent.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "${choice.skillCheck.skill.label} DC ${choice.skillCheck.dc}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldAccent,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(16.dp))
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }

    // Modal Polyhedral Dice Tray Dialog
    if (showDiceTray) {
        AlertDialog(
            onDismissRequest = { showDiceTray = false },
            containerColor = DarkCardSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_d20),
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("TABLETOP DICE TRAY", color = GoldAccent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Select Roll Condition:", color = TextMuted, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        RollType.values().forEach { rType ->
                            FilterChip(
                                selected = (selectedRollType == rType),
                                onClick = { selectedRollType = rType },
                                label = { Text(rType.label, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GoldAccent,
                                    selectedLabelColor = TextDark
                                )
                            )
                        }
                    }

                    HorizontalDivider(color = DarkBorder)

                    Text("Tap Die to Roll:", color = TextMuted, fontSize = 12.sp)
                    val diceList = listOf(4, 6, 8, 10, 12, 20, 100)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        diceList.forEach { sides ->
                            Button(
                                onClick = {
                                    val res = if (sides == 20) {
                                        diceEngine.rollD20(0, selectedRollType)
                                    } else {
                                        diceEngine.rollDamage(1, sides)
                                    }
                                    manualDiceResult = res
                                    triggerHapticFeedback(context, res.isCriticalHit)
                                    if (res.isCriticalHit) {
                                        soundManager.playDiceCrit()
                                    } else {
                                        soundManager.playDiceRoll()
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                                border = BorderStroke(1.dp, GoldMuted)
                            ) {
                                Text("d$sides", fontSize = 11.sp, color = TextParchment)
                            }
                        }
                    }

                    if (manualDiceResult != null) {
                        Surface(
                            color = DarkSurface,
                            border = BorderStroke(1.dp, GoldAccent),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    "Result: ${manualDiceResult?.summary()}",
                                    color = GoldAccent,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDiceTray = false }) {
                    Text("Done", color = GoldAccent, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showInventoryModal) {
        InventoryModal(
            character = character,
            diceEngine = diceEngine,
            onDismiss = { showInventoryModal = false },
            onItemUsed = { message ->
                soundManager.playPotionDrink()
                lastDiceLog = message
                refreshKey++
                autoSave()
            }
        )
    }

    if (showLevelUpDialog) {
        LevelUpDialog(
            character = character,
            diceEngine = diceEngine,
            onDismiss = { showLevelUpDialog = false },
            onLevelUpConfirmed = { result ->
                soundManager.playLevelUp()
                showLevelUpDialog = false
                lastDiceLog = "ASCENSION! Level ${result.newLevel} reached (+${result.hpGained} HP)!"
                refreshKey++
                autoSave()
            }
        )
    }

    if (activeRuleExplanation != null) {
        val expl = activeRuleExplanation!!
        AlertDialog(
            onDismissRequest = { activeRuleExplanation = null },
            containerColor = DarkCardSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_scroll),
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(expl.title, color = GoldAccent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        color = DarkSurface,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, GoldAccent),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = expl.breakdownText,
                            color = GoldAccent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Text(expl.adviceText, color = TextParchment, fontSize = 13.sp, lineHeight = 19.sp)
                    HorizontalDivider(color = DarkBorder)
                    Text("Rule Authority: ${expl.ruleCitation}", color = TextMuted, fontSize = 11.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = { activeRuleExplanation = null },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                ) {
                    Text("Got It!", color = TextDark, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
