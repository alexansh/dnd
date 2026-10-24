package com.grimoire.dnd.engine

import kotlin.math.max

enum class CharacterClass(val label: String, val hitDie: Int, val primarySubclass: String) {
    BARBARIAN("Barbarian", 12, "Path of the Berserker"),
    BARD("Bard", 8, "College of Lore"),
    CLERIC("Cleric", 8, "Life Domain"),
    DRUID("Druid", 8, "Circle of the Land"),
    FIGHTER("Fighter", 10, "Champion"),
    MONK("Monk", 8, "Way of the Open Hand"),
    PALADIN("Paladin", 10, "Oath of Devotion"),
    RANGER("Ranger", 10, "Hunter"),
    ROGUE("Rogue", 8, "Thief"),
    SORCERER("Sorcerer", 6, "Draconic Bloodline"),
    WARLOCK("Warlock", 8, "The Fiend"),
    WIZARD("Wizard", 6, "School of Evocation")
}

enum class ItemType(val label: String) {
    WEAPON("Weapon"),
    ARMOR("Armor"),
    SHIELD("Shield"),
    POTION("Potion"),
    SCROLL("Scroll"),
    VALUABLE("Valuable"),
    QUEST("Quest Item")
}

data class Item(
    val id: String,
    val name: String,
    var quantity: Int = 1,
    var isEquipped: Boolean = false,
    val armorClassBonus: Int = 0,
    val damageFormula: String? = null,
    val itemType: ItemType = ItemType.VALUABLE,
    val healFormula: String? = null,
    val valueGp: Int = 10,
    val weightLbs: Double = 1.0,
    val description: String = ""
)

data class LevelUpResult(
    val newLevel: Int,
    val hpGained: Int,
    val newHpMax: Int,
    val featuresUnlocked: List<String>
)

data class SpellSlotPool(
    val level: Int,
    var current: Int,
    val maximum: Int
)

