package com.grimoire.dnd.engine

data class WeaponProperty(val code: String, val label: String)

data class WeaponDefinition(
    val id: String,
    val name: String,
    val category: String, // "Simple Melee", "Simple Ranged", "Martial Melee", "Martial Ranged", "Magical"
    val damageFormula: String,
    val damageType: String, // "slashing", "piercing", "bludgeoning"
    val weightLbs: Double,
    val valueGp: Int,
    val properties: List<String>,
    val description: String = ""
)

data class ArmorAndGearDefinition(
    val id: String,
    val name: String,
    val itemType: ItemType,
    val baseAc: Int = 0,
    val acBonus: Int = 0,
    val strRequirement: Int = 0,
    val stealthDisadvantage: Boolean = false,
    val weightLbs: Double = 1.0,
    val valueGp: Int = 10,
    val description: String = ""
)

object SRD5eEquipmentDatabase {

    // ==========================================
    // 50+ WEAPONS
    // ==========================================
    val WEAPONS: List<WeaponDefinition> = listOf(
        // Simple Melee (12)
        WeaponDefinition("wpn_club", "Club", "Simple Melee", "1d4", "bludgeoning", 2.0, 1, listOf("Light")),
        WeaponDefinition("wpn_dagger", "Dagger", "Simple Melee", "1d4", "piercing", 1.0, 2, listOf("Finesse", "Light", "Thrown (20/60)")),
        WeaponDefinition("wpn_greatclub", "Greatclub", "Simple Melee", "1d8", "bludgeoning", 10.0, 1, listOf("Two-Handed")),
        WeaponDefinition("wpn_handaxe", "Handaxe", "Simple Melee", "1d6", "slashing", 2.0, 5, listOf("Light", "Thrown (20/60)")),
        WeaponDefinition("wpn_javelin", "Javelin", "Simple Melee", "1d6", "piercing", 2.0, 1, listOf("Thrown (30/120)")),
        WeaponDefinition("wpn_light_hammer", "Light Hammer", "Simple Melee", "1d4", "bludgeoning", 2.0, 2, listOf("Light", "Thrown (20/60)")),
        WeaponDefinition("wpn_mace", "Mace", "Simple Melee", "1d6", "bludgeoning", 4.0, 5, emptyList()),
        WeaponDefinition("wpn_quarterstaff", "Quarterstaff", "Simple Melee", "1d6", "bludgeoning", 4.0, 1, listOf("Versatile (1d8)")),
        WeaponDefinition("wpn_sickle", "Sickle", "Simple Melee", "1d4", "slashing", 2.0, 1, listOf("Light")),
        WeaponDefinition("wpn_spear", "Spear", "Simple Melee", "1d6", "piercing", 3.0, 1, listOf("Thrown (20/60)", "Versatile (1d8)")),
        WeaponDefinition("wpn_yklwa", "Yklwa", "Simple Melee", "1d8", "piercing", 2.0, 1, listOf("Thrown (10/30)")),
        WeaponDefinition("wpn_cestus", "Spiked Cestus", "Simple Melee", "1d4", "bludgeoning", 1.0, 5, listOf("Finesse", "Light")),

        // Simple Ranged (5)
        WeaponDefinition("wpn_light_crossbow", "Light Crossbow", "Simple Ranged", "1d8", "piercing", 5.0, 25, listOf("Ammunition (80/320)", "Loading", "Two-Handed")),
        WeaponDefinition("wpn_dart", "Dart", "Simple Ranged", "1d4", "piercing", 0.25, 1, listOf("Finesse", "Thrown (20/60)")),
        WeaponDefinition("wpn_shortbow", "Shortbow", "Simple Ranged", "1d6", "piercing", 2.0, 25, listOf("Ammunition (80/320)", "Two-Handed")),
        WeaponDefinition("wpn_sling", "Sling", "Simple Ranged", "1d4", "bludgeoning", 0.0, 1, listOf("Ammunition (30/120)")),
        WeaponDefinition("wpn_blowgun", "Blowgun", "Simple Ranged", "1", "piercing", 1.0, 10, listOf("Ammunition (25/100)", "Loading")),

        // Martial Melee (18)
        WeaponDefinition("wpn_battleaxe", "Battleaxe", "Martial Melee", "1d8", "slashing", 4.0, 10, listOf("Versatile (1d10)")),
        WeaponDefinition("wpn_flail", "Flail", "Martial Melee", "1d8", "bludgeoning", 2.0, 10, emptyList()),
        WeaponDefinition("wpn_glaive", "Glaive", "Martial Melee", "1d10", "slashing", 6.0, 20, listOf("Heavy", "Reach", "Two-Handed")),
        WeaponDefinition("wpn_greataxe", "Greataxe", "Martial Melee", "1d12", "slashing", 7.0, 30, listOf("Heavy", "Two-Handed")),
        WeaponDefinition("wpn_greatsword", "Greatsword", "Martial Melee", "2d6", "slashing", 6.0, 50, listOf("Heavy", "Two-Handed")),
        WeaponDefinition("wpn_halberd", "Halberd", "Martial Melee", "1d10", "slashing", 6.0, 20, listOf("Heavy", "Reach", "Two-Handed")),
        WeaponDefinition("wpn_lance", "Lance", "Martial Melee", "1d12", "piercing", 6.0, 10, listOf("Reach", "Special")),
        WeaponDefinition("wpn_longsword", "Longsword", "Martial Melee", "1d8", "slashing", 3.0, 15, listOf("Versatile (1d10)")),
        WeaponDefinition("wpn_maul", "Maul", "Martial Melee", "2d6", "bludgeoning", 10.0, 10, listOf("Heavy", "Two-Handed")),
        WeaponDefinition("wpn_morningstar", "Morningstar", "Martial Melee", "1d8", "piercing", 4.0, 15, emptyList()),
        WeaponDefinition("wpn_pike", "Pike", "Martial Melee", "1d10", "piercing", 18.0, 5, listOf("Heavy", "Reach", "Two-Handed")),
        WeaponDefinition("wpn_rapier", "Rapier", "Martial Melee", "1d8", "piercing", 2.0, 25, listOf("Finesse")),
        WeaponDefinition("wpn_scimitar", "Scimitar", "Martial Melee", "1d6", "slashing", 3.0, 25, listOf("Finesse", "Light")),
        WeaponDefinition("wpn_shortsword", "Shortsword", "Martial Melee", "1d6", "piercing", 2.0, 10, listOf("Finesse", "Light")),
        WeaponDefinition("wpn_trident", "Trident", "Martial Melee", "1d6", "piercing", 4.0, 5, listOf("Thrown (20/60)", "Versatile (1d8)")),
        WeaponDefinition("wpn_war_pick", "War Pick", "Martial Melee", "1d8", "piercing", 2.0, 5, emptyList()),
        WeaponDefinition("wpn_warhammer", "Warhammer", "Martial Melee", "1d8", "bludgeoning", 2.0, 15, listOf("Versatile (1d10)")),
        WeaponDefinition("wpn_whip", "Whip", "Martial Melee", "1d4", "slashing", 3.0, 2, listOf("Finesse", "Reach")),

        // Martial Ranged (4)
        WeaponDefinition("wpn_heavy_crossbow", "Heavy Crossbow", "Martial Ranged", "1d10", "piercing", 18.0, 50, listOf("Ammunition (100/400)", "Heavy", "Loading", "Two-Handed")),
        WeaponDefinition("wpn_longbow", "Longbow", "Martial Ranged", "1d8", "piercing", 2.0, 50, listOf("Ammunition (150/600)", "Heavy", "Two-Handed")),
        WeaponDefinition("wpn_hand_crossbow", "Hand Crossbow", "Martial Ranged", "1d6", "piercing", 3.0, 75, listOf("Ammunition (30/120)", "Light", "Loading")),
        WeaponDefinition("wpn_net", "Net", "Martial Ranged", "0", "special", 3.0, 1, listOf("Special", "Thrown (5/15)")),

        // Enchanted, Relic & Rare Weapons (15)
        WeaponDefinition("wpn_plus1_dagger", "+1 Arcane Dagger", "Magical", "1d4+1", "piercing", 1.0, 500, listOf("Finesse", "Light", "Magical")),
        WeaponDefinition("wpn_flame_tongue", "Flame Tongue Greatsword", "Magical", "2d6+2d6", "fire", 6.0, 2500, listOf("Heavy", "Two-Handed", "Fire Burst")),
        WeaponDefinition("wpn_frost_brand", "Frost Brand Rapier", "Magical", "1d8+1d6", "cold", 2.0, 3000, listOf("Finesse", "Cold Resistance", "Magical")),
        WeaponDefinition("wpn_sun_blade", "Sun Blade", "Magical", "1d8+2", "radiant", 2.0, 5000, listOf("Finesse", "Versatile (1d10)", "Pure Sunlight")),
        WeaponDefinition("wpn_dragonslayer", "Dragonslayer Longsword", "Magical", "1d8+3d6", "slashing", 3.0, 4000, listOf("Versatile (1d10)", "Dragon Bane")),
        WeaponDefinition("wpn_mace_disruption", "Mace of Disruption", "Magical", "1d6+2d6", "radiant", 4.0, 3500, listOf("Fiend & Undead Bane")),
        WeaponDefinition("wpn_dagger_venom", "Dagger of Venom", "Magical", "1d4+2d10", "poison", 1.0, 1800, listOf("Finesse", "Black Drip Poison")),
        WeaponDefinition("wpn_oathbow", "Oathbow", "Magical", "1d8+3d6", "piercing", 2.0, 6000, listOf("Sworn Enemy", "Advantage")),
        WeaponDefinition("wpn_holy_avenger", "Holy Avenger Greatsword", "Magical", "2d6+2d10", "radiant", 6.0, 15000, listOf("Paladin Bond", "Magic Resistance Aura")),
        WeaponDefinition("wpn_dwarven_thrower", "Dwarven Thrower Warhammer", "Magical", "1d8+2d8", "bludgeoning", 2.0, 8000, listOf("Returning", "Thrown (20/60)")),
        WeaponDefinition("wpn_scimitar_speed", "Scimitar of Speed", "Magical", "1d6+2", "slashing", 3.0, 4500, listOf("Bonus Action Attack", "Finesse")),
        WeaponDefinition("wpn_hammer_thunderbolts", "Hammer of Thunderbolts", "Magical", "2d6+4", "thunder", 15.0, 20000, listOf("Giant Strength", "Thunderclap")),
        WeaponDefinition("wpn_staff_striking", "Staff of Striking", "Magical", "1d6+3", "force", 4.0, 5000, listOf("Force Burst", "Versatile (1d8)")),
        WeaponDefinition("wpn_javelin_lightning", "Javelin of Lightning", "Magical", "1d6+4d6", "lightning", 2.0, 1200, listOf("Lightning Bolt (120 ft line)")),
        WeaponDefinition("wpn_vorpal_sword", "Vorpal Longsword", "Magical", "1d8+3", "slashing", 3.0, 30000, listOf("Decapitation on Nat 20", "Versatile (1d10)"))
    )

