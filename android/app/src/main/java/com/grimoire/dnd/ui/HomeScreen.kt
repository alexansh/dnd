package com.grimoire.dnd.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grimoire.dnd.R
import com.grimoire.dnd.data.LocalSaveManager
import com.grimoire.dnd.data.SaveGameData
import com.grimoire.dnd.ui.components.BestiaryModal
import com.grimoire.dnd.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    saveManager: LocalSaveManager,
    onNewAdventure: () -> Unit,
    onContinueGame: (SaveGameData) -> Unit,
    onOpenDiceTray: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showLoadModal by remember { mutableStateOf(false) }
    var showBestiaryModal by remember { mutableStateOf(false) }
    var savesList by remember { mutableStateOf(saveManager.listSaves()) }
    val latestSave = remember(savesList) { saveManager.getLatestSave() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_dragon),
                contentDescription = "Grimoire Crest",
                tint = GoldAccent,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "GRIMOIRE 5E",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = GoldAccent,
                letterSpacing = 3.sp,
                fontFamily = FontFamily.Serif
            )
            Text(
                text = "THE TABLETOP CHRONICLE",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMuted,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(44.dp))

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(0.9f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Continue Button (if existing save)
                if (latestSave != null) {
                    Button(
                        onClick = { onContinueGame(latestSave) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_sword),
                            contentDescription = null,
                            tint = TextDark,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CONTINUE: ${latestSave.character.name.uppercase()}",
                            color = TextDark,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // New Adventure Button
                Button(
                    onClick = onNewAdventure,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (latestSave != null) DarkCardSurface else GoldAccent
                    ),
                    border = BorderStroke(1.dp, GoldAccent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_scroll),
                        contentDescription = null,
                        tint = if (latestSave != null) GoldAccent else TextDark,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "NEW ADVENTURE",
                        color = if (latestSave != null) TextParchment else TextDark,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                // Load Game Button
                OutlinedButton(
                    onClick = {
                        savesList = saveManager.listSaves()
                        showLoadModal = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    border = BorderStroke(1.dp, DarkBorder),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("LOAD SAVED GAME (${savesList.size})", color = TextParchment, fontSize = 14.sp)
                }

                // Open Dice Tray Anytime
                OutlinedButton(
                    onClick = onOpenDiceTray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    border = BorderStroke(1.dp, DarkBorder),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_d20),
                        contentDescription = null,
                        tint = GoldMuted,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("QUICK DICE TRAY", color = TextMuted, fontSize = 14.sp)
                }

                // Monster Bestiary Button
                OutlinedButton(
                    onClick = { showBestiaryModal = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    border = BorderStroke(1.dp, GoldMuted),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_dragon),
                        contentDescription = null,
                        tint = Crimson,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("MONSTER CODEX & BESTIARY", color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = "100% Offline • D&D 5th Edition SRD 5.1 Rules",
                fontSize = 11.sp,
                color = DarkBorder
            )
        }
    }

    // Load Game Modal
    if (showLoadModal) {
        AlertDialog(
            onDismissRequest = { showLoadModal = false },
            containerColor = DarkCardSurface,
            title = {
                Text("SAVED ADVENTURES", color = GoldAccent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                if (savesList.isEmpty()) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text("No saved games found. Start a new adventure!", color = TextMuted)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(savesList) { save ->
                            val dateStr = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(save.timestamp))
                            Card(
                                onClick = {
                                    showLoadModal = false
                                    onContinueGame(save)
                                },
                                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                                border = BorderStroke(1.dp, DarkBorder),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            save.character.name,
                                            color = TextParchment,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            "${save.character.race} ${save.character.characterClass.label} Lv.${save.character.level}",
                                            color = GoldAccent,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            "HP: ${save.character.hpCurrent}/${save.character.hpMax} • $dateStr",
                                            color = TextMuted,
                                            fontSize = 11.sp
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            saveManager.deleteSave(save.slotId)
                                            savesList = saveManager.listSaves()
                                        }
                                    ) {
                                        Text("🗑️", fontSize = 16.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLoadModal = false }) {
                    Text("Close", color = GoldAccent)
                }
            }
        )
    }

    if (showBestiaryModal) {
        BestiaryModal(onDismiss = { showBestiaryModal = false })
    }
}
