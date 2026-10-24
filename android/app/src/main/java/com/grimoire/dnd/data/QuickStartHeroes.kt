package com.grimoire.dnd.data

import com.grimoire.dnd.engine.*

data class QuickStartHero(
    val title: String,
    val subtitle: String,
    val tagline: String,
    val character: Character
)

object QuickStartHeroes {
    fun getStarterHeroes(): List<QuickStartHero> = listOf(
        QuickStartHero(
            title = "Sir Valen the Stalwart",
            subtitle = "Human Fighter • Level 1",
            tagline = "Durable frontline warrior who protects companions with heavy steel and unyielding courage.",
            character = Character(
                id = "hero_valen",
                name = "Sir Valen",
                race = "Human",
                characterClass = CharacterClass.FIGHTER,
                subclass = "Champion",
                level = 1,
                strength = 16,
                dexterity = 12,
                constitution = 15,
                intelligence = 10,
                wisdom = 12,
                charisma = 11,
                hpCurrent = 12,
                hpMax = 12,
                gold = 15,
                savingThrowProficiencies = mutableListOf(Ability.STR, Ability.CON),
                skillProficiencies = mutableListOf(Skill.ATHLETICS, Skill.INTIMIDATION, Skill.SURVIVAL)
            ).apply {
                inventory.add(Item("longsword", "Tempered Longsword", 1, isEquipped = true, damageFormula = "1d8+3", itemType = ItemType.WEAPON, description = "A polished knight's blade."))
                inventory.add(Item("chain", "Chain Mail", 1, isEquipped = true, armorClassBonus = 16, itemType = ItemType.ARMOR, description = "Interlocking steel rings."))
                inventory.add(Item("shield", "Heraldic Shield", 1, isEquipped = true, armorClassBonus = 2, itemType = ItemType.SHIELD, description = "Heater shield painted with a roaring lion."))
                inventory.add(Item("pot_valen", "Potion of Healing", 2, itemType = ItemType.POTION, healFormula = "2d4+2", description = "Restores 2d4+2 HP."))
            }
        ),
        QuickStartHero(
            title = "Lyra the Shadow-Dancer",
            subtitle = "Elf Rogue • Level 1",
            tagline = "Agile shadow specialist who picks locks, spots traps, and strikes deadly sneak attacks.",
            character = Character(
                id = "hero_lyra",
                name = "Lyra",
                race = "Elf",
                characterClass = CharacterClass.ROGUE,
                subclass = "Thief",
                level = 1,
                strength = 10,
                dexterity = 17,
                constitution = 13,
                intelligence = 13,
                wisdom = 12,
                charisma = 14,
                hpCurrent = 9,
                hpMax = 9,
                gold = 25,
                savingThrowProficiencies = mutableListOf(Ability.DEX, Ability.INT),
                skillProficiencies = mutableListOf(Skill.STEALTH, Skill.SLEIGHT_OF_HAND, Skill.ACROBATICS, Skill.DECEPTION)
            ).apply {
                inventory.add(Item("dagger_lyra", "Shadow Stiletto", 1, isEquipped = true, damageFormula = "1d4+3", itemType = ItemType.WEAPON, description = "A blackened blade balanced for sneak attacks."))
                inventory.add(Item("shortbow_lyra", "Recurve Shortbow", 1, isEquipped = false, damageFormula = "1d6+3", itemType = ItemType.WEAPON, description = "Silenced elven shortbow."))
                inventory.add(Item("leather_lyra", "Studded Leather", 1, isEquipped = true, armorClassBonus = 12, itemType = ItemType.ARMOR, description = "Supple leather reinforced with dark iron studs."))
                inventory.add(Item("pot_lyra", "Potion of Healing", 2, itemType = ItemType.POTION, healFormula = "2d4+2", description = "Restores 2d4+2 HP."))
            }
        ),
        QuickStartHero(
            title = "Rowan the Sun-Caller",
            subtitle = "Dwarf Cleric • Level 1",
            tagline = "Devoted divine healer who channels radiant light, mends wounds, and smites undead.",
            character = Character(
                id = "hero_rowan",
                name = "Rowan Stoneguard",
                race = "Dwarf",
                characterClass = CharacterClass.CLERIC,
                subclass = "Life Domain",
                level = 1,
                strength = 14,
                dexterity = 10,
                constitution = 16,
                intelligence = 10,
                wisdom = 16,
                charisma = 12,
                hpCurrent = 11,
                hpMax = 11,
                gold = 20,
                savingThrowProficiencies = mutableListOf(Ability.WIS, Ability.CHA),
                skillProficiencies = mutableListOf(Skill.INSIGHT, Skill.RELIGION, Skill.MEDICINE)
            ).apply {
                inventory.add(Item("warhammer_rowan", "Sanctified Warhammer", 1, isEquipped = true, damageFormula = "1d8+2", itemType = ItemType.WEAPON, description = "A silver-plated dwarven hammer."))
                inventory.add(Item("scale_rowan", "Scale Mail", 1, isEquipped = true, armorClassBonus = 14, itemType = ItemType.ARMOR, description = "Heavy overlapping bronze scales."))
                inventory.add(Item("shield_rowan", "Sunburst Shield", 1, isEquipped = true, armorClassBonus = 2, itemType = ItemType.SHIELD, description = "Shield bearing the golden sunrise crest."))
                inventory.add(Item("pot_rowan", "Potion of Healing", 2, itemType = ItemType.POTION, healFormula = "2d4+2", description = "Restores 2d4+2 HP."))
                spellSlots[1] = SpellSlotPool(1, 2, 2)
            }
        ),
        QuickStartHero(
            title = "Morrigan the Pyromancer",
            subtitle = "Tiefling Wizard • Level 1",
            tagline = "Master of the arcane arts who incinerates enemies with explosive spells from safe distance.",
            character = Character(
                id = "hero_morrigan",
                name = "Morrigan",
                race = "Tiefling",
                characterClass = CharacterClass.WIZARD,
                subclass = "School of Evocation",
                level = 1,
                strength = 8,
                dexterity = 14,
                constitution = 13,
                intelligence = 17,
                wisdom = 12,
                charisma = 12,
                hpCurrent = 7,
                hpMax = 7,
                gold = 35,
                savingThrowProficiencies = mutableListOf(Ability.INT, Ability.WIS),
                skillProficiencies = mutableListOf(Skill.ARCANA, Skill.INVESTIGATION, Skill.HISTORY)
            ).apply {
                inventory.add(Item("staff_morrigan", "Fire-Quartz Staff", 1, isEquipped = true, damageFormula = "1d6", itemType = ItemType.WEAPON, description = "Ash staff crowned with an ember crystal."))
                inventory.add(Item("robe_morrigan", "Scholar's Robes", 1, isEquipped = true, armorClassBonus = 10, itemType = ItemType.ARMOR, description = "Embroidered silk robes lined with warding thread."))
                inventory.add(Item("pot_morrigan", "Potion of Healing", 2, itemType = ItemType.POTION, healFormula = "2d4+2", description = "Restores 2d4+2 HP."))
                spellSlots[1] = SpellSlotPool(1, 2, 2)
            }
        )
    )
}
