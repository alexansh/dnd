package com.grimoire.dnd.engine

/**
 * 5E SRD 5.1 Character Creation Engine.
 * Handles class recommended base ability scores, racial ability score increases (ASI),
 * point buy calculation & validation, saving throw proficiencies, and class starting equipment.
 */
object CharacterCreationEngine {

    val AVAILABLE_RACES = listOf(
        "Human",
        "High Elf",
        "Mountain Dwarf",
        "Halfling",
        "Tiefling",
        "Dragonborn"
    )

    data class RacialBonus(
        val str: Int = 0,
        val dex: Int = 0,
        val con: Int = 0,
        val int: Int = 0,
        val wis: Int = 0,
        val cha: Int = 0,
        val summary: String = ""
    )

    fun getRacialBonus(race: String): RacialBonus = when (race) {
        "Human" -> RacialBonus(1, 1, 1, 1, 1, 1, "+1 to all Ability Scores")
        "High Elf" -> RacialBonus(dex = 2, int = 1, summary = "+2 DEX, +1 INT")
        "Mountain Dwarf" -> RacialBonus(str = 2, con = 2, summary = "+2 STR, +2 CON")
        "Halfling" -> RacialBonus(dex = 2, cha = 1, summary = "+2 DEX, +1 CHA")
        "Tiefling" -> RacialBonus(cha = 2, int = 1, summary = "+2 CHA, +1 INT")
        "Dragonborn" -> RacialBonus(str = 2, cha = 1, summary = "+2 STR, +1 CHA")
        else -> RacialBonus(1, 1, 1, 1, 1, 1, "+1 to all Ability Scores")
    }

    data class BaseAbilityArray(
        val str: Int,
        val dex: Int,
        val con: Int,
        val int: Int,
        val wis: Int,
        val cha: Int
    )

    /**
     * Standard 5E SRD 5.1 Recommended Ability Score Arrays (15, 14, 13, 12, 10, 8)
     * optimized for each class's primary and secondary combat/spellcasting dependencies.
     */
    fun getRecommendedBaseArray(characterClass: CharacterClass): BaseAbilityArray = when (characterClass) {
        CharacterClass.BARBARIAN -> BaseAbilityArray(str = 15, dex = 13, con = 14, int = 8, wis = 12, cha = 10)
        CharacterClass.BARD -> BaseAbilityArray(str = 8, dex = 14, con = 13, int = 10, wis = 12, cha = 15)
        CharacterClass.CLERIC -> BaseAbilityArray(str = 13, dex = 10, con = 14, int = 8, wis = 15, cha = 12)
        CharacterClass.DRUID -> BaseAbilityArray(str = 10, dex = 13, con = 14, int = 12, wis = 15, cha = 8)
        CharacterClass.FIGHTER -> BaseAbilityArray(str = 15, dex = 13, con = 14, int = 8, wis = 12, cha = 10)
        CharacterClass.MONK -> BaseAbilityArray(str = 12, dex = 15, con = 13, int = 10, wis = 14, cha = 8)
        CharacterClass.PALADIN -> BaseAbilityArray(str = 15, dex = 10, con = 13, int = 8, wis = 12, cha = 14)
        CharacterClass.RANGER -> BaseAbilityArray(str = 12, dex = 15, con = 13, int = 8, wis = 14, cha = 10)
        CharacterClass.ROGUE -> BaseAbilityArray(str = 8, dex = 15, con = 13, int = 14, wis = 12, cha = 10)
        CharacterClass.SORCERER -> BaseAbilityArray(str = 8, dex = 13, con = 14, int = 10, wis = 12, cha = 15)
        CharacterClass.WARLOCK -> BaseAbilityArray(str = 8, dex = 13, con = 14, int = 10, wis = 12, cha = 15)
        CharacterClass.WIZARD -> BaseAbilityArray(str = 8, dex = 13, con = 14, int = 15, wis = 12, cha = 10)
    }

