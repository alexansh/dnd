package com.grimoire.dnd.engine

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
        return "$name $changeWord (+$delta)$tierNote"
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
            if (choiceTags.contains("heroic") && comp.moralAlignment == MoralAlignment.LAWFUL_GOOD) delta += 5
            if (choiceTags.contains("ruthless") && comp.moralAlignment == MoralAlignment.LAWFUL_GOOD) delta -= 8
            if (choiceTags.contains("mercenary") && comp.moralAlignment == MoralAlignment.TRUE_NEUTRAL) delta += 4
            if (choiceTags.contains("shadow") && comp.moralAlignment == MoralAlignment.CHAOTIC_EVIL) delta += 6
            if (choiceTags.contains("mercy") && comp.moralAlignment == MoralAlignment.CHAOTIC_GOOD) delta += 5

            if (delta != 0) {
                reactionLogs.add(comp.modifyApproval(delta))
            }
        }
        return reactionLogs
    }
}
