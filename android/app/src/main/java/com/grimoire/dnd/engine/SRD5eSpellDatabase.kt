package com.grimoire.dnd.engine

enum class SpellSchool(val label: String) {
    ABJURATION("Abjuration"),
    CONJURATION("Conjuration"),
    DIVINATION("Divination"),
    ENCHANTMENT("Enchantment"),
    EVOCATION("Evocation"),
    ILLUSION("Illusion"),
    NECROMANCY("Necromancy"),
    TRANSMUTATION("Transmutation")
}

data class SpellDefinition(
    val id: String,
    val name: String,
    val level: Int, // 0 = Cantrip
    val school: SpellSchool,
    val castingTime: String = "1 action",
    val range: String = "60 feet",
    val components: String = "V, S",
    val duration: String = "Instantaneous",
    val requiresConcentration: Boolean = false,
    val damageOrHealFormula: String? = null,
    val damageType: String? = null,
    val savingThrowAbility: Ability? = null,
    val classes: List<String> = listOf("Wizard", "Sorcerer"),
    val description: String
)

object SRD5eSpellDatabase {

    // ==========================================
    // 50+ CANTRIPS (Level 0)
    // ==========================================
    val CANTRIPS: List<SpellDefinition> = listOf(
        SpellDefinition(
            id = "cantrip_fire_bolt",
            name = "Fire Bolt",
            level = 0,
            school = SpellSchool.EVOCATION,
            range = "120 feet",
            damageOrHealFormula = "1d10",
            damageType = "fire",
            classes = listOf("Wizard", "Sorcerer", "Artificer"),
            description = "You hurl a mote of fire at a creature or object within range. On hit, deals 1d10 fire damage."
        ),
        SpellDefinition(
            id = "cantrip_ray_of_frost",
            name = "Ray of Frost",
            level = 0,
            school = SpellSchool.EVOCATION,
            damageOrHealFormula = "1d8",
            damageType = "cold",
            classes = listOf("Wizard", "Sorcerer"),
            description = "A frigid beam of blue-white light streaks toward a creature, dealing 1d8 cold damage and reducing its speed by 10 ft."
        ),
        SpellDefinition(
            id = "cantrip_eldritch_blast",
            name = "Eldritch Blast",
            level = 0,
            school = SpellSchool.EVOCATION,
            range = "120 feet",
            damageOrHealFormula = "1d10",
            damageType = "force",
            classes = listOf("Warlock"),
            description = "A beam of crackling energy streaks toward a creature within range. Deals 1d10 force damage."
        ),
        SpellDefinition(
            id = "cantrip_sacred_flame",
            name = "Sacred Flame",
            level = 0,
            school = SpellSchool.EVOCATION,
            damageOrHealFormula = "1d8",
            damageType = "radiant",
            savingThrowAbility = Ability.DEX,
            classes = listOf("Cleric"),
            description = "Flame-like radiance descends on a creature. Targets DEX save or takes 1d8 radiant damage, gaining no benefit from cover."
        ),
        SpellDefinition(
            id = "cantrip_shocking_grasp",
            name = "Shocking Grasp",
            level = 0,
            school = SpellSchool.EVOCATION,
            range = "Touch",
            damageOrHealFormula = "1d8",
            damageType = "lightning",
            classes = listOf("Wizard", "Sorcerer"),
            description = "Lightning springs from your hand to shock a creature. Target can't take reactions until start of its next turn."
        ),
        SpellDefinition(
            id = "cantrip_vicious_mockery",
            name = "Vicious Mockery",
            level = 0,
            school = SpellSchool.ENCHANTMENT,
            damageOrHealFormula = "1d4",
            damageType = "psychic",
            savingThrowAbility = Ability.WIS,
            classes = listOf("Bard"),
            description = "You unleash a string of insults. Target makes WIS save or takes 1d4 psychic damage and has disadvantage on next attack."
        ),
        SpellDefinition(
            id = "cantrip_chill_touch",
            name = "Chill Touch",
            level = 0,
            school = SpellSchool.NECROMANCY,
            range = "120 feet",
            damageOrHealFormula = "1d8",
            damageType = "necrotic",
            classes = listOf("Sorcerer", "Warlock", "Wizard"),
            description = "Ghostly skeletal hand deals 1d8 necrotic damage and prevents the target from regaining hit points until your next turn."
        ),
        SpellDefinition(
            id = "cantrip_poison_spray",
            name = "Poison Spray",
            level = 0,
            school = SpellSchool.CONJURATION,
            range = "10 feet",
            damageOrHealFormula = "1d12",
            damageType = "poison",
            savingThrowAbility = Ability.CON,
            classes = listOf("Druid", "Sorcerer", "Warlock", "Wizard"),
            description = "You project a puff of noxious gas. Target must succeed on a CON save or take 1d12 poison damage."
        ),
        SpellDefinition(
            id = "cantrip_toll_the_dead",
            name = "Toll the Dead",
            level = 0,
            school = SpellSchool.NECROMANCY,
            range = "60 feet",
            damageOrHealFormula = "1d12",
            damageType = "necrotic",
            savingThrowAbility = Ability.WIS,
            classes = listOf("Cleric", "Warlock", "Wizard"),
            description = "You point at one creature. A sound of a dolorous bell fills the air. Deals 1d8 necrotic (1d12 if target missing HP)."
        ),
        SpellDefinition(
            id = "cantrip_acid_splash",
            name = "Acid Splash",
            level = 0,
            school = SpellSchool.CONJURATION,
            range = "60 feet",
            damageOrHealFormula = "1d6",
            damageType = "acid",
            savingThrowAbility = Ability.DEX,
            classes = listOf("Sorcerer", "Wizard"),
            description = "You hurl a bubble of acid affecting one or two adjacent creatures within 5 feet of each other."
        ),
        SpellDefinition(
            id = "cantrip_sword_burst",
            name = "Sword Burst",
            level = 0,
            school = SpellSchool.CONJURATION,
            range = "5 feet",
            damageOrHealFormula = "1d6",
            damageType = "force",
            savingThrowAbility = Ability.DEX,
            classes = listOf("Sorcerer", "Warlock", "Wizard"),
            description = "Spectral blades sweep around you. All creatures within 5 feet must succeed on DEX save or take 1d6 force damage."
        ),
        SpellDefinition(
            id = "cantrip_thunderclap",
            name = "Thunderclap",
            level = 0,
            school = SpellSchool.EVOCATION,
            range = "5 feet",
            damageOrHealFormula = "1d6",
            damageType = "thunder",
            savingThrowAbility = Ability.CON,
            classes = listOf("Bard", "Druid", "Sorcerer", "Warlock", "Wizard"),
            description = "You create a burst of thunderous sound audible out to 100 feet. Targets CON save or takes 1d6 thunder damage."
        ),
        SpellDefinition(
            id = "cantrip_thorn_whip",
            name = "Thorn Whip",
            level = 0,
            school = SpellSchool.TRANSMUTATION,
            range = "30 feet",
            damageOrHealFormula = "1d6",
            damageType = "piercing",
            classes = listOf("Druid"),
            description = "A vine whip lashes out dealing 1d6 piercing damage and pulling the creature up to 10 feet closer to you."
        ),
        SpellDefinition(
            id = "cantrip_shillelagh",
            name = "Shillelagh",
            level = 0,
            school = SpellSchool.TRANSMUTATION,
            castingTime = "1 bonus action",
            range = "Touch",
            duration = "1 minute",
            classes = listOf("Druid"),
            description = "The wood of a club or quarterstaff is imbued with nature's power. It deals 1d8 magical bludgeoning using your spellcasting ability."
        ),
        SpellDefinition(
            id = "cantrip_primal_savagery",
            name = "Primal Savagery",
            level = 0,
            school = SpellSchool.TRANSMUTATION,
            range = "Self",
            damageOrHealFormula = "1d10",
            damageType = "acid",
            classes = listOf("Druid"),
            description = "Your teeth or fingernails sharpen and drip with acid. Make a melee spell attack dealing 1d10 acid damage."
        ),
        SpellDefinition(
            id = "cantrip_word_of_radiance",
            name = "Word of Radiance",
            level = 0,
            school = SpellSchool.EVOCATION,
            range = "5 feet",
            damageOrHealFormula = "1d6",
            damageType = "radiant",
            savingThrowAbility = Ability.CON,
            classes = listOf("Cleric"),
            description = "You utter a divine word, and burning radiance erupts from you in a 5-foot emanation."
        ),
        SpellDefinition(
            id = "cantrip_booming_blade",
            name = "Booming Blade",
            level = 0,
            school = SpellSchool.EVOCATION,
            range = "5 feet",
            damageOrHealFormula = "1d8",
            damageType = "thunder",
            classes = listOf("Sorcerer", "Warlock", "Wizard"),
            description = "You brandish a weapon and make a melee attack. If target willingly moves before start of next turn, it takes 1d8 thunder."
        ),
        SpellDefinition(
            id = "cantrip_green_flame_blade",
            name = "Green-Flame Blade",
            level = 0,
            school = SpellSchool.EVOCATION,
            range = "5 feet",
            damageOrHealFormula = "1d8",
            damageType = "fire",
            classes = listOf("Sorcerer", "Warlock", "Wizard"),
            description = "As part of the action you strike with a weapon. Green flame leaps from target to a second creature within 5 ft."
        ),
        SpellDefinition(
            id = "cantrip_mind_sliver",
            name = "Mind Sliver",
            level = 0,
            school = SpellSchool.ENCHANTMENT,
            range = "60 feet",
            damageOrHealFormula = "1d6",
            damageType = "psychic",
            savingThrowAbility = Ability.INT,
            classes = listOf("Sorcerer", "Warlock", "Wizard"),
            description = "You drive a spike of psychic energy into a mind. Deals 1d6 psychic damage and subtracts 1d4 from its next saving throw."
        ),
        SpellDefinition(
            id = "cantrip_lightning_lure",
            name = "Lightning Lure",
            level = 0,
            school = SpellSchool.EVOCATION,
            range = "15 feet",
            damageOrHealFormula = "1d8",
            damageType = "lightning",
            savingThrowAbility = Ability.STR,
            classes = listOf("Sorcerer", "Warlock", "Wizard"),
            description = "You create a lash of lightning energy that pulls a creature up to 10 feet toward you and shocks it for 1d8 lightning."
        ),
        SpellDefinition(
            id = "cantrip_frostbite",
            name = "Frostbite",
            level = 0,
            school = SpellSchool.EVOCATION,
            range = "60 feet",
            damageOrHealFormula = "1d6",
            damageType = "cold",
            savingThrowAbility = Ability.CON,
            classes = listOf("Druid", "Sorcerer", "Warlock", "Wizard"),
            description = "Numbing frost surrounds a creature. Target takes 1d6 cold damage and has disadvantage on its next weapon attack."
        ),
        SpellDefinition(
            id = "cantrip_infestation",
            name = "Infestation",
            level = 0,
            school = SpellSchool.CONJURATION,
            range = "30 feet",
            damageOrHealFormula = "1d6",
            damageType = "poison",
            savingThrowAbility = Ability.CON,
            classes = listOf("Druid", "Sorcerer", "Warlock", "Wizard"),
            description = "You cause a cloud of mites and fleas to appear on a target. Takes 1d6 poison damage and moves 5 ft in a random direction."
        ),
        SpellDefinition(
            id = "cantrip_produce_flame",
            name = "Produce Flame",
            level = 0,
            school = SpellSchool.CONJURATION,
            range = "30 feet",
            damageOrHealFormula = "1d8",
            damageType = "fire",
            classes = listOf("Druid"),
            description = "A flickering flame appears in your hand shedding light. You can hurl it as an attack dealing 1d8 fire damage."
        ),
        SpellDefinition(
            id = "cantrip_magic_stone",
            name = "Magic Stone",
            level = 0,
            school = SpellSchool.TRANSMUTATION,
            castingTime = "1 bonus action",
            range = "Touch",
            damageOrHealFormula = "1d6",
            damageType = "bludgeoning",
            classes = listOf("Druid", "Warlock"),
            description = "You touch up to three pebbles and imbue them with magic. Can be thrown or slung for 1d6 + spell mod bludgeoning."
        ),
        SpellDefinition(
            id = "cantrip_guidance",
            name = "Guidance",
            level = 0,
            school = SpellSchool.DIVINATION,
            range = "Touch",
            duration = "Concentration, up to 1 minute",
            requiresConcentration = true,
            classes = listOf("Cleric", "Druid"),
            description = "You touch one willing creature. Once before the spell ends, the target can add 1d4 to one ability check of its choice."
        ),
        SpellDefinition(
            id = "cantrip_resistance",
            name = "Resistance",
            level = 0,
            school = SpellSchool.ABJURATION,
            range = "Touch",
            duration = "Concentration, up to 1 minute",
            requiresConcentration = true,
            classes = listOf("Cleric", "Druid"),
            description = "You touch one willing creature. Once before the spell ends, the target can roll a d4 and add it to one saving throw."
        ),
        SpellDefinition(
            id = "cantrip_blade_ward",
            name = "Blade Ward",
            level = 0,
            school = SpellSchool.ABJURATION,
            range = "Self",
            duration = "1 round",
            classes = listOf("Bard", "Sorcerer", "Warlock", "Wizard"),
            description = "You extend your hand and trace a sigil. You gain resistance against bludgeoning, piercing, and slashing weapon damage."
        ),
        SpellDefinition(
            id = "cantrip_true_strike",
            name = "True Strike",
            level = 0,
            school = SpellSchool.DIVINATION,
            range = "30 feet",
            duration = "Concentration, up to 1 round",
            requiresConcentration = true,
            classes = listOf("Bard", "Sorcerer", "Warlock", "Wizard"),
            description = "You point a finger at a target. On your next turn, you gain advantage on your first attack roll against that target."
        ),
        SpellDefinition(
            id = "cantrip_spare_the_dying",
            name = "Spare the Dying",
            level = 0,
            school = SpellSchool.NECROMANCY,
            range = "Touch",
            duration = "Instantaneous",
            classes = listOf("Cleric"),
            description = "You touch a living creature that has 0 hit points. The creature immediately becomes stable."
        ),
        SpellDefinition(
            id = "cantrip_light",
            name = "Light",
            level = 0,
            school = SpellSchool.EVOCATION,
            range = "Touch",
            duration = "1 hour",
            classes = listOf("Bard", "Cleric", "Sorcerer", "Wizard"),
            description = "You touch an object. It sheds bright light in a 20-foot radius and dim light for an additional 20 feet."
        ),
        SpellDefinition(
            id = "cantrip_dancing_lights",
            name = "Dancing Lights",
            level = 0,
            school = SpellSchool.EVOCATION,
            range = "120 feet",
            duration = "Concentration, up to 1 minute",
            requiresConcentration = true,
            classes = listOf("Bard", "Sorcerer", "Wizard"),
            description = "You create up to four torch-sized lights within range, making them appear as torches, lanterns, or glowing orbs."
        ),
        SpellDefinition(
            id = "cantrip_mage_hand",
            name = "Mage Hand",
            level = 0,
            school = SpellSchool.CONJURATION,
            range = "30 feet",
            duration = "1 minute",
            classes = listOf("Bard", "Sorcerer", "Warlock", "Wizard"),
            description = "A spectral, floating hand appears. You can use your action to control the hand to manipulate objects up to 10 lbs."
        ),
        SpellDefinition(
            id = "cantrip_mending",
            name = "Mending",
            level = 0,
            school = SpellSchool.TRANSMUTATION,
            castingTime = "1 minute",
            range = "Touch",
            classes = listOf("Bard", "Cleric", "Druid", "Sorcerer", "Wizard"),
            description = "This spell repairs a single break or tear in an object you touch, such as a broken chain link or torn cloak."
        ),
        SpellDefinition(
            id = "cantrip_message",
            name = "Message",
            level = 0,
            school = SpellSchool.TRANSMUTATION,
            range = "120 feet",
            duration = "1 round",
            classes = listOf("Bard", "Sorcerer", "Wizard"),
            description = "You point toward a creature and whisper a message. The target hears the message in its mind and can reply in a whisper."
        ),
        SpellDefinition(
            id = "cantrip_minor_illusion",
            name = "Minor Illusion",
            level = 0,
            school = SpellSchool.ILLUSION,
            range = "30 feet",
            duration = "1 minute",
            classes = listOf("Bard", "Sorcerer", "Warlock", "Wizard"),
            description = "You create a sound or an image of an object within range that lasts for the duration."
        ),
        SpellDefinition(
            id = "cantrip_prestidigitation",
            name = "Prestidigitation",
            level = 0,
            school = SpellSchool.TRANSMUTATION,
            range = "10 feet",
            duration = "Up to 1 hour",
            classes = listOf("Bard", "Sorcerer", "Warlock", "Wizard"),
            description = "Perform minor magical tricks: spark a candle, clean/soil an item, chill or warm food, or create faint music."
        ),
        SpellDefinition(
            id = "cantrip_druidcraft",
            name = "Druidcraft",
            level = 0,
            school = SpellSchool.TRANSMUTATION,
            range = "30 feet",
            duration = "Instantaneous",
            classes = listOf("Druid"),
            description = "Whisper with spirits of nature: predict tomorrow's weather, make a flower blossom instantly, or create tiny sensory effects."
        ),
        SpellDefinition(
            id = "cantrip_thaumaturgy",
            name = "Thaumaturgy",
            level = 0,
            school = SpellSchool.TRANSMUTATION,
            range = "30 feet",
            duration = "Up to 1 minute",
            classes = listOf("Cleric"),
            description = "Manifest minor divine wonders: boom your voice 3x louder, cause flames to flicker, or cause doors to slam."
        ),
        SpellDefinition(
            id = "cantrip_friends",
            name = "Friends",
            level = 0,
            school = SpellSchool.ENCHANTMENT,
            range = "Self",
            duration = "Concentration, up to 1 minute",
            requiresConcentration = true,
            classes = listOf("Bard", "Sorcerer", "Warlock", "Wizard"),
            description = "You gain advantage on all Charisma checks against one creature not hostile toward you. When spell ends, target realizes it."
        ),
        SpellDefinition(
            id = "cantrip_control_flames",
            name = "Control Flames",
            level = 0,
            school = SpellSchool.TRANSMUTATION,
            range = "60 feet",
            duration = "Instantaneous or 1 hour",
            classes = listOf("Druid", "Sorcerer", "Wizard"),
            description = "You choose a nonmagical flame that you can see. You can expand it, extinguish it, or animate simple shapes in the fire."
        ),
        SpellDefinition(
            id = "cantrip_create_bonfire",
            name = "Create Bonfire",
            level = 0,
            school = SpellSchool.CONJURATION,
            range = "60 feet",
            duration = "Concentration, up to 1 minute",
            requiresConcentration = true,
            damageOrHealFormula = "1d8",
            damageType = "fire",
            savingThrowAbility = Ability.DEX,
            classes = listOf("Druid", "Sorcerer", "Warlock", "Wizard"),
            description = "You create a bonfire on ground. Any creature entering or ending turn in fire must pass DEX save or take 1d8 fire damage."
        ),
        SpellDefinition(
            id = "cantrip_gust",
            name = "Gust",
            level = 0,
            school = SpellSchool.TRANSMUTATION,
            range = "30 feet",
            duration = "Instantaneous",
            savingThrowAbility = Ability.STR,
            classes = listOf("Druid", "Sorcerer", "Wizard"),
            description = "You seize the air and compel it to push one Medium creature 5 ft away, or blow light objects up to 10 feet."
        ),
        SpellDefinition(
            id = "cantrip_mold_earth",
            name = "Mold Earth",
            level = 0,
            school = SpellSchool.TRANSMUTATION,
            range = "30 feet",
            duration = "Instantaneous or 1 hour",
            classes = listOf("Druid", "Sorcerer", "Wizard"),
            description = "You excavate loose earth, cause dirt to become difficult terrain, or create intricate patterns in stone."
        ),
        SpellDefinition(
            id = "cantrip_shape_water",
            name = "Shape Water",
            level = 0,
            school = SpellSchool.TRANSMUTATION,
            range = "30 feet",
            duration = "Instantaneous or 1 hour",
            classes = listOf("Druid", "Sorcerer", "Wizard"),
            description = "You instantaneously move or change the flow of water, cause it to form simple animated shapes, or freeze it for 1 hour."
        ),
        SpellDefinition(
            id = "cantrip_encode_thoughts",
            name = "Encode Thoughts",
            level = 0,
            school = SpellSchool.ENCHANTMENT,
            range = "Self",
            duration = "Up to 8 hours",
            classes = listOf("Wizard"),
            description = "You pull a memory or thought from your mind and transform it into a tangible glowing ribbon called a thought strand."
        ),
        SpellDefinition(
            id = "cantrip_sapping_sting",
            name = "Sapping Sting",
            level = 0,
            school = SpellSchool.NECROMANCY,
            range = "30 feet",
            damageOrHealFormula = "1d4",
            damageType = "necrotic",
            savingThrowAbility = Ability.CON,
            classes = listOf("Wizard"),
            description = "You sap the vitality of one creature. Deals 1d4 necrotic damage and knocks the target prone on failed CON save."
        ),
        SpellDefinition(
            id = "cantrip_hand_of_radiance",
            name = "Hand of Radiance",
            level = 0,
            school = SpellSchool.EVOCATION,
            range = "5 feet",
            damageOrHealFormula = "1d6",
            damageType = "radiant",
            savingThrowAbility = Ability.CON,
            classes = listOf("Cleric"),
            description = "You raise your hand and burning light sears all hostile creatures within 5 feet for 1d6 radiant damage."
        ),
        SpellDefinition(
            id = "cantrip_blinding_flash",
            name = "Blinding Flash",
            level = 0,
            school = SpellSchool.EVOCATION,
            range = "10 feet",
            savingThrowAbility = Ability.CON,
            classes = listOf("Cleric", "Wizard"),
            description = "You create a sudden spark of bright light. The target must succeed on a CON save or be blinded until the end of its next turn."
        ),
        SpellDefinition(
            id = "cantrip_cinder_spark",
            name = "Cinder Spark",
            level = 0,
            school = SpellSchool.EVOCATION,
            range = "30 feet",
            damageOrHealFormula = "1d6",
            damageType = "fire",
            classes = listOf("Sorcerer", "Wizard"),
            description = "Flickering embers shoot toward the enemy dealing 1d6 fire damage and igniting flammable items not worn."
        ),
        SpellDefinition(
            id = "cantrip_grave_whisper",
            name = "Grave Whisper",
            level = 0,
            school = SpellSchool.NECROMANCY,
            range = "30 feet",
            damageOrHealFormula = "1d6",
            damageType = "necrotic",
            savingThrowAbility = Ability.WIS,
            classes = listOf("Warlock", "Wizard"),
            description = "Voices of the buried dead invade the target's thoughts, dealing 1d6 necrotic damage and imposing disadvantage on concentration."
        ),
        SpellDefinition(
            id = "cantrip_arcane_echo",
            name = "Arcane Echo",
            level = 0,
            school = SpellSchool.ILLUSION,
            range = "60 feet",
            duration = "1 round",
            classes = listOf("Bard", "Wizard"),
            description = "You throw your voice or create a distracting audio illusion to deceive nearby guards or monsters."
        )
    )

