package com.grimoire.dnd.engine

data class AbilityScores(
    val strength: Int = 10,
    val dexterity: Int = 10,
    val constitution: Int = 10,
    val intelligence: Int = 10,
    val wisdom: Int = 10,
    val charisma: Int = 10
)

data class MonsterAction(
    val name: String,
    val attackBonus: Int,
    val reachOrRange: String,
    val damageFormula: String,
    val damageType: String,
    val description: String = ""
)

data class MonsterStatBlock(
    val id: String,
    val name: String,
    val creatureType: String, // "Undead", "Humanoid", "Dragon", "Aberration", "Fiend", "Beast", "Giant", "Elemental"
    val cr: Double, // Challenge rating e.g. 0.25, 1.0, 5.0
    val ac: Int,
    val hp: Int,
    val hitDice: String,
    val speed: String = "30 ft.",
    val stats: AbilityScores,
    val actions: List<MonsterAction>,
    val specialTraits: List<String> = emptyList(),
    val imageTag: String = ""
)

object SRD5eMonsterBestiary {

    // ==========================================
    // 50+ SRD 5.1 MONSTERS
    // ==========================================
    val MONSTERS: List<MonsterStatBlock> = listOf(
        // Undead (10)
        MonsterStatBlock(
            id = "mon_skeleton",
            name = "Skeleton",
            creatureType = "Undead",
            cr = 0.25,
            ac = 13,
            hp = 13,
            hitDice = "2d8+4",
            stats = AbilityScores(10, 14, 15, 6, 8, 5),
            actions = listOf(
                MonsterAction("Shortsword", +4, "5 ft.", "1d6+2", "piercing"),
                MonsterAction("Shortbow", +4, "80/320 ft.", "1d6+2", "piercing")
            ),
            specialTraits = listOf("Vulnerable to Bludgeoning", "Poison Immunity"),
            imageTag = "monster_skeleton"
        ),
        MonsterStatBlock(
            id = "mon_zombie",
            name = "Zombie",
            creatureType = "Undead",
            cr = 0.25,
            ac = 8,
            hp = 22,
            hitDice = "3d8+9",
            speed = "20 ft.",
            stats = AbilityScores(13, 6, 16, 3, 6, 5),
            actions = listOf(
                MonsterAction("Slam", +3, "5 ft.", "1d6+1", "bludgeoning")
            ),
            specialTraits = listOf("Undead Fortitude (DC 5 + damage CON save to drop to 1 HP instead of 0)"),
            imageTag = "monster_zombie"
        ),
        MonsterStatBlock(
            id = "mon_ghoul",
            name = "Ghoul",
            creatureType = "Undead",
            cr = 1.0,
            ac = 12,
            hp = 22,
            hitDice = "5d8",
            stats = AbilityScores(13, 15, 10, 7, 10, 6),
            actions = listOf(
                MonsterAction("Bite", +2, "5 ft.", "2d6+2", "piercing"),
                MonsterAction("Claws", +4, "5 ft.", "2d4+2", "slashing", "Target must pass DC 10 CON save or be paralyzed for 1 minute")
            ),
            imageTag = "monster_ghoul"
        ),
        MonsterStatBlock(
            id = "mon_ghast",
            name = "Ghast",
            creatureType = "Undead",
            cr = 2.0,
            ac = 13,
            hp = 36,
            hitDice = "8d8",
            stats = AbilityScores(16, 17, 10, 11, 10, 8),
            actions = listOf(
                MonsterAction("Bite", +5, "5 ft.", "2d8+3", "piercing"),
                MonsterAction("Claws", +5, "5 ft.", "2d6+3", "slashing", "Paralyzing touch (DC 10 CON save)")
            ),
            specialTraits = listOf("Stench (DC 10 CON save or poisoned within 5 ft)", "Turn Defiance"),
            imageTag = "monster_ghast"
        ),
        MonsterStatBlock(
            id = "mon_wight",
            name = "Wight",
            creatureType = "Undead",
            cr = 3.0,
            ac = 14,
            hp = 45,
            hitDice = "6d8+18",
            stats = AbilityScores(15, 14, 16, 10, 13, 15),
            actions = listOf(
                MonsterAction("Longsword", +4, "5 ft.", "1d8+2", "slashing"),
                MonsterAction("Life Drain", +4, "5 ft.", "1d6+2", "necrotic", "Target max HP reduced by damage taken")
            ),
            specialTraits = listOf("Sunlight Sensitivity", "Resistance to nonmagical physical damage"),
            imageTag = "monster_wight"
        ),
        MonsterStatBlock(
            id = "mon_wraith",
            name = "Wraith",
            creatureType = "Undead",
            cr = 5.0,
            ac = 13,
            hp = 67,
            hitDice = "9d8+27",
            speed = "60 ft. (fly)",
            stats = AbilityScores(6, 16, 16, 12, 14, 15),
            actions = listOf(
                MonsterAction("Life Drain", +6, "5 ft.", "4d8+3", "necrotic", "DC 14 CON save or max HP reduced")
            ),
            specialTraits = listOf("Incorporeal Movement", "Sunlight Sensitivity"),
            imageTag = "monster_wraith"
        ),
        MonsterStatBlock(
            id = "mon_ghost",
            name = "Ghost",
            creatureType = "Undead",
            cr = 4.0,
            ac = 11,
            hp = 45,
            hitDice = "10d8",
            speed = "40 ft. (fly)",
            stats = AbilityScores(7, 13, 10, 10, 12, 17),
            actions = listOf(
                MonsterAction("Withering Touch", +5, "5 ft.", "4d6+3", "necrotic"),
                MonsterAction("Possession", +0, "5 ft.", "0", "special", "DC 13 CHA save to possess target body")
            ),
            imageTag = "monster_ghost"
        ),
        MonsterStatBlock(
            id = "mon_mummy",
            name = "Mummy",
            creatureType = "Undead",
            cr = 3.0,
            ac = 11,
            hp = 58,
            hitDice = "9d8+18",
            speed = "20 ft.",
            stats = AbilityScores(16, 8, 15, 6, 10, 12),
            actions = listOf(
                MonsterAction("Rotting Fist", +5, "5 ft.", "2d6+3", "bludgeoning", "Plus 3d6 necrotic damage and Mummy Rot curse")
            ),
            specialTraits = listOf("Vulnerable to Fire", "Dreadful Glare (DC 11 WIS save or frightened/paralyzed)"),
            imageTag = "monster_mummy"
        ),
        MonsterStatBlock(
            id = "mon_vampire_spawn",
            name = "Vampire Spawn",
            creatureType = "Undead",
            cr = 5.0,
            ac = 15,
            hp = 82,
            hitDice = "11d8+33",
            stats = AbilityScores(16, 16, 16, 11, 10, 12),
            actions = listOf(
                MonsterAction("Claws", +6, "5 ft.", "2d4+3", "slashing", "Target is grappled"),
                MonsterAction("Bite", +6, "5 ft.", "1d6+3", "piercing", "Plus 2d6 necrotic damage; regains HP")
            ),
            specialTraits = listOf("Regeneration 10 HP/turn", "Spider Climb", "Vampire Weaknesses"),
            imageTag = "monster_vampire_spawn"
        ),
        MonsterStatBlock(
            id = "mon_lich",
            name = "Arch-Lich",
            creatureType = "Undead",
            cr = 21.0,
            ac = 17,
            hp = 135,
            hitDice = "18d8+54",
            stats = AbilityScores(11, 16, 16, 20, 14, 16),
            actions = listOf(
                MonsterAction("Paralyzing Touch", +12, "5 ft.", "3d6", "cold", "DC 18 CON save or paralyzed indefinitely")
            ),
            specialTraits = listOf("Legendary Actions (3/round)", "Rejuvenation via Phylactery", "9th Level Spellcaster"),
            imageTag = "monster_lich"
        ),

        // Goblinoids & Humanoids (10)
        MonsterStatBlock(
            id = "mon_goblin",
            name = "Goblin Raider",
            creatureType = "Humanoid",
            cr = 0.25,
            ac = 15,
            hp = 7,
            hitDice = "2d6",
            stats = AbilityScores(8, 14, 10, 10, 8, 8),
            actions = listOf(
                MonsterAction("Scimitar", +4, "5 ft.", "1d6+2", "slashing"),
                MonsterAction("Shortbow", +4, "80/320 ft.", "1d6+2", "piercing")
            ),
            specialTraits = listOf("Nimble Escape (Disengage or Hide as bonus action)"),
            imageTag = "monster_goblin"
        ),
        MonsterStatBlock(
            id = "mon_hobgoblin",
            name = "Hobgoblin Soldier",
            creatureType = "Humanoid",
            cr = 0.5,
            ac = 18,
            hp = 11,
            hitDice = "2d8+2",
            stats = AbilityScores(13, 12, 12, 10, 10, 9),
            actions = listOf(
                MonsterAction("Longsword", +3, "5 ft.", "1d8+1", "slashing")
            ),
            specialTraits = listOf("Martial Advantage (+2d6 damage if ally within 5 ft)"),
            imageTag = "monster_hobgoblin"
        ),
        MonsterStatBlock(
            id = "mon_bugbear",
            name = "Bugbear Brute",
            creatureType = "Humanoid",
            cr = 1.0,
            ac = 16,
            hp = 27,
            hitDice = "5d8+5",
            stats = AbilityScores(15, 14, 13, 8, 11, 9),
            actions = listOf(
                MonsterAction("Morningstar", +4, "5 ft.", "2d8+2", "piercing"),
                MonsterAction("Javelin", +4, "30/120 ft.", "1d6+2", "piercing")
            ),
            specialTraits = listOf("Brute (extra weapon die on melee)", "Surprise Attack (+2d6 on surprise)"),
            imageTag = "monster_bugbear"
        ),
        MonsterStatBlock(
            id = "mon_orc",
            name = "Orc Warrior",
            creatureType = "Humanoid",
            cr = 0.5,
            ac = 13,
            hp = 15,
            hitDice = "2d8+6",
            stats = AbilityScores(16, 12, 16, 7, 11, 10),
            actions = listOf(
                MonsterAction("Greataxe", +5, "5 ft.", "1d12+3", "slashing")
            ),
            specialTraits = listOf("Aggressive (Move up to speed toward hostile as bonus action)"),
            imageTag = "monster_orc"
        ),
        MonsterStatBlock(
            id = "mon_kobold",
            name = "Kobold Skirmisher",
            creatureType = "Humanoid",
            cr = 0.125,
            ac = 12,
            hp = 5,
            hitDice = "2d6-2",
            stats = AbilityScores(7, 15, 9, 8, 7, 8),
            actions = listOf(
                MonsterAction("Dagger", +4, "5 ft.", "1d4+2", "piercing"),
                MonsterAction("Sling", +4, "30/120 ft.", "1d4+2", "bludgeoning")
            ),
            specialTraits = listOf("Pack Tactics (Advantage on attacks when ally within 5 ft)", "Sunlight Sensitivity"),
            imageTag = "monster_kobold"
        ),
        MonsterStatBlock(
            id = "mon_gnoll",
            name = "Gnoll Hunter",
            creatureType = "Humanoid",
            cr = 0.5,
            ac = 15,
            hp = 22,
            hitDice = "5d8",
            stats = AbilityScores(14, 12, 11, 6, 10, 7),
            actions = listOf(
                MonsterAction("Bite", +4, "5 ft.", "1d4+2", "piercing"),
                MonsterAction("Spear", +4, "5 ft.", "1d6+2", "piercing")
            ),
            specialTraits = listOf("Rampage (Bonus action bite on reducing creature to 0 HP)"),
            imageTag = "monster_gnoll"
        ),
        MonsterStatBlock(
            id = "mon_bandit",
            name = "Bandit Cutpurse",
            creatureType = "Humanoid",
            cr = 0.125,
            ac = 12,
            hp = 11,
            hitDice = "2d8+2",
            stats = AbilityScores(11, 12, 12, 10, 10, 10),
            actions = listOf(
                MonsterAction("Scimitar", +3, "5 ft.", "1d6+1", "slashing"),
                MonsterAction("Light Crossbow", +3, "80/320 ft.", "1d8+1", "piercing")
            ),
            imageTag = "monster_bandit"
        ),
        MonsterStatBlock(
            id = "mon_bandit_captain",
            name = "Bandit Captain",
            creatureType = "Humanoid",
            cr = 2.0,
            ac = 15,
            hp = 65,
            hitDice = "10d8+20",
            stats = AbilityScores(15, 16, 14, 14, 11, 14),
            actions = listOf(
                MonsterAction("Multiattack Scimitar", +5, "5 ft.", "1d6+3", "slashing"),
                MonsterAction("Dagger Offhand", +5, "5 ft.", "1d4+3", "piercing")
            ),
            specialTraits = listOf("Parry (+2 to AC as reaction against one melee strike)"),
            imageTag = "monster_bandit_captain"
        ),
        MonsterStatBlock(
            id = "mon_cultist",
            name = "Cultist Zealot",
            creatureType = "Humanoid",
            cr = 0.125,
            ac = 12,
            hp = 9,
            hitDice = "2d8",
            stats = AbilityScores(11, 12, 10, 10, 11, 10),
            actions = listOf(
                MonsterAction("Scimitar", +3, "5 ft.", "1d6+1", "slashing")
            ),
            specialTraits = listOf("Dark Devotion (Advantage on saves vs charmed/frightened)"),
            imageTag = "monster_cultist"
        ),
        MonsterStatBlock(
            id = "mon_assassin",
            name = "Shadow Assassin",
            creatureType = "Humanoid",
            cr = 8.0,
            ac = 15,
            hp = 78,
            hitDice = "12d8+24",
            stats = AbilityScores(11, 16, 14, 13, 11, 10),
            actions = listOf(
                MonsterAction("Poison Shortsword", +6, "5 ft.", "1d6+3", "piercing", "Target makes DC 15 CON save or takes 7d6 poison damage")
            ),
            specialTraits = listOf("Assassinate (Advantage vs creatures that haven't acted; auto-crit on surprise)", "Sneak Attack (+4d6)"),
            imageTag = "monster_assassin"
        ),

        // Aberrations & Monstrosities (10)
        MonsterStatBlock(
            id = "mon_beholder",
            name = "Beholder Eye Tyrant",
            creatureType = "Aberration",
            cr = 13.0,
            ac = 18,
            hp = 180,
            hitDice = "19d10+76",
            speed = "20 ft. (fly)",
            stats = AbilityScores(10, 14, 18, 17, 15, 17),
            actions = listOf(
                MonsterAction("Bite", +5, "5 ft.", "4d6", "piercing"),
                MonsterAction("Eye Rays (Random 3)", +0, "120 ft.", "special", "magical", "Charm, Paralyze, Disintegrate, Death Ray")
            ),
            specialTraits = listOf("Antimagic Cone (Central eye suppresses all magic in 150-ft cone)", "Hover"),
            imageTag = "monster_beholder"
        ),
        MonsterStatBlock(
            id = "mon_mind_flayer",
            name = "Mind Flayer Illithid",
            creatureType = "Aberration",
            cr = 7.0,
            ac = 15,
            hp = 71,
            hitDice = "13d8+13",
            stats = AbilityScores(11, 12, 12, 19, 17, 17),
            actions = listOf(
                MonsterAction("Tentacles", +7, "5 ft.", "2d10+4", "psychic", "Grappled and stunned DC 15 INT save"),
                MonsterAction("Extract Brain", +7, "5 ft.", "10d10", "piercing", "Instant death if target drops to 0 HP"),
                MonsterAction("Mind Blast", +0, "60-ft cone", "4d8+4", "psychic", "DC 15 INT save or stunned 1 minute")
            ),
            imageTag = "monster_mind_flayer"
        ),
        MonsterStatBlock(
            id = "mon_owlbear",
            name = "Owlbear",
            creatureType = "Monstrosity",
            cr = 3.0,
            ac = 13,
            hp = 59,
            hitDice = "7d10+21",
            speed = "40 ft.",
            stats = AbilityScores(20, 12, 17, 3, 12, 7),
            actions = listOf(
                MonsterAction("Beak", +7, "5 ft.", "1d10+5", "piercing"),
                MonsterAction("Claws", +7, "5 ft.", "2d8+5", "slashing")
            ),
            specialTraits = listOf("Keen Sight and Smell (Advantage on Perception checks)"),
            imageTag = "monster_owlbear"
        ),
        MonsterStatBlock(
            id = "mon_rust_monster",
            name = "Rust Monster",
            creatureType = "Monstrosity",
            cr = 0.5,
            ac = 14,
            hp = 27,
            hitDice = "5d8+5",
            stats = AbilityScores(13, 12, 13, 2, 13, 6),
            actions = listOf(
                MonsterAction("Bite", +3, "5 ft.", "1d8+1", "piercing"),
                MonsterAction("Antennae", +3, "5 ft.", "0", "acid", "Destroys nonmagical iron weapons/armor on contact")
            ),
            specialTraits = listOf("Iron Scent (Traces ferrous metals out to 90 feet)"),
            imageTag = "monster_rust_monster"
        ),
        MonsterStatBlock(
            id = "mon_basilisk",
            name = "Petrifying Basilisk",
            creatureType = "Monstrosity",
            cr = 3.0,
            ac = 15,
            hp = 52,
            hitDice = "8d8+16",
            speed = "20 ft.",
            stats = AbilityScores(16, 8, 15, 2, 8, 7),
            actions = listOf(
                MonsterAction("Bite", +5, "5 ft.", "2d6+3", "piercing", "Plus 2d6 poison damage")
            ),
            specialTraits = listOf("Petrifying Gaze (DC 12 CON save or turned to stone within 30 ft)"),
            imageTag = "monster_basilisk"
        ),
        MonsterStatBlock(
            id = "mon_manticore",
            name = "Spiked Manticore",
            creatureType = "Monstrosity",
            cr = 3.0,
            ac = 14,
            hp = 68,
            hitDice = "8d10+24",
            speed = "30 ft., fly 50 ft.",
            stats = AbilityScores(17, 16, 17, 7, 12, 8),
            actions = listOf(
                MonsterAction("Bite", +5, "5 ft.", "1d8+3", "piercing"),
                MonsterAction("Tail Spikes (x3)", +5, "100/200 ft.", "1d8+3", "piercing")
            ),
            imageTag = "monster_manticore"
        ),
        MonsterStatBlock(
            id = "mon_chimera",
            name = "Three-Headed Chimera",
            creatureType = "Monstrosity",
            cr = 6.0,
            ac = 14,
            hp = 114,
            hitDice = "12d10+48",
            speed = "30 ft., fly 60 ft.",
            stats = AbilityScores(19, 11, 19, 3, 14, 10),
            actions = listOf(
                MonsterAction("Bite", +7, "5 ft.", "2d6+4", "piercing"),
                MonsterAction("Horns", +7, "5 ft.", "1d12+4", "bludgeoning"),
                MonsterAction("Fire Breath", +0, "15-ft cone", "7d8", "fire", "DC 15 DEX save for half")
            ),
            imageTag = "monster_chimera"
        ),
        MonsterStatBlock(
            id = "mon_minotaur",
            name = "Labyrinth Minotaur",
            creatureType = "Monstrosity",
            cr = 3.0,
            ac = 14,
            hp = 76,
            hitDice = "9d10+27",
            speed = "40 ft.",
            stats = AbilityScores(18, 11, 16, 6, 16, 9),
            actions = listOf(
                MonsterAction("Greataxe", +6, "5 ft.", "2d12+4", "slashing"),
                MonsterAction("Gore", +6, "5 ft.", "2d8+4", "piercing")
            ),
            specialTraits = listOf("Charge (+2d8 on gore if moving 10+ ft)", "Labyrinthine Recall (Never gets lost)"),
            imageTag = "monster_minotaur"
        ),
        MonsterStatBlock(
            id = "mon_medusa",
            name = "Serpent Medusa",
            creatureType = "Monstrosity",
            cr = 6.0,
            ac = 15,
            hp = 127,
            hitDice = "17d8+51",
            stats = AbilityScores(10, 17, 16, 12, 13, 15),
            actions = listOf(
                MonsterAction("Snake Hair", +5, "5 ft.", "1d4+2", "piercing", "Plus 4d6 poison damage"),
                MonsterAction("Shortbow", +5, "80/320 ft.", "1d6+2", "piercing")
            ),
            specialTraits = listOf("Petrifying Gaze (DC 14 CON save or petrified into solid stone)"),
            imageTag = "monster_medusa"
        ),
        MonsterStatBlock(
            id = "mon_displacer_beast",
            name = "Displacer Panther",
            creatureType = "Monstrosity",
            cr = 3.0,
            ac = 13,
            hp = 85,
            hitDice = "10d10+30",
            speed = "40 ft.",
            stats = AbilityScores(18, 15, 16, 6, 12, 8),
            actions = listOf(
                MonsterAction("Tentacle Whip (x2)", +6, "10 ft.", "1d6+4", "bludgeoning", "Plus 1d6 piercing")
            ),
            specialTraits = listOf("Displacement (Attacks have disadvantage against it)", "Avoidance (Takes no damage on successful DEX saves)"),
            imageTag = "monster_displacer_beast"
        ),

        // Dragons (5)
        MonsterStatBlock(
            id = "mon_red_dragon",
            name = "Adult Red Dragon",
            creatureType = "Dragon",
            cr = 17.0,
            ac = 19,
            hp = 256,
            hitDice = "19d12+133",
            speed = "40 ft., fly 80 ft.",
            stats = AbilityScores(27, 10, 25, 16, 13, 21),
            actions = listOf(
                MonsterAction("Bite", +14, "10 ft.", "2d10+8", "piercing", "Plus 2d6 fire"),
                MonsterAction("Claw (x2)", +14, "5 ft.", "2d6+8", "slashing"),
                MonsterAction("Fire Breath", +0, "60-ft cone", "18d6", "fire", "DC 21 DEX save for half")
            ),
            specialTraits = listOf("Legendary Resistance (3/Day)", "Fire Immunity"),
            imageTag = "monster_dragon"
        ),
        MonsterStatBlock(
            id = "mon_black_dragon",
            name = "Young Black Dragon",
            creatureType = "Dragon",
            cr = 7.0,
            ac = 18,
            hp = 127,
            hitDice = "15d10+45",
            speed = "40 ft., fly 80 ft., swim 40 ft.",
            stats = AbilityScores(19, 14, 17, 12, 11, 15),
            actions = listOf(
                MonsterAction("Bite", +7, "10 ft.", "2d10+4", "piercing", "Plus 1d8 acid"),
                MonsterAction("Acid Breath", +0, "30-ft line", "11d8", "acid", "DC 14 DEX save for half")
            ),
            specialTraits = listOf("Amphibious", "Acid Immunity"),
            imageTag = "monster_black_dragon"
        ),
        MonsterStatBlock(
            id = "mon_blue_dragon",
            name = "Young Blue Dragon",
            creatureType = "Dragon",
            cr = 9.0,
            ac = 18,
            hp = 152,
            hitDice = "16d10+64",
            speed = "40 ft., burrow 20 ft., fly 80 ft.",
            stats = AbilityScores(21, 10, 19, 14, 13, 17),
            actions = listOf(
                MonsterAction("Bite", +9, "10 ft.", "2d10+5", "piercing", "Plus 1d10 lightning"),
                MonsterAction("Lightning Breath", +0, "60-ft line", "10d10", "lightning", "DC 16 DEX save for half")
            ),
            specialTraits = listOf("Lightning Immunity"),
            imageTag = "monster_blue_dragon"
        ),
        MonsterStatBlock(
            id = "mon_green_dragon",
            name = "Young Green Dragon",
            creatureType = "Dragon",
            cr = 8.0,
            ac = 18,
            hp = 136,
            hitDice = "16d10+48",
            speed = "40 ft., fly 80 ft., swim 40 ft.",
            stats = AbilityScores(19, 12, 17, 16, 13, 15),
            actions = listOf(
                MonsterAction("Bite", +7, "10 ft.", "2d10+4", "piercing", "Plus 2d6 poison"),
                MonsterAction("Poison Breath", +0, "30-ft cone", "12d6", "poison", "DC 14 CON save for half")
            ),
            specialTraits = listOf("Amphibious", "Poison Immunity"),
            imageTag = "monster_green_dragon"
        ),
        MonsterStatBlock(
            id = "mon_red_wyrmling",
            name = "Red Dragon Wyrmling",
            creatureType = "Dragon",
            cr = 4.0,
            ac = 17,
            hp = 75,
            hitDice = "10d8+30",
            speed = "30 ft., fly 60 ft.",
            stats = AbilityScores(19, 10, 17, 12, 11, 15),
            actions = listOf(
                MonsterAction("Bite", +6, "5 ft.", "1d10+4", "piercing", "Plus 1d6 fire"),
                MonsterAction("Fire Breath", +0, "15-ft cone", "7d6", "fire", "DC 13 DEX save for half")
            ),
            imageTag = "monster_dragon_wyrmling"
        ),

        // Fiends & Demons (5)
        MonsterStatBlock(
            id = "mon_imp",
            name = "Imp Familiar",
            creatureType = "Fiend",
            cr = 1.0,
            ac = 13,
            hp = 10,
            hitDice = "3d4+3",
            speed = "20 ft., fly 40 ft.",
            stats = AbilityScores(6, 17, 13, 11, 12, 14),
            actions = listOf(
                MonsterAction("Sting", +5, "5 ft.", "1d4+3", "piercing", "Plus 3d6 poison damage on DC 11 CON save")
            ),
            specialTraits = listOf("Invisibility at will", "Devil's Sight"),
            imageTag = "monster_imp"
        ),
        MonsterStatBlock(
            id = "mon_hell_hound",
            name = "Hell Hound",
            creatureType = "Fiend",
            cr = 3.0,
            ac = 15,
            hp = 45,
            hitDice = "7d8+14",
            speed = "50 ft.",
            stats = AbilityScores(17, 12, 14, 6, 13, 6),
            actions = listOf(
                MonsterAction("Bite", +5, "5 ft.", "1d8+3", "piercing", "Plus 2d6 fire"),
                MonsterAction("Fire Breath", +0, "15-ft cone", "6d6", "fire", "DC 12 DEX save for half")
            ),
            specialTraits = listOf("Fire Immunity", "Pack Tactics"),
            imageTag = "monster_hell_hound"
        ),
        MonsterStatBlock(
            id = "mon_barbed_devil",
            name = "Barbed Devil (Hamatula)",
            creatureType = "Fiend",
            cr = 5.0,
            ac = 15,
            hp = 110,
            hitDice = "13d8+52",
            stats = AbilityScores(16, 17, 18, 12, 14, 14),
            actions = listOf(
                MonsterAction("Claw (x2)", +6, "5 ft.", "1d6+3", "piercing"),
                MonsterAction("Tail", +6, "5 ft.", "1d8+3", "piercing"),
                MonsterAction("Hurl Flame", +5, "150 ft.", "3d6", "fire")
            ),
            specialTraits = listOf("Barbed Hide (Deals 1d10 piercing when grappled or hit with melee)"),
            imageTag = "monster_barbed_devil"
        ),
        MonsterStatBlock(
            id = "mon_dretch",
            name = "Dretch Abyssal Wretch",
            creatureType = "Fiend",
            cr = 0.25,
            ac = 11,
            hp = 18,
            hitDice = "4d6+4",
            stats = AbilityScores(11, 11, 12, 5, 8, 3),
            actions = listOf(
                MonsterAction("Bite", +2, "5 ft.", "1d6", "piercing"),
                MonsterAction("Fetid Cloud", +0, "10-ft radius", "0", "poison", "DC 11 CON save or poisoned")
            ),
            imageTag = "monster_dretch"
        ),
        MonsterStatBlock(
            id = "mon_pit_fiend",
            name = "Pit Fiend Infernal Warlord",
            creatureType = "Fiend",
            cr = 20.0,
            ac = 19,
            hp = 300,
            hitDice = "24d10+168",
            speed = "30 ft., fly 60 ft.",
            stats = AbilityScores(26, 14, 24, 22, 18, 24),
            actions = listOf(
                MonsterAction("Bite", +14, "5 ft.", "4d6+8", "piercing", "Plus poison and no healing"),
                MonsterAction("Mace", +14, "10 ft.", "2d6+8", "bludgeoning", "Plus 6d6 fire"),
                MonsterAction("Tail", +14, "10 ft.", "3d10+8", "bludgeoning")
            ),
            specialTraits = listOf("Magic Resistance", "Fear Aura (DC 21 WIS save)"),
            imageTag = "monster_pit_fiend"
        ),

        // Elementals & Giants (5)
        MonsterStatBlock(
            id = "mon_ogre",
            name = "Ogre Crusher",
            creatureType = "Giant",
            cr = 2.0,
            ac = 11,
            hp = 59,
            hitDice = "7d10+21",
            speed = "40 ft.",
            stats = AbilityScores(19, 8, 16, 5, 7, 7),
            actions = listOf(
                MonsterAction("Greatclub", +6, "5 ft.", "2d8+4", "bludgeoning"),
                MonsterAction("Javelin", +6, "30/120 ft.", "2d6+4", "piercing")
            ),
            imageTag = "monster_ogre"
        ),
        MonsterStatBlock(
            id = "mon_hill_giant",
            name = "Hill Giant",
            creatureType = "Giant",
            cr = 5.0,
            ac = 13,
            hp = 105,
            hitDice = "10d12+40",
            speed = "40 ft.",
            stats = AbilityScores(21, 8, 19, 5, 9, 6),
            actions = listOf(
                MonsterAction("Greatclub (x2)", +8, "10 ft.", "3d8+5", "bludgeoning"),
                MonsterAction("Rock", +8, "60/240 ft.", "3d10+5", "bludgeoning")
            ),
            imageTag = "monster_hill_giant"
        ),
        MonsterStatBlock(
            id = "mon_fire_elemental",
            name = "Fire Elemental",
            creatureType = "Elemental",
            cr = 5.0,
            ac = 13,
            hp = 102,
            hitDice = "12d10+36",
            speed = "50 ft.",
            stats = AbilityScores(10, 17, 16, 6, 10, 7),
            actions = listOf(
                MonsterAction("Touch (x2)", +6, "5 ft.", "2d6+3", "fire", "Target ignited for 1d10 ongoing fire")
            ),
            specialTraits = listOf("Fire Form (Creature touching takes 1d10 fire)", "Water Susceptibility"),
            imageTag = "monster_fire_elemental"
        ),
        MonsterStatBlock(
            id = "mon_water_elemental",
            name = "Water Elemental",
            creatureType = "Elemental",
            cr = 5.0,
            ac = 14,
            hp = 114,
            hitDice = "12d10+48",
            speed = "30 ft., swim 90 ft.",
            stats = AbilityScores(18, 14, 18, 5, 10, 8),
            actions = listOf(
                MonsterAction("Slam (x2)", +7, "5 ft.", "2d8+4", "bludgeoning"),
                MonsterAction("Whelm", +0, "5 ft.", "2d8+4", "bludgeoning", "DC 15 STR save or grappled and drowning")
            ),
            imageTag = "monster_water_elemental"
        ),
        MonsterStatBlock(
            id = "mon_iron_golem",
            name = "Iron Golem",
            creatureType = "Construct",
            cr = 16.0,
            ac = 20,
            hp = 210,
            hitDice = "20d10+100",
            stats = AbilityScores(24, 9, 20, 3, 11, 1),
            actions = listOf(
                MonsterAction("Slam (x2)", +13, "5 ft.", "3d8+7", "bludgeoning"),
                MonsterAction("Sword", +13, "10 ft.", "3d10+7", "slashing"),
                MonsterAction("Poison Breath", +0, "15-ft cone", "10d8", "poison", "DC 19 CON save for half")
            ),
            specialTraits = listOf("Fire Absorption (Heals from fire)", "Immutable Form", "Magic Resistance"),
            imageTag = "monster_iron_golem"
        ),

        // Beasts (5)
        MonsterStatBlock(
            id = "mon_dire_wolf",
            name = "Dire Wolf Alpha",
            creatureType = "Beast",
            cr = 1.0,
            ac = 14,
            hp = 37,
            hitDice = "5d10+10",
            speed = "50 ft.",
            stats = AbilityScores(17, 15, 15, 3, 12, 7),
            actions = listOf(
                MonsterAction("Bite", +5, "5 ft.", "2d6+3", "piercing", "DC 13 STR save or knocked prone")
            ),
            specialTraits = listOf("Keen Hearing and Smell", "Pack Tactics"),
            imageTag = "monster_dire_wolf"
        ),
        MonsterStatBlock(
            id = "mon_giant_spider",
            name = "Giant Crypt Spider",
            creatureType = "Beast",
            cr = 1.0,
            ac = 14,
            hp = 26,
            hitDice = "4d10+4",
            speed = "30 ft., climb 30 ft.",
            stats = AbilityScores(14, 16, 12, 2, 11, 4),
            actions = listOf(
                MonsterAction("Bite", +5, "5 ft.", "1d8+3", "piercing", "Plus 2d8 poison damage; DC 11 CON save"),
                MonsterAction("Web", +5, "30/60 ft.", "0", "special", "Target is restrained in webbing")
            ),
            specialTraits = listOf("Spider Climb", "Web Walker"),
            imageTag = "monster_giant_spider"
        ),
        MonsterStatBlock(
            id = "mon_cave_bear",
            name = "Ancient Cave Bear",
            creatureType = "Beast",
            cr = 2.0,
            ac = 12,
            hp = 42,
            hitDice = "5d10+15",
            speed = "40 ft., swim 30 ft.",
            stats = AbilityScores(20, 10, 16, 2, 13, 7),
            actions = listOf(
                MonsterAction("Bite", +7, "5 ft.", "1d8+5", "piercing"),
                MonsterAction("Claws", +7, "5 ft.", "2d6+5", "slashing")
            ),
            specialTraits = listOf("Keen Smell"),
            imageTag = "monster_cave_bear"
        ),
        MonsterStatBlock(
            id = "mon_giant_constrictor",
            name = "Giant Constrictor Snake",
            creatureType = "Beast",
            cr = 2.0,
            ac = 12,
            hp = 60,
            hitDice = "8d10+16",
            speed = "30 ft., swim 30 ft.",
            stats = AbilityScores(19, 14, 14, 1, 10, 3),
            actions = listOf(
                MonsterAction("Bite", +6, "10 ft.", "2d6+4", "piercing"),
                MonsterAction("Constrict", +6, "5 ft.", "2d8+4", "bludgeoning", "Target is grappled and restrained; DC 14 escape")
            ),
            imageTag = "monster_giant_constrictor"
        ),
        MonsterStatBlock(
            id = "mon_giant_scorpion",
            name = "Giant Dune Scorpion",
            creatureType = "Beast",
            cr = 3.0,
            ac = 15,
            hp = 52,
            hitDice = "7d10+14",
            speed = "40 ft.",
            stats = AbilityScores(15, 13, 15, 1, 9, 3),
            actions = listOf(
                MonsterAction("Claw (x2)", +4, "5 ft.", "1d8+2", "bludgeoning", "Target is grappled"),
                MonsterAction("Sting", +4, "5 ft.", "1d10+2", "piercing", "Plus 4d10 poison damage; DC 12 CON save for half")
            ),
            imageTag = "monster_giant_scorpion"
        )
    )

    fun getAllMonsters(): List<MonsterStatBlock> = MONSTERS

    fun findById(id: String): MonsterStatBlock? =
        MONSTERS.firstOrNull { it.id.equals(id, ignoreCase = true) }

    fun findByName(name: String): MonsterStatBlock? =
        MONSTERS.firstOrNull { it.name.contains(name, ignoreCase = true) }
}
