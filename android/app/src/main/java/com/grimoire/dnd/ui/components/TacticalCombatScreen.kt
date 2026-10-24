package com.grimoire.dnd.ui.components

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.grimoire.dnd.engine.*
import com.grimoire.dnd.ui.theme.*
import com.grimoire.dnd.ui.triggerHapticFeedback

@Composable
fun TacticalCombatScreen(
    arena: TacticalCombatArena,
    onCombatFinished: (isVictory: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val soundManager = remember { SoundManager.getInstance(context) }
    var selectedTarget by remember { mutableStateOf<Combatant?>(null) }
    var actionMessage by remember { mutableStateOf<String?>(null) }
    var refreshKey by remember { mutableStateOf(0) }

    val currentTurnCombatant = remember(refreshKey) { arena.getCurrentCombatant() }
    val livingHeroes = remember(refreshKey) { arena.combatants.filter { it.isPlayerParty && !it.isDowned } }
    val livingEnemies = remember(refreshKey) { arena.combatants.filter { !it.isPlayerParty && !it.isDowned } }

    LaunchedEffect(Unit) {
        soundManager.playBattleMusic()
    }

    LaunchedEffect(refreshKey) {
        if (arena.isCombatResolved) {
            onCombatFinished(arena.isVictory)
        } else if (currentTurnCombatant != null && !currentTurnCombatant.isPlayerParty && !currentTurnCombatant.hasTakenAction) {
            // Trigger Enemy AI Move
            val aiLog = arena.executeAITurn(currentTurnCombatant)
            actionMessage = aiLog
            triggerHapticFeedback(context, true)
            arena.nextTurn()
            refreshKey++
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground,
        topBar = {
            Surface(
                color = DarkSurface,
                border = BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TACTICAL BATTLE ARENA",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "ROUND ${arena.roundNumber} • ACTIVE: ${currentTurnCombatant?.name ?: "None"}",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }

                    Surface(
                        color = if (currentTurnCombatant?.isPlayerParty == true) HealthGreen.copy(alpha = 0.2f) else Crimson.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, if (currentTurnCombatant?.isPlayerParty == true) HealthGreen else Crimson),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (currentTurnCombatant?.isPlayerParty == true) "YOUR TURN" else "ENEMY TURN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (currentTurnCombatant?.isPlayerParty == true) HealthGreen else Crimson,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        },
        bottomBar = {
            if (currentTurnCombatant != null && currentTurnCombatant.isPlayerParty && !arena.isCombatResolved) {
                Surface(
                    color = DarkSurface,
                    border = BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "COMMAND ${currentTurnCombatant.name.uppercase()}:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Melee Attack Button
                            Button(
                                onClick = {
                                    val target = selectedTarget ?: livingEnemies.firstOrNull()
                                    if (target != null) {
                                        soundManager.playSwordHit()
                                        actionMessage = arena.executePlayerAction(currentTurnCombatant, target, CombatActionType.ATTACK_MELEE)
                                        triggerHapticFeedback(context, false)
                                        arena.nextTurn()
                                        refreshKey++
                                    }
                                },
                                enabled = livingEnemies.isNotEmpty(),
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                            ) {
                                Text("ATTACK", color = TextDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            // Cast Spell Button
                            Button(
                                onClick = {
                                    val target = selectedTarget ?: livingEnemies.firstOrNull()
                                    if (target != null) {
                                        soundManager.playSpellCast()
                                        actionMessage = arena.executePlayerAction(currentTurnCombatant, target, CombatActionType.CAST_SPELL)
                                        triggerHapticFeedback(context, true)
                                        arena.nextTurn()
                                        refreshKey++
                                    }
                                },
                                enabled = livingEnemies.isNotEmpty(),
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = DarkCardSurface),
                                border = BorderStroke(1.dp, GoldAccent)
                            ) {
                                Text("SPELL", color = TextParchment, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            // Potion / Heal
                            Button(
                                onClick = {
                                    soundManager.playPotionDrink()
                                    actionMessage = arena.executePlayerAction(currentTurnCombatant, currentTurnCombatant, CombatActionType.USE_POTION)
                                    arena.nextTurn()
                                    refreshKey++
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = DarkCardSurface),
                                border = BorderStroke(1.dp, DarkBorder)
                            ) {
                                Text("POTION", color = TextParchment, fontSize = 12.sp)
                            }

                            // Defensive Dodge
                            Button(
                                onClick = {
                                    actionMessage = arena.executePlayerAction(currentTurnCombatant, currentTurnCombatant, CombatActionType.DODGE)
                                    arena.nextTurn()
                                    refreshKey++
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = DarkCardSurface),
                                border = BorderStroke(1.dp, DarkBorder)
                            ) {
                                Text("DODGE", color = TextParchment, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Action Feedback Alert
            if (actionMessage != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = BorderStroke(1.dp, GoldMuted),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⚔️", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = actionMessage ?: "",
                                color = TextParchment,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // 1. Enemy Battlefront Row
            item {
                Text(
                    text = "ENEMY HOSTILES (TAP TO TARGET)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Crimson,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(livingEnemies) { enemy ->
                        val isSelected = selectedTarget?.id == enemy.id
                        Card(
                            onClick = { selectedTarget = enemy },
                            colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
                            border = BorderStroke(
                                if (isSelected) 2.dp else 1.dp,
                                if (isSelected) GoldAccent else Crimson
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.width(160.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = enemy.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextParchment
                                    )
                                    Text("${enemy.armorClass} AC", fontSize = 11.sp, color = GoldAccent)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "HP: ${enemy.currentHp}/${enemy.maxHp}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (enemy.currentHp < enemy.maxHp / 2) Crimson else HealthGreen
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { (enemy.currentHp.toFloat() / enemy.maxHp.toFloat()).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = Crimson,
                                    trackColor = DarkSurface
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Zone: ${enemy.currentZone.label.take(12)}", fontSize = 10.sp, color = TextMuted)
                            }
                        }
                    }
                }
            }

            // 2. Player Adventurer Party Frontline
            item {
                Text(
                    text = "YOUR ADVENTURING PARTY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(livingHeroes) { hero ->
                        val isActiveTurn = currentTurnCombatant?.id == hero.id
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
                            border = BorderStroke(
                                if (isActiveTurn) 2.dp else 1.dp,
                                if (isActiveTurn) GoldAccent else DarkBorder
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.width(160.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = hero.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextParchment
                                    )
                                    Text("${hero.armorClass} AC", fontSize = 11.sp, color = GoldAccent)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "HP: ${hero.currentHp}/${hero.maxHp}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (hero.currentHp < hero.maxHp / 3) HealthRed else HealthGreen
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { (hero.currentHp.toFloat() / hero.maxHp.toFloat()).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = HealthGreen,
                                    trackColor = DarkSurface
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (isActiveTurn) "ACTIVE TURN" else "Waiting...",
                                    fontSize = 10.sp,
                                    color = if (isActiveTurn) GoldAccent else TextMuted,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // 3. Combat Chronicle Log
            item {
                Text(
                    text = "BATTLE CHRONICLE LOG",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )
            }

            items(arena.combatLog.takeLast(5).reversed()) { entry ->
                Surface(
                    color = DarkSurface,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "R${entry.round}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldMuted
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = entry.actionText,
                            fontSize = 12.sp,
                            color = TextParchment
                        )
                    }
                }
            }
        }
    }
}
