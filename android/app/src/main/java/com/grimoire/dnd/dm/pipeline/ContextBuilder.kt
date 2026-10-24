package com.grimoire.dnd.dm.pipeline

import com.grimoire.dnd.data.db.WorldStateRepository
import com.grimoire.dnd.dm.prompts.PromptTemplates
import com.grimoire.dnd.engine.Character

class ContextBuilder(
    private val worldStateRepository: WorldStateRepository? = null
) {
    suspend fun buildTurnPrompt(
        campaignId: String,
        character: Character,
        sanitizedPlayerAttempt: String,
        locationTitle: String = "Unknown Location"
    ): String {
        val turnContext = worldStateRepository?.getTurnContext(campaignId, recentLimit = 3)

        val inventorySummary = character.inventory.joinToString(", ") {
            "${it.name}${if (it.quantity > 1) " x${it.quantity}" else ""}"
        }.ifEmpty { "Empty" }

        val nearbyNpcs = turnContext?.visibleEntities
            ?.filter { it.entityType.equals("NPC", ignoreCase = true) }
            ?.map { it.name } ?: emptyList()

        val activeClocks = turnContext?.activeClocks?.map {
            "${it.name} (${it.currentTicks}/${it.maxTicks})"
        } ?: emptyList()

        val recentLogs = turnContext?.recentEvents?.map {
            "Turn ${it.turnIndex}: ${it.playerAttemptText.take(60)} -> ${it.dmNarration.take(100)}..."
        } ?: listOf("The journey begins.")

        return PromptTemplates.buildUserTurnPrompt(
            sanitizedPlayerAttempt = sanitizedPlayerAttempt,
            locationTitle = locationTitle,
            characterName = character.name,
            characterClass = character.characterClass.label,
            currentHp = character.hpCurrent,
            maxHp = character.hpMax,
            inventorySummary = inventorySummary,
            nearbyNpcNames = nearbyNpcs,
            activeClockNames = activeClocks,
            recentTurnLogs = recentLogs
        )
    }
}
