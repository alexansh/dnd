package com.grimoire.dnd.data

import com.google.gson.Gson
import com.grimoire.dnd.data.db.*
import org.junit.Assert.*
import org.junit.Test

class WorldStateAndReplayTest {

    private val gson = Gson()

    @Test
    fun testEventLogReplayReconstruction() {
        val initial = ReplayState(
            hp = 12,
            xp = 0,
            gold = 10,
            items = mutableListOf("Dagger", "Health Potion"),
            currentLocation = "c1_tavern"
        )

        val events = listOf(
            EventLogRecord(
                campaignId = "camp_1",
                turnIndex = 1,
                playerAttemptText = "I march to the sunken crypt",
                engineStateDiffJson = gson.toJson(StateDiff(
                    xpGained = 50,
                    location = "c1_crypt",
                    flagsSet = mapOf("knows_crypt" to "true")
                )),
                dmNarration = "You arrive at the damp, ominous crypt entrance."
            ),
            EventLogRecord(
                campaignId = "camp_1",
                turnIndex = 2,
                playerAttemptText = "I open the rusted iron chest",
                engineStateDiffJson = gson.toJson(StateDiff(
                    goldChange = 25,
                    itemsAdded = listOf("Sunken King Crown", "Ancient Ring"),
                    itemsRemoved = listOf("Health Potion")
                )),
                dmNarration = "You drink your potion and loot the chest, recovering gold and royal relics."
            ),
            EventLogRecord(
                campaignId = "camp_1",
                turnIndex = 3,
                playerAttemptText = "A trapped dart hits me as I trigger the plate",
                engineStateDiffJson = gson.toJson(StateDiff(
                    hpChange = -4
                )),
                dmNarration = "A poison dart grazes your shoulder (-4 HP)!"
            )
        )

        val replayEngine = EventReplayEngine()
        val finalState = replayEngine.replay(initial, events)

        assertEquals("HP must be 12 - 4 = 8", 8, finalState.hp)
        assertEquals("XP must be 50", 50, finalState.xp)
        assertEquals("Gold must be 10 + 25 = 35", 35, finalState.gold)
        assertEquals("Location must be c1_crypt", "c1_crypt", finalState.currentLocation)
        assertTrue("Must have Sunken King Crown", finalState.items.contains("Sunken King Crown"))
        assertFalse("Health Potion must have been removed", finalState.items.contains("Health Potion"))
        assertEquals("Flag knows_crypt must be true", "true", finalState.flags["knows_crypt"])
    }

    @Test
    fun testCanonAuditorDetectsDirectContradictions() {
        val auditor = CanonAuditor()
        val establishedCanon = listOf(
            CanonLedgerRecord(
                id = "c1",
                campaignId = "camp_1",
                turnIndex = 1,
                category = "NPC_LORE",
                subject = "King Kar-Drak",
                assertionText = "King Kar-Drak is dead and sealed in the submerged tomb."
            ),
            CanonLedgerRecord(
                id = "c2",
                campaignId = "camp_1",
                turnIndex = 2,
                category = "LOCATION_DETAIL",
                subject = "Bronze Gate",
                assertionText = "The bronze gate is locked with ancient magic."
            )
        )

        // Contradicting Kar-Drak's death
        val contradictoryFact = CanonLedgerRecord(
            id = "c3",
            campaignId = "camp_1",
            turnIndex = 5,
            category = "NPC_LORE",
            subject = "King Kar-Drak",
            assertionText = "King Kar-Drak is alive and feasting in his castle."
        )

        val report = auditor.auditAssertion(contradictoryFact, establishedCanon)
        assertTrue("Must detect contradiction between dead and alive", report.hasContradiction)
        assertTrue(report.reason.contains("dead") && report.reason.contains("alive"))
    }

    @Test
    fun testCanonAuditorAllowsConsistentFacts() {
        val auditor = CanonAuditor()
        val establishedCanon = listOf(
            CanonLedgerRecord(
                id = "c1",
                campaignId = "camp_1",
                turnIndex = 1,
                category = "LOCATION_DETAIL",
                subject = "Riverfall",
                assertionText = "Riverfall is a quiet marsh town."
            )
        )

        val consistentFact = CanonLedgerRecord(
            id = "c2",
            campaignId = "camp_1",
            turnIndex = 2,
            category = "NPC_LORE",
            subject = "Elder Judith",
            assertionText = "Elder Judith is the village matriarch and keeper of the lore."
        )

        val report = auditor.auditAssertion(consistentFact, establishedCanon)
        assertFalse("Consistent new facts must pass audit without contradiction", report.hasContradiction)
    }

    @Test
    fun testTimedClockProgressionAndTrigger() {
        val clock = TimedClockRecord(
            id = "clock_flood",
            campaignId = "camp_1",
            name = "Marsh Flood",
            description = "Rising flood waters engulfing the crypt",
            currentTicks = 3,
            maxTicks = 4,
            isTriggered = false
        )

        assertEquals(0.75f, clock.progressFraction, 0.001f)
        assertFalse(clock.isTriggered)

        val triggeredClock = clock.copy(currentTicks = 4, isTriggered = true)
        assertEquals(1.0f, triggeredClock.progressFraction, 0.001f)
        assertTrue(triggeredClock.isTriggered)
    }

    @Test
    fun testDMSecretIsolation() {
        val secrets = listOf(
            DMSecretRecord(
                id = "sec_1",
                campaignId = "camp_1",
                title = "Judith's True Identity",
                secretText = "Judith was secretly the warlord's concubine 300 years ago.",
                revealedToPlayer = false
            ),
            DMSecretRecord(
                id = "sec_2",
                campaignId = "camp_1",
                title = "Hidden Sarcophagus Switch",
                secretText = "Behind the north gargoyle is a ruby pressure switch.",
                revealedToPlayer = true
            )
        )

        val playerVisible = secrets.filter { it.revealedToPlayer }
        val dmOnlySecrets = secrets.filter { !it.revealedToPlayer }

        assertEquals(1, playerVisible.size)
        assertEquals("Hidden Sarcophagus Switch", playerVisible.first().title)
        assertEquals(1, dmOnlySecrets.size)
        assertEquals("Judith's True Identity", dmOnlySecrets.first().title)
    }
}
