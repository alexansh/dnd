package com.grimoire.dnd.data

import com.grimoire.dnd.R

data class MonsterLore(
    val id: String,
    val name: String,
    val type: String,
    val challengeRating: String,
    val armorClass: Int,
    val hitPoints: String,
    val speed: String,
    val description: String,
    val actions: List<String>,
    val imageRes: Int = R.drawable.ic_dragon
)

object MonsterCatalog {
    val monsters = listOf(
        MonsterLore(
            id = "red_dragon",
            name = "Young Red Dragon",
            type = "Large Dragon, Chaotic Evil",
            challengeRating = "CR 10 (5,900 XP)",
            armorClass = 18,
            hitPoints = "178 (17d10 + 85)",
            speed = "40 ft., climb 40 ft., fly 80 ft.",
            description = "The most covetous of the true dragons, red dragons tirelessly seek to increase their treasure hoards. They are exceptionally vain, whose conceit is reflected in their haughty bearing and fiery wrath.",
            actions = listOf(
                "Multiattack: Makes three attacks: one with its bite and two with its claws.",
                "Bite: Melee Weapon Attack: +10 to hit, reach 10 ft., one target. Hit: 17 (2d10 + 6) piercing damage plus 3 (1d6) fire damage.",
                "Claw: Melee Weapon Attack: +10 to hit, reach 5 ft., one target. Hit: 13 (2d6 + 6) slashing damage.",
                "Fire Breath (Recharge 5–6): The dragon exhales fire in a 30-foot cone. Each creature must make a DC 17 Dexterity saving throw, taking 56 (16d6) fire damage on a failure, or half as much on a success."
            )
        ),
        MonsterLore(
            id = "goblin",
            name = "Goblin Skirmisher",
            type = "Small Humanoid (Goblinoid), Neutral Evil",
            challengeRating = "CR 1/4 (50 XP)",
            armorClass = 15,
            hitPoints = "7 (2d6)",
            speed = "30 ft.",
            description = "Black-hearted, gatherers of shiny baubles who infest ruins and subterranean caves. They rely on sheer numbers, ambush, and nimble escape.",
            actions = listOf(
                "Nimble Escape: The goblin can take the Disengage or Hide action as a bonus action on each of its turns.",
                "Scimitar: Melee Weapon Attack: +4 to hit, reach 5 ft., one target. Hit: 5 (1d6 + 2) slashing damage.",
                "Shortbow: Ranged Weapon Attack: +4 to hit, range 80/320 ft., one target. Hit: 5 (1d6 + 2) piercing damage."
            )
        ),
        MonsterLore(
            id = "skeleton",
            name = "Skeleton Warrior",
            type = "Medium Undead, Lawful Evil",
            challengeRating = "CR 1/4 (50 XP)",
            armorClass = 13,
            hitPoints = "13 (2d8 + 4)",
            speed = "30 ft.",
            description = "Animated by dark necromantic sorcery, skeletons heed only the sinister commands of their masters, knowing neither fear nor mercy.",
            actions = listOf(
                "Shortsword: Melee Weapon Attack: +4 to hit, reach 5 ft., one target. Hit: 5 (1d6 + 2) piercing damage.",
                "Shortbow: Ranged Weapon Attack: +4 to hit, range 80/320 ft., one target. Hit: 5 (1d6 + 2) piercing damage."
            )
        ),
        MonsterLore(
            id = "beholder",
            name = "Beholder Eye Tyrant",
            type = "Large Aberration, Lawful Evil",
            challengeRating = "CR 13 (10,000 XP)",
            armorClass = 18,
            hitPoints = "180 (19d10 + 76)",
            speed = "0 ft., fly 20 ft. (hover)",
            description = "A sphere of alien malevolence with a gaping maw, a great central eye radiating an antimagic cone, and ten eyestalks that unleash lethal magical rays.",
            actions = listOf(
                "Antimagic Cone: The beholder's central eye creates an area of antimagic, as in the antimagic field spell, in a 150-foot cone.",
                "Bite: Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 14 (4d6) piercing damage.",
                "Eye Rays: Shoots 3 random magical rays chosen from Charm, Paralyzing, Fear, Slowing, Enervation, Telekinetic, Sleep, Petrification, Disintegration, or Death Ray."
            )
        ),
        MonsterLore(
            id = "wererat",
            name = "Guildmaster Slink (Wererat)",
            type = "Medium Humanoid (Shapechanger), Lawful Evil",
            challengeRating = "CR 2 (450 XP)",
            armorClass = 12,
            hitPoints = "33 (6d8 + 6)",
            speed = "30 ft.",
            description = "A cunning lycanthrope who walks as a human or a giant bipedal rat, leading sewer smuggling guilds and assassinating enemies with poisoned blades.",
            actions = listOf(
                "Shapechanger: Can use its action to polymorph into a rat-humanoid hybrid or into a giant rat, or back into its true form.",
                "Bite: Melee Weapon Attack: +4 to hit, reach 5 ft., one target. Hit: 4 (1d4 + 2) piercing damage. Target must succeed on a DC 11 Con save or be cursed with wererat lycanthropy.",
                "Twin Daggers: Melee Weapon Attack: +4 to hit, reach 5 ft., one target. Hit: 4 (1d4 + 2) piercing damage plus 7 (2d6) poison damage."
            )
        )
    )
}
