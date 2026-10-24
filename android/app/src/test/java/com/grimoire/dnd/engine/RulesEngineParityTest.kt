package com.grimoire.dnd.engine

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.junit.Assert.*
import org.junit.BeforeClass
import org.junit.Test
import java.io.File
import java.io.InputStreamReader

class RulesEngineParityTest {

    companion object {
        private lateinit var goldenData: JsonObject

        @BeforeClass
        @JvmStatic
        fun loadGoldenVectors() {
            val resourceStream = RulesEngineParityTest::class.java.classLoader?.getResourceAsStream("golden_vectors_5e.json")
            val reader = if (resourceStream != null) {
                InputStreamReader(resourceStream)
            } else {
                val fallbackFile = File("src/test/resources/golden_vectors_5e.json")
                if (fallbackFile.exists()) {
                    fallbackFile.reader()
                } else {
                    File("../tests/golden_vectors_5e.json").reader()
                }
            }
            goldenData = Gson().fromJson(reader, JsonObject::class.java)
            assertNotNull("Golden vectors must be loaded", goldenData)
        }
    }

    @Test
    fun testAbilityModifiersParity() {
        val array = goldenData.getAsJsonArray("ability_modifiers")
        assertTrue("Ability modifier vectors must not be empty", array.size() > 0)
        for (elem in array) {
            val obj = elem.asJsonObject
            val score = obj.get("score").asInt
            val expectedMod = obj.get("expected_modifier").asInt
            val actualMod = calculateAbilityModifier(score)
            assertEquals("Ability mod mismatch for score $score", expectedMod, actualMod)
        }
    }

    @Test
    fun testProficiencyBonusProgressionParity() {
        val array = goldenData.getAsJsonArray("proficiency_bonuses")
        assertTrue(array.size() == 20)
        for (elem in array) {
            val obj = elem.asJsonObject
            val level = obj.get("level").asInt
            val expectedProf = obj.get("expected_proficiency").asInt
            val actualProf = calculateProficiencyBonus(level)
            assertEquals("Proficiency bonus mismatch for level $level", expectedProf, actualProf)
        }
    }

    @Test
    fun testSpellSaveDcParity() {
        val array = goldenData.getAsJsonArray("spell_save_dcs")
        assertTrue(array.size() > 0)
        for (elem in array) {
            val obj = elem.asJsonObject
            val prof = obj.get("proficiency_bonus").asInt
            val mod = obj.get("casting_mod").asInt
            val expected = obj.get("expected_dc").asInt
            val actual = calculateSpellSaveDc(prof, mod)
            assertEquals("Spell save DC mismatch for prof=$prof, mod=$mod", expected, actual)
        }
    }

    @Test
    fun testSpellAttackModParity() {
        val array = goldenData.getAsJsonArray("spell_attack_mods")
        assertTrue(array.size() > 0)
        for (elem in array) {
            val obj = elem.asJsonObject
            val prof = obj.get("proficiency_bonus").asInt
            val mod = obj.get("casting_mod").asInt
            val expected = obj.get("expected_attack_mod").asInt
            val actual = calculateSpellAttackModifier(prof, mod)
            assertEquals("Spell attack mod mismatch for prof=$prof, mod=$mod", expected, actual)
        }
    }

    @Test
    fun testPassiveScoresParity() {
        val array = goldenData.getAsJsonArray("passive_scores")
        assertTrue(array.size() > 0)
        for (elem in array) {
            val obj = elem.asJsonObject
            val mod = obj.get("ability_mod").asInt
            val isProf = obj.get("is_proficient").asBoolean
            val prof = obj.get("proficiency_bonus").asInt
            val expected = obj.get("expected_passive").asInt
            val actual = calculatePassiveScore(mod, isProf, prof)
            assertEquals("Passive score mismatch for mod=$mod, isProf=$isProf, prof=$prof", expected, actual)
        }
    }

    @Test
    fun testConcentrationDcParity() {
        val array = goldenData.getAsJsonArray("concentration_dcs")
        assertTrue(array.size() == 60)
        for (elem in array) {
            val obj = elem.asJsonObject
            val dmg = obj.get("damage").asInt
            val expected = obj.get("expected_dc").asInt
            val actual = calculateConcentrationDc(dmg)
            assertEquals("Concentration DC mismatch for damage=$dmg", expected, actual)
        }
    }

    @Test
    fun testDeathSaveOutcomesParity() {
        val array = goldenData.getAsJsonArray("death_save_outcomes")
        assertTrue(array.size() == 20)
        for (elem in array) {
            val obj = elem.asJsonObject
            val roll = obj.get("roll").asInt
            val expectedSuccesses = obj.get("successes_count").asInt
            val expectedFailures = obj.get("failures_count").asInt
            val expectedStabilized = obj.get("is_stabilized").asBoolean
            val expectedDead = obj.get("is_dead").asBoolean
            val expectedHp = obj.get("hp_regained").asInt

            val tracker = DeathSaveTracker()
            tracker.recordRoll(roll)

            assertEquals("Death save successes mismatch for roll $roll", expectedSuccesses, tracker.successes)
            assertEquals("Death save failures mismatch for roll $roll", expectedFailures, tracker.failures)
            assertEquals("Death save isStabilized mismatch for roll $roll", expectedStabilized, tracker.isStabilized)
            assertEquals("Death save isDead mismatch for roll $roll", expectedDead, tracker.isDead)
            assertEquals("Death save hpRegained mismatch for roll $roll", expectedHp, tracker.hpRegained)
        }
    }

    @Test
    fun testDeterministicDiceEngineReplayability() {
        // Run A with seed 42
        val engineA = DiceEngine(seed = 42L)
        val rollsA = (1..50).map { engineA.rollDie(20) }

        // Run B with seed 42
        val engineB = DiceEngine(seed = 42L)
        val rollsB = (1..50).map { engineB.rollDie(20) }

        assertEquals("Seeded dice rolls must be 100% deterministic and replayable", rollsA, rollsB)

        // Reseed engineA and verify it replays identically
        engineA.reseed(42L)
        val rollsA2 = (1..50).map { engineA.rollDie(20) }
        assertEquals("Reseeded dice engine must reproduce identical sequence", rollsA, rollsA2)
    }

    @Test
    fun testClassHitDiceParity() {
        val hitDiceObj = goldenData.getAsJsonObject("class_hit_dice")
        for (characterClass in CharacterClass.values()) {
            val expectedHitDie = hitDiceObj.get(characterClass.label).asInt
            assertEquals("Hit die mismatch for ${characterClass.label}", expectedHitDie, characterClass.hitDie)
        }
    }

    @Test
    fun testSubclassesParity() {
        val subclassesObj = goldenData.getAsJsonObject("srd_subclasses")
        for (characterClass in CharacterClass.values()) {
            val expectedSubclass = subclassesObj.get(characterClass.label).asString
            assertEquals("Subclass mismatch for ${characterClass.label}", expectedSubclass, characterClass.primarySubclass)
        }
    }

    @Test
    fun testFormulaDamageParsing() {
        val engine = DiceEngine(seed = 999L)
        val outcome = engine.parseAndRoll("2d6+3")
        assertEquals(2, outcome.diceResults.size)
        assertEquals(3, outcome.modifier)
        assertTrue(outcome.total in 5..15)

        val critOutcome = engine.parseAndRoll("1d8+2", isCritical = true)
        assertEquals(2, critOutcome.diceResults.size) // Doubled from 1 to 2
        assertEquals(2, critOutcome.modifier)
        assertTrue(critOutcome.total in 4..18)
    }
}