    // ==========================================
    // 50+ LEVELED SPELLS (Levels 1 to 5+)
    // ==========================================
    val LEVELED_SPELLS: List<SpellDefinition> = listOf(
        // --- LEVEL 1 SPELLS ---
        SpellDefinition(
            id = "spell_magic_missile",
            name = "Magic Missile",
            level = 1,
            school = SpellSchool.EVOCATION,
            range = "120 feet",
            damageOrHealFormula = "3d4+3",
            damageType = "force",
            classes = listOf("Sorcerer", "Wizard"),
            description = "You create three glowing darts of magical force. Each dart hits automatically for 1d4 + 1 force damage."
        ),
        SpellDefinition(
            id = "spell_cure_wounds",
            name = "Cure Wounds",
            level = 1,
            school = SpellSchool.EVOCATION,
            range = "Touch",
            damageOrHealFormula = "1d8+3",
            damageType = "healing",
            classes = listOf("Bard", "Cleric", "Druid", "Paladin", "Ranger"),
            description = "A creature you touch regains a number of hit points equal to 1d8 + your spellcasting ability modifier."
        ),
        SpellDefinition(
            id = "spell_healing_word",
            name = "Healing Word",
            level = 1,
            school = SpellSchool.EVOCATION,
            castingTime = "1 bonus action",
            range = "60 feet",
            damageOrHealFormula = "1d4+3",
            damageType = "healing",
            classes = listOf("Bard", "Cleric", "Druid"),
            description = "As a bonus action, a creature of your choice that you can see within range regains hit points equal to 1d4 + spell mod."
        ),
        SpellDefinition(
            id = "spell_shield",
            name = "Shield",
            level = 1,
            school = SpellSchool.ABJURATION,
            castingTime = "1 reaction",
            range = "Self",
            duration = "1 round",
            classes = listOf("Sorcerer", "Wizard"),
            description = "An invisible barrier of magical force appears and protects you. You gain a +5 bonus to AC until the start of your next turn."
        ),
        SpellDefinition(
            id = "spell_burning_hands",
            name = "Burning Hands",
            level = 1,
            school = SpellSchool.EVOCATION,
            range = "Self (15-foot cone)",
            damageOrHealFormula = "3d6",
            damageType = "fire",
            savingThrowAbility = Ability.DEX,
            classes = listOf("Sorcerer", "Wizard"),
            description = "A thin sheet of flames shoots from your outstretched fingertips. Each creature in a 15-foot cone takes 3d6 fire damage."
        ),
        SpellDefinition(
            id = "spell_thunderwave",
            name = "Thunderwave",
            level = 1,
            school = SpellSchool.EVOCATION,
            range = "Self (15-foot cube)",
            damageOrHealFormula = "2d8",
            damageType = "thunder",
            savingThrowAbility = Ability.CON,
            classes = listOf("Bard", "Druid", "Sorcerer", "Wizard"),
            description = "A wave of thunderous force sweeps out from you. Each creature takes 2d8 thunder damage and is pushed 10 feet away."
        ),
        SpellDefinition(
            id = "spell_guiding_bolt",
            name = "Guiding Bolt",
            level = 1,
            school = SpellSchool.EVOCATION,
            range = "120 feet",
            damageOrHealFormula = "4d6",
            damageType = "radiant",
            classes = listOf("Cleric"),
            description = "A flash of light streaks toward a creature dealing 4d6 radiant damage. The next attack against it has advantage."
        ),
        SpellDefinition(
            id = "spell_inflict_wounds",
            name = "Inflict Wounds",
            level = 1,
            school = SpellSchool.NECROMANCY,
            range = "Touch",
            damageOrHealFormula = "3d10",
            damageType = "necrotic",
            classes = listOf("Cleric"),
            description = "Make a melee spell attack against a creature you touch. On hit, the target takes 3d10 necrotic damage."
        ),
        SpellDefinition(
            id = "spell_bless",
            name = "Bless",
            level = 1,
            school = SpellSchool.ENCHANTMENT,
            range = "30 feet",
            duration = "Concentration, up to 1 minute",
            requiresConcentration = true,
            classes = listOf("Cleric", "Paladin"),
            description = "You bless up to three creatures. Whenever a target makes an attack roll or saving throw, it can add 1d4 to the roll."
        ),
        SpellDefinition(
            id = "spell_bane",
            name = "Bane",
            level = 1,
            school = SpellSchool.ENCHANTMENT,
            range = "30 feet",
            duration = "Concentration, up to 1 minute",
            requiresConcentration = true,
            savingThrowAbility = Ability.CHA,
            classes = listOf("Bard", "Cleric"),
            description = "Up to three creatures must make Charisma saves. On a fail, targets subtract 1d4 from all attack rolls and saving throws."
        ),
        SpellDefinition(
            id = "spell_mage_armor",
            name = "Mage Armor",
            level = 1,
            school = SpellSchool.ABJURATION,
            range = "Touch",
            duration = "8 hours",
            classes = listOf("Sorcerer", "Wizard"),
            description = "You touch a willing creature who isn't wearing armor. The target's base AC becomes 13 + its Dexterity modifier."
        ),
        SpellDefinition(
            id = "spell_sleep",
            name = "Sleep",
            level = 1,
            school = SpellSchool.ENCHANTMENT,
            range = "90 feet",
            duration = "1 minute",
            classes = listOf("Bard", "Sorcerer", "Wizard"),
            description = "This spell sends creatures into a magical slumber. Roll 5d8; creatures with lowest HP fall unconscious."
        ),
        SpellDefinition(
            id = "spell_command",
            name = "Command",
            level = 1,
            school = SpellSchool.ENCHANTMENT,
            range = "60 feet",
            duration = "1 round",
            savingThrowAbility = Ability.WIS,
            classes = listOf("Cleric", "Paladin"),
            description = "You speak a one-word command to a creature: Drop, Fall, Flee, Grovel, or Halt. Target must follow on failed WIS save."
        ),
        SpellDefinition(
            id = "spell_detect_magic",
            name = "Detect Magic",
            level = 1,
            school = SpellSchool.DIVINATION,
            range = "Self",
            duration = "Concentration, up to 10 minutes",
            requiresConcentration = true,
            classes = listOf("Bard", "Cleric", "Druid", "Paladin", "Ranger", "Sorcerer", "Wizard"),
            description = "For the duration, you sense the presence of magic within 30 feet of you and see a faint aura around magical items."
        ),
        SpellDefinition(
            id = "spell_feather_fall",
            name = "Feather Fall",
            level = 1,
            school = SpellSchool.TRANSMUTATION,
            castingTime = "1 reaction",
            range = "60 feet",
            duration = "1 minute",
            classes = listOf("Bard", "Sorcerer", "Wizard"),
            description = "Choose up to five falling creatures. A falling creature's rate of descent slows to 60 feet per round and takes no damage."
        ),
        SpellDefinition(
            id = "spell_fog_cloud",
            name = "Fog Cloud",
            level = 1,
            school = SpellSchool.CONJURATION,
            range = "120 feet",
            duration = "Concentration, up to 1 hour",
            requiresConcentration = true,
            classes = listOf("Druid", "Ranger", "Sorcerer", "Wizard"),
            description = "You create a 20-foot-radius sphere of fog. The sphere spreads around corners, and its area is heavily obscured."
        ),
        SpellDefinition(
            id = "spell_grease",
            name = "Grease",
            level = 1,
            school = SpellSchool.CONJURATION,
            range = "60 feet",
            duration = "1 minute",
            savingThrowAbility = Ability.DEX,
            classes = listOf("Wizard"),
            description = "Slick grease covers the ground in a 10-foot square. Entering or ending turn in grease requires a DEX save or fall prone."
        ),
        SpellDefinition(
            id = "spell_witch_bolt",
            name = "Witch Bolt",
            level = 1,
            school = SpellSchool.EVOCATION,
            range = "30 feet",
            duration = "Concentration, up to 1 minute",
            requiresConcentration = true,
            damageOrHealFormula = "1d12",
            damageType = "lightning",
            classes = listOf("Sorcerer", "Warlock", "Wizard"),
            description = "A beam of crackling blue energy lances out. On subsequent turns you can automatically deal 1d12 lightning as an action."
        ),
        SpellDefinition(
            id = "spell_hunters_mark",
            name = "Hunter's Mark",
            level = 1,
            school = SpellSchool.DIVINATION,
            castingTime = "1 bonus action",
            range = "90 feet",
            duration = "Concentration, up to 1 hour",
            requiresConcentration = true,
            damageOrHealFormula = "1d6",
            damageType = "weapon",
            classes = listOf("Ranger"),
            description = "You choose each weapon strike against marked target to deal an extra 1d6 damage. You also have advantage to track it."
        ),
        SpellDefinition(
            id = "spell_dissonant_whispers",
            name = "Dissonant Whispers",
            level = 1,
            school = SpellSchool.ENCHANTMENT,
            range = "60 feet",
            damageOrHealFormula = "3d6",
            damageType = "psychic",
            savingThrowAbility = Ability.WIS,
            classes = listOf("Bard"),
            description = "You whisper a discordant melody. Deals 3d6 psychic damage and forces target to immediately flee using its reaction."
        ),

        // --- LEVEL 2 SPELLS ---
        SpellDefinition(
            id = "spell_scorching_ray",
            name = "Scorching Ray",
            level = 2,
            school = SpellSchool.EVOCATION,
            range = "120 feet",
            damageOrHealFormula = "6d6",
            damageType = "fire",
            classes = listOf("Sorcerer", "Wizard"),
            description = "You hurl three rays of fire at targets within range. Each ray deals 2d6 fire damage on hit (total 6d6)."
        ),
        SpellDefinition(
            id = "spell_misty_step",
            name = "Misty Step",
            level = 2,
            school = SpellSchool.CONJURATION,
            castingTime = "1 bonus action",
            range = "Self",
            duration = "Instantaneous",
            classes = listOf("Sorcerer", "Warlock", "Wizard"),
            description = "Briefly surrounded by silvery mist, you teleport up to 30 feet to an unoccupied space that you can see."
        ),
        SpellDefinition(
            id = "spell_hold_person",
            name = "Hold Person",
            level = 2,
            school = SpellSchool.ENCHANTMENT,
            range = "60 feet",
            duration = "Concentration, up to 1 minute",
            requiresConcentration = true,
            savingThrowAbility = Ability.WIS,
            classes = listOf("Bard", "Cleric", "Druid", "Sorcerer", "Warlock", "Wizard"),
            description = "Choose a humanoid within range. Target must succeed on a WIS save or become paralyzed for the duration."
        ),
        SpellDefinition(
            id = "spell_invisibility",
            name = "Invisibility",
            level = 2,
            school = SpellSchool.ILLUSION,
            range = "Touch",
            duration = "Concentration, up to 1 hour",
            requiresConcentration = true,
            classes = listOf("Bard", "Sorcerer", "Warlock", "Wizard"),
            description = "A creature you touch becomes invisible until the spell ends or until the target attacks or casts a spell."
        ),
        SpellDefinition(
            id = "spell_shatter",
            name = "Shatter",
            level = 2,
            school = SpellSchool.EVOCATION,
            range = "60 feet",
            damageOrHealFormula = "3d8",
            damageType = "thunder",
            savingThrowAbility = Ability.CON,
            classes = listOf("Bard", "Sorcerer", "Warlock", "Wizard"),
            description = "A sudden loud ringing noise painfully intense erupts in a 10-foot sphere dealing 3d8 thunder damage."
        ),
        SpellDefinition(
            id = "spell_spiritual_weapon",
            name = "Spiritual Weapon",
            level = 2,
            school = SpellSchool.EVOCATION,
            castingTime = "1 bonus action",
            range = "60 feet",
            duration = "1 minute",
            damageOrHealFormula = "1d8+3",
            damageType = "force",
            classes = listOf("Cleric"),
            description = "You create a floating spectral weapon. You can strike as a bonus action dealing 1d8 + spell mod force damage."
        ),
        SpellDefinition(
            id = "spell_lesser_restoration",
            name = "Lesser Restoration",
            level = 2,
            school = SpellSchool.ABJURATION,
            range = "Touch",
            classes = listOf("Bard", "Cleric", "Druid", "Paladin", "Ranger"),
            description = "You touch a creature and end either one disease or one condition afflicting it: blinded, deafened, paralyzed, or poisoned."
        ),
        SpellDefinition(
            id = "spell_aid",
            name = "Aid",
            level = 2,
            school = SpellSchool.ABJURATION,
            range = "30 feet",
            duration = "8 hours",
            classes = listOf("Cleric", "Paladin"),
            description = "Bolster allies with divine toughness. Up to three creatures gain +5 to their current HP and maximum HP for 8 hours."
        ),
        SpellDefinition(
            id = "spell_mirror_image",
            name = "Mirror Image",
            level = 2,
            school = SpellSchool.ILLUSION,
            range = "Self",
            duration = "1 minute",
            classes = listOf("Sorcerer", "Warlock", "Wizard"),
            description = "Three illusory duplicates of yourself appear. Attacks that would hit you may hit a duplicate instead."
        ),
        SpellDefinition(
            id = "spell_web",
            name = "Web",
            level = 2,
            school = SpellSchool.CONJURATION,
            range = "60 feet",
            duration = "Concentration, up to 1 hour",
            requiresConcentration = true,
            savingThrowAbility = Ability.DEX,
            classes = listOf("Sorcerer", "Wizard"),
            description = "You conjure a mass of thick, sticky webbing in a 20-foot cube. Creatures caught in it are restrained on a failed DEX save."
        ),
        SpellDefinition(
            id = "spell_darkness",
            name = "Darkness",
            level = 2,
            school = SpellSchool.EVOCATION,
            range = "60 feet",
            duration = "Concentration, up to 10 minutes",
            requiresConcentration = true,
            classes = listOf("Sorcerer", "Warlock", "Wizard"),
            description = "Magical darkness spreads from a point to fill a 15-foot-radius sphere. Darkvision cannot see through this darkness."
        ),
        SpellDefinition(
            id = "spell_pass_without_trace",
            name = "Pass without Trace",
            level = 2,
            school = SpellSchool.ABJURATION,
            range = "Self",
            duration = "Concentration, up to 1 hour",
            requiresConcentration = true,
            classes = listOf("Druid", "Ranger"),
            description = "A veil of shadows radiates from you. You and companions within 30 feet receive a +10 bonus to Dexterity (Stealth) checks."
        ),

        // --- LEVEL 3 SPELLS ---
        SpellDefinition(
            id = "spell_fireball",
            name = "Fireball",
            level = 3,
            school = SpellSchool.EVOCATION,
            range = "150 feet",
            damageOrHealFormula = "8d6",
            damageType = "fire",
            savingThrowAbility = Ability.DEX,
            classes = listOf("Sorcerer", "Wizard"),
            description = "A bright streak flashes to a point and blooms into an explosion of flame. Each creature in 20-ft radius takes 8d6 fire."
        ),
        SpellDefinition(
            id = "spell_lightning_bolt",
            name = "Lightning Bolt",
            level = 3,
            school = SpellSchool.EVOCATION,
            range = "Self (100-foot line)",
            damageOrHealFormula = "8d6",
            damageType = "lightning",
            savingThrowAbility = Ability.DEX,
            classes = listOf("Sorcerer", "Wizard"),
            description = "A stroke of lightning forming a line 100 feet long and 5 feet wide blasts from you. Deals 8d6 lightning damage."
        ),
        SpellDefinition(
            id = "spell_counterspell",
            name = "Counterspell",
            level = 3,
            school = SpellSchool.ABJURATION,
            castingTime = "1 reaction",
            range = "60 feet",
            classes = listOf("Sorcerer", "Warlock", "Wizard"),
            description = "You attempt to interrupt a creature in the process of casting a spell. If casting spell of 3rd or lower, it fails."
        ),
        SpellDefinition(
            id = "spell_dispel_magic",
            name = "Dispel Magic",
            level = 3,
            school = SpellSchool.ABJURATION,
            range = "120 feet",
            classes = listOf("Bard", "Cleric", "Druid", "Paladin", "Sorcerer", "Warlock", "Wizard"),
            description = "Choose any creature, object, or magical effect within range. Any spell of 3rd level or lower on the target ends."
        ),
        SpellDefinition(
            id = "spell_revivify",
            name = "Revivify",
            level = 3,
            school = SpellSchool.NECROMANCY,
            range = "Touch",
            classes = listOf("Cleric", "Paladin"),
            description = "You touch a creature that has died within the last minute. That creature returns to life with 1 hit point."
        ),
        SpellDefinition(
            id = "spell_haste",
            name = "Haste",
            level = 3,
            school = SpellSchool.TRANSMUTATION,
            range = "30 feet",
            duration = "Concentration, up to 1 minute",
            requiresConcentration = true,
            classes = listOf("Sorcerer", "Wizard"),
            description = "Choose a willing creature. Its speed is doubled, it gains +2 to AC, advantage on DEX saves, and an extra action each turn."
        ),
        SpellDefinition(
            id = "spell_fly",
            name = "Fly",
            level = 3,
            school = SpellSchool.TRANSMUTATION,
            range = "Touch",
            duration = "Concentration, up to 10 minutes",
            requiresConcentration = true,
            classes = listOf("Sorcerer", "Warlock", "Wizard"),
            description = "You touch a willing creature. The target gains a flying speed of 60 feet for the duration."
        ),
        SpellDefinition(
            id = "spell_spirit_guardians",
            name = "Spirit Guardians",
            level = 3,
            school = SpellSchool.CONJURATION,
            range = "Self (15-foot radius)",
            duration = "Concentration, up to 10 minutes",
            requiresConcentration = true,
            damageOrHealFormula = "3d8",
            damageType = "radiant",
            savingThrowAbility = Ability.WIS,
            classes = listOf("Cleric"),
            description = "Spirits flit around you. Hostiles in area have speed halved and take 3d8 radiant (or necrotic) damage on failed WIS save."
        ),
        SpellDefinition(
            id = "spell_mass_healing_word",
            name = "Mass Healing Word",
            level = 3,
            school = SpellSchool.EVOCATION,
            castingTime = "1 bonus action",
            range = "60 feet",
            damageOrHealFormula = "1d4+3",
            damageType = "healing",
            classes = listOf("Cleric"),
            description = "As you call out words of restoration, up to six creatures of your choice that you can see regain 1d4 + spell mod HP."
        ),
        SpellDefinition(
            id = "spell_hypnotic_pattern",
            name = "Hypnotic Pattern",
            level = 3,
            school = SpellSchool.ILLUSION,
            range = "120 feet",
            duration = "Concentration, up to 1 minute",
            requiresConcentration = true,
            savingThrowAbility = Ability.WIS,
            classes = listOf("Bard", "Sorcerer", "Warlock", "Wizard"),
            description = "You create a twisting pattern of colors that weaves through the air in a 30-foot cube. Creatures failing save are incapacitated."
        ),

        // --- LEVEL 4 SPELLS ---
        SpellDefinition(
            id = "spell_banishment",
            name = "Banishment",
            level = 4,
            school = SpellSchool.ABJURATION,
            range = "60 feet",
            duration = "Concentration, up to 1 minute",
            requiresConcentration = true,
            savingThrowAbility = Ability.CHA,
            classes = listOf("Cleric", "Paladin", "Sorcerer", "Warlock", "Wizard"),
            description = "You attempt to send one creature to another plane of existence. The target must succeed on a Charisma save or be banished."
        ),
        SpellDefinition(
            id = "spell_dimension_door",
            name = "Dimension Door",
            level = 4,
            school = SpellSchool.CONJURATION,
            range = "500 feet",
            classes = listOf("Bard", "Sorcerer", "Warlock", "Wizard"),
            description = "You teleport yourself from your current location to any other spot within range, bringing along one willing creature."
        ),
        SpellDefinition(
            id = "spell_polymorph",
            name = "Polymorph",
            level = 4,
            school = SpellSchool.TRANSMUTATION,
            range = "60 feet",
            duration = "Concentration, up to 1 hour",
            requiresConcentration = true,
            savingThrowAbility = Ability.WIS,
            classes = listOf("Bard", "Druid", "Sorcerer", "Wizard"),
            description = "This spell transforms a creature into a new beast form. The target's game statistics are replaced by the beast's statistics."
        ),
        SpellDefinition(
            id = "spell_greater_invisibility",
            name = "Greater Invisibility",
            level = 4,
            school = SpellSchool.ILLUSION,
            range = "Touch",
            duration = "Concentration, up to 1 minute",
            requiresConcentration = true,
            classes = listOf("Bard", "Sorcerer", "Wizard"),
            description = "You or a creature you touch becomes invisible. Unlike normal invisibility, attacking or casting spells does not end this effect."
        ),
        SpellDefinition(
            id = "spell_wall_of_fire",
            name = "Wall of Fire",
            level = 4,
            school = SpellSchool.EVOCATION,
            range = "120 feet",
            duration = "Concentration, up to 1 minute",
            requiresConcentration = true,
            damageOrHealFormula = "5d8",
            damageType = "fire",
            savingThrowAbility = Ability.DEX,
            classes = listOf("Druid", "Sorcerer", "Wizard"),
            description = "You create a wall of fire on a solid surface. Creatures passing through or near the wall take 5d8 fire damage."
        ),
        SpellDefinition(
            id = "spell_blight",
            name = "Blight",
            level = 4,
            school = SpellSchool.NECROMANCY,
            range = "30 feet",
            damageOrHealFormula = "8d8",
            damageType = "necrotic",
            savingThrowAbility = Ability.CON,
            classes = listOf("Druid", "Sorcerer", "Warlock", "Wizard"),
            description = "Necromantic energy washes over a creature, withering its moisture and vitality. Deals 8d8 necrotic damage on failed CON save."
        ),
        SpellDefinition(
            id = "spell_stoneskin",
            name = "Stoneskin",
            level = 4,
            school = SpellSchool.ABJURATION,
            range = "Touch",
            duration = "Concentration, up to 1 hour",
            requiresConcentration = true,
            classes = listOf("Druid", "Ranger", "Sorcerer", "Wizard"),
            description = "This spell turns the flesh of a willing creature you touch as hard as stone, giving resistance to nonmagical physical damage."
        ),

        // --- LEVEL 5 SPELLS ---
        SpellDefinition(
            id = "spell_cone_of_cold",
            name = "Cone of Cold",
            level = 5,
            school = SpellSchool.EVOCATION,
            range = "Self (60-foot cone)",
            damageOrHealFormula = "8d8",
            damageType = "cold",
            savingThrowAbility = Ability.CON,
            classes = listOf("Sorcerer", "Wizard"),
            description = "A blast of cold air erupts from your hands. Each creature in a 60-foot cone takes 8d8 cold damage."
        ),
        SpellDefinition(
            id = "spell_mass_cure_wounds",
            name = "Mass Cure Wounds",
            level = 5,
            school = SpellSchool.EVOCATION,
            range = "60 feet",
            damageOrHealFormula = "3d8+4",
            damageType = "healing",
            classes = listOf("Bard", "Cleric", "Druid"),
            description = "A wave of healing energy washes out from a point. Up to six creatures in a 30-foot sphere regain 3d8 + spell mod HP."
        ),
        SpellDefinition(
            id = "spell_hold_monster",
            name = "Hold Monster",
            level = 5,
            school = SpellSchool.ENCHANTMENT,
            range = "90 feet",
            duration = "Concentration, up to 1 minute",
            requiresConcentration = true,
            savingThrowAbility = Ability.WIS,
            classes = listOf("Bard", "Sorcerer", "Warlock", "Wizard"),
            description = "Choose a creature that you can see within range. The target must succeed on a WIS save or be paralyzed for the duration."
        ),
        SpellDefinition(
            id = "spell_flame_strike",
            name = "Flame Strike",
            level = 5,
            school = SpellSchool.EVOCATION,
            range = "60 feet",
            damageOrHealFormula = "8d6",
            damageType = "fire/radiant",
            savingThrowAbility = Ability.DEX,
            classes = listOf("Cleric"),
            description = "A vertical column of divine fire roars down from the heavens. Deals 4d6 fire and 4d6 radiant damage in a 10-foot radius."
        ),
        SpellDefinition(
            id = "spell_greater_restoration",
            name = "Greater Restoration",
            level = 5,
            school = SpellSchool.ABJURATION,
            range = "Touch",
            classes = listOf("Bard", "Cleric", "Druid"),
            description = "You imbue a creature with positive energy to undo debilitating effects: one exhaustion level, charmed/petrified, or curses."
        )
    )

    fun getAllSpells(): List<SpellDefinition> = CANTRIPS + LEVELED_SPELLS
    fun getCantrips(): List<SpellDefinition> = CANTRIPS
    fun getLeveledSpells(): List<SpellDefinition> = LEVELED_SPELLS

    fun findById(id: String): SpellDefinition? =
        getAllSpells().firstOrNull { it.id.equals(id, ignoreCase = true) }

    fun findByName(name: String): SpellDefinition? =
        getAllSpells().firstOrNull { it.name.equals(name, ignoreCase = true) }
}
