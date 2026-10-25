package com.grimoire.dnd.engine

import org.junit.Assert.*
import org.junit.Test

class MassiveAssetAndContentExpansionTest {

    @Test
    fun testCantripCatalogMeetsFiftyPlusThreshold() {
        val cantrips = SRD5eSpellDatabase.getCantrips()
        assertTrue("Expected 50+ cantrips, found ${cantrips.size}", cantrips.size >= 50)

        // Verify validity of cantrips
        for (c in cantrips) {
            assertEquals("Cantrip level must be 0", 0, c.level)
            assertTrue("Cantrip name cannot be blank", c.name.isNotBlank())
            assertTrue("Cantrip description cannot be blank", c.description.isNotBlank())
            assertNotNull(c.school)
        }

        // Spot-check iconic 5E cantrips
        assertNotNull(SRD5eSpellDatabase.findByName("Fire Bolt"))
        assertNotNull(SRD5eSpellDatabase.findByName("Eldritch Blast"))
        assertNotNull(SRD5eSpellDatabase.findByName("Mage Hand"))
        assertNotNull(SRD5eSpellDatabase.findByName("Sacred Flame"))
        assertNotNull(SRD5eSpellDatabase.findByName("Vicious Mockery"))
    }

    @Test
    fun testLeveledSpellsCatalogMeetsFiftyPlusThreshold() {
        val leveled = SRD5eSpellDatabase.getLeveledSpells()
        assertTrue("Expected 50+ leveled spells, found ${leveled.size}", leveled.size >= 50)

        for (s in leveled) {
            assertTrue("Spell level must be between 1 and 9, got ${s.level}", s.level in 1..9)
            assertTrue("Spell name cannot be blank", s.name.isNotBlank())
            assertTrue("Spell description cannot be blank", s.description.isNotBlank())
        }

        // Spot-check iconic leveled spells
        assertNotNull(SRD5eSpellDatabase.findByName("Magic Missile"))
        assertNotNull(SRD5eSpellDatabase.findByName("Cure Wounds"))
        assertNotNull(SRD5eSpellDatabase.findByName("Fireball"))
        assertNotNull(SRD5eSpellDatabase.findByName("Haste"))
        assertNotNull(SRD5eSpellDatabase.findByName("Cone of Cold"))
    }

    @Test
    fun testWeaponsCatalogMeetsFiftyPlusThreshold() {
        val weapons = SRD5eEquipmentDatabase.getAllWeapons()
        assertTrue("Expected 50+ weapons, found ${weapons.size}", weapons.size >= 50)

        for (w in weapons) {
            assertTrue("Weapon ID must not be blank", w.id.isNotBlank())
            assertTrue("Weapon name must not be blank", w.name.isNotBlank())
            assertTrue("Weapon damage formula must not be blank", w.damageFormula.isNotBlank())
            assertTrue("Weapon category must not be blank", w.category.isNotBlank())
            assertTrue("Weapon value GP must be >= 1", w.valueGp >= 1)
        }

        // Spot check simple, martial, and magic weapons
        assertNotNull(SRD5eEquipmentDatabase.findWeaponById("wpn_dagger"))
        assertNotNull(SRD5eEquipmentDatabase.findWeaponById("wpn_greatsword"))
        assertNotNull(SRD5eEquipmentDatabase.findWeaponById("wpn_longbow"))
        assertNotNull(SRD5eEquipmentDatabase.findWeaponById("wpn_flame_tongue"))
        assertNotNull(SRD5eEquipmentDatabase.findWeaponById("wpn_vorpal_sword"))
    }

    @Test
    fun testArmorAndEquipmentMeetsFiftyPlusThreshold() {
        val gear = SRD5eEquipmentDatabase.getAllArmorAndGear()
        assertTrue("Expected 50+ armor and equipment items, found ${gear.size}", gear.size >= 50)

        for (g in gear) {
            assertTrue("Gear ID must not be blank", g.id.isNotBlank())
            assertTrue("Gear name must not be blank", g.name.isNotBlank())
            assertTrue("Gear description must not be blank", g.description.isNotBlank())
        }

        // Spot check light/med/heavy armor, shields, potions, tools, wondrous relics
        assertNotNull(SRD5eEquipmentDatabase.findGearById("arm_leather"))
        assertNotNull(SRD5eEquipmentDatabase.findGearById("arm_plate"))
        assertNotNull(SRD5eEquipmentDatabase.findGearById("shd_steel"))
        assertNotNull(SRD5eEquipmentDatabase.findGearById("pot_healing"))
        assertNotNull(SRD5eEquipmentDatabase.findGearById("relic_bag_of_holding"))
    }

    @Test
    fun testMonsterBestiaryMeetsFiftyPlusThreshold() {
        val monsters = SRD5eMonsterBestiary.getAllMonsters()
        assertTrue("Expected 50+ monsters, found ${monsters.size}", monsters.size >= 50)

        for (m in monsters) {
            assertTrue("Monster ID must not be blank", m.id.isNotBlank())
            assertTrue("Monster name must not be blank", m.name.isNotBlank())
            assertTrue("Monster AC must be > 0", m.ac > 0)
            assertTrue("Monster HP must be > 0", m.hp > 0)
            assertTrue("Monster must have at least one action", m.actions.isNotEmpty())
            assertTrue("Monster CR must be >= 0", m.cr >= 0.0)
        }

        // Spot check iconic SRD 5.1 monsters across types
        assertNotNull(SRD5eMonsterBestiary.findById("mon_skeleton"))
        assertNotNull(SRD5eMonsterBestiary.findById("mon_goblin"))
        assertNotNull(SRD5eMonsterBestiary.findById("mon_beholder"))
        assertNotNull(SRD5eMonsterBestiary.findById("mon_red_dragon"))
        assertNotNull(SRD5eMonsterBestiary.findById("mon_mind_flayer"))
        assertNotNull(SRD5eMonsterBestiary.findById("mon_pit_fiend"))
        assertNotNull(SRD5eMonsterBestiary.findById("mon_dire_wolf"))
    }
}