    /**
     * 5E Standard Point Buy Cost Table (Base score 8..15).
     * 8: 0 pts, 9: 1, 10: 2, 11: 3, 12: 4, 13: 5, 14: 7, 15: 9
     */
    val POINT_BUY_BUDGET = 27

    fun getPointCost(score: Int): Int = when (score) {
        8 -> 0
        9 -> 1
        10 -> 2
        11 -> 3
        12 -> 4
        13 -> 5
        14 -> 7
        15 -> 9
        else -> if (score < 8) 0 else 9 + (score - 15) * 2
    }

    fun calculateTotalCost(array: BaseAbilityArray): Int {
        return getPointCost(array.str) +
                getPointCost(array.dex) +
                getPointCost(array.con) +
                getPointCost(array.int) +
                getPointCost(array.wis) +
                getPointCost(array.cha)
    }

    fun getRemainingPoints(array: BaseAbilityArray): Int {
        return POINT_BUY_BUDGET - calculateTotalCost(array)
    }

    fun canIncrease(currentScore: Int, remainingPoints: Int): Boolean {
        if (currentScore >= 15) return false
        val costNext = getPointCost(currentScore + 1) - getPointCost(currentScore)
        return remainingPoints >= costNext
    }

    fun canDecrease(currentScore: Int): Boolean {
        return currentScore > 8
    }

    fun getClassSavingThrows(characterClass: CharacterClass): List<Ability> = when (characterClass) {
        CharacterClass.BARBARIAN -> listOf(Ability.STR, Ability.CON)
        CharacterClass.BARD -> listOf(Ability.DEX, Ability.CHA)
        CharacterClass.CLERIC -> listOf(Ability.WIS, Ability.CHA)
        CharacterClass.DRUID -> listOf(Ability.INT, Ability.WIS)
        CharacterClass.FIGHTER -> listOf(Ability.STR, Ability.CON)
        CharacterClass.MONK -> listOf(Ability.STR, Ability.DEX)
        CharacterClass.PALADIN -> listOf(Ability.WIS, Ability.CHA)
        CharacterClass.RANGER -> listOf(Ability.STR, Ability.DEX)
        CharacterClass.ROGUE -> listOf(Ability.DEX, Ability.INT)
        CharacterClass.SORCERER -> listOf(Ability.CON, Ability.CHA)
        CharacterClass.WARLOCK -> listOf(Ability.WIS, Ability.CHA)
        CharacterClass.WIZARD -> listOf(Ability.INT, Ability.WIS)
    }

    fun getPrimaryAbility(characterClass: CharacterClass): String = when (characterClass) {
        CharacterClass.BARBARIAN -> "Strength (STR)"
        CharacterClass.BARD -> "Charisma (CHA)"
        CharacterClass.CLERIC -> "Wisdom (WIS)"
        CharacterClass.DRUID -> "Wisdom (WIS)"
        CharacterClass.FIGHTER -> "Strength or Dexterity"
        CharacterClass.MONK -> "Dexterity & Wisdom"
        CharacterClass.PALADIN -> "Strength & Charisma"
        CharacterClass.RANGER -> "Dexterity & Wisdom"
        CharacterClass.ROGUE -> "Dexterity (DEX)"
        CharacterClass.SORCERER -> "Charisma (CHA)"
        CharacterClass.WARLOCK -> "Charisma (CHA)"
        CharacterClass.WIZARD -> "Intelligence (INT)"
    }

