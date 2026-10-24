package com.grimoire.dnd.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grimoire.dnd.data.CampaignInfo
import com.grimoire.dnd.data.QuickStartHeroes
import com.grimoire.dnd.engine.*
import com.grimoire.dnd.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterCreationScreen(
    campaign: CampaignInfo,
    onCharacterCreated: (Character) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showPrimerModal by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("Sir Valen") }
    var selectedRace by remember { mutableStateOf("Human") }
    var selectedClass by remember { mutableStateOf(CharacterClass.PALADIN) }

    // Ability scores (Standard Array: 16, 14, 14, 12, 10, 9)
    var str by remember { mutableStateOf(16) }
    var dex by remember { mutableStateOf(10) }
    var con by remember { mutableStateOf(14) }
    var intScore by remember { mutableStateOf(9) }
    var wis by remember { mutableStateOf(12) }
    var cha by remember { mutableStateOf(15) }

    val races = listOf("Human", "High Elf", "Mountain Dwarf", "Halfling", "Tiefling", "Dragonborn")
    val classes = CharacterClass.values().toList()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "CREATE CHARACTER",
                        color = GoldAccent,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", fontSize = 20.sp, color = GoldAccent)
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
                Button(
                    onClick = {
                        val hitDie = selectedClass.hitDie
                        val conMod = calculateAbilityModifier(con)
                        val maxHp = hitDie + conMod

                        val newChar = Character(
                            id = "hero_${System.currentTimeMillis()}",
                            name = name.ifBlank { "Adventurer" },
                            race = selectedRace,
                            characterClass = selectedClass,
                            subclass = selectedClass.primarySubclass,
                            level = 1,
                            strength = str,
                            dexterity = dex,
                            constitution = con,
                            intelligence = intScore,
                            wisdom = wis,
                            charisma = cha,
                            hpCurrent = maxHp,
                            hpMax = maxHp,
                            hitDiceCurrent = 1,
                            hitDiceMax = 1,
                            gold = 15,
                            savingThrowProficiencies = mutableListOf(Ability.WIS, Ability.CHA),
                            skillProficiencies = mutableListOf(Skill.ATHLETICS, Skill.PERSUASION)
                        )

                        // Add starting gear
                        when (selectedClass) {
                            CharacterClass.ROGUE -> {
                                newChar.inventory.add(Item("shortsword", "Shortsword", 1, isEquipped = true, damageFormula = "1d6+3", itemType = ItemType.WEAPON, description = "Finesse blade ideal for precise strikes."))
                                newChar.inventory.add(Item("leather", "Leather Armor", 1, isEquipped = true, armorClassBonus = 11, itemType = ItemType.ARMOR, description = "Supple molded leather armor."))
                            }
                            CharacterClass.WIZARD -> {
                                newChar.inventory.add(Item("staff", "Arcane Quarterstaff", 1, isEquipped = true, damageFormula = "1d6", itemType = ItemType.WEAPON, description = "Carved ash staff crowned with an amethyst focus."))
                                newChar.spellSlots[1] = SpellSlotPool(1, 2, 2)
                            }
                            else -> {
                                newChar.inventory.add(Item("longsword", "Steel Longsword", 1, isEquipped = true, damageFormula = "1d8+3", itemType = ItemType.WEAPON, description = "Tempered steel blade with a leather-wrapped grip."))
                                newChar.inventory.add(Item("chain", "Chain Mail", 1, isEquipped = true, armorClassBonus = 16, itemType = ItemType.ARMOR, description = "Heavy interlocking metal rings."))
                                newChar.inventory.add(Item("shield", "Shield", 1, isEquipped = true, armorClassBonus = 2, itemType = ItemType.SHIELD, description = "Reinforced wooden heater shield."))
                            }
                        }
                        newChar.inventory.add(Item("potion_starter", "Potion of Healing", 2, itemType = ItemType.POTION, healFormula = "2d4+2", description = "Restores 2d4+2 hit points when consumed."))

                        onCharacterCreated(newChar)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("START ADVENTURE", color = TextDark, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Selected Campaign reminder
            item {
                Surface(
                    color = DarkCardSurface,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, GoldMuted),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📜", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("CAMPAIGN: ${campaign.title}", color = GoldAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(campaign.subtitle, color = TextMuted, fontSize = 11.sp)
                        }
                    }
                }
            }

            // 1. Beginner 30-Sec Primer Banner
            item {
                Card(
                    onClick = { showPrimerModal = true },
                    colors = CardDefaults.cardColors(containerColor = GoldAccent.copy(alpha = 0.15f)),
                    border = BorderStroke(1.dp, GoldAccent),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("💡", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "NEW TO D&D? TAP FOR 30-SEC CRASH COURSE",
                                color = GoldAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                "Learn how dice, DC checks, and the Dungeon Master work in seconds.",
                                color = TextParchment,
                                fontSize = 11.sp
                            )
                        }
                        Text("READ ➔", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 2. Quick-Start Pre-Made Heroes (Recommended for Newcomers)
            item {
                Text(
                    "⚡ QUICK-START HEROES (1-TAP PLAY)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Ideal for first-time players. Choose a hero and jump straight into the story!",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            // 4 Pre-made hero cards
            items(QuickStartHeroes.getStarterHeroes().size) { idx ->
                val hero = QuickStartHeroes.getStarterHeroes()[idx]
                Surface(
                    color = DarkSurface,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, GoldMuted),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = hero.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextParchment
                                )
                                Text(
                                    text = hero.subtitle,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = GoldAccent
                                )
                            }
                            Button(
                                onClick = { onCharacterCreated(hero.character) },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text("EMBARK", color = TextDark, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = hero.tagline,
                            fontSize = 11.sp,
                            color = TextMuted,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // 3. Or Custom Character Creation Section
            item {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = DarkBorder)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "🛠️ OR FORGE A CUSTOM HERO (ADVANCED)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )
            }

            // Character Name
            item {
                Text("CHARACTER NAME", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextParchment,
                        unfocusedTextColor = TextParchment,
                        focusedBorderColor = GoldAccent,
                        unfocusedBorderColor = DarkBorder,
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Race Selection
            item {
                Text("RACE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    races.take(3).forEach { r ->
                        FilterChip(
                            selected = (selectedRace == r),
                            onClick = { selectedRace = r },
                            label = { Text(r, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldAccent,
                                selectedLabelColor = TextDark
                            )
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    races.drop(3).forEach { r ->
                        FilterChip(
                            selected = (selectedRace == r),
                            onClick = { selectedRace = r },
                            label = { Text(r, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldAccent,
                                selectedLabelColor = TextDark
                            )
                        )
                    }
                }
            }

            // Class Selection
            item {
                Text("CLASS (SRD 5.1)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    classes.take(6).chunked(3).forEach { rowClasses ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rowClasses.forEach { c ->
                                FilterChip(
                                    selected = (selectedClass == c),
                                    onClick = { selectedClass = c },
                                    label = { Text("${c.label} (d${c.hitDie})", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = GoldAccent,
                                        selectedLabelColor = TextDark
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Ability Scores Overview
            item {
                Text("ABILITY SCORES & MODIFIERS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
                    border = BorderStroke(1.dp, DarkBorder),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        val scoreList = listOf(
                            Triple("STR", str, calculateAbilityModifier(str)),
                            Triple("DEX", dex, calculateAbilityModifier(dex)),
                            Triple("CON", con, calculateAbilityModifier(con)),
                            Triple("INT", intScore, calculateAbilityModifier(intScore)),
                            Triple("WIS", wis, calculateAbilityModifier(wis)),
                            Triple("CHA", cha, calculateAbilityModifier(cha))
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            scoreList.forEach { (lbl, score, mod) ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(lbl, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                                    Text("$score", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = TextParchment)
                                    Text(
                                        "${if (mod >= 0) "+" else ""}$mod",
                                        fontSize = 12.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 30-Second D&D Beginner Crash Course Modal
    if (showPrimerModal) {
        AlertDialog(
            onDismissRequest = { showPrimerModal = false },
            containerColor = DarkCardSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🎲", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "D&D 5E IN 30 SECONDS",
                        color = GoldAccent,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Welcome to Tabletop D&D! You don't need to know any rules to play—the phone enforces every mechanic for you.",
                        color = TextParchment,
                        fontSize = 13.sp
                    )
                    HorizontalDivider(color = DarkBorder)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("1. The 20-Sided Die (d20)", color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Whenever you attempt an action (attacking, sneaking, persuading), you roll a d20. Higher is always better!", color = TextParchment, fontSize = 12.sp)

                        Text("2. Modifiers (+2, +3)", color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Your attributes add bonus points. A nimble Rogue adds their Dexterity (+3) directly to their d20 roll.", color = TextParchment, fontSize = 12.sp)

                        Text("3. DC (Difficulty Class)", color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("The target number to beat. If picking a lock is DC 12 and your roll + bonus totals 14, you succeed!", color = TextParchment, fontSize = 12.sp)

                        Text("4. Companions & Morality", color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Just like in Baldur's Gate 3, companions watch every deed. Show mercy or strike ruthlessly—they will react!", color = TextParchment, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPrimerModal = false },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                ) {
                    Text("I'm Ready to Play!", color = TextDark, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
