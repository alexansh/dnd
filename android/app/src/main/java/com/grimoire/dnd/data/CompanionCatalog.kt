package com.grimoire.dnd.data

import com.grimoire.dnd.engine.*

object CompanionCatalog {
    fun createStarterCompanions(): List<Companion> = listOf(
        // Companion 1: Elira Moonshadow (Rogue / Arcane Trickster)
        Companion(
            id = "comp_elira",
            name = "Elira Moonshadow",
            title = "Exiled Arcane Thief",
            avatarTag = "EL",
            moralAlignment = MoralAlignment.CHAOTIC_GOOD,
            approvalScore = 50,
            backstory = "Cast out of the Azure Spire for stealing forbidden spell scrolls, Elira seeks vengeance against the Corrupt Magisters.",
            character = Character(
                id = "char_elira",
                name = "Elira Moonshadow",
                race = "High Elf",
                characterClass = CharacterClass.ROGUE,
                subclass = "Thief",
                level = 1,
                strength = 10,
                dexterity = 16,
                constitution = 14,
                intelligence = 14,
                wisdom = 12,
                charisma = 10,
                hpCurrent = 10,
                hpMax = 10,
                gold = 25,
                savingThrowProficiencies = mutableListOf(Ability.DEX, Ability.INT),
                skillProficiencies = mutableListOf(Skill.STEALTH, Skill.SLEIGHT_OF_HAND, Skill.ACROBATICS, Skill.DECEPTION)
            ).apply {
                inventory.add(Item("elira_dagger", "Silver Kris Dagger", 1, isEquipped = true, damageFormula = "1d4+3", itemType = ItemType.WEAPON, description = "A sleek stiletto with a silvered edge."))
                inventory.add(Item("elira_bow", "Shortbow", 1, isEquipped = false, damageFormula = "1d6+3", itemType = ItemType.WEAPON, description = "Elven recurve bow."))
                inventory.add(Item("leather", "Studded Leather", 1, isEquipped = true, armorClassBonus = 12, itemType = ItemType.ARMOR, description = "Hardened leather reinforced with rivets."))
                inventory.add(Item("potion_heal_elira", "Potion of Healing", 2, itemType = ItemType.POTION, healFormula = "2d4+2", description = "A ruby vial restoring 2d4+2 hit points."))
            }
        ),

        // Companion 2: Thorgar Stonebreaker (Barbarian / Path of the Berserker)
        Companion(
            id = "comp_thorgar",
            name = "Thorgar Ironbreaker",
            title = "Last Bastion Guard",
            avatarTag = "TH",
            moralAlignment = MoralAlignment.TRUE_NEUTRAL,
            approvalScore = 50,
            backstory = "A stoic veteran whose mountain clan was slaughtered during the Caldera siege. He respects strength, honor, and decisive action.",
            character = Character(
                id = "char_thorgar",
                name = "Thorgar Ironbreaker",
                race = "Mountain Dwarf",
                characterClass = CharacterClass.BARBARIAN,
                subclass = "Path of the Berserker",
                level = 1,
                strength = 16,
                dexterity = 12,
                constitution = 16,
                intelligence = 8,
                wisdom = 12,
                charisma = 9,
                hpCurrent = 15,
                hpMax = 15,
                gold = 10,
                savingThrowProficiencies = mutableListOf(Ability.STR, Ability.CON),
                skillProficiencies = mutableListOf(Skill.ATHLETICS, Skill.INTIMIDATION, Skill.SURVIVAL)
            ).apply {
                inventory.add(Item("thorgar_greataxe", "Dwarven Greataxe", 1, isEquipped = true, damageFormula = "1d12+3", itemType = ItemType.WEAPON, description = "A heavy double-bitted battle axe."))
                inventory.add(Item("potion_heal_thorgar", "Potion of Healing", 1, itemType = ItemType.POTION, healFormula = "2d4+2", description = "A crimson draft smelling of crushed elderberries."))
            }
        ),

        // Companion 3: Vespera the Vengeful (Cleric / Life & Shadow Domain)
        Companion(
            id = "comp_vespera",
            name = "Vespera of the Pale Moon",
            title = "Acolyte of the Veil",
            avatarTag = "VE",
            moralAlignment = MoralAlignment.LAWFUL_EVIL,
            approvalScore = 45,
            backstory = "Devoted to an enigmatic deity of night and transitions. She believes true order requires pruning the weak, but fiercely protects allies.",
            character = Character(
                id = "char_vespera",
                name = "Vespera",
                race = "Tiefling",
                characterClass = CharacterClass.CLERIC,
                subclass = "Life Domain",
                level = 1,
                strength = 12,
                dexterity = 10,
                constitution = 14,
                intelligence = 10,
                wisdom = 16,
                charisma = 14,
                hpCurrent = 10,
                hpMax = 10,
                gold = 30,
                savingThrowProficiencies = mutableListOf(Ability.WIS, Ability.CHA),
                skillProficiencies = mutableListOf(Skill.RELIGION, Skill.INSIGHT, Skill.PERSUASION)
            ).apply {
                inventory.add(Item("vespera_mace", "Morningstar", 1, isEquipped = true, damageFormula = "1d8+1", itemType = ItemType.WEAPON, description = "A spiked flail radiating faint dim light."))
                inventory.add(Item("vespera_chain", "Scale Mail", 1, isEquipped = true, armorClassBonus = 14, itemType = ItemType.ARMOR, description = "Overlapping bronze scales on supple leather."))
                inventory.add(Item("vespera_shield", "Shield of the Veil", 1, isEquipped = true, armorClassBonus = 2, itemType = ItemType.SHIELD, description = "A heater shield etched with the crescent moon."))
                inventory.add(Item("potion_heal_vespera", "Potion of Healing", 2, itemType = ItemType.POTION, healFormula = "2d4+2", description = "A blessed restorative phial."))
                spellSlots[1] = SpellSlotPool(1, 2, 2)
            }
        )
    )
}
