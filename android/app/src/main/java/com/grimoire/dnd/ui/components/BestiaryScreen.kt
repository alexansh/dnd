package com.grimoire.dnd.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grimoire.dnd.R
import com.grimoire.dnd.data.MonsterCatalog
import com.grimoire.dnd.data.MonsterLore
import com.grimoire.dnd.ui.theme.*

fun getMonsterDrawableRes(monsterId: String): Int {
    return when (monsterId) {
        "red_dragon" -> R.drawable.monster_dragon
        "goblin" -> R.drawable.monster_goblin
        "skeleton" -> R.drawable.monster_skeleton
        "beholder" -> R.drawable.monster_beholder
        else -> R.drawable.ic_dragon
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BestiaryModal(
    onDismiss: () -> Unit
) {
    var selectedMonster by remember { mutableStateOf(MonsterCatalog.monsters.first()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DarkCardSurface,
        scrimColor = DarkBackground.copy(alpha = 0.85f),
        dragHandle = { BottomSheetDefaults.DragHandle(color = GoldMuted) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_dragon),
                        contentDescription = null,
                        tint = Crimson,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "MONSTER BESTIARY & CODEX",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent,
                        letterSpacing = 1.sp
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Monster selector carousel
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(MonsterCatalog.monsters) { mon ->
                    val isSelected = mon.id == selectedMonster.id
                    Surface(
                        onClick = { selectedMonster = mon },
                        color = if (isSelected) GoldAccent.copy(alpha = 0.2f) else DarkSurface,
                        border = BorderStroke(1.dp, if (isSelected) GoldAccent else DarkBorder),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = mon.name,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) GoldAccent else TextParchment,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Selected Monster Detail Sheet
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    // Monster Hero Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = BorderStroke(1.dp, GoldMuted),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            // 2D Painting Banner
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = getMonsterDrawableRes(selectedMonster.id)),
                                    contentDescription = selectedMonster.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                                listOf(
                                                    androidx.compose.ui.graphics.Color.Transparent,
                                                    DarkSurface.copy(alpha = 0.95f)
                                                )
                                            )
                                        )
                                )
                                Surface(
                                    color = Crimson,
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = selectedMonster.challengeRating,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextParchment,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = selectedMonster.name,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextParchment,
                                    fontFamily = FontFamily.Serif
                                )
                                Text(
                                    text = selectedMonster.type,
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Vitals Banner
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("ARMOR CLASS", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                        Text("${selectedMonster.armorClass} AC", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("HIT POINTS", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                        Text(selectedMonster.hitPoints, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = HealthRed)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("SPEED", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                        Text(selectedMonster.speed, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextParchment)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = DarkBorder)
                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "LORE & ECOLOGY",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldAccent,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = selectedMonster.description,
                                    fontSize = 13.sp,
                                    color = TextParchment,
                                    lineHeight = 19.sp
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = "5E ACTIONS & TRAITS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldAccent,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                selectedMonster.actions.forEach { act ->
                                    Text(
                                        text = "• $act",
                                        fontSize = 12.sp,
                                        color = TextParchment,
                                        lineHeight = 18.sp,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}
