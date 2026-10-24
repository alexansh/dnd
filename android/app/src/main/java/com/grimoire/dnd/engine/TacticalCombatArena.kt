package com.grimoire.dnd.engine

enum class CombatZone(val label: String, val rangeFeet: Int, val providesHalfCover: Boolean) {
    MELEE_FRONTLINE("Frontline (Melee 5ft)", 5, false),
    MID_FLANK("Mid-Flank (15–30ft)", 30, true),
    HIGH_GROUND("High Ground Ledge (60ft)", 60, true)
}

enum class CombatActionType {
    ATTACK_MELEE,
    ATTACK_RANGED,
    CAST_SPELL,
    DASH,
    DISENGAGE,
    DODGE,
    TAKE_COVER,
    USE_POTION
}

data class LegalCombatAction(
    val actionType: CombatActionType,
    val targetId: String,
    val description: String
)

data class Combatant(
    val id: String,
    val name: String,
    val isPlayerParty: Boolean,
    var currentHp: Int,
    val maxHp: Int,
    val armorClass: Int,
    var currentZone: CombatZone = CombatZone.MELEE_FRONTLINE,
    val speedFeet: Int = 30,
    var movementRemainingFeet: Int = 30,
    val attackBonus: Int = 4,
    val damageFormula: String = "1d6+2",
    val isSpellcaster: Boolean = false,
    val initiativeBonus: Int = 2,
    val conModifier: Int = 2,
    var initiativeRoll: Int = 0,
    var isDodgeActive: Boolean = false,
    var hasTakenAction: Boolean = false,
    var hasTakenBonusAction: Boolean = false,
    var hasTakenReaction: Boolean = false,
    var isDowned: Boolean = false,
    val conditions: MutableSet<String> = mutableSetOf(),
    var activeConcentrationSpell: String? = null
) {
    val effectiveAc: Int
        get() = if (currentZone.providesHalfCover) armorClass + 2 else armorClass

    fun canAct(): Boolean {
        if (isDowned) return false
        val disablingConditions = setOf("INCAPACITATED", "STUNNED", "PARALYZED", "UNCONSCIOUS", "PETRIFIED")
        return conditions.none { it.uppercase() in disablingConditions }
    }
}

data class CombatLogEntry(
    val round: Int,
    val actorName: String,
    val actionText: String,
    val isCrit: Boolean = false,
    val damageDealt: Int = 0
)

