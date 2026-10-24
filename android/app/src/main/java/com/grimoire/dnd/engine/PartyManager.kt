package com.grimoire.dnd.engine

import com.grimoire.dnd.dm.models.DmToolCall
import com.grimoire.dnd.dm.models.ToolCallType

enum class MoralAlignment(val label: String) {
    LAWFUL_GOOD("Lawful Good"),
    CHAOTIC_GOOD("Chaotic Good"),
    TRUE_NEUTRAL("True Neutral"),
    LAWFUL_EVIL("Lawful Evil"),
    CHAOTIC_EVIL("Chaotic Evil")
}

data class Companion(
    val id: String,
    val name: String,
    val title: String,
    val avatarTag: String,
    val character: Character,
    val moralAlignment: MoralAlignment,
    var approvalScore: Int = 50, // 0 to 100
    val backstory: String,
    val goal: String = "",
    val bond: String = "",
    val flaw: String = "",
    var isRecruited: Boolean = false,
    var isInActiveParty: Boolean = false
) {
    fun getApprovalTier(): String = when {
        approvalScore >= 80 -> "Devoted"
        approvalScore >= 60 -> "Friendly"
        approvalScore >= 40 -> "Neutral"
        approvalScore >= 20 -> "Distrustful"
        else -> "Hostile"
    }

    fun modifyApproval(delta: Int): String {
        val oldTier = getApprovalTier()
        approvalScore = (approvalScore + delta).coerceIn(0, 100)
        val newTier = getApprovalTier()
        val changeWord = if (delta > 0) "approves" else "disapproves"
        val tierNote = if (oldTier != newTier) " ($newTier)" else ""
        return "$name $changeWord (${if (delta > 0) "+$delta" else "$delta"})$tierNote"
    }

    /**
     * Bounded autonomy: companion evaluates a proposed party action and expresses agreement or dissent.
     */
    fun evaluateProposal(actionTag: String): String? {
        return when {
            actionTag.contains("mercy", true) && moralAlignment == MoralAlignment.CHAOTIC_EVIL ->
                "$name scoffed: 'Mercy is a luxury for fools who wish an early grave.'"
            actionTag.contains("slaughter", true) && moralAlignment == MoralAlignment.LAWFUL_GOOD ->
                "$name gripped their blade and stood between you: 'I will not permit cold-blooded slaughter while I breathe!'"
            actionTag.contains("bribe", true) && moralAlignment == MoralAlignment.LAWFUL_GOOD ->
                "$name frowned: 'A righteous cause should not stoop to petty extortion.'"
            actionTag.contains("glory", true) && moralAlignment == MoralAlignment.CHAOTIC_GOOD ->
                "$name grinned: 'Now you speak my language! Let's give them hell!'"
            else -> null
        }
    }
}

class PartyManager(
    val protagonist: Character
) {
    val companions: MutableList<Companion> = mutableListOf()

    fun getActiveParty(): List<Character> {
        val list = mutableListOf(protagonist)
        list.addAll(companions.filter { it.isInActiveParty }.map { it.character }.take(3))
        return list
    }

    fun recruitCompanion(companionId: String): Boolean {
        val comp = companions.firstOrNull { it.id == companionId } ?: return false
        comp.isRecruited = true
        if (companions.count { it.isInActiveParty } < 3) {
            comp.isInActiveParty = true
        }
        return true
    }

    fun applyPartyApproval(choiceTags: List<String>): List<String> {
        val reactionLogs = mutableListOf<String>()
        for (comp in companions.filter { it.isRecruited }) {
            var delta = 0
            if (choiceTags.any { it.contains("heroic", true) } && comp.moralAlignment == MoralAlignment.LAWFUL_GOOD) delta += 5
            if (choiceTags.any { it.contains("ruthless", true) } && comp.moralAlignment == MoralAlignment.LAWFUL_GOOD) delta -= 8
            if (choiceTags.any { it.contains("mercenary", true) } && comp.moralAlignment == MoralAlignment.TRUE_NEUTRAL) delta += 4
            if (choiceTags.any { it.contains("shadow", true) } && comp.moralAlignment == MoralAlignment.CHAOTIC_EVIL) delta += 6
            if (choiceTags.any { it.contains("mercy", true) } && comp.moralAlignment == MoralAlignment.CHAOTIC_GOOD) delta += 5

            if (delta != 0) {
                reactionLogs.add(comp.modifyApproval(delta))
            }
        }
        return reactionLogs
    }

    /**
     * Checks if any companion's approval has dropped to desertion/mutiny thresholds (<= 15).
     * If so, they leave active party status and a narrative incident is emitted.
     */
    fun checkPartyLoyalty(): List<String> {
        val departureEvents = mutableListOf<String>()
        for (comp in companions.filter { it.isInActiveParty }) {
            if (comp.approvalScore <= 15) {
                comp.isInActiveParty = false
                comp.isRecruited = false
                departureEvents.add(
                    "PARTY MUTINY: ${comp.name} (${comp.title}) has abandoned the expedition in disgust! (Approval: ${comp.approvalScore}/100)"
                )
            }
        }
        return departureEvents
    }

    /**
     * Autonomous companion combat helper: generates a legal 5E tool action from companion.
     */
    fun proposeCompanionCombatAction(companion: Companion, targetId: String): DmToolCall {
        return DmToolCall(
            type = ToolCallType.APPLY_DAMAGE,
            targetId = targetId,
            amount = 4 + companion.character.getAbilityModifier(Ability.STR),
            damageType = "slashing"
        )
    }
}
