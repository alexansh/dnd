package com.grimoire.dnd.ui.components

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
import androidx.compose.ui.window.Dialog
import com.grimoire.dnd.R
import com.grimoire.dnd.data.db.*
import com.grimoire.dnd.ui.theme.*

enum class JournalTab(val label: String) {
    CLOCKS("Clocks & Quests"),
    NPCS("Codex & NPCs"),
    CANON("Canon Lore"),
    SETTINGS("Settings")
}

@Composable
fun LivingJournalModal(
    turnContext: TurnContext?,
    onDismiss: () -> Unit,
    onToneChanged: (String) -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(JournalTab.CLOCKS) }
    var currentTone by remember { mutableStateOf("Heroic High Fantasy") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DarkBackground,
            border = BorderStroke(1.dp, GoldAccent),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_scroll),
                            contentDescription = "Living Journal",
                            tint = GoldAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "LIVING CHRONICLE",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent,
                            letterSpacing = 1.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Text("✕", fontSize = 16.sp, color = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Navigation Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    containerColor = DarkSurface,
                    contentColor = GoldAccent,
                    edgePadding = 0.dp
                ) {
                    JournalTab.values().forEach { tab ->
                        Tab(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            text = { Text(tab.label, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Content
                when (selectedTab) {
                    JournalTab.CLOCKS -> {
                        val clocks = turnContext?.activeClocks ?: emptyList()
                        if (clocks.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No active countdown clocks.", color = TextMuted, fontSize = 13.sp)
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(clocks) { clock ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                                        border = BorderStroke(1.dp, DarkBorder),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(clock.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextParchment)
                                                Text("${clock.currentTicks}/${clock.maxTicks} Ticks", fontSize = 12.sp, color = GoldAccent, fontWeight = FontWeight.Bold)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            LinearProgressIndicator(
                                                progress = { clock.progressFraction },
                                                modifier = Modifier.fillMaxWidth().height(6.dp),
                                                color = if (clock.progressFraction >= 0.75f) Crimson else GoldAccent,
                                                trackColor = DarkBackground
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(clock.description, fontSize = 11.sp, color = TextMuted)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    JournalTab.NPCS -> {
                        val npcs = turnContext?.visibleEntities?.filter { it.entityType.equals("NPC", true) } ?: emptyList()
                        if (npcs.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No NPCs encountered yet.", color = TextMuted, fontSize = 13.sp)
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(npcs) { npc ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                                        border = BorderStroke(1.dp, GoldMuted),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(npc.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(npc.description, fontSize = 12.sp, color = TextParchment, lineHeight = 16.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    JournalTab.CANON -> {
                        val facts = turnContext?.canonFacts ?: emptyList()
                        if (facts.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No verified canon facts recorded yet.", color = TextMuted, fontSize = 13.sp)
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(facts) { fact ->
                                    Surface(
                                        color = DarkSurface,
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.dp, DarkBorder),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(fact.subject.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                                                Text("Turn ${fact.turnIndex}", fontSize = 10.sp, color = TextMuted)
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(fact.assertionText, fontSize = 12.sp, color = TextParchment, fontFamily = FontFamily.Serif)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    JournalTab.SETTINGS -> {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text("NARRATIVE TONE & SAFETY", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GoldAccent, letterSpacing = 1.sp)
                            listOf("Heroic High Fantasy", "Gritty Gothic Horror", "Cunning Mystery").forEach { tone ->
                                Surface(
                                    onClick = {
                                        currentTone = tone
                                        onToneChanged(tone)
                                    },
                                    color = if (currentTone == tone) GoldAccent.copy(alpha = 0.2f) else DarkSurface,
                                    border = BorderStroke(1.dp, if (currentTone == tone) GoldAccent else DarkBorder),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(selected = currentTone == tone, onClick = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(tone, color = TextParchment, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text("ACCESSIBILITY", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GoldAccent, letterSpacing = 1.sp)
                            Text("• High contrast font tokens enabled by default.\n• Compatible with Android TalkBack screen reader.\n• Colorblind-safe dice roll alerts.", fontSize = 12.sp, color = TextMuted, lineHeight = 18.sp)
                        }
                    }
                }
            }
        }
    }
}
