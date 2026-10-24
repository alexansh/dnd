package com.grimoire.dnd.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.grimoire.dnd.engine.Character

data class SaveGameData(
    val slotId: String,
    val slotName: String,
    val character: Character,
    val campaignId: String,
    val currentNodeId: String,
    val visitedNodes: List<String>,
    val questFlags: Map<String, String>,
    val timestamp: Long = System.currentTimeMillis()
)

class LocalSaveManager(context: Context) {
    private val prefs = context.getSharedPreferences("grimoire_5e_saves", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun listSaves(): List<SaveGameData> {
        val savesJson = prefs.getString("all_slots", "[]") ?: "[]"
        val type = object : TypeToken<List<String>>() {}.type
        val slotIds: List<String> = try {
            gson.fromJson(savesJson, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }

        return slotIds.mapNotNull { id ->
            val slotJson = prefs.getString("save_$id", null) ?: return@mapNotNull null
            try {
                gson.fromJson(slotJson, SaveGameData::class.java)
            } catch (_: Exception) {
                null
            }
        }.sortedByDescending { it.timestamp }
    }

    fun getSave(slotId: String): SaveGameData? {
        val slotJson = prefs.getString("save_$slotId", null) ?: return null
        return try {
            gson.fromJson(slotJson, SaveGameData::class.java)
        } catch (_: Exception) {
            null
        }
    }

    fun saveGame(save: SaveGameData) {
        val editor = prefs.edit()
        val slotJson = gson.toJson(save)
        editor.putString("save_${save.slotId}", slotJson)

        val existing = listSaves().map { it.slotId }.toMutableSet()
        existing.add(save.slotId)
        editor.putString("all_slots", gson.toJson(existing.toList()))
        editor.putString("latest_slot_id", save.slotId)
        editor.apply()
    }

    fun getLatestSave(): SaveGameData? {
        val latestId = prefs.getString("latest_slot_id", null) ?: return null
        return getSave(latestId)
    }

    fun deleteSave(slotId: String) {
        val editor = prefs.edit()
        editor.remove("save_$slotId")

        val existing = listSaves().map { it.slotId }.filter { it != slotId }
        editor.putString("all_slots", gson.toJson(existing))

        if (prefs.getString("latest_slot_id", null) == slotId) {
            editor.remove("latest_slot_id")
        }
        editor.apply()
    }
}
