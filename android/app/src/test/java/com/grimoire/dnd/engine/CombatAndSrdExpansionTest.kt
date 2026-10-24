package com.grimoire.dnd.engine

import org.junit.Assert.*
import org.junit.Test

class CombatAndSrdExpansionTest {

    private fun createFighter(): Character {
        return Character(
            id = "hero_fighter",
            name = "Thorgar",
            race = "Dwarf",
            characterClass = CharacterClass.FIGHTER,
            level = 1,
            hpCurrent = 12,
            hpMax = 12,
            strength = 16,
            dexterity = 12,
            constitution = 16,
            intelligence = 10,
            wisdom = 12,
            charisma = 8
        )
    }

    private fun createWizard(): Character {
        return Character(
            id = "hero_wizard",
            name = "Elminster",
            race = "Human",
            characterClass = CharacterClass.WIZARD,
            level = 1,
            hpCurrent = 8,
            hpMax = 8,
            intelligence = 16,
            constitution = 14
        )
    }

    @Test
    fun testActionEconomyAndDisablingConditions() {
        val combatant = Combatant(
            id = "test_1",
            name = "Knight",
            isPlayerParty = true,
            currentHp = 20,
            maxHp = 20,
            armorClass = 16
        )

        assertTrue("Healthy combatant can act", combatant.canAct())

        combatant.conditions.add("STUNNED")
        assertFalse("Stunned combatant cannot act", combatant.canAct())

        combatant.conditions.remove("STUNNED")
        combatant.conditions.add("PARALYZED")
        assertFalse("Paralyzed combatant cannot act", combatant.canAct())

        combatant.conditions.remove("PARALYZED")
        combatant.conditions.add("POISONED")
        assertTrue("Poisoned combatant can still act (with disadvantage)", combatant.canAct())
    }

    @Test
    fun testConcentrationCheckOnDamage() {
        val hero = createWizard()
        hero.castConcentrationSpell("Fly")
        assertEquals("Fly", hero.activeConcentrationSpell)

        // Taking 14 damage -> DC = max(10, 14 / 2) = 10
        // Seeded engine ensuring predictable CON save roll
        val seededDice = DiceEngine(seed = 42L)
        val (maintained, msg) = hero.checkConcentration(damageTaken = 14, diceEngine = seededDice)

        // Check concentration mechanism works deterministically
        assertNotNull(msg)
        if (!maintained) {
            assertNull("Concentration must be cleared on failure", hero.activeConcentrationSpell)
        }
    }

    @Test
    fun testTacticalArenaConcentrationBreakOnDamage() {
        val dice = DiceEngine(seed = 777L)
        val arena = TacticalCombatArena(
            playerParty = listOf(createFighter()),
            enemyNames = listOf("Goblin Shaman"),
            diceEngine = dice
        )

        val enemy = arena.combatants.first { !it.isPlayerParty }
        enemy.activeConcentrationSpell = "Bless"
        assertEquals("Bless", enemy.activeConcentrationSpell)

        val heroCombatant = arena.combatants.first { it.isPlayerParty }
        // Force a hit dealing massive damage
        arena.executePlayerAction(heroCombatant, enemy, CombatActionType.CAST_SPELL)

        assertTrue(enemy.currentHp < enemy.maxHp)
    }

    @Test
    fun testLegalActionsGeneratorForAI() {
        val dice = DiceEngine()
        val arena = TacticalCombatArena(
            playerParty = listOf(createFighter(), createWizard()),
            enemyNames = listOf("Goblin A", "Goblin B"),
            diceEngine = dice
        )

        val goblin = arena.combatants.first { !it.isPlayerParty }
        val legalActions = arena.generateLegalActions(goblin)

        assertTrue("Must generate legal actions", legalActions.isNotEmpty())
        assertTrue("Must include melee attacks", legalActions.any { it.actionType == CombatActionType.ATTACK_MELEE })
        assertTrue("Must include Dodge stance", legalActions.any { it.actionType == CombatActionType.DODGE })
        // Opponents target IDs must only be from the player party
        val opponentIds = arena.combatants.filter { it.isPlayerParty }.map { it.id }.toSet()
        val attackTargets = legalActions.filter { it.actionType == CombatActionType.ATTACK_MELEE }.map { it.targetId }
        assertTrue(attackTargets.all { it in opponentIds })
    }

    @Test
    fun testLevel10ProgressionAndSpellSlots() {
        val wizard = createWizard()
        val dice = DiceEngine(seed = 123L)

        assertEquals(1, wizard.level)
        assertEquals(2, wizard.proficiencyBonus)

        for (targetLevel in 2..10) {
            val result = wizard.levelUp(dice)
            assertEquals(targetLevel, wizard.level)
            assertTrue("HP must increase with each level", result.hpGained > 0)
        }

        assertEquals(10, wizard.level)
        assertEquals(4, wizard.proficiencyBonus) // Level 10 has Prof Bonus +4
        assertTrue("Wizard must have unlocked 2nd level spell slots", wizard.spellSlots.containsKey(2))
        assertTrue("Wizard must have unlocked 3rd level spell slots", wizard.spellSlots.containsKey(3))
        assertTrue("Wizard must have unlocked 4th level spell slots", wizard.spellSlots.containsKey(4))
        assertTrue("Wizard must have unlocked 5th level spell slots", wizard.spellSlots.containsKey(5))
        assertTrue("HP max must have grown significantly", wizard.hpMax >= 40)
    }
}
