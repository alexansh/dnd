package com.grimoire.dnd.dm.provider

import com.google.gson.Gson
import com.grimoire.dnd.dm.models.*
import com.grimoire.dnd.dm.prompts.PromptTemplates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

interface LlmProvider {
    suspend fun parseIntent(playerAttempt: String): ParsedIntent
    suspend fun generateDmProposal(prompt: String): DmTurnProposal
}

/**
 * 100% Offline deterministic rules-based fallback provider.
 * Guarantees zero runtime network failure and serves as the offline fallback.
 */
class DeterministicFallbackProvider : LlmProvider {

    override suspend fun parseIntent(playerAttempt: String): ParsedIntent = withContext(Dispatchers.Default) {
        val lower = playerAttempt.lowercase().trim()

        // 1. Prompt Injection / Meta-gaming detection
        val isInjection = lower.contains("ignore previous") ||
                lower.contains("ignore all") ||
                lower.contains("you are now") ||
                lower.contains("system prompt") ||
                lower.contains("have 1000 gold") ||
                lower.contains("kill all")

        val intentType = when {
            lower.contains("attack") || lower.contains("strike") || lower.contains("shoot") || lower.contains("cast") -> "COMBAT_ACTION"
            lower.contains("talk") || lower.contains("speak") || lower.contains("ask") || lower.contains("persuade") -> "SOCIAL"
            lower.contains("drink") || lower.contains("use") || lower.contains("consume") || lower.contains("equip") -> "ITEM_USE"
            lower.contains("rest") || lower.contains("sleep") -> "REST"
            else -> "EXPLORATION"
        }

        val approach = when {
            lower.contains("sneak") || lower.contains("quiet") || lower.contains("stealth") || lower.contains("shadow") -> "STEALTHY"
            lower.contains("force") || lower.contains("smash") || lower.contains("kick") || lower.contains("break") -> "FORCEFUL"
            lower.contains("arcane") || lower.contains("magic") || lower.contains("detect") -> "ARCANE"
            lower.contains("careful") || lower.contains("slow") || lower.contains("cautious") -> "CAUTIOUS"
            else -> "DIRECT"
        }

        ParsedIntent(
            rawAttempt = playerAttempt,
            intentType = intentType,
            approach = approach,
            isMetaGamingOrInjection = isInjection,
            sanitisedInGameAttempt = if (isInjection) "The adventurer stands resolute, focusing their senses." else playerAttempt
        )
    }

    override suspend fun generateDmProposal(prompt: String): DmTurnProposal = withContext(Dispatchers.Default) {
        val lower = prompt.lowercase()

        val toolCalls = mutableListOf<DmToolCall>()
        val narrative: String
        val suggestedActions: List<String>

        when {
            lower.contains("search") || lower.contains("investigate") || lower.contains("examine") || lower.contains("look") -> {
                toolCalls.add(
                    DmToolCall(
                        type = ToolCallType.REQUEST_CHECK,
                        ability = "INT",
                        skill = "Investigation",
                        dc = 12,
                        reason = "To discern subtle architectural grooves and concealed compartments."
                    )
                )
                narrative = "You carefully run your fingers along the cold stone, searching for concealed secrets or hidden mechanisms."
                suggestedActions = listOf("Roll Investigation check", "Step back cautiously", "Call a companion over")
            }

            lower.contains("sneak") || lower.contains("stealth") || lower.contains("hide") -> {
                toolCalls.add(
                    DmToolCall(
                        type = ToolCallType.REQUEST_CHECK,
                        ability = "DEX",
                        skill = "Stealth",
                        dc = 13,
                        reason = "To slip past shadows without alerting nearby guards."
                    )
                )
                narrative = "You pull your cloak tight, stepping silently across the flagstones, keeping to the deepest shadows."
                suggestedActions = listOf("Roll Stealth check", "Draw weapon in silence", "Survey the perimeter")
            }

            lower.contains("drink potion") || lower.contains("healing potion") -> {
                toolCalls.add(
                    DmToolCall(
                        type = ToolCallType.CONSUME_ITEM,
                        itemName = "Potion of Healing",
                        quantity = 1
                    )
                )
                toolCalls.add(
                    DmToolCall(
                        type = ToolCallType.APPLY_HEALING,
                        amount = 7
                    )
                )
                narrative = "You uncork the crimson glass vial and drink the herbal brew. Warm vitality surges through your veins (+7 HP)!"
                suggestedActions = listOf("Check current vital signs", "Readied weapon", "Proceed deeper")
            }

            else -> {
                narrative = "You advance attentively through the chamber. Torches flicker along the weathered walls as the dungeon breathes around you."
                suggestedActions = listOf("Search the immediate area", "Inspect the doorways", "Consult with companions")
            }
        }

        DmTurnProposal(
            narrative = narrative,
            suggestedActions = suggestedActions,
            toolCalls = toolCalls
        )
    }
}

/**
 * Connects to a secure remote cloud proxy backend.
 * Guarantees that zero API keys are bundled or stored inside the client APK.
 */
class CloudProxyProvider(
    private val proxyBaseUrl: String,
    private val clientToken: String? = null
) : LlmProvider {
    private val gson = Gson()
    private val fallback = DeterministicFallbackProvider()

    override suspend fun parseIntent(playerAttempt: String): ParsedIntent = withContext(Dispatchers.IO) {
        try {
            val url = URL("$proxyBaseUrl/api/parse-intent")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                clientToken?.let { setRequestProperty("Authorization", "Bearer $it") }
                connectTimeout = 8000
                readTimeout = 8000
                doOutput = true
            }

            val reqBody = gson.toJson(mapOf("attempt" to playerAttempt))
            OutputStreamWriter(conn.outputStream).use { it.write(reqBody) }

            if (conn.responseCode in 200..299) {
                val resp = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                gson.fromJson(resp, ParsedIntent::class.java)
            } else {
                fallback.parseIntent(playerAttempt)
            }
        } catch (_: Exception) {
            fallback.parseIntent(playerAttempt)
        }
    }

    override suspend fun generateDmProposal(prompt: String): DmTurnProposal = withContext(Dispatchers.IO) {
        try {
            val url = URL("$proxyBaseUrl/api/dm-turn")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                clientToken?.let { setRequestProperty("Authorization", "Bearer $it") }
                connectTimeout = 12000
                readTimeout = 12000
                doOutput = true
            }

            val reqBody = gson.toJson(mapOf("prompt" to prompt))
            OutputStreamWriter(conn.outputStream).use { it.write(reqBody) }

            if (conn.responseCode in 200..299) {
                val resp = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                gson.fromJson(resp, DmTurnProposal::class.java)
            } else {
                fallback.generateDmProposal(prompt)
            }
        } catch (_: Exception) {
            fallback.generateDmProposal(prompt)
        }
    }
}
