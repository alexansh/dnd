package com.grimoire.dnd.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grimoire.dnd.R
import com.grimoire.dnd.engine.Character
import com.grimoire.dnd.engine.DiceEngine
import com.grimoire.dnd.engine.Item
import com.grimoire.dnd.engine.ItemType
import com.grimoire.dnd.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryModal(
    character: Character,
    diceEngine: DiceEngine,
    onDismiss: () -> Unit,
    onItemUsed: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var refreshTrigger by remember { mutableStateOf(0) }
    val tabs = listOf("All", "Gear", "Potions", "Treasures")

    val filteredItems = remember(selectedTab, refreshTrigger, character.inventory.size) {
        when (selectedTab) {
            1 -> character.inventory.filter { it.itemType in listOf(ItemType.WEAPON, ItemType.ARMOR, ItemType.SHIELD) }
            2 -> character.inventory.filter { it.itemType in listOf(ItemType.POTION, ItemType.SCROLL) }
            3 -> character.inventory.filter { it.itemType in listOf(ItemType.VALUABLE, ItemType.QUEST) }
            else -> character.inventory
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DarkCardSurface,
        scrimColor = DarkBackground.copy(alpha = 0.8f),
        dragHandle = { BottomSheetDefaults.DragHandle(color = GoldMuted) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 16.dp)
        ) {
            // Header Bar
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
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "EQUIPMENT & SATCHEL",
                        fontSize = 18.sp,
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

            // Stat & Coin Banner
            Surface(
                color = DarkSurface,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ARMOR CLASS", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                        Text("${character.armorClass} AC", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                    }
                    VerticalDivider(modifier = Modifier.height(30.dp), color = DarkBorder)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("PURSE", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_coins),
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${character.gold} GP", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextParchment)
                        }
                    }
                    VerticalDivider(modifier = Modifier.height(30.dp), color = DarkBorder)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("CAPACITY", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                        Text("${character.inventory.size} Items", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextParchment)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkSurface,
                contentColor = GoldAccent,
                divider = { HorizontalDivider(color = DarkBorder) }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) GoldAccent else TextMuted
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Items List
            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No items found in this category.", color = TextMuted, fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredItems, key = { it.id }) { item ->
                        ItemRowCard(
                            item = item,
                            onEquipToggle = {
                                if (item.isEquipped) {
                                    character.unequipItem(item.id)
                                } else {
                                    character.equipItem(item.id)
                                }
                                refreshTrigger++
                            },
                            onUseItem = {
                                val message = character.useItem(item.id, diceEngine)
                                onItemUsed(message)
                                refreshTrigger++
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
fun ItemRowCard(
    item: Item,
    onEquipToggle: () -> Unit,
    onUseItem: () -> Unit
) {
    Surface(
        color = DarkSurface,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, if (item.isEquipped) GoldAccent else DarkBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (item.isEquipped) GoldAccent.copy(alpha = 0.2f) else DarkCardSurface),
                contentAlignment = Alignment.Center
            ) {
                val iconRes = when (item.itemType) {
                    ItemType.WEAPON -> R.drawable.ic_sword
                    ItemType.ARMOR -> R.drawable.ic_shield
                    ItemType.SHIELD -> R.drawable.ic_shield
                    ItemType.POTION -> R.drawable.ic_potion
                    ItemType.SCROLL -> R.drawable.ic_scroll
                    else -> R.drawable.ic_coins
                }
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = if (item.isEquipped) GoldAccent else TextMuted,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextParchment
                    )
                    if (item.quantity > 1) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "x${item.quantity}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent
                        )
                    }
                    if (item.isEquipped) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = GoldAccent,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "EQUIPPED",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextDark,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Stats preview
                val statText = when {
                    item.damageFormula != null -> "Damage: ${item.damageFormula}"
                    item.armorClassBonus > 0 -> "AC: +${item.armorClassBonus}"
                    item.healFormula != null -> "Heals: ${item.healFormula}"
                    else -> item.itemType.label
                }
                Text(statText, fontSize = 12.sp, color = GoldAccent, fontWeight = FontWeight.SemiBold)

                if (item.description.isNotEmpty()) {
                    Text(
                        text = item.description,
                        fontSize = 11.sp,
                        color = TextMuted,
                        maxLines = 2
                    )
                }
            }

            // Action Button
            when (item.itemType) {
                ItemType.WEAPON, ItemType.ARMOR, ItemType.SHIELD -> {
                    Button(
                        onClick = onEquipToggle,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (item.isEquipped) DarkCardSurface else GoldAccent
                        ),
                        border = if (item.isEquipped) BorderStroke(1.dp, GoldMuted) else null,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (item.isEquipped) "Unequip" else "Equip",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (item.isEquipped) TextParchment else TextDark
                        )
                    }
                }
                ItemType.POTION -> {
                    Button(
                        onClick = onUseItem,
                        colors = ButtonDefaults.buttonColors(containerColor = HealthGreen),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Drink",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                    }
                }
                else -> {
                    // Non-equippable items (treasures / quest keys)
                }
            }
        }
    }
}
