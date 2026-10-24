package com.grimoire.dnd.dm

import com.grimoire.dnd.data.db.CanonAuditor
import com.grimoire.dnd.data.db.CanonLedgerRecord
import com.grimoire.dnd.data.db.EventLogRecord
import com.grimoire.dnd.data.db.SceneMemoryManager
import com.grimoire.dnd.data.modules.SunkenKingModule
import com.grimoire.dnd.dm.campaign.CampaignGenerator
import org.junit.Assert.*
import org.junit.Test

class CampaignAndMemoryTest {

    @Test
    fun testProceduralCampaignGenerator() {
        val generator = CampaignGenerator()
        val skeleton = generator.generateProceduralSkeleton("Gothic Marsh")

        assertTrue("Campaign ID must be populated", skeleton.campaignId.startsWith("camp_"))
        assertEquals("Must generate 3 factions", 3, skeleton.factions.size)
        assertEquals("Must generate 4 progress clocks", 4, skeleton.clocks.size)
        assertEquals("Must generate 6 core NPCs", 6, skeleton.keyNpcs.size)
        assertEquals("Must generate 3 DM secrets", 3, skeleton.secrets.size)
        assertNotNull("Starting location must be defined", skeleton.startingLocation)
        assertTrue("Starting location must be known to player", skeleton.startingLocation.isKnownToPlayer)
    }

    @Test
    fun testSceneMemoryRollingSummarizer() {
        val memoryManager = SceneMemoryManager()
        val events = listOf(
            EventLogRecord(
                campaignId = "camp_1",
                turnIndex = 1,
                playerAttemptText = "I ask Judith about the marsh",
                dmNarration = "Judith tells of the drowned king and points to the graveyard."
            ),
            EventLogRecord(
                campaignId = "camp_1",
                turnIndex = 2,
                playerAttemptText = "I march to the cemetery gates",
                dmNarration = "Cold fog swirls around weeping willows."
            )
        )

        val summary = memoryManager.summarizeRecentTurns(events, "Riverfall Cemetery")
        assertTrue(summary.contains("Recent Events at Riverfall Cemetery"))
        assertTrue(summary.contains("Turn 1:"))
        assertTrue(summary.contains("Turn 2:"))
    }

    @Test
    fun testSunkenKingModuleIntegrity() {
        val legacyNodes = SunkenKingModule.getLegacyNodes()
        assertTrue("Sunken King module must provide legacy nodes for offline fallback", legacyNodes.isNotEmpty())
        assertEquals("campaign_1", SunkenKingModule.CAMPAIGN_ID)
        assertEquals("The Sunken King's Legacy", SunkenKingModule.CAMPAIGN_TITLE)
    }

    @Test
    fun testPreCommitCanonContradictionRejection() {
        val auditor = CanonAuditor()
        val establishedCanon = listOf(
            CanonLedgerRecord(
                id = "canon_1",
                campaignId = "camp_1",
                turnIndex = 1,
                category = "FACT",
                subject = "The Sunken Vault",
                assertionText = "The sunken vault is locked and sealed by ancient bronze."
            )
        )

        val contradictoryProposal = CanonLedgerRecord(
            id = "canon_2",
            campaignId = "camp_1",
            turnIndex = 2,
            category = "FACT",
            subject = "The Sunken Vault",
            assertionText = "The sunken vault is unlocked and open to all."
        )

        val audit = auditor.auditAssertion(contradictoryProposal, establishedCanon)
        assertTrue("Contradictory canon proposal must be detected and rejected", audit.hasContradiction)
    }
}