data class Character(
    val id: String,
    var name: String,
    val race: String,
    val characterClass: CharacterClass,
    val subclass: String? = null,
    var level: Int = 1,
    var xp: Int = 0,

    var strength: Int = 10,
    var dexterity: Int = 10,
    var constitution: Int = 10,
    var intelligence: Int = 10,
    var wisdom: Int = 10,
    var charisma: Int = 10,

    var hpCurrent: Int = 10,
    var hpMax: Int = 10,
    var tempHp: Int = 0,
    var hitDiceCurrent: Int = 1,
    var hitDiceMax: Int = 1,

    var speedFeet: Int = 30,
    var inspiration: Boolean = false,
    var exhaustionLevel: ExhaustionLevel = ExhaustionLevel.NONE,

    val savingThrowProficiencies: MutableList<Ability> = mutableListOf(),
    val skillProficiencies: MutableList<Skill> = mutableListOf(),

    val inventory: MutableList<Item> = mutableListOf(),
    var gold: Int = 10,
    var silver: Int = 0,
    var copper: Int = 0,

    val spellSlots: MutableMap<Int, SpellSlotPool> = mutableMapOf(),
    val activeConditions: MutableList<String> = mutableListOf(),
    var deathSaves: DeathSaveTracker = DeathSaveTracker()
) {
    val proficiencyBonus: Int
        get() = calculateProficiencyBonus(level)

    fun getAbilityScore(ability: Ability): Int = when (ability) {
        Ability.STR -> strength
        Ability.DEX -> dexterity
        Ability.CON -> constitution
        Ability.INT -> intelligence
        Ability.WIS -> wisdom
        Ability.CHA -> charisma
    }

    fun getAbilityModifier(ability: Ability): Int =
        calculateAbilityModifier(getAbilityScore(ability))

    fun getSavingThrowModifier(ability: Ability): Int {
        val base = getAbilityModifier(ability)
        return if (savingThrowProficiencies.contains(ability)) base + proficiencyBonus else base
    }

    fun getSkillModifier(skill: Skill): Int {
        val base = getAbilityModifier(skill.ability)
        return if (skillProficiencies.contains(skill)) base + proficiencyBonus else base
    }

    val armorClass: Int
        get() {
            val dexMod = getAbilityModifier(Ability.DEX)
            var shieldBonus = 0
            var equippedArmor: Item? = null

            for (item in inventory) {
                if (item.isEquipped) {
                    if (item.itemType == ItemType.SHIELD || item.name.contains("Shield", ignoreCase = true)) {
                        shieldBonus += if (item.armorClassBonus > 0) item.armorClassBonus else 2
                    } else if (item.itemType == ItemType.ARMOR || item.armorClassBonus >= 10) {
                        equippedArmor = item
                    }
                }
            }

            if (equippedArmor == null) {
                if (characterClass == CharacterClass.BARBARIAN) {
                    val conMod = getAbilityModifier(Ability.CON)
                    return 10 + dexMod + conMod + shieldBonus
                }
                if (characterClass == CharacterClass.MONK) {
                    val wisMod = getAbilityModifier(Ability.WIS)
                    return 10 + dexMod + wisMod
                }
                return 10 + dexMod + shieldBonus
            }

            val baseArmor = equippedArmor.armorClassBonus
            return when {
                baseArmor >= 16 -> baseArmor + shieldBonus
                baseArmor in 13..15 -> baseArmor + minOf(2, dexMod) + shieldBonus
                else -> baseArmor + dexMod + shieldBonus
            }
        }

    fun applyDamage(damage: Int): Int {
        if (damage <= 0) return 0
        var effectiveDamage = damage
        if (tempHp > 0) {
            val absorbed = minOf(tempHp, effectiveDamage)
            tempHp -= absorbed
            effectiveDamage -= absorbed
        }

        val hpLost = minOf(hpCurrent, effectiveDamage)
        hpCurrent = maxOf(0, hpCurrent - effectiveDamage)

        if (hpCurrent == 0) {
            deathSaves = DeathSaveTracker()
            if (!activeConditions.contains("Unconscious")) {
                activeConditions.add("Unconscious")
            }
        }
        return hpLost
    }

    fun heal(amount: Int): Int {
        if (amount <= 0 || hpCurrent >= hpMax) return 0
        val oldHp = hpCurrent
        hpCurrent = minOf(hpMax, hpCurrent + amount)
        val actualHealed = hpCurrent - oldHp

        if (oldHp == 0 && hpCurrent > 0) {
            activeConditions.remove("Unconscious")
            deathSaves = DeathSaveTracker()
        }
        return actualHealed
    }

    fun performShortRest(hitDiceToSpend: Int, diceEngine: DiceEngine): Map<String, Int> {
        if (hitDiceToSpend <= 0 || hitDiceCurrent <= 0) return mapOf("spent" to 0, "healed" to 0)
        val toSpend = minOf(hitDiceToSpend, hitDiceCurrent)
        val sides = characterClass.hitDie
        val conMod = getAbilityModifier(Ability.CON)

        var totalHealed = 0
        for (i in 0 until toSpend) {
            val roll = diceEngine.rollDie(sides)
            val healAmount = maxOf(1, roll + conMod)
            totalHealed += heal(healAmount)
            hitDiceCurrent--
        }

        if (characterClass == CharacterClass.WARLOCK) {
            spellSlots.values.forEach { it.current = it.maximum }
        }
        return mapOf("spent" to toSpend, "healed" to totalHealed)
    }

    fun performLongRest(): Map<String, Int> {
        val oldHp = hpCurrent
        hpCurrent = hpMax
        val healed = hpMax - oldHp
        tempHp = 0

        val recoveredDice = maxOf(1, hitDiceMax / 2)
        val oldHd = hitDiceCurrent
        hitDiceCurrent = minOf(hitDiceMax, hitDiceCurrent + recoveredDice)
        val hdRegained = hitDiceCurrent - oldHd

        spellSlots.values.forEach { it.current = it.maximum }

        if (exhaustionLevel > ExhaustionLevel.NONE) {
            val prevTier = exhaustionLevel.tier - 1
            exhaustionLevel = ExhaustionLevel.values().firstOrNull { it.tier == prevTier } ?: ExhaustionLevel.NONE
        }

        activeConditions.remove("Unconscious")
        deathSaves = DeathSaveTracker()

        return mapOf("healed" to healed, "hdRegained" to hdRegained)
    }

    val xpForNextLevel: Int
        get() = when (level) {
            1 -> 300
            2 -> 900
            3 -> 2700
            4 -> 6500
            5 -> 14000
            else -> level * 10000
        }

    fun canLevelUp(): Boolean = xp >= xpForNextLevel && level < 20

    fun levelUp(diceEngine: DiceEngine, chosenSubclass: String? = null): LevelUpResult {
        if (level >= 20) return LevelUpResult(level, 0, hpMax, emptyList())
        level++
        val conMod = getAbilityModifier(Ability.CON)
        val roll = diceEngine.rollDie(characterClass.hitDie)
        val avg = (characterClass.hitDie / 2) + 1
        val baseGain = maxOf(roll, avg)
        val hpGained = maxOf(1, baseGain + conMod)
        hpMax += hpGained
        hpCurrent += hpGained
        hitDiceMax++
        hitDiceCurrent++

        val unlocked = mutableListOf<String>()
        unlocked.add("Hit Points increased by +$hpGained (New Max: $hpMax HP)")
        unlocked.add("Hit Dice increased to $hitDiceMax d${characterClass.hitDie}")

        if (level == 2) {
            when (characterClass) {
                CharacterClass.FIGHTER -> unlocked.add("Action Surge (Take 1 additional action per short rest)")
                CharacterClass.WIZARD -> unlocked.add("Arcane Recovery & Extra 1st-level Spell Slot")
                CharacterClass.ROGUE -> unlocked.add("Cunning Action (Dash, Disengage, or Hide as Bonus Action)")
                CharacterClass.CLERIC -> unlocked.add("Channel Divinity: Turn Undead & Preserve Life")
                CharacterClass.PALADIN -> unlocked.add("Divine Smite & 1st-level Spell Slots")
                CharacterClass.DRUID -> unlocked.add("Wild Shape (Transform into beasts)")
                CharacterClass.BARBARIAN -> unlocked.add("Reckless Attack & Danger Sense")
                else -> unlocked.add("Level 2 Class Archetype Features")
            }
        } else if (level == 3) {
            val sub = chosenSubclass ?: characterClass.primarySubclass
            unlocked.add("Specialization Archetype: $sub")
            if (characterClass in listOf(CharacterClass.WIZARD, CharacterClass.CLERIC, CharacterClass.SORCERER, CharacterClass.DRUID, CharacterClass.BARD)) {
                val slot2 = spellSlots.getOrPut(2) { SpellSlotPool(2, 0, 0) }
                spellSlots[2] = SpellSlotPool(2, slot2.maximum + 2, slot2.maximum + 2)
                unlocked.add("2nd-Level Spell Slots Unlocked (2 Slots)")
            }
        }

        return LevelUpResult(level, hpGained, hpMax, unlocked)
    }

    fun equipItem(itemId: String): Boolean {
        val target = inventory.firstOrNull { it.id == itemId } ?: return false
        when (target.itemType) {
            ItemType.ARMOR -> {
                inventory.filter { it.itemType == ItemType.ARMOR }.forEach { it.isEquipped = false }
                target.isEquipped = true
            }
            ItemType.SHIELD -> {
                inventory.filter { it.itemType == ItemType.SHIELD }.forEach { it.isEquipped = false }
                target.isEquipped = true
            }
            ItemType.WEAPON -> {
                inventory.filter { it.itemType == ItemType.WEAPON }.forEach { it.isEquipped = false }
                target.isEquipped = true
            }
            else -> {
                target.isEquipped = !target.isEquipped
            }
        }
        return true
    }

    fun unequipItem(itemId: String): Boolean {
        val target = inventory.firstOrNull { it.id == itemId } ?: return false
        target.isEquipped = false
        return true
    }

    fun useItem(itemId: String, diceEngine: DiceEngine): String {
        val item = inventory.firstOrNull { it.id == itemId } ?: return "Item not found."
        if (item.itemType == ItemType.POTION && item.healFormula != null) {
            val roll = diceEngine.parseAndRoll(item.healFormula)
            val healed = heal(roll.total)
            if (item.quantity > 1) {
                item.quantity--
            } else {
                inventory.remove(item)
            }
            return "Consumed ${item.name}: Restored $healed HP! (${roll.summary()})"
        }
        return "Used ${item.name}."
    }

    fun addItem(newItem: Item) {
        val existing = inventory.firstOrNull { it.id == newItem.id }
        if (existing != null && (newItem.itemType == ItemType.POTION || newItem.itemType == ItemType.VALUABLE)) {
            existing.quantity += newItem.quantity
        } else {
            inventory.add(newItem)
        }
    }
}
