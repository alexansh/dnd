package com.grimoire.dnd.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grimoire.dnd.R
import com.grimoire.dnd.engine.Character
import com.grimoire.dnd.engine.DiceEngine
import com.grimoire.dnd.engine.LevelUpResult
import com.grimoire.dnd.ui.theme.*
import com.grimoire.dnd.ui.triggerHapticFeedback

@Composable
fun LevelUpDialog(
    character: Character,
    diceEngine: DiceEngine,
    onDismiss: () -> Unit,
    onLevelUpConfirmed: (LevelUpResult) -> Unit
) {
    val context = LocalContext.current
    val targetLevel = character.level + 1
    var selectedSubclass by remember {
        mutableStateOf(character.subclass ?: character.characterClass.primarySubclass)
    }

    val availableSubclasses = remember(character.characterClass) {
        listOf(
            character.characterClass.primarySubclass,
            when (character.characterClass.label) {
                "Fighter" -> "Battle Master"
                "Rogue" -> "Assassin"
                "Wizard" -> "School of Abjuration"
                "Cleric" -> "War Domain"
                "Paladin" -> "Oath of Vengeance"
                "Barbarian" -> "Totem Warrior"
                else -> "Way of the Shadow"
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkCardSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_dragon),
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        "ASCENSION AWAITS",
                        color = GoldAccent,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        "Level ${character.level} ➔ Level $targetLevel",
                        color = TextParchment,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Progression Highlights
                Surface(
                    color = DarkSurface,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "UNLOCKED GAINS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "• Hit Points: +d${character.characterClass.hitDie} + CON mod",
                            fontSize = 13.sp,
                            color = TextParchment
                        )
                        Text(
                            "• Hit Dice: +1 d${character.characterClass.hitDie} recovery die",
                            fontSize = 13.sp,
                            color = TextParchment
                        )
                        Text(
                            "• New Class Traits & Spell Capacities",
                            fontSize = 13.sp,
                            color = GoldAccent
                        )
                    }
                }

                // If Level 3, Archetype specialization selection
                if (targetLevel == 3) {
                    Text(
                        "Select Specialization Archetype:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent
                    )

                    availableSubclasses.forEach { subclassOption ->
                        Surface(
                            onClick = { selectedSubclass = subclassOption },
                            color = if (selectedSubclass == subclassOption) GoldAccent.copy(alpha = 0.2f) else DarkSurface,
                            border = BorderStroke(1.dp, if (selectedSubclass == subclassOption) GoldAccent else DarkBorder),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedSubclass == subclassOption,
                                    onClick = { selectedSubclass = subclassOption },
                                    colors = RadioButtonDefaults.colors(selectedColor = GoldAccent)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = subclassOption,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedSubclass == subclassOption) GoldAccent else TextParchment
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    triggerHapticFeedback(context, isCritical = true)
                    val result = character.levelUp(diceEngine, selectedSubclass)
                    onLevelUpConfirmed(result)
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "ASCEND TO LEVEL $targetLevel",
                    color = TextDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Later", color = TextMuted)
            }
        }
    )
}
