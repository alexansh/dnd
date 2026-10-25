package com.grimoire.dnd.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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

    // Dynamic 5E Base Ability Scores (defaults to class recommended array)
    var baseScores by remember {
        mutableStateOf(CharacterCreationEngine.getRecommendedBaseArray(CharacterClass.PALADIN))
    }

    // Racial bonuses
    val racialBonus = remember(selectedRace) {
        CharacterCreationEngine.getRacialBonus(selectedRace)
    }

    // Final Ability Scores = Base + Racial Bonus
    val finalStr = baseScores.str + racialBonus.str
    val finalDex = baseScores.dex + racialBonus.dex
    val finalCon = baseScores.con + racialBonus.con
    val finalInt = baseScores.int + racialBonus.int
    val finalWis = baseScores.wis + racialBonus.wis
    val finalCha = baseScores.cha + racialBonus.cha

    val conMod = calculateAbilityModifier(finalCon)
    val maxHp = maxOf(1, selectedClass.hitDie + conMod)
    val remainingPoints = CharacterCreationEngine.getRemainingPoints(baseScores)

    val races = CharacterCreationEngine.AVAILABLE_RACES
    val classes = CharacterClass.values().toList()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "FORGE CHARACTER",
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
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${name.ifBlank { "Hero" }} • Level 1 ${selectedRace} ${selectedClass.label}",
                                color = TextParchment,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Max HP: $maxHp  •  Hit Die: d${selectedClass.hitDie}  •  Points Left: $remainingPoints",
                                color = GoldAccent,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            val newChar = Character(
                                id = "hero_${System.currentTimeMillis()}",
                                name = name.ifBlank { "Adventurer" },
                                race = selectedRace,
                                characterClass = selectedClass,
                                subclass = selectedClass.primarySubclass,
                                level = 1,
                                strength = finalStr,
                                dexterity = finalDex,
                                constitution = finalCon,
                                intelligence = finalInt,
                                wisdom = finalWis,
                                charisma = finalCha,
                                hpCurrent = maxHp,
                                hpMax = maxHp,
                                hitDiceCurrent = 1,
                                hitDiceMax = 1,
                                gold = 15,
                                savingThrowProficiencies = CharacterCreationEngine.getClassSavingThrows(selectedClass).toMutableList(),
                                skillProficiencies = mutableListOf(Skill.ATHLETICS, Skill.PERCEPTION)
                            )

                            // Equip authentic 5E starting weapons, armor, spells and gear
                            CharacterCreationEngine.populateStartingGearAndSpells(newChar)

                            onCharacterCreated(newChar)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("START ADVENTURE", color = TextDark, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp),
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
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Choose a seasoned adventurer and jump straight into the story!",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            // Pre-made hero cards
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

            // 3. Custom Character Creation Section
            item {
                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(color = DarkBorder)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "🛠️ FORGE A CUSTOM HERO (5E SRD 5.1)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GoldAccent,
                    letterSpacing = 1.sp
                )
                Text(
                    "Select your race and class to auto-load standard arrays, or customize your scores via Point Buy.",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            // Character Name
            item {
                Text("CHARACTER NAME", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                Spacer(modifier = Modifier.height(4.dp))
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("RACE & RACIAL TRAITS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Text(racialBonus.summary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = GoldAccent)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    races.chunked(3).forEach { rowRaces ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rowRaces.forEach { r ->
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
                }
            }

            // Class Selection
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("CLASS (ALL 12 SRD CLASSES)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Text("Hit Die: d${selectedClass.hitDie}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = GoldAccent)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    classes.chunked(3).forEach { rowClasses ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rowClasses.forEach { c ->
                                FilterChip(
                                    selected = (selectedClass == c),
                                    onClick = {
                                        selectedClass = c
                                        // Dynamically re-assign optimal base array for chosen class!
                                        baseScores = CharacterCreationEngine.getRecommendedBaseArray(c)
                                    },
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

            // Ability Scores Section with Point Buy Stepper Controls
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "ABILITY SCORES (POINT BUY)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                    Surface(
                        color = if (remainingPoints == 0) DarkCardSurface else GoldAccent.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, if (remainingPoints == 0) DarkBorder else GoldAccent),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Points Left: $remainingPoints / 27",
                            color = if (remainingPoints == 0) TextMuted else GoldAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
                    border = BorderStroke(1.dp, DarkBorder),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Quick Reset Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Key: ${CharacterCreationEngine.getPrimaryAbility(selectedClass)}",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                            TextButton(
                                onClick = {
                                    baseScores = CharacterCreationEngine.getRecommendedBaseArray(selectedClass)
                                },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("↺ RESET TO ${selectedClass.label.uppercase()}", fontSize = 10.sp, color = GoldAccent)
                            }
                        }

                        HorizontalDivider(color = DarkBorder)

                        // 6 Abilities
                        AbilityScoreRow(
                            name = "Strength",
                            code = "STR",
                            isPrimary = selectedClass == CharacterClass.BARBARIAN || selectedClass == CharacterClass.FIGHTER || selectedClass == CharacterClass.PALADIN,
                            baseScore = baseScores.str,
                            racialBonus = racialBonus.str,
                            finalScore = finalStr,
                            canMinus = CharacterCreationEngine.canDecrease(baseScores.str),
                            canPlus = CharacterCreationEngine.canIncrease(baseScores.str, remainingPoints),
                            onMinus = {
                                if (CharacterCreationEngine.canDecrease(baseScores.str)) {
                                    baseScores = baseScores.copy(str = baseScores.str - 1)
                                }
                            },
                            onPlus = {
                                if (CharacterCreationEngine.canIncrease(baseScores.str, remainingPoints)) {
                                    baseScores = baseScores.copy(str = baseScores.str + 1)
                                }
                            }
                        )

                        AbilityScoreRow(
                            name = "Dexterity",
                            code = "DEX",
                            isPrimary = selectedClass == CharacterClass.ROGUE || selectedClass == CharacterClass.RANGER || selectedClass == CharacterClass.MONK,
                            baseScore = baseScores.dex,
                            racialBonus = racialBonus.dex,
                            finalScore = finalDex,
                            canMinus = CharacterCreationEngine.canDecrease(baseScores.dex),
                            canPlus = CharacterCreationEngine.canIncrease(baseScores.dex, remainingPoints),
                            onMinus = {
                                if (CharacterCreationEngine.canDecrease(baseScores.dex)) {
                                    baseScores = baseScores.copy(dex = baseScores.dex - 1)
                                }
                            },
                            onPlus = {
                                if (CharacterCreationEngine.canIncrease(baseScores.dex, remainingPoints)) {
                                    baseScores = baseScores.copy(dex = baseScores.dex + 1)
                                }
                            }
                        )

                        AbilityScoreRow(
                            name = "Constitution",
                            code = "CON",
                            isPrimary = selectedClass == CharacterClass.BARBARIAN,
                            baseScore = baseScores.con,
                            racialBonus = racialBonus.con,
                            finalScore = finalCon,
                            canMinus = CharacterCreationEngine.canDecrease(baseScores.con),
                            canPlus = CharacterCreationEngine.canIncrease(baseScores.con, remainingPoints),
                            onMinus = {
                                if (CharacterCreationEngine.canDecrease(baseScores.con)) {
                                    baseScores = baseScores.copy(con = baseScores.con - 1)
                                }
                            },
                            onPlus = {
                                if (CharacterCreationEngine.canIncrease(baseScores.con, remainingPoints)) {
                                    baseScores = baseScores.copy(con = baseScores.con + 1)
                                }
                            }
                        )

                        AbilityScoreRow(
                            name = "Intelligence",
                            code = "INT",
                            isPrimary = selectedClass == CharacterClass.WIZARD,
                            baseScore = baseScores.int,
                            racialBonus = racialBonus.int,
                            finalScore = finalInt,
                            canMinus = CharacterCreationEngine.canDecrease(baseScores.int),
                            canPlus = CharacterCreationEngine.canIncrease(baseScores.int, remainingPoints),
                            onMinus = {
                                if (CharacterCreationEngine.canDecrease(baseScores.int)) {
                                    baseScores = baseScores.copy(int = baseScores.int - 1)
                                }
                            },
                            onPlus = {
                                if (CharacterCreationEngine.canIncrease(baseScores.int, remainingPoints)) {
                                    baseScores = baseScores.copy(int = baseScores.int + 1)
                                }
                            }
                        )

                        AbilityScoreRow(
                            name = "Wisdom",
                            code = "WIS",
                            isPrimary = selectedClass == CharacterClass.CLERIC || selectedClass == CharacterClass.DRUID || selectedClass == CharacterClass.MONK || selectedClass == CharacterClass.RANGER,
                            baseScore = baseScores.wis,
                            racialBonus = racialBonus.wis,
                            finalScore = finalWis,
                            canMinus = CharacterCreationEngine.canDecrease(baseScores.wis),
                            canPlus = CharacterCreationEngine.canIncrease(baseScores.wis, remainingPoints),
                            onMinus = {
                                if (CharacterCreationEngine.canDecrease(baseScores.wis)) {
                                    baseScores = baseScores.copy(wis = baseScores.wis - 1)
                                }
                            },
                            onPlus = {
                                if (CharacterCreationEngine.canIncrease(baseScores.wis, remainingPoints)) {
                                    baseScores = baseScores.copy(wis = baseScores.wis + 1)
                                }
                            }
                        )

                        AbilityScoreRow(
                            name = "Charisma",
                            code = "CHA",
                            isPrimary = selectedClass == CharacterClass.BARD || selectedClass == CharacterClass.PALADIN || selectedClass == CharacterClass.SORCERER || selectedClass == CharacterClass.WARLOCK,
                            baseScore = baseScores.cha,
                            racialBonus = racialBonus.cha,
                            finalScore = finalCha,
                            canMinus = CharacterCreationEngine.canDecrease(baseScores.cha),
                            canPlus = CharacterCreationEngine.canIncrease(baseScores.cha, remainingPoints),
                            onMinus = {
                                if (CharacterCreationEngine.canDecrease(baseScores.cha)) {
                                    baseScores = baseScores.copy(cha = baseScores.cha - 1)
                                }
                            },
                            onPlus = {
                                if (CharacterCreationEngine.canIncrease(baseScores.cha, remainingPoints)) {
                                    baseScores = baseScores.copy(cha = baseScores.cha + 1)
                                }
                            }
                        )
                    }
                }
            }

            // Summary Stats Card (HP, AC, Saving Throws)
            item {
                Surface(
                    color = DarkSurface,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, GoldMuted),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("HERO COMBAT OVERVIEW", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("MAX HIT POINTS", fontSize = 10.sp, color = TextMuted)
                                Text("$maxHp HP", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = TextParchment)
                                Text("d${selectedClass.hitDie} + CON (${if (conMod >= 0) "+$conMod" else "$conMod"})", fontSize = 10.sp, color = GoldMuted)
                            }
                            Column {
                                Text("SAVING THROWS", fontSize = 10.sp, color = TextMuted)
                                Text(
                                    CharacterCreationEngine.getClassSavingThrows(selectedClass).joinToString(", ") { it.name },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextParchment
                                )
                                Text("Proficiency: +2", fontSize = 10.sp, color = GoldMuted)
                            }
                            Column {
                                Text("PRIMARY STAT", fontSize = 10.sp, color = TextMuted)
                                Text(
                                    CharacterCreationEngine.getPrimaryAbility(selectedClass),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldAccent
                                )
                                Text("Core Modifier", fontSize = 10.sp, color = TextMuted)
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

@Composable
private fun AbilityScoreRow(
    name: String,
    code: String,
    isPrimary: Boolean,
    baseScore: Int,
    racialBonus: Int,
    finalScore: Int,
    canMinus: Boolean,
    canPlus: Boolean,
    onMinus: () -> Unit,
    onPlus: () -> Unit
) {
    val mod = calculateAbilityModifier(finalScore)
    val modString = if (mod >= 0) "+$mod" else "$mod"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Label & Primary tag
        Column(modifier = Modifier.width(90.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = code,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = if (isPrimary) GoldAccent else TextParchment
                )
                if (isPrimary) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("★", fontSize = 11.sp, color = GoldAccent)
                }
            }
            Text(
                text = name,
                fontSize = 10.sp,
                color = TextMuted
            )
        }

        // Stepper Controls [-] Base [+]
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            FilledIconButton(
                onClick = onMinus,
                enabled = canMinus,
                modifier = Modifier.size(28.dp),
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = DarkBorder,
                    disabledContainerColor = DarkBorder.copy(alpha = 0.3f)
                )
            ) {
                Text("-", color = if (canMinus) TextParchment else TextMuted, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            Text(
                text = "$baseScore",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextParchment,
                modifier = Modifier.width(22.dp),
                fontFamily = FontFamily.Monospace
            )

            FilledIconButton(
                onClick = onPlus,
                enabled = canPlus,
                modifier = Modifier.size(28.dp),
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = GoldAccent.copy(alpha = 0.3f),
                    disabledContainerColor = DarkBorder.copy(alpha = 0.3f)
                )
            ) {
                Text("+", color = if (canPlus) GoldAccent else TextMuted, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Racial Bonus Pill
        Surface(
            color = if (racialBonus > 0) GoldAccent.copy(alpha = 0.15f) else DarkBorder.copy(alpha = 0.2f),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.width(36.dp)
        ) {
            Text(
                text = if (racialBonus > 0) "+$racialBonus" else "0",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (racialBonus > 0) GoldAccent else TextMuted,
                modifier = Modifier.padding(vertical = 2.dp),
                fontFamily = FontFamily.Monospace
            )
        }

        // Final Score & Modifier Badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "$finalScore",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextParchment
            )
            Surface(
                color = GoldAccent,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.width(32.dp)
            ) {
                Text(
                    text = modString,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDark,
                    modifier = Modifier.padding(vertical = 2.dp),
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
