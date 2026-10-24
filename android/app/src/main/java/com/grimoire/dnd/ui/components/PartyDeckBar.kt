package com.grimoire.dnd.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grimoire.dnd.R
import com.grimoire.dnd.engine.Companion
import com.grimoire.dnd.engine.PartyManager
import com.grimoire.dnd.ui.theme.*

fun getCompanionPortraitRes(companionId: String): Int {
    return when (companionId) {
        "comp_elira" -> R.drawable.portrait_elira
        "comp_thorgar" -> R.drawable.portrait_thorgar
        "comp_vespera" -> R.drawable.portrait_vespera
        else -> R.drawable.portrait_elira
    }
}

@Composable
fun PartyDeckBar(
    partyManager: PartyManager,
    modifier: Modifier = Modifier
) {
    var selectedCompanion by remember { mutableStateOf<Companion?>(null) }
    val recruited = partyManager.companions.filter { it.isRecruited }

    if (recruited.isNotEmpty()) {
        Column(modifier = modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACTIVE PARTY COMPANIONS (${recruited.size}/3)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(recruited) { comp ->
                    Card(
                        onClick = { selectedCompanion = comp },
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = BorderStroke(1.dp, GoldMuted),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = getCompanionPortraitRes(comp.id)),
                                contentDescription = comp.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, GoldAccent, RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = comp.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextParchment
                                )
                                Text(
                                    text = "Approval: ${comp.getApprovalTier()} (${comp.approvalScore}%)",
                                    fontSize = 10.sp,
                                    color = GoldAccent
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Companion detail modal
    if (selectedCompanion != null) {
        val comp = selectedCompanion!!
        AlertDialog(
            onDismissRequest = { selectedCompanion = null },
            containerColor = DarkCardSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = getCompanionPortraitRes(comp.id)),
                        contentDescription = comp.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, GoldAccent, RoundedCornerShape(10.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(comp.name, color = GoldAccent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(comp.title, color = TextMuted, fontSize = 12.sp)
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Moral Alignment: ${comp.moralAlignment.label}", color = TextParchment, fontSize = 13.sp)
                    Text("Approval Standing: ${comp.getApprovalTier()} (${comp.approvalScore}/100)", color = GoldAccent, fontSize = 13.sp)
                    Text("Class: ${comp.character.race} ${comp.character.characterClass.label} (AC: ${comp.character.armorClass}, HP: ${comp.character.hpCurrent}/${comp.character.hpMax})", color = TextParchment, fontSize = 13.sp)
                    HorizontalDivider(color = DarkBorder)
                    Text("Backstory:", fontWeight = FontWeight.Bold, color = TextMuted, fontSize = 12.sp)
                    Text(comp.backstory, color = TextParchment, fontSize = 12.sp, lineHeight = 18.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedCompanion = null }) {
                    Text("Close", color = GoldAccent)
                }
            }
        )
    }
}

