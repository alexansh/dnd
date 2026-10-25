package com.grimoire.dnd.engine

import org.junit.Assert.*
import org.junit.Test

class CharacterCreationEngineTest {

    @Test
    fun testAllClassesHaveValidStandardArrayCosting27Points() {
        CharacterClass.values().forEach { c ->
            val array = CharacterCreationEngine.getRecommendedBaseArray(c)
            val cost = CharacterCreationEngine.calculateTotalCost(array)
            assertEquals("Standard array for ${c.label} must cost 27 point-buy points", 27, cost)
            assertEquals("Remaining points for ${c.label} should be 0 by default", 0, CharacterCreationEngine.getRemainingPoints(array))
        }
    }

    @Test
    fun testClassDifferencesInBaseStats() {
        val barbarian = CharacterCreationEngine.getRecommendedBaseArray(CharacterClass.BARBARIAN)
        val wizard = CharacterCreationEngine.getRecommendedBaseArray(CharacterClass.WIZARD)
        val rogue = CharacterCreationEngine.getRecommendedBaseArray(CharacterClass.ROGUE)
        val cleric = CharacterCreationEngine.getRecommendedBaseArray(CharacterClass.CLERIC)

        assertEquals("Barbarian primary stat should be STR 15", 15, barbarian.str)
        assertEquals("Wizard primary stat should be INT 15", 15, wizard.int)
        assertEquals("Rogue primary stat should be DEX 15", 15, rogue.dex)
        assertEquals("Cleric primary stat should be WIS 15", 15, cleric.wis)

        assertNotEquals("Barbarian and Wizard should not have identical stats", barbarian, wizard)
        assertNotEquals("Rogue and Cleric should not have identical stats", rogue, cleric)
    }

    @Test
    fun testRacialBonuses() {
        val dwarf = CharacterCreationEngine.getRacialBonus("Mountain Dwarf")
        assertEquals(2, dwarf.str)
        assertEquals(2, dwarf.con)

        val elf = CharacterCreationEngine.getRacialBonus("High Elf")
        assertEquals(2, elf.dex)
        assertEquals(1, elf.int)

        val human = CharacterCreationEngine.getRacialBonus("Human")
        assertEquals(1, human.str)
        assertEquals(1, human.dex)
        assertEquals(1, human.con)
        assertEquals(1, human.int)
        assertEquals(1, human.wis)
        assertEquals(1, human.cha)
    }

    @Test
    fun testPointBuyAdjustments() {
        var array = CharacterCreationEngine.BaseAbilityArray(str = 10, dex = 10, con = 10, int = 10, wis = 10, cha = 10)
        // Base cost for all 10s: 6 * 2 = 12 points.
        assertEquals(12, CharacterCreationEngine.calculateTotalCost(array))
        assertEquals(15, CharacterCreationEngine.getRemainingPoints(array))

        // Can decrease 10 to 9
        assertTrue(CharacterCreationEngine.canDecrease(10))
        assertFalse(CharacterCreationEngine.canDecrease(8))

        // Can increase with points
        assertTrue(CharacterCreationEngine.canIncrease(10, 15))
        assertFalse(CharacterCreationEngine.canIncrease(15, 15)) // Cap is 15 before racial bonus
    }

    @Test
    fun testPopulateStartingGear() {
        val wizard = Character(
            id = "w1",
            name = "Mage",
            race = "High Elf",
            characterClass = CharacterClass.WIZARD
        )
        CharacterCreationEngine.populateStartingGearAndSpells(wizard)
        assertTrue("Wizard must have spellbook", wizard.inventory.any { it.id == "spellbook" })
        assertTrue("Wizard must have level 1 spell slots", wizard.spellSlots.containsKey(1))

        val fighter = Character(
            id = "f1",
            name = "Warrior",
            race = "Mountain Dwarf",
            characterClass = CharacterClass.FIGHTER
        )
        CharacterCreationEngine.populateStartingGearAndSpells(fighter)
        assertTrue("Fighter must have chain mail", wizard.inventory.none { it.id == "chain_mail" } && fighter.inventory.any { it.id == "chain_mail" })
    }
}