    // ==========================================
    // 50+ ARMOR & EQUIPMENT
    // ==========================================
    val ARMOR_AND_GEAR: List<ArmorAndGearDefinition> = listOf(
        // Light Armor (3)
        ArmorAndGearDefinition("arm_padded", "Padded Armor", ItemType.ARMOR, baseAc = 11, stealthDisadvantage = true, weightLbs = 8.0, valueGp = 5, description = "Quilted layers of cloth and batting."),
        ArmorAndGearDefinition("arm_leather", "Leather Armor", ItemType.ARMOR, baseAc = 11, weightLbs = 10.0, valueGp = 10, description = "Cured stiff leather breastpiece and greaves."),
        ArmorAndGearDefinition("arm_studded_leather", "Studded Leather Armor", ItemType.ARMOR, baseAc = 12, weightLbs = 13.0, valueGp = 45, description = "Tough leather reinforced with close-set metal rivets."),

        // Medium Armor (5)
        ArmorAndGearDefinition("arm_hide", "Hide Armor", ItemType.ARMOR, baseAc = 12, weightLbs = 12.0, valueGp = 10, description = "Crude armor made from thick furs and pelts."),
        ArmorAndGearDefinition("arm_chain_shirt", "Chain Shirt", ItemType.ARMOR, baseAc = 13, weightLbs = 20.0, valueGp = 50, description = "Interlocking metal rings worn between layers of cloth."),
        ArmorAndGearDefinition("arm_scale_mail", "Scale Mail", ItemType.ARMOR, baseAc = 14, stealthDisadvantage = true, weightLbs = 45.0, valueGp = 50, description = "Overlapping scales of bronze or iron."),
        ArmorAndGearDefinition("arm_breastplate", "Breastplate", ItemType.ARMOR, baseAc = 14, weightLbs = 20.0, valueGp = 400, description = "Fitted metal chest piece with leather backing."),
        ArmorAndGearDefinition("arm_half_plate", "Half Plate", ItemType.ARMOR, baseAc = 15, stealthDisadvantage = true, weightLbs = 40.0, valueGp = 750, description = "Shaped metal plates covering most of the body."),

        // Heavy Armor (4)
        ArmorAndGearDefinition("arm_ring_mail", "Ring Mail", ItemType.ARMOR, baseAc = 14, stealthDisadvantage = true, weightLbs = 40.0, valueGp = 30, description = "Leather armor with heavy iron rings sewn into it."),
        ArmorAndGearDefinition("arm_chain_mail", "Chain Mail", ItemType.ARMOR, baseAc = 16, strRequirement = 13, stealthDisadvantage = true, weightLbs = 55.0, valueGp = 75, description = "Made of interlocking metal rings covering the whole torso and limbs."),
        ArmorAndGearDefinition("arm_splint", "Splint Armor", ItemType.ARMOR, baseAc = 17, strRequirement = 15, stealthDisadvantage = true, weightLbs = 60.0, valueGp = 200, description = "Vertical strips of metal riveted to a leather backing."),
        ArmorAndGearDefinition("arm_plate", "Full Plate", ItemType.ARMOR, baseAc = 18, strRequirement = 15, stealthDisadvantage = true, weightLbs = 65.0, valueGp = 1500, description = "Fitted interlocking steel plates covering the entire body."),

        // Shields (4)
        ArmorAndGearDefinition("shd_wooden", "Wooden Round Shield", ItemType.SHIELD, acBonus = 2, weightLbs = 6.0, valueGp = 10, description = "Oak wood rimmed with beaten copper."),
        ArmorAndGearDefinition("shd_steel", "Knight's Steel Shield", ItemType.SHIELD, acBonus = 2, weightLbs = 6.0, valueGp = 20, description = "Polished heraldic steel heater shield."),
        ArmorAndGearDefinition("shd_buckler", "Duelist Buckler", ItemType.SHIELD, acBonus = 1, weightLbs = 2.0, valueGp = 15, description = "Small round metal shield strapped to the forearm."),
        ArmorAndGearDefinition("shd_tower", "Tower Pavilion Shield", ItemType.SHIELD, acBonus = 2, strRequirement = 13, weightLbs = 15.0, valueGp = 35, description = "Massive full-body pavise providing half cover when planted."),

        // Potions & Consumables (10)
        ArmorAndGearDefinition("pot_healing", "Potion of Healing", ItemType.POTION, valueGp = 50, description = "Magical red draught. Drinking it restores 2d4 + 2 hit points."),
        ArmorAndGearDefinition("pot_greater_healing", "Potion of Greater Healing", ItemType.POTION, valueGp = 150, description = "Deep crimson potion. Restores 4d4 + 4 hit points."),
        ArmorAndGearDefinition("pot_superior_healing", "Potion of Superior Healing", ItemType.POTION, valueGp = 500, description = "Glowing ruby elixir. Restores 8d4 + 8 hit points."),
        ArmorAndGearDefinition("pot_supreme_healing", "Potion of Supreme Healing", ItemType.POTION, valueGp = 1500, description = "Liquid light. Restores 10d4 + 20 hit points."),
        ArmorAndGearDefinition("pot_speed", "Potion of Speed", ItemType.POTION, valueGp = 400, description = "Yellow fluid. Grants Haste effect for 1 minute without concentration."),
        ArmorAndGearDefinition("pot_invisibility", "Potion of Invisibility", ItemType.POTION, valueGp = 500, description = "Clear liquid that renders the drinker invisible for 1 hour."),
        ArmorAndGearDefinition("pot_flying", "Potion of Flying", ItemType.POTION, valueGp = 500, description = "Effervescent sky-blue draft granting a 60 ft fly speed for 1 hour."),
        ArmorAndGearDefinition("pot_giant_strength", "Potion of Hill Giant Strength", ItemType.POTION, valueGp = 300, description = "Sets Strength score to 21 for 1 hour."),
        ArmorAndGearDefinition("pot_antidote", "Antidote Draught", ItemType.POTION, valueGp = 25, description = "Neutralizes all natural and magical poisons afflicting the consumer."),
        ArmorAndGearDefinition("pot_water_breathing", "Potion of Water Breathing", ItemType.POTION, valueGp = 100, description = "Allows breathing underwater for 24 hours."),

        // Adventuring Tools & Kits (15)
        ArmorAndGearDefinition("gear_backpack", "Leather Backpack", ItemType.VALUABLE, weightLbs = 5.0, valueGp = 2, description = "Sturdy pack capable of holding 30 lbs of adventuring gear."),
        ArmorAndGearDefinition("gear_bedroll", "Fleece Bedroll", ItemType.VALUABLE, weightLbs = 7.0, valueGp = 1, description = "Warm woolen bedding essential for field rests."),
        ArmorAndGearDefinition("gear_crowbar", "Iron Crowbar", ItemType.VALUABLE, weightLbs = 5.0, valueGp = 2, description = "Grants advantage to Strength checks to pry open doors or chests."),
        ArmorAndGearDefinition("gear_grappling_hook", "Steel Grappling Hook", ItemType.VALUABLE, weightLbs = 4.0, valueGp = 2, description = "Three-pronged hook for scaling vertical ramparts."),
        ArmorAndGearDefinition("gear_hempen_rope", "Hempen Rope (50 ft)", ItemType.VALUABLE, weightLbs = 10.0, valueGp = 1, description = "Braided hemp rope holding up to 500 lbs."),
        ArmorAndGearDefinition("gear_silk_rope", "Silk Rope (50 ft)", ItemType.VALUABLE, weightLbs = 5.0, valueGp = 10, description = "Supple elven silk rope with high tensile strength."),
        ArmorAndGearDefinition("gear_lantern_bullseye", "Bullseye Lantern", ItemType.VALUABLE, weightLbs = 2.0, valueGp = 10, description = "Casts a 60-foot cone of bright light and 60-foot dim light."),
        ArmorAndGearDefinition("gear_torches", "Torches (bundle of 5)", ItemType.VALUABLE, weightLbs = 5.0, valueGp = 1, description = "Burns for 1 hour each, shedding 20-foot radius bright light."),
        ArmorAndGearDefinition("gear_tinderbox", "Flint & Tinderbox", ItemType.VALUABLE, weightLbs = 1.0, valueGp = 1, description = "Flint, steel, and tinder used to kindle fires."),
        ArmorAndGearDefinition("gear_thieves_tools", "Thieves' Tools", ItemType.VALUABLE, weightLbs = 1.0, valueGp = 25, description = "Lockpicks, tension wrenches, and miniature mirrors for locks and traps."),
        ArmorAndGearDefinition("gear_healer_kit", "Healer's Kit", ItemType.VALUABLE, weightLbs = 3.0, valueGp = 5, description = "10 uses. Stabilizes a dying creature without requiring a Medicine check."),
        ArmorAndGearDefinition("gear_spellbook", "Blank Leather Spellbook", ItemType.VALUABLE, weightLbs = 3.0, valueGp = 50, description = "100 vellum pages bound in dragonhide for recording spells."),
        ArmorAndGearDefinition("gear_spyglass", "Brass Spyglass", ItemType.VALUABLE, weightLbs = 1.0, valueGp = 1000, description = "Magnifies distant objects by a factor of 3."),
        ArmorAndGearDefinition("gear_rations", "Trail Rations (5 Days)", ItemType.VALUABLE, weightLbs = 10.0, valueGp = 3, description = "Dried meats, hardtack biscuits, nuts, and dried fruit."),
        ArmorAndGearDefinition("gear_waterskin", "Sealed Waterskin", ItemType.VALUABLE, weightLbs = 5.0, valueGp = 1, description = "Holds 4 pints of drinkable fresh water."),

        // Wondrous Relics & Magical Gear (15)
        ArmorAndGearDefinition("relic_bag_of_holding", "Bag of Holding", ItemType.VALUABLE, weightLbs = 15.0, valueGp = 500, description = "Interior space holding up to 500 lbs while weighing only 15 lbs."),
        ArmorAndGearDefinition("relic_boots_elvenkind", "Boots of Elvenkind", ItemType.ARMOR, valueGp = 600, description = "Your footsteps make no sound, granting advantage on Stealth checks."),
        ArmorAndGearDefinition("relic_cloak_protection", "Cloak of Protection", ItemType.ARMOR, acBonus = 1, valueGp = 800, description = "You gain a +1 bonus to AC and all saving throws while wearing this cloak."),
        ArmorAndGearDefinition("relic_ring_protection", "Ring of Protection", ItemType.VALUABLE, acBonus = 1, valueGp = 1000, description = "Gilded ring granting +1 to AC and all saves."),
        ArmorAndGearDefinition("relic_amulet_health", "Amulet of Health", ItemType.VALUABLE, valueGp = 4000, description = "Your Constitution score becomes 19 while wearing this amulet."),
        ArmorAndGearDefinition("relic_bracers_defense", "Bracers of Defense", ItemType.ARMOR, acBonus = 2, valueGp = 2000, description = "+2 bonus to AC while wearing no armor and using no shield."),
        ArmorAndGearDefinition("relic_pearl_power", "Pearl of Power", ItemType.VALUABLE, valueGp = 1200, description = "Once per day, as an action you can regain one expended spell slot of up to 3rd level."),
        ArmorAndGearDefinition("relic_wand_magic_missiles", "Wand of Magic Missiles", ItemType.VALUABLE, valueGp = 1500, description = "Contains 7 charges to cast Magic Missile without expending spell slots."),
        ArmorAndGearDefinition("relic_goggles_night", "Goggles of Night", ItemType.VALUABLE, valueGp = 750, description = "Grants darkvision out to a range of 60 feet in nonmagical darkness."),
        ArmorAndGearDefinition("relic_periapt_wound_closure", "Periapt of Wound Closure", ItemType.VALUABLE, valueGp = 850, description = "Stabilizes automatically at the start of your turn when at 0 HP."),
        ArmorAndGearDefinition("relic_ring_warmth", "Ring of Warmth", ItemType.VALUABLE, valueGp = 1000, description = "Grants resistance to cold damage and immunity to extreme freezing temperatures."),
        ArmorAndGearDefinition("relic_broom_flying", "Broom of Flying", ItemType.VALUABLE, valueGp = 2500, description = "Flies at 50 ft speed carrying up to 400 lbs on command."),
        ArmorAndGearDefinition("relic_gauntlets_ogre_power", "Gauntlets of Ogre Power", ItemType.ARMOR, valueGp = 1800, description = "Your Strength score becomes 19 while wearing these iron gauntlets."),
        ArmorAndGearDefinition("relic_headband_intellect", "Headband of Intellect", ItemType.ARMOR, valueGp = 1800, description = "Your Intelligence score becomes 19 while wearing this silver diadem."),
        ArmorAndGearDefinition("relic_cloak_elvenkind", "Cloak of Elvenkind", ItemType.ARMOR, valueGp = 1200, description = "Shifts color to match environment; confers advantage on Stealth checks.")
    )

    fun getAllWeapons(): List<WeaponDefinition> = WEAPONS
    fun getAllArmorAndGear(): List<ArmorAndGearDefinition> = ARMOR_AND_GEAR

    fun findWeaponById(id: String): WeaponDefinition? =
        WEAPONS.firstOrNull { it.id.equals(id, ignoreCase = true) }

    fun findGearById(id: String): ArmorAndGearDefinition? =
        ARMOR_AND_GEAR.firstOrNull { it.id.equals(id, ignoreCase = true) }
}
