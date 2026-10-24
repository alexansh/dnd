package com.grimoire.dnd.dm.validator

import com.grimoire.dnd.dm.models.DmToolCall
import com.grimoire.dnd.dm.models.DmTurnProposal
import com.grimoire.dnd.dm.models.ToolCallType
import com.grimoire.dnd.engine.Ability
import com.grimoire.dnd.engine.Character
import com.grimoire.dnd.engine.Skill

data class ValidationResult(
    val isValid: Boolean,
    val violations: List<String>,
    val sanitizedProposal: DmTurnProposal?
)

class RuleLegalityValidator {

    private val validAbilities = Ability.values().map { it.name }.toSet()
    private val validSkills = Skill.values().map { it.label.lowercase() }.toSet()

    fun validateProposal(
        proposal: DmTurnProposal,
        character: Character
    ): ValidationResult {
        val violations = mutableListOf<String>()
        val approvedToolCalls = mutableListOf<DmToolCall>()

        for (call in proposal.toolCalls) {
            when (call.type) {
                ToolCallType.REQUEST_CHECK -> {
                    val dc = call.dc
                    if (dc == null || dc < 5 || dc > 30) {
                        violations.add("Illegal DC: $dc. Standard 5E SRD DC must be between 5 and 30.")
                    } else if (call.ability != null && !validAbilities.contains(call.ability.uppercase())) {
                        violations.add("Invalid 5E ability score '${call.ability}'. Expected one of $validAbilities.")
                    } else {
                        approvedToolCalls.add(call)
                    }
                }

                ToolCallType.CONSUME_ITEM -> {
                    val targetName = (call.itemName ?: call.itemId ?: "").lowercase().trim()
                    val ownedItem = character.inventory.firstOrNull {
                        it.name.lowercase().contains(targetName) || it.id.lowercase() == targetName
                    }

                    if (ownedItem == null) {
                        violations.add("Illegal item consumption: Character does not possess '$targetName' in inventory.")
                    } else if (ownedItem.quantity < call.quantity) {
                        violations.add("Illegal item consumption: Requested ${call.quantity} of '${ownedItem.name}', but character only owns ${ownedItem.quantity}.")
                    } else {
                        approvedToolCalls.add(call)
                    }
                }

                ToolCallType.APPLY_DAMAGE -> {
                    val amount = call.amount ?: 0
                    if (amount < 0) {
                        violations.add("Negative damage is illegal ($amount). Use APPLY_HEALING instead.")
                    } else if (amount > 150) {
                        violations.add("Excessive single-action damage ($amount) exceeds SRD bounds for early-to-mid tier play.")
                    } else {
                        approvedToolCalls.add(call)
                    }
                }

                ToolCallType.APPLY_HEALING -> {
                    val amount = call.amount ?: 0
                    if (amount < 0) {
                        violations.add("Negative healing is illegal ($amount). Use APPLY_DAMAGE instead.")
                    } else {
                        approvedToolCalls.add(call)
                    }
                }

                ToolCallType.GRANT_ITEM -> {
                    if (call.itemName.isNullOrBlank() && call.itemId.isNullOrBlank()) {
                        violations.add("Grant item call missing valid item name or ID.")
                    } else if (call.quantity <= 0 || call.quantity > 50) {
                        violations.add("Illegal item grant quantity: ${call.quantity}.")
                    } else {
                        approvedToolCalls.add(call)
                    }
                }

                ToolCallType.ADVANCE_CLOCK -> {
                    if (call.clockId.isNullOrBlank()) {
                        violations.add("Advance clock call missing target clock ID.")
                    } else if (call.clockTicks <= 0) {
                        violations.add("Clock ticks to advance must be positive.")
                    } else {
                        approvedToolCalls.add(call)
                    }
                }

                ToolCallType.UPDATE_NPC_ATTITUDE -> {
                    if (call.npcId.isNullOrBlank()) {
                        violations.add("Update NPC attitude call missing target NPC ID.")
                    } else if (call.attitudeDelta < -50 || call.attitudeDelta > 50) {
                        violations.add("Attitude delta ${call.attitudeDelta} exceeds realistic single-turn bounds (-50 to +50).")
                    } else {
                        approvedToolCalls.add(call)
                    }
                }

                ToolCallType.START_COMBAT, ToolCallType.REVEAL_SECRET -> {
                    approvedToolCalls.add(call)
                }
            }
        }

        val isValid = violations.isEmpty()
        val sanitized = if (isValid) {
            proposal.copy(toolCalls = approvedToolCalls)
        } else null

        return ValidationResult(
            isValid = isValid,
            violations = violations,
            sanitizedProposal = sanitized
        )
    }
}