    fun populateStartingGearAndSpells(character: Character) {
        character.inventory.clear()
        when (character.characterClass) {
            CharacterClass.BARBARIAN -> {
                character.inventory.add(Item("greataxe", "Greataxe", 1, isEquipped = true, damageFormula = "1d12+3", itemType = ItemType.WEAPON, description = "Massive double-bladed battle axe."))
                character.inventory.add(Item("handaxe", "Handaxe", 2, damageFormula = "1d6+3", itemType = ItemType.WEAPON, description = "Light throwing axe."))
            }
            CharacterClass.BARD -> {
                character.inventory.add(Item("rapier", "Rapier", 1, isEquipped = true, damageFormula = "1d8+2", itemType = ItemType.WEAPON, description = "Slender, sharply pointed thrusting sword."))
                character.inventory.add(Item("leather", "Leather Armor", 1, isEquipped = true, armorClassBonus = 11, itemType = ItemType.ARMOR, description = "Supple molded leather armor."))
                character.inventory.add(Item("lute", "Ornate Lute", 1, itemType = ItemType.VALUABLE, description = "Fine musical instrument for bardic inspiration."))
                character.spellSlots[1] = SpellSlotPool(1, 2, 2)
            }
            CharacterClass.CLERIC -> {
                character.inventory.add(Item("mace", "Iron Mace", 1, isEquipped = true, damageFormula = "1d6+2", itemType = ItemType.WEAPON, description = "Heavy flanged iron mace bearing holy runes."))
                character.inventory.add(Item("scale_mail", "Scale Mail", 1, isEquipped = true, armorClassBonus = 14, itemType = ItemType.ARMOR, description = "Overlapping bronze scales on leather."))
                character.inventory.add(Item("shield", "Shield", 1, isEquipped = true, armorClassBonus = 2, itemType = ItemType.SHIELD, description = "Wooden shield emblazoned with holy crest."))
                character.inventory.add(Item("holy_symbol", "Amulet Holy Symbol", 1, itemType = ItemType.QUEST, description = "Consecrated divine talisman."))
                character.spellSlots[1] = SpellSlotPool(1, 2, 2)
            }
            CharacterClass.DRUID -> {
                character.inventory.add(Item("scimitar", "Curved Scimitar", 1, isEquipped = true, damageFormula = "1d6+2", itemType = ItemType.WEAPON, description = "Swept bronze blade crafted without worked iron."))
                character.inventory.add(Item("leather", "Leather Armor", 1, isEquipped = true, armorClassBonus = 11, itemType = ItemType.ARMOR, description = "Supple molded hide armor."))
                character.inventory.add(Item("wood_shield", "Wooden Shield", 1, isEquipped = true, armorClassBonus = 2, itemType = ItemType.SHIELD, description = "Bark-hardened wooden shield."))
                character.spellSlots[1] = SpellSlotPool(1, 2, 2)
            }
            CharacterClass.FIGHTER -> {
                character.inventory.add(Item("longsword", "Steel Longsword", 1, isEquipped = true, damageFormula = "1d8+3", itemType = ItemType.WEAPON, description = "Tempered steel blade with a leather-wrapped grip."))
                character.inventory.add(Item("chain_mail", "Chain Mail", 1, isEquipped = true, armorClassBonus = 16, itemType = ItemType.ARMOR, description = "Heavy interlocking steel rings."))
                character.inventory.add(Item("shield", "Shield", 1, isEquipped = true, armorClassBonus = 2, itemType = ItemType.SHIELD, description = "Reinforced wooden heater shield."))
            }
            CharacterClass.MONK -> {
                character.inventory.add(Item("shortsword", "Shortsword", 1, isEquipped = true, damageFormula = "1d6+3", itemType = ItemType.WEAPON, description = "Straight-edged monastic blade."))
                character.inventory.add(Item("darts", "Steel Darts", 10, damageFormula = "1d4+3", itemType = ItemType.WEAPON, description = "Precision balanced throwing darts."))
            }
            CharacterClass.PALADIN -> {
                character.inventory.add(Item("longsword", "Steel Longsword", 1, isEquipped = true, damageFormula = "1d8+3", itemType = ItemType.WEAPON, description = "Tempered steel blade with a leather-wrapped grip."))
                character.inventory.add(Item("chain_mail", "Chain Mail", 1, isEquipped = true, armorClassBonus = 16, itemType = ItemType.ARMOR, description = "Heavy interlocking steel rings."))
                character.inventory.add(Item("shield", "Shield", 1, isEquipped = true, armorClassBonus = 2, itemType = ItemType.SHIELD, description = "Reinforced heater shield with oath engraving."))
                character.inventory.add(Item("holy_symbol", "Holy Symbol", 1, itemType = ItemType.QUEST, description = "Talisman of Devotion."))
            }
            CharacterClass.RANGER -> {
                character.inventory.add(Item("longbow", "Longbow", 1, isEquipped = true, damageFormula = "1d8+3", itemType = ItemType.WEAPON, description = "Yew longbow with 20 arrows."))
                character.inventory.add(Item("shortsword", "Shortsword", 2, damageFormula = "1d6+3", itemType = ItemType.WEAPON, description = "Paired hunter's shortswords."))
                character.inventory.add(Item("scale_mail", "Scale Mail", 1, isEquipped = true, armorClassBonus = 14, itemType = ItemType.ARMOR, description = "Mottled scale mail for forest camouflage."))
            }
            CharacterClass.ROGUE -> {
                character.inventory.add(Item("rapier", "Rapier", 1, isEquipped = true, damageFormula = "1d8+3", itemType = ItemType.WEAPON, description = "Finesse blade ideal for sneak attacks."))
                character.inventory.add(Item("leather", "Leather Armor", 1, isEquipped = true, armorClassBonus = 11, itemType = ItemType.ARMOR, description = "Supple molded leather armor."))
                character.inventory.add(Item("dagger", "Daggers", 2, damageFormula = "1d4+3", itemType = ItemType.WEAPON, description = "Concealed steel daggers."))
                character.inventory.add(Item("thieves_tools", "Thieves' Tools", 1, itemType = ItemType.VALUABLE, description = "Lockpicks, small tension wrenches, and miniature mirrors."))
            }
            CharacterClass.SORCERER -> {
                character.inventory.add(Item("light_crossbow", "Light Crossbow", 1, isEquipped = true, damageFormula = "1d8+2", itemType = ItemType.WEAPON, description = "Spannable crossbow with 20 bolts."))
                character.inventory.add(Item("dagger", "Dagger", 2, damageFormula = "1d4+2", itemType = ItemType.WEAPON, description = "Curved ceremonial dagger."))
                character.inventory.add(Item("arcane_focus", "Crystal Arcane Focus", 1, itemType = ItemType.QUEST, description = "Resonant bloodstone channeling wild magic."))
                character.spellSlots[1] = SpellSlotPool(1, 2, 2)
            }
            CharacterClass.WARLOCK -> {
                character.inventory.add(Item("light_crossbow", "Light Crossbow", 1, isEquipped = true, damageFormula = "1d8+2", itemType = ItemType.WEAPON, description = "Crossbow inscribed with eldritch runes."))
                character.inventory.add(Item("leather", "Leather Armor", 1, isEquipped = true, armorClassBonus = 11, itemType = ItemType.ARMOR, description = "Supple molded leather armor."))
                character.inventory.add(Item("arcane_focus", "Obsidian Arcane Focus", 1, itemType = ItemType.QUEST, description = "Tether to the patron realm."))
                character.spellSlots[1] = SpellSlotPool(1, 1, 1) // Pact magic slot
            }
            CharacterClass.WIZARD -> {
                character.inventory.add(Item("staff", "Arcane Quarterstaff", 1, isEquipped = true, damageFormula = "1d6", itemType = ItemType.WEAPON, description = "Carved ash staff crowned with an amethyst focus."))
                character.inventory.add(Item("spellbook", "Spellbook", 1, itemType = ItemType.QUEST, description = "Leather-bound tome containing learned incantations."))
                character.spellSlots[1] = SpellSlotPool(1, 2, 2)
            }
        }
        // Universal starter supplies
        character.inventory.add(Item("potion_starter", "Potion of Healing", 2, itemType = ItemType.POTION, healFormula = "2d4+2", description = "Restores 2d4+2 hit points when consumed."))
        character.inventory.add(Item("torch", "Torch", 2, itemType = ItemType.VALUABLE, description = "Illuminates dark dungeons."))
    }
}
