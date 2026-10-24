package com.grimoire.dnd.engine

import kotlin.random.Random

enum class RollType(val label: String) {
    NORMAL("Normal"),
    ADVANTAGE("Advantage"),
    DISADVANTAGE("Disadvantage")
}

data class DieResult(
    val dieType: Int,
    val value: Int,
    var isDiscarded: Boolean = false
)

data class RollOutcome(
    val total: Int,
    val diceResults: List<DieResult>,
    val modifier: Int = 0,
    val rollType: RollType = RollType.NORMAL,
    val isCriticalHit: Boolean = false,
    val isCriticalMiss: Boolean = false,
    val formula: String = ""
) {
    fun summary(): String {
        val kept = diceResults.filter { !it.isDiscarded }.map { it.value.toString() }
        val discarded = diceResults.filter { it.isDiscarded }.map { "(${it.value} discarded)" }
        val diceStr = (kept + discarded).joinToString(", ")
        val modStr = when {
            modifier > 0 -> " + $modifier"
            modifier < 0 -> " - ${kotlin.math.abs(modifier)}"
            else -> ""
        }
        val critStr = when {
            isCriticalHit -> " [NATURAL 20 CRITICAL!]"
            isCriticalMiss -> " [NATURAL 1 FAIL!]"
            else -> ""
        }
        return "[$diceStr]$modStr = $total$critStr"
    }
}

class DiceEngine(
    private var rng: Random = Random.Default,
    var initialSeed: Long? = null
) {
    constructor(seed: Long) : this(Random(seed), seed)

    fun reseed(seed: Long) {
        this.rng = Random(seed)
        this.initialSeed = seed
    }

    fun rollDie(sides: Int): Int {
        require(sides >= 1) { "Die must have at least 1 side." }
        return rng.nextInt(1, sides + 1)
    }

    fun rollD20(modifier: Int = 0, rollType: RollType = RollType.NORMAL): RollOutcome {
        val r1 = rollDie(20)

        if (rollType == RollType.NORMAL) {
            val isCrit = (r1 == 20)
            val isFumble = (r1 == 1)
            return RollOutcome(
                total = r1 + modifier,
                diceResults = listOf(DieResult(20, r1)),
                modifier = modifier,
                rollType = rollType,
                isCriticalHit = isCrit,
                isCriticalMiss = isFumble,
                formula = "1d20${if (modifier >= 0) "+" else ""}$modifier"
            )
        }

        val r2 = rollDie(20)
        val kept: Int
        val d1: DieResult
        val d2: DieResult

        if (rollType == RollType.ADVANTAGE) {
            kept = maxOf(r1, r2)
            d1 = DieResult(20, r1, isDiscarded = (r1 < r2))
            d2 = DieResult(20, r2, isDiscarded = (r2 < r1 || (r1 == r2 && false)))
            if (r1 == r2) d2.isDiscarded = true
        } else {
            kept = minOf(r1, r2)
            d1 = DieResult(20, r1, isDiscarded = (r1 > r2))
            d2 = DieResult(20, r2, isDiscarded = (r2 > r1))
            if (r1 == r2) d2.isDiscarded = true
        }

        val isCrit = (kept == 20)
        val isFumble = (kept == 1)

        return RollOutcome(
            total = kept + modifier,
            diceResults = listOf(d1, d2),
            modifier = modifier,
            rollType = rollType,
            isCriticalHit = isCrit,
            isCriticalMiss = isFumble,
            formula = "1d20 (${rollType.label})${if (modifier >= 0) "+" else ""}$modifier"
        )
    }

    fun rollDamage(diceCount: Int, sides: Int, modifier: Int = 0, isCritical: Boolean = false): RollOutcome {
        val actualCount = if (isCritical) diceCount * 2 else diceCount
        val results = List(actualCount) { DieResult(sides, rollDie(sides)) }
        val diceTotal = results.sumOf { it.value }
        val total = maxOf(0, diceTotal + modifier)

        return RollOutcome(
            total = total,
            diceResults = results,
            modifier = modifier,
            isCriticalHit = isCritical,
            formula = "${actualCount}d$sides${if (modifier >= 0) "+" else ""}$modifier"
        )
    }

    fun parseAndRoll(formula: String, isCritical: Boolean = false): RollOutcome {
        val cleaned = formula.replace(" ", "").lowercase()
        val regex = Regex("([+-]?\\d*d\\d+|[+-]?\\d+)")
        val matches = regex.findAll(cleaned).map { it.value }.toList()

        val results = mutableListOf<DieResult>()
        var staticMod = 0

        for (token in matches) {
            val sign = if (token.startsWith("-")) -1 else 1
            val raw = token.trimStart('+', '-')

            if (raw.contains("d")) {
                val parts = raw.split("d")
                val count = if (parts[0].isEmpty()) 1 else parts[0].toInt()
                val sides = parts[1].toInt()
                val actualCount = if (isCritical) count * 2 else count
                for (i in 0 until actualCount) {
                    val rolled = rollDie(sides) * sign
                    results.add(DieResult(sides, rolled))
                }
            } else {
                staticMod += raw.toInt() * sign
            }
        }

        val total = results.sumOf { it.value } + staticMod
        return RollOutcome(
            total = total,
            diceResults = results,
            modifier = staticMod,
            isCriticalHit = isCritical,
            formula = formula
        )
    }
}
