package com.grimoire.dnd.engine

import com.grimoire.dnd.data.db.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CompanionAndJournalTest {

    private lateinit var hero: Character
    private lateinit var elira: Companion
    private lateinit var thorgar: Companion
    private lateinit var partyManager: PartyManager

    @Before
    fun setUp() {
        hero = Character(
            id = "hero_1",
            name = "Aric",
            characterClass = CharacterClass.FIGHTER,
            race = "Human",
            strength = 16,
            dexterity = 14,
            constitution = 14,
            intelligence = 10,
            wisdom = 12,
            charisma = 8,
            hpCurrent = 28,
            hpMax = 28,
            level = 3
        )
        partyManager = PartyManager(hero)

        val rogueChar = Character(
            id = "comp_elira",
            name = "Elira",
            characterClass = CharacterClass.ROGUE,
            race = "Elf",
            strength = 10,
            dexterity = 16,
            constitution = 12,
            intelligence = 14,
            wisdom = 12,
            charisma = 10,
            hpCurrent = 20,
            hpMax = 20,
            level = 3
        )
        elira = Companion(
            id = "comp_elira",
            name = "Elira",
            title = "Whisperblade",
            avatarTag = "portrait_elira",
            character = rogueChar,
            moralAlignment = MoralAlignment.CHAOTIC_GOOD,
            approvalScore = 50,
            backstory = "Former operative from the city of brass."
        )

        val paladinChar = Character(
            id = "comp_thorgar",
            name = "Thorgar",
            characterClass = CharacterClass.PALADIN,
            race = "Dwarf",
            strength = 16,
            dexterity = 8,
            constitution = 16,
            intelligence = 10,
            wisdom = 12,
            charisma = 14,
            hpCurrent = 32,
            hpMax = 32,
            level = 3
        )
        thorgar = Companion(
            id = "comp_thorgar",
            name = "Thorgar",
            title = "Ironheart",
            avatarTag = "portrait_thorgar",
            character = paladinChar,
            moralAlignment = MoralAlignment.LAWFUL_GOOD,
            approvalScore = 50,
            backstory = "Veteran sentinel seeking redemption."
        )

        partyManager.companions.add(elira)
        partyManager.companions.add(thorgar)
    }

    @Test
    fun testCompanionRecruitmentAndActivePartyCap() {
        assertEquals(1, partyManager.getActiveParty().size)

        assertTrue(partyManager.recruitCompanion("comp_elira"))
        assertTrue(elira.isRecruited)
        assertTrue(elira.isInActiveParty)
        assertEquals(2, partyManager.getActiveParty().size)

        assertTrue(partyManager.recruitCompanion("comp_thorgar"))
        assertEquals(3, partyManager.getActiveParty().size)
    }

    @Test
    fun testApprovalTiersAndModifiers() {
        assertEquals("Neutral", elira.getApprovalTier())

        elira.modifyApproval(15) // 65 -> Friendly
        assertEquals("Friendly", elira.getApprovalTier())

        elira.modifyApproval(20) // 85 -> Devoted
        assertEquals("Devoted", elira.getApprovalTier())

        elira.modifyApproval(-60) // 25 -> Distrustful
        assertEquals("Distrustful", elira.getApprovalTier())

        elira.modifyApproval(-20) // 5 -> Hostile
        assertEquals("Hostile", elira.getApprovalTier())
    }

    @Test
    fun testPartyApprovalDynamicsOnChoices() {
        partyManager.recruitCompanion("comp_elira")
        partyManager.recruitCompanion("comp_thorgar")

        // Choice: Heroic & Mercy
        val reactions = partyManager.applyPartyApproval(listOf("heroic", "mercy"))
        assertEquals(2, reactions.size)
        assertTrue(reactions.any { it.contains("Thorgar approves (+5)") })
        assertTrue(reactions.any { it.contains("Elira approves (+5)") })

        // Choice: Ruthless
        val ruthlessReactions = partyManager.applyPartyApproval(listOf("ruthless"))
        assertEquals(1, ruthlessReactions.size)
        assertTrue(ruthlessReactions[0].contains("Thorgar disapproves (-8)"))
    }

    @Test
    fun testCompanionDissentAndBoundedAutonomy() {
        val protest = thorgar.evaluateProposal("slaughter")
        assertNotNull(protest)
        assertTrue(protest!!.contains("will not permit cold-blooded slaughter"))

        val cheer = elira.evaluateProposal("glory")
        assertNotNull(cheer)
        assertTrue(cheer!!.contains("Now you speak my language!"))
    }

    @Test
    fun testCompanionMutinyWhenMistreated() {
        partyManager.recruitCompanion("comp_thorgar")
        assertTrue(thorgar.isInActiveParty)

        // Severely drop approval below 15
        thorgar.approvalScore = 12

        val departureLog = partyManager.checkPartyLoyalty()
        assertEquals(1, departureLog.size)
        assertTrue(departureLog[0].contains("PARTY MUTINY: Thorgar"))
        assertFalse(thorgar.isInActiveParty)
        assertFalse(thorgar.isRecruited)
    }

    @Test
    fun testTurnContextMappingForLivingJournal() {
        val clock = TimedClockRecord(
            id = "clk_flood",
            campaignId = "camp_test",
            name = "Crypt Flooding",
            description = "Rising seawater fills the vaults",
            currentTicks = 3,
            maxTicks = 6,
            isTriggered = false
        )

        val npc = EntityRecord(
            id = "npc_elira",
            campaignId = "camp_test",
            name = "Elira",
            entityType = "NPC",
            description = "A cunning wood elf rogue sharpening daggers."
        )

        val fact = CanonLedgerRecord(
            id = "fact_1",
            campaignId = "camp_test",
            turnIndex = 2,
            category = "FACT",
            subject = "Crypt Vault",
            assertionText = "The door is sealed with an iron serpent crest."
        )

        val turnContext = TurnContext(
            visibleEntities = listOf(npc),
            relationships = emptyList(),
            activeClocks = listOf(clock),
            unrevealedSecrets = emptyList(),
            recentEvents = emptyList(),
            canonFacts = listOf(fact)
        )

        assertEquals(1, turnContext.activeClocks.size)
        assertEquals(0.5f, turnContext.activeClocks[0].progressFraction, 0.001f)
        assertEquals(1, turnContext.visibleEntities.size)
        assertEquals("NPC", turnContext.visibleEntities[0].entityType)
        assertEquals(1, turnContext.canonFacts.size)
    }
}
