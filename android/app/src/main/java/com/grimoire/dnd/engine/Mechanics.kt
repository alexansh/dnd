package com.grimoire.dnd.engine

import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

enum class Ability(val label: String) {
    STR("Strength"),
    DEX("Dexterity"),
    CON("Constitution"),
    INT("Intelligence"),
    WIS("Wisdom"),
    CHA("Charisma")
}

enum class Skill(val label: String, val ability: Ability) {
    ATHLETICS("Athletics", Ability.STR),
    ACROBATICS("Acrobatics", Ability.DEX),
    SLEIGHT_OF_HAND("Sleight of Hand", Ability.DEX),
    STEALTH("Stealth", Ability.DEX),
    ARCANA("Arcana", Ability.INT),
    HISTORY("History", Ability.INT),
    INVESTIGATION("Investigation", Ability.INT),
    NATURE("Nature", Ability.INT),
    RELIGION("Religion", Ability.INT),
    ANIMAL_HANDLING("Animal Handling", Ability.WIS),
    INSIGHT("Insight", Ability.WIS),
    MEDICINE("Medicine", Ability.WIS),
    PERCEPTION("Perception", Ability.WIS),
    SURVIVAL("Survival", Ability.WIS),
    DECEPTION("Deception", Ability.CHA),
    INTIMIDATION("Intimidation", Ability.CHA),
    PERFORMANCE("Performance", Ability.CHA),
    PERSUASION("Persuasion", Ability.CHA)
}

enum class ExhaustionLevel(val tier: Int) {
    NONE(0),
    LEVEL_1(1), // Disadvantage on ability checks
    LEVEL_2(2), // Speed halved
    LEVEL_3(3), // Disadvantage on attacks/saves
    LEVEL_4(4), // HP max halved
    LEVEL_5(5), // Speed 0
    LEVEL_6(6)  // Death
}

fun calculateAbilityModifier(score: Int): Int {
    return floor((score - 10) / 2.0).toInt()
}

fun calculateProficiencyBonus(level: Int): Int {
    require(level >= 1) { "Level must be at least 1." }
    return ceil(1 + (level / 4.0)).toInt()
}

fun calculateSpellSaveDc(proficiencyBonus: Int, castingMod: Int): Int {
    return 8 + proficiencyBonus + castingMod
}

fun calculateSpellAttackModifier(proficiencyBonus: Int, castingMod: Int): Int {
    return proficiencyBonus + castingMod
}

fun calculatePassiveScore(abilityMod: Int, isProficient: Boolean, proficiencyBonus: Int): Int {
    val prof = if (isProficient) proficiencyBonus else 0
    return 10 + abilityMod + prof
}

fun calculateConcentrationDc(damageTaken: Int): Int {
    return max(10, damageTaken / 2)
}

data class DeathSaveTracker(
    var successes: Int = 0,
    var failures: Int = 0,
    var isStabilized: Boolean = false,
    var isDead: Boolean = false,
    var hpRegained: Int = 0
) {
    fun recordRoll(rollValue: Int): Pair<Boolean, String> {
        if (isDead) return true to "Character is dead."
        if (isStabilized) return true to "Character is stabilized."

        if (rollValue == 20) {
            hpRegained = 1
            isStabilized = true
            successes = 0
            failures = 0
            return true to "Natural 20! You regain 1 HP and regain consciousness!"
        }

        if (rollValue == 1) {
            failures += 2
            if (failures >= 3) {
                isDead = true
                return true to "Natural 1! Two failures suffered. You have perished."
            }
            return false to "Natural 1! ($failures/3 failures)."
        }

        if (rollValue >= 10) {
            successes++
            if (successes >= 3) {
                isStabilized = true
                return true to "3 Successes! You are stabilized at 0 HP."
            }
            return false to "Success ($successes/3)."
        } else {
            failures++
            if (failures >= 3) {
                isDead = true
                return true to "3 Failures! You have perished."
            }
            return false to "Failure ($failures/3)."
        }
    }

    fun takeDamageAtZeroHp(isCritical: Boolean = false) {
        val added = if (isCritical) 2 else 1
        failures += added
        isStabilized = false
        if (failures >= 3) {
            isDead = true
        }
    }
}
