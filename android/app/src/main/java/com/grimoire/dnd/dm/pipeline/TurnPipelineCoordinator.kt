package com.grimoire.dnd.dm.pipeline

import com.google.gson.Gson
import com.grimoire.dnd.data.db.*
import com.grimoire.dnd.dm.models.DmToolCall
import com.grimoire.dnd.dm.models.DmTurnProposal
import com.grimoire.dnd.dm.models.ToolCallType
import com.grimoire.dnd.dm.prompts.PromptTemplates
import com.grimoire.dnd.dm.provider.LlmProvider
import com.grimoire.dnd.dm.validator.RuleLegalityValidator
import com.grimoire.dnd.engine.*

data class TurnExecutionResult(
    val turnIndex: Int,
    val dmNarration: String,
    val suggestedActions: List<String>,
    val diceRollOutcome: RollOutcome? = null,
    val ruleExplanation: RuleExplanation? = null,
    val stateDiff: StateDiff = StateDiff(),
    val isCombatTriggered: Boolean = false,
    val combatEnemyIds: List<String> = emptyList()
)

class TurnPipelineCoordinator(
    private val llmProvider: LlmProvider,
    private val contextBuilder: ContextBuilder,
    private val validator: RuleLegalityValidator = RuleLegalityValidator(),
    private val diceEngine: DiceEngine = DiceEngine(),
    private val worldStateRepository: WorldStateRepository? = null
) {
    private val gson = Gson()

    suspend fun executeTurn(
        campaignId: String,
        character: Character,
        playerFreeText: String,
        locationTitle: String = "Riverfall Marsh"
    ): TurnExecutionResult {
        // Step 1: Parse player intent & detect meta-gaming/injection
        val parsedIntent = llmProvider.parseIntent(playerFreeText)
        val sanitizedAttempt = parsedIntent.sanitisedInGameAttempt

        // Step 2: Build bounded context prompt
        val turnPrompt = contextBuilder.buildTurnPrompt(
            campaignId = campaignId,
            character = character,
            sanitizedPlayerAttempt = sanitizedAttempt,
            locationTitle = locationTitle
        )

        // Step 3: Query LLM for proposed turn adjudication with up to 2 retries
        var proposal = llmProvider.generateDmProposal(turnPrompt)
        var validation = validator.validateProposal(proposal, character)
        var retryCount = 0

        while (!validation.isValid && retryCount < 2) {
            retryCount++
            val retryPrompt = PromptTemplates.buildRetryPrompt(sanitizedAttempt, validation.violations)
            proposal = llmProvider.generateDmProposal(retryPrompt)
            validation = validator.validateProposal(proposal, character)
        }

        // If still invalid after retries, sanitize by dropping illegal calls
        val approvedProposal = validation.sanitizedProposal ?: proposal.copy(toolCalls = emptyList())

        // Step 4: Deterministic 5E Engine applies mechanics ("The Engine Disposes")
        var primaryRollOutcome: RollOutcome? = null
        var ruleExplanation: RuleExplanation? = null
        var hpDelta = 0
        var goldDelta = 0
        var xpGained = 0
        val itemsAdded = mutableListOf<String>()
        val itemsRemoved = mutableListOf<String>()
        val clocksToAdvance = mutableListOf<Pair<String, Int>>()
        val secretsToReveal = mutableListOf<String>()
        var combatTriggered = false
        val enemyIds = mutableListOf<String>()

        for (call in approvedProposal.toolCalls) {
            when (call.type) {
                ToolCallType.REQUEST_CHECK -> {
                    val dc = call.dc ?: 12
                    val rollType = when (call.rollType.uppercase()) {
                        "ADVANTAGE" -> RollType.ADVANTAGE
                        "DISADVANTAGE" -> RollType.DISADVANTAGE
                        else -> RollType.NORMAL
                    }

                    // Resolve skill / ability modifier
                    val skillEnum = Skill.values().firstOrNull {
                        it.label.equals(call.skill, ignoreCase = true) || it.name.equals(call.skill, ignoreCase = true)
                    }
                    val abilityEnum = Ability.values().firstOrNull {
                        it.name.equals(call.ability, ignoreCase = true)
                    } ?: skillEnum?.ability ?: Ability.DEX

                    val modifier = if (skillEnum != null) {
                        character.getSkillModifier(skillEnum)
                    } else {
                        character.getAbilityModifier(abilityEnum)
                    }

                    val roll = diceEngine.rollD20(modifier = modifier, rollType = rollType)
                    primaryRollOutcome = roll
                    val isSuccess = (roll.total >= dc || roll.isCriticalHit) && !roll.isCriticalMiss

                    val breakdown = "d20 Roll (${roll.diceResults.firstOrNull { !it.isDiscarded }?.value ?: roll.total}) " +
                            "${if (modifier >= 0) "+$modifier" else "$modifier"} = ${roll.total} vs DC $dc"

                    ruleExplanation = RuleExplanation(
                        title = if (isSuccess) "CHECK PASSED: ${skillEnum?.label ?: abilityEnum.label}" else "CHECK FAILED: ${skillEnum?.label ?: abilityEnum.label}",
                        ruleCitation = "D&D 5E SRD 5.1 — Using Ability Scores",
                        breakdownText = breakdown,
                        adviceText = if (isSuccess) "Success! You overcame the DC." else "Failure! The situation becomes more dangerous."
                    )
                }

                ToolCallType.APPLY_DAMAGE -> {
                    val dmg = call.amount ?: 0
                    character.hpCurrent = (character.hpCurrent - dmg).coerceAtLeast(0)
                    hpDelta -= dmg
                }

                ToolCallType.APPLY_HEALING -> {
                    val heal = call.amount ?: 0
                    character.hpCurrent = (character.hpCurrent + heal).coerceAtMost(character.hpMax)
                    hpDelta += heal
                }

                ToolCallType.GRANT_ITEM -> {
                    val name = call.itemName ?: call.itemId ?: "Mysterious Item"
                    val existing = character.inventory.firstOrNull { it.name.equals(name, ignoreCase = true) }
                    if (existing != null) {
                        existing.quantity += call.quantity
                    } else {
                        character.inventory.add(Item(id = "item_${name.hashCode()}", name = name, quantity = call.quantity))
                    }
                    itemsAdded.add(name)
                }

                ToolCallType.CONSUME_ITEM -> {
                    val name = call.itemName ?: call.itemId ?: ""
                    val existing = character.inventory.firstOrNull {
                        it.name.contains(name, ignoreCase = true) || it.id.equals(name, ignoreCase = true)
                    }
                    if (existing != null) {
                        existing.quantity = (existing.quantity - call.quantity).coerceAtLeast(0)
                        if (existing.quantity == 0) {
                            character.inventory.remove(existing)
                        }
                    }
                    itemsRemoved.add(name)
                }

                ToolCallType.ADVANCE_CLOCK -> {
                    call.clockId?.let { clocksToAdvance.add(it to call.clockTicks) }
                }

                ToolCallType.REVEAL_SECRET -> {
                    call.secretId?.let { secretsToReveal.add(it) }
                }

                ToolCallType.START_COMBAT -> {
                    combatTriggered = true
                    enemyIds.addAll(call.enemyIds)
                }

                ToolCallType.UPDATE_NPC_ATTITUDE -> {
                    // Handled in relationships
                }
            }
        }

        val stateDiff = StateDiff(
            hpChange = hpDelta,
            xpGained = xpGained,
            goldChange = goldDelta,
            itemsAdded = itemsAdded,
            itemsRemoved = itemsRemoved
        )

        // Step 5: Record Turn & Commit to Room
        val currentTurn = (worldStateRepository?.getLatestTurnIndex(campaignId) ?: 0) + 1
        val newCanonRecords = approvedProposal.proposedCanonFacts.map { fact ->
            CanonLedgerRecord(
                id = "canon_${System.currentTimeMillis()}_${fact.subject.hashCode()}",
                campaignId = campaignId,
                turnIndex = currentTurn,
                category = fact.category,
                subject = fact.subject,
                assertionText = fact.assertionText
            )
        }

        worldStateRepository?.recordTurn(
            campaignId = campaignId,
            turnIndex = currentTurn,
            playerAttempt = playerFreeText,
            parsedIntentJson = gson.toJson(parsedIntent),
            diceRollsJson = gson.toJson(primaryRollOutcome?.diceResults ?: emptyList<DieResult>()),
            stateDiffJson = gson.toJson(stateDiff),
            dmNarration = approvedProposal.narrative,
            isCombat = combatTriggered,
            newCanonFacts = newCanonRecords,
            clocksToAdvance = clocksToAdvance,
            revealedSecretIds = secretsToReveal
        )

        return TurnExecutionResult(
            turnIndex = currentTurn,
            dmNarration = approvedProposal.narrative,
            suggestedActions = approvedProposal.suggestedActions,
            diceRollOutcome = primaryRollOutcome,
            ruleExplanation = ruleExplanation,
            stateDiff = stateDiff,
            isCombatTriggered = combatTriggered,
            combatEnemyIds = enemyIds
        )
    }
}
