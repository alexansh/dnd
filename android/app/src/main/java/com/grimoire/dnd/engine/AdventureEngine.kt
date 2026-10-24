package com.grimoire.dnd.engine

data class SkillCheckRequirement(
    val skill: Skill,
    val dc: Int,
    val successNodeId: String,
    val failureNodeId: String,
    val failureDamageFormula: String? = null,
    val successMessage: String = "Success!",
    val failureMessage: String = "Failure."
)

data class NodeChoice(
    val id: String,
    val text: String,
    val targetNodeId: String? = null,
    val skillCheck: SkillCheckRequirement? = null,
    val requiredFlag: Pair<String, String>? = null,
    val setFlagOnSelect: Pair<String, String>? = null,
    val moralTags: List<String> = emptyList(), // e.g. "heroic", "ruthless", "shadow", "mercy"
    val recruitCompanionId: String? = null
)

data class AdventureNode(
    val nodeId: String,
    val campaignId: String,
    val title: String,
    val sceneVisualTag: String = "crypt", // "tavern", "graveyard", "crypt", "sanctum"
    val speakerName: String? = null,
    val narration: String,
    val returnNarration: String? = null,
    val choices: List<NodeChoice> = emptyList(),
    val monsterIds: List<String> = emptyList(),
    val isRestArea: Boolean = false,
    val rewardXp: Int = 0,
    val rewardGold: Int = 0
)

data class TransitionResult(
    val nextNodeId: String,
    val narrativeLog: String,
    val rollOutcome: RollOutcome? = null,
    val isCombatTriggered: Boolean = false,
    val monstersEncountered: List<String> = emptyList(),
    val companionReactions: List<String> = emptyList()
)

class AdventureEngine(
    private val diceEngine: DiceEngine,
    val partyManager: PartyManager? = null
) {
    private val nodes: MutableMap<String, AdventureNode> = mutableMapOf()
    var currentNodeId: String? = null
        private set
    val visitedNodes: MutableSet<String> = mutableSetOf()
    val questFlags: MutableMap<String, String> = mutableMapOf()

    fun registerNode(node: AdventureNode) {
        nodes[node.nodeId] = node
    }

    fun startAdventure(startingNodeId: String): AdventureNode {
        currentNodeId = startingNodeId
        return getCurrentNode()
    }

    fun getCurrentNode(): AdventureNode {
        return nodes[currentNodeId] ?: throw IllegalStateException("Invalid node: $currentNodeId")
    }

    fun getCurrentNarration(): String {
        val node = getCurrentNode()
        return if (visitedNodes.contains(node.nodeId) && node.returnNarration != null) {
            node.returnNarration
        } else {
            node.narration
        }
    }

    fun getAvailableChoices(): List<NodeChoice> {
        val node = getCurrentNode()
        return node.choices.filter { choice ->
            choice.requiredFlag == null || questFlags[choice.requiredFlag.first] == choice.requiredFlag.second
        }
    }

    fun executeChoice(choiceId: String, character: Character): TransitionResult {
        val node = getCurrentNode()
        val choice = node.choices.firstOrNull { it.id == choiceId }
            ?: throw IllegalArgumentException("Choice $choiceId not found")

        visitedNodes.add(node.nodeId)

        choice.setFlagOnSelect?.let { (key, value) ->
            questFlags[key] = value
        }

        // Handle companion recruitment
        if (choice.recruitCompanionId != null && partyManager != null) {
            partyManager.recruitCompanion(choice.recruitCompanionId)
        }

        // Calculate companion approval reactions
        val companionReactions = if (partyManager != null && choice.moralTags.isNotEmpty()) {
            partyManager.applyPartyApproval(choice.moralTags)
        } else {
            emptyList()
        }

        var rollOutcome: RollOutcome? = null
        var log = ""
        var targetNodeId = choice.targetNodeId

        if (choice.skillCheck != null) {
            val check = choice.skillCheck
            val mod = character.getSkillModifier(check.skill)
            val roll = diceEngine.rollD20(modifier = mod)
            rollOutcome = roll

            val isSuccess = (roll.total >= check.dc || roll.isCriticalHit) && !roll.isCriticalMiss

            if (isSuccess) {
                targetNodeId = check.successNodeId
                log = "[${check.skill.label} DC ${check.dc} | ${roll.summary()}] ${check.successMessage}"
            } else {
                targetNodeId = check.failureNodeId
                if (check.failureDamageFormula != null) {
                    val dmgRoll = diceEngine.parseAndRoll(check.failureDamageFormula)
                    val damageTaken = character.applyDamage(dmgRoll.total)
                    log = "[${check.skill.label} DC ${check.dc} | ${roll.summary()}] ${check.failureMessage} (Took $damageTaken damage)"
                } else {
                    log = "[${check.skill.label} DC ${check.dc} | ${roll.summary()}] ${check.failureMessage}"
                }
            }
        }

        val destination = targetNodeId ?: throw IllegalStateException("No destination node")
        currentNodeId = destination

        if (!nodes.containsKey(destination) && destination.startsWith("inf_floor_")) {
            val floorNum = destination.substringAfter("inf_floor_").toIntOrNull() ?: 1
            val generated = com.grimoire.dnd.data.InfiniteUnderdarkData.createFloorNode(floorNum, floorNum % 5 == 0)
            registerNode(generated)
        }

        val nextNode = nodes[destination] ?: throw IllegalStateException("Target $destination does not exist")

        if (nextNode.rewardGold > 0) {
            character.gold += nextNode.rewardGold
        }
        if (nextNode.rewardXp > 0) {
            character.xp += nextNode.rewardXp
        }

        return TransitionResult(
            nextNodeId = destination,
            narrativeLog = log,
            rollOutcome = rollOutcome,
            isCombatTriggered = nextNode.monsterIds.isNotEmpty(),
            monstersEncountered = nextNode.monsterIds,
            companionReactions = companionReactions
        )
    }
}