class TacticalCombatArena(
    val playerParty: List<Character>,
    val enemyNames: List<String>,
    private val diceEngine: DiceEngine
) {
    var roundNumber: Int = 1
        private set
    var currentTurnIndex: Int = 0
        private set
    val combatants: MutableList<Combatant> = mutableListOf()
    val combatLog: MutableList<CombatLogEntry> = mutableListOf()
    var isCombatResolved: Boolean = false
        private set
    var isVictory: Boolean = false
        private set

    init {
        // 1. Initialize Player Party Combatants
        playerParty.forEachIndexed { idx, hero ->
            val isRanged = hero.characterClass == CharacterClass.ROGUE || hero.characterClass == CharacterClass.WIZARD
            combatants.add(
                Combatant(
                    id = "hero_${hero.id}",
                    name = hero.name,
                    isPlayerParty = true,
                    currentHp = hero.hpCurrent,
                    maxHp = hero.hpMax,
                    armorClass = hero.armorClass,
                    currentZone = if (isRanged) CombatZone.MID_FLANK else CombatZone.MELEE_FRONTLINE,
                    speedFeet = hero.speedFeet,
                    attackBonus = hero.getAbilityModifier(Ability.STR) + hero.proficiencyBonus,
                    damageFormula = hero.inventory.firstOrNull { it.isEquipped && it.damageFormula != null }?.damageFormula ?: "1d8+3",
                    isSpellcaster = hero.characterClass == CharacterClass.WIZARD || hero.characterClass == CharacterClass.CLERIC,
                    initiativeBonus = hero.getAbilityModifier(Ability.DEX)
                )
            )
        }

        // 2. Initialize Enemies
        enemyNames.forEachIndexed { idx, enName ->
            val isBoss = enName.contains("King", ignoreCase = true) || enName.contains("Lord", ignoreCase = true)
            val hp = if (isBoss) 45 else 13
            val ac = if (isBoss) 16 else 13
            val dmg = if (isBoss) "1d8+3" else "1d6+2"

            combatants.add(
                Combatant(
                    id = "enemy_${idx}_${enName.hashCode()}",
                    name = enName,
                    isPlayerParty = false,
                    currentHp = hp,
                    maxHp = hp,
                    armorClass = ac,
                    currentZone = if (isBoss) CombatZone.MELEE_FRONTLINE else CombatZone.MID_FLANK,
                    speedFeet = 30,
                    attackBonus = if (isBoss) 5 else 4,
                    damageFormula = dmg,
                    initiativeBonus = 2
                )
            )
        }

        // 3. Roll Initiatives
        rollInitiativeOrder()
    }

    private fun rollInitiativeOrder() {
        combatants.forEach { c ->
            val roll = diceEngine.rollD20(c.initiativeBonus)
            c.initiativeRoll = roll.total
        }
        combatants.sortByDescending { it.initiativeRoll }
    }

    fun getCurrentCombatant(): Combatant? {
        val active = combatants.filter { !it.isDowned }
        if (active.isEmpty()) return null
        return active[currentTurnIndex % active.size]
    }

    fun generateLegalActions(actor: Combatant): List<LegalCombatAction> {
        if (!actor.canAct() || actor.hasTakenAction) return emptyList()

        val legal = mutableListOf<LegalCombatAction>()
        val opponents = combatants.filter { it.isPlayerParty != actor.isPlayerParty && !it.isDowned }

        for (target in opponents) {
            legal.add(LegalCombatAction(CombatActionType.ATTACK_MELEE, target.id, "Melee Attack against ${target.name}"))
            legal.add(LegalCombatAction(CombatActionType.ATTACK_RANGED, target.id, "Ranged Attack against ${target.name}"))
            if (actor.isSpellcaster) {
                legal.add(LegalCombatAction(CombatActionType.CAST_SPELL, target.id, "Cast Offensive Spell at ${target.name}"))
            }
        }

        legal.add(LegalCombatAction(CombatActionType.DODGE, actor.id, "Adopt Dodge Stance (impose disadvantage on attackers)"))
        legal.add(LegalCombatAction(CombatActionType.DISENGAGE, actor.id, "Disengage and reposition"))
        legal.add(LegalCombatAction(CombatActionType.USE_POTION, actor.id, "Drink Potion of Healing"))

        return legal
    }

    fun executePlayerAction(
        actor: Combatant,
        target: Combatant,
        actionType: CombatActionType
    ): String {
        if (!actor.canAct()) return "${actor.name} cannot act due to disabling condition (${actor.conditions.joinToString()})."
        if (actor.hasTakenAction) return "${actor.name} has already taken an action this turn."

        var outcomeMsg = ""

        when (actionType) {
            CombatActionType.ATTACK_MELEE, CombatActionType.ATTACK_RANGED -> {
                val hasAdvantage = target.conditions.contains("PRONE") || target.conditions.contains("PARALYZED") || target.conditions.contains("RESTRAINED") || target.conditions.contains("BLINDED")
                val hasDisadvantage = target.isDodgeActive || actor.conditions.contains("POISONED")

                val rollType = when {
                    hasAdvantage && !hasDisadvantage -> RollType.ADVANTAGE
                    hasDisadvantage && !hasAdvantage -> RollType.DISADVANTAGE
                    else -> RollType.NORMAL
                }

                val attackRoll = diceEngine.rollD20(actor.attackBonus, rollType)
                val targetAc = target.effectiveAc

                if (attackRoll.total >= targetAc || attackRoll.isCriticalHit) {
                    val dmgRoll = diceEngine.parseAndRoll(actor.damageFormula, attackRoll.isCriticalHit)
                    target.currentHp = maxOf(0, target.currentHp - dmgRoll.total)
                    if (target.currentHp == 0) {
                        target.isDowned = true
                    }
                    val critText = if (attackRoll.isCriticalHit) " [CRITICAL HIT!]" else ""
                    outcomeMsg = "${actor.name} strikes ${target.name} for ${dmgRoll.total} damage!$critText (Roll: ${attackRoll.total} vs AC ${targetAc})"
                    combatLog.add(CombatLogEntry(roundNumber, actor.name, outcomeMsg, attackRoll.isCriticalHit, dmgRoll.total))

                    // Concentration Check on Target
                    checkTargetConcentration(target, dmgRoll.total)
                } else {
                    outcomeMsg = "${actor.name} swings at ${target.name} and MISSES! (Roll: ${attackRoll.total} vs AC ${targetAc})"
                    combatLog.add(CombatLogEntry(roundNumber, actor.name, outcomeMsg))
                }
                actor.hasTakenAction = true
            }

            CombatActionType.CAST_SPELL -> {
                val spellDmg = diceEngine.parseAndRoll("2d8+3", false)
                target.currentHp = maxOf(0, target.currentHp - spellDmg.total)
                if (target.currentHp == 0) {
                    target.isDowned = true
                }
                outcomeMsg = "${actor.name} hurls a burst of Radiant Fire at ${target.name} dealing ${spellDmg.total} damage!"
                combatLog.add(CombatLogEntry(roundNumber, actor.name, outcomeMsg, false, spellDmg.total))
                checkTargetConcentration(target, spellDmg.total)
                actor.hasTakenAction = true
            }

            CombatActionType.USE_POTION -> {
                val healRoll = diceEngine.parseAndRoll("2d4+2", false)
                actor.currentHp = minOf(actor.maxHp, actor.currentHp + healRoll.total)
                outcomeMsg = "${actor.name} drinks a Potion of Healing, recovering ${healRoll.total} HP!"
                combatLog.add(CombatLogEntry(roundNumber, actor.name, outcomeMsg))
                actor.hasTakenAction = true
            }

            CombatActionType.DODGE -> {
                actor.isDodgeActive = true
                outcomeMsg = "${actor.name} adopts a Defensive Dodge posture (Attacks against them suffer Disadvantage)!"
                combatLog.add(CombatLogEntry(roundNumber, actor.name, outcomeMsg))
                actor.hasTakenAction = true
            }

            CombatActionType.DISENGAGE, CombatActionType.DASH, CombatActionType.TAKE_COVER -> {
                outcomeMsg = "${actor.name} takes tactical cover!"
                actor.hasTakenAction = true
            }
        }

        checkVictoryConditions()
        return outcomeMsg
    }

    private fun checkTargetConcentration(target: Combatant, damageTaken: Int) {
        if (target.activeConcentrationSpell != null && target.currentHp > 0) {
            val dc = maxOf(10, damageTaken / 2)
            val conSave = diceEngine.rollD20(target.conModifier)
            if (conSave.total < dc) {
                val brokenSpell = target.activeConcentrationSpell
                target.activeConcentrationSpell = null
                combatLog.add(CombatLogEntry(roundNumber, target.name, "Concentration on $brokenSpell BROKEN (Save: ${conSave.total} vs DC $dc)!"))
            } else {
                combatLog.add(CombatLogEntry(roundNumber, target.name, "Concentration maintained on ${target.activeConcentrationSpell} (Save: ${conSave.total} vs DC $dc)."))
            }
        }
    }

    fun executeAITurn(aiActor: Combatant, chosenAction: LegalCombatAction? = null): String {
        if (!aiActor.canAct()) return "${aiActor.name} cannot act due to disabling condition (${aiActor.conditions.joinToString()})."

        val livingTargets = combatants.filter { it.isPlayerParty && !it.isDowned }
        if (livingTargets.isEmpty()) {
            checkVictoryConditions()
            return "No player targets remain."
        }

        val target = if (chosenAction != null) {
            combatants.firstOrNull { it.id == chosenAction.targetId } ?: livingTargets.first()
        } else {
            // Tactical Heuristic: Focus lowest HP target
            livingTargets.minByOrNull { it.currentHp } ?: livingTargets.first()
        }

        val hasAdvantage = target.conditions.contains("PRONE") || target.conditions.contains("PARALYZED") || target.conditions.contains("RESTRAINED")
        val hasDisadvantage = target.isDodgeActive || aiActor.conditions.contains("POISONED")

        val rollType = when {
            hasAdvantage && !hasDisadvantage -> RollType.ADVANTAGE
            hasDisadvantage && !hasAdvantage -> RollType.DISADVANTAGE
            else -> RollType.NORMAL
        }

        val attack = diceEngine.rollD20(aiActor.attackBonus, rollType)
        val targetAc = target.effectiveAc

        val msg = if (attack.total >= targetAc || attack.isCriticalHit) {
            val dmg = diceEngine.parseAndRoll(aiActor.damageFormula, attack.isCriticalHit)
            target.currentHp = maxOf(0, target.currentHp - dmg.total)
            if (target.currentHp == 0) target.isDowned = true
            val crit = if (attack.isCriticalHit) " [CRIT!]" else ""
            checkTargetConcentration(target, dmg.total)
            "${aiActor.name} attacks ${target.name} for ${dmg.total} damage!$crit"
        } else {
            "${aiActor.name} attacks ${target.name} and MISSES!"
        }

        combatLog.add(CombatLogEntry(roundNumber, aiActor.name, msg))
        aiActor.hasTakenAction = true
        checkVictoryConditions()
        return msg
    }

    fun nextTurn() {
        val active = combatants.filter { !it.isDowned }
        if (active.isEmpty()) return

        currentTurnIndex++
        if (currentTurnIndex >= active.size) {
            currentTurnIndex = 0
            roundNumber++
            // Reset per-round action flags
            combatants.forEach {
                it.hasTakenAction = false
                it.hasTakenBonusAction = false
                it.isDodgeActive = false
            }
        }
    }

    private fun checkVictoryConditions() {
        val livingEnemies = combatants.filter { !it.isPlayerParty && !it.isDowned }
        val livingHeroes = combatants.filter { it.isPlayerParty && !it.isDowned }

        if (livingEnemies.isEmpty()) {
            isCombatResolved = true
            isVictory = true
        } else if (livingHeroes.isEmpty()) {
            isCombatResolved = true
            isVictory = false
        }
    }
}
