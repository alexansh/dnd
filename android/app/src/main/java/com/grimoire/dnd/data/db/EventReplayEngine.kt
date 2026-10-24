package com.grimoire.dnd.data.db

import com.google.gson.Gson

data class StateDiff(
    val hpChange: Int = 0,
    val xpGained: Int = 0,
    val goldChange: Int = 0,
    val itemsAdded: List<String> = emptyList(),
    val itemsRemoved: List<String> = emptyList(),
    val location: String? = null,
    val flagsSet: Map<String, String> = emptyMap()
)

data class ReplayState(
    var hp: Int,
    var xp: Int,
    var gold: Int,
    val items: MutableList<String> = mutableListOf(),
    var currentLocation: String = "c1_tavern",
    val flags: MutableMap<String, String> = mutableMapOf()
)

class EventReplayEngine {
    private val gson = Gson()

    fun replay(initialState: ReplayState, events: List<EventLogRecord>): ReplayState {
        val current = initialState.copy(
            items = initialState.items.toMutableList(),
            flags = initialState.flags.toMutableMap()
        )

        for (event in events.sortedBy { it.turnIndex }) {
            if (event.engineStateDiffJson.isBlank() || event.engineStateDiffJson == "{}") continue
            val diff = try {
                gson.fromJson(event.engineStateDiffJson, StateDiff::class.java)
            } catch (_: Exception) {
                null
            } ?: continue

            current.hp += diff.hpChange
            current.xp += diff.xpGained
            current.gold += diff.goldChange

            current.items.addAll(diff.itemsAdded)
            current.items.removeAll(diff.itemsRemoved.toSet())

            diff.location?.let { current.currentLocation = it }
            current.flags.putAll(diff.flagsSet)
        }

        return current
    }
}
