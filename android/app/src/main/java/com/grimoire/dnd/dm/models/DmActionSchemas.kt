package com.grimoire.dnd.dm.models

data class ParsedIntent(
    val rawAttempt: String,
    val intentType: String, // "EXPLORATION", "SOCIAL", "COMBAT_ACTION", "ITEM_USE", "REST", "UNKNOWN"
    val approach: String,   // "STEALTHY", "FORCEFUL", "DIPLOMATIC", "ARCANE", "DIRECT"
    val targetName: String? = null,
    val isMetaGamingOrInjection: Boolean = false,
    val sanitisedInGameAttempt: String
)

enum class ToolCallType {
    REQUEST_CHECK,
    APPLY_DAMAGE,
    APPLY_HEALING,
    GRANT_ITEM,
    CONSUME_ITEM,
    START_COMBAT,
    UPDATE_NPC_ATTITUDE,
    ADVANCE_CLOCK,
    REVEAL_SECRET
}

data class DmToolCall(
    val type: ToolCallType,
    // request_check
    val ability: String? = null,
    val skill: String? = null,
    val dc: Int? = null,
    val reason: String? = null,
    val rollType: String = "NORMAL", // "NORMAL", "ADVANTAGE", "DISADVANTAGE"

    // apply_damage / apply_healing
    val targetId: String? = null,
    val amount: Int? = null,
    val damageType: String? = null,

    // grant_item / consume_item
    val itemId: String? = null,
    val itemName: String? = null,
    val quantity: Int = 1,

    // start_combat
    val enemyIds: List<String> = emptyList(),
    val initialZone: String = "FRONTLINE",

    // update_npc_attitude
    val npcId: String? = null,
    val attitudeDelta: Int = 0,
    val justification: String? = null,

    // advance_clock
    val clockId: String? = null,
    val clockTicks: Int = 1,

    // reveal_secret
    val secretId: String? = null
)

data class ProposedCanonFact(
    val category: String, // "FACT", "NPC_LORE", "LOCATION_DETAIL", "HISTORICAL_EVENT", "PROMISE"
    val subject: String,
    val assertionText: String
)

data class DmTurnProposal(
    val narrative: String,
    val suggestedActions: List<String> = emptyList(),
    val toolCalls: List<DmToolCall> = emptyList(),
    val proposedCanonFacts: List<ProposedCanonFact> = emptyList()
)
