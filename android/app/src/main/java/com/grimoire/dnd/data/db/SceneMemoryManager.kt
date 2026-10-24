package com.grimoire.dnd.data.db

data class SceneSummary(
    val campaignId: String,
    val startTurn: Int,
    val endTurn: Int,
    val location: String,
    val summaryText: String
)

class SceneMemoryManager {
    /**
     * Condenses a sequence of event log turns into a rolling, high-density scene summary
     * to keep context window usage strictly bounded.
     */
    fun summarizeRecentTurns(events: List<EventLogRecord>, currentLocation: String): String {
        if (events.isEmpty()) return "The adventurer stands at $currentLocation, preparing for the perils ahead."

        val sorted = events.sortedBy { it.turnIndex }
        val sb = StringBuilder()
        sb.append("Recent Events at ").append(currentLocation).append(":\n")

        for (event in sorted.takeLast(4)) {
            val rollInfo = if (event.diceRollsJson != "[]" && event.diceRollsJson.isNotBlank()) " [Check Resolved]" else ""
            val preview = event.dmNarration.take(120).trimEnd()
            sb.append("• Turn ${event.turnIndex}: ${event.playerAttemptText.take(50)}$rollInfo -> \"$preview...\"\n")
        }

        return sb.toString().trimEnd()
    }
}
