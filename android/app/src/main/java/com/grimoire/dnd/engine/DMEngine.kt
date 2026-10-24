package com.grimoire.dnd.engine

data class RuleExplanation(
    val title: String,
    val ruleCitation: String,
    val breakdownText: String,
    val adviceText: String
)

data class DMWhisper(
    val insightType: String, // "Tactical", "Lore", "Warning", "Rule Tip"
    val message: String
)

class DMEngine(
    private val diceEngine: DiceEngine
) {
    fun evaluateSkillCheck(
        character: Character,
        skill: Skill,
        dc: Int,
        selectedRollType: RollType = RollType.NORMAL
    ): Pair<RollOutcome, RuleExplanation> {
        val skillMod = character.getSkillModifier(skill)
        val roll = diceEngine.rollD20(modifier = skillMod, rollType = selectedRollType)
        val isSuccess = (roll.total >= dc || roll.isCriticalHit) && !roll.isCriticalMiss

        val ability = skill.ability
        val abilityScore = character.getAbilityScore(ability)
        val abilityMod = character.getAbilityModifier(ability)
        val isProficient = character.skillProficiencies.contains(skill)
        val profBonus = if (isProficient) character.proficiencyBonus else 0

        val sign = if (abilityMod >= 0) "+$abilityMod" else "$abilityMod"
        val profText = if (isProficient) " + Prof (+${profBonus})" else ""
        val keptDie = roll.diceResults.firstOrNull { !it.isDiscarded }?.value ?: (roll.total - skillMod)
        val rollBreakdown = "d20 Roll ($keptDie) + ${ability.label} ($sign)$profText = ${roll.total} vs DC $dc"

        val ruleCitation = "D&D 5E SRD 5.1 — Chapter 7: Using Ability Scores"
        val adviceText = when {
            roll.isCriticalHit -> "NATURAL 20! A legendary triumph. In 5E, natural twenties represent the pinnacle of mortal capability."
            roll.isCriticalMiss -> "NATURAL 1! Complete catastrophe. Even master adventurers occasionally suffer the cruel whims of chaos."
            isSuccess -> "SUCCESS! You overcame the Difficulty Class (DC $dc) through superior training and quick wits."
            else -> "FAILURE. You fell short of DC $dc. In D&D, failure doesn't mean game over—it complicates the story and drives you into danger!"
        }

        val explanation = RuleExplanation(
            title = if (isSuccess) "CHECK PASSED: ${skill.label}" else "CHECK FAILED: ${skill.label}",
            ruleCitation = ruleCitation,
            breakdownText = rollBreakdown,
            adviceText = adviceText
        )

        return Pair(roll, explanation)
    }

    fun generateDMWhisper(node: AdventureNode, character: Character): DMWhisper {
        // Class-tailored sensory perception
        return when {
            character.characterClass == CharacterClass.ROGUE && node.sceneVisualTag == "crypt" -> {
                DMWhisper(
                    "Tactical Rogue Insight",
                    "Your keen rogue eyes notice subtle scrapes on the flagstones. The ancient pressure plates here can be bypassed if you watch your footing."
                )
            }
            character.characterClass == CharacterClass.WIZARD && (node.sceneVisualTag == "sanctum" || node.sceneVisualTag == "crypt") -> {
                DMWhisper(
                    "Arcane Scholar Insight",
                    "A faint ozone tingle prickles your skin. There is lingering transmutation magic woven into the architecture of this room."
                )
            }
            character.characterClass == CharacterClass.CLERIC || character.characterClass == CharacterClass.PALADIN -> {
                DMWhisper(
                    "Divine Smite Sense",
                    "Your holy talisman warms against your chest. The veil between life and death is thin here—prepare your prayers."
                )
            }
            character.characterClass == CharacterClass.BARBARIAN || character.characterClass == CharacterClass.FIGHTER -> {
                DMWhisper(
                    "Martial Veteran Instinct",
                    "You instinctively gauge room choke points and sightlines. Positioning yourself near cover will give you +2 Armor Class in battle."
                )
            }
            node.isRestArea -> {
                DMWhisper(
                    "Tabletop Rule Tip",
                    "This is a Sanctuary. In D&D 5E, taking a Short Rest lets you roll your Hit Dice to heal without wasting potions!"
                )
            }
            node.monsterIds.isNotEmpty() -> {
                DMWhisper(
                    "Combat Advisory",
                    "Hostiles ahead! High Ground grants Advantage (+5 effective roll) to ranged attacks. Protect your spellcasters!"
                )
            }
            else -> {
                DMWhisper(
                    "Dungeon Master's Guidance",
                    "Every choice shapes how companions judge you. Heroic deeds earn respect, while cunning choices open secret passages."
                )
            }
        }
    }

    fun calculateTensionLevel(character: Character, currentNode: AdventureNode): String {
        val hpPct = character.hpCurrent.toFloat() / character.hpMax.toFloat()
        return when {
            hpPct <= 0.3f -> "CRITICAL DANGER: Heart pounding, blood dripping. One blow could spell doom."
            currentNode.monsterIds.isNotEmpty() -> "HIGH TENSION: Blades drawn, adrenaline surging. Steel is about to meet flesh."
            currentNode.isRestArea -> "RESPITE: Hearthlight flickers warmly. The party catches their breath."
            else -> "EXPLORING: Shadows whisper across ancient stone. The unknown awaits."
        }
    }
}
