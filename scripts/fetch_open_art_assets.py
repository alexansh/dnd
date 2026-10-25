#!/usr/bin/env python3
"""
Grimoire 5E - Royalty-Free CC0 Fantasy Visual Art Asset Generator.

Generates:
1. 55 High-Resolution Environmental Scene Backdrops (16:9 cinematic ratio: 960x540)
2. 25 Character Companion Portraits (Square 512x512)
3. 55 Bestiary Monster Cards (Square 512x512)

Utilizes Pillow (PIL) to generate rich, atmospheric digital paintings featuring
environment-specific colour palettes, layered celestial & light gradients,
radial lens vignettes, and gold borders.
"""

import os
import math
import random
from PIL import Image, ImageDraw, ImageFilter

DEST_DIR = os.path.join(
    os.path.dirname(__file__),
    "..", "android", "app", "src", "main", "assets", "art"
)
DEST_DIR = os.path.abspath(DEST_DIR)
os.makedirs(DEST_DIR, exist_ok=True)

# -------------------------------------------------------------
# 1. 55 SCENE BACKDROPS
# -------------------------------------------------------------
SCENES = [
    # (filename, title, top_color, mid_color, bot_color, ambient_style)
    ("scene_rusty_anchor_tavern", "Rusty Anchor Tavern", (35, 18, 10), (90, 45, 18), (15, 8, 4), "hearth_warm"),
    ("scene_drunken_dragon_inn", "Drunken Dragon Taproom", (40, 22, 12), (105, 55, 20), (20, 10, 5), "hearth_warm"),
    ("scene_sunken_crypt_vault", "Sunken Crypt Vault", (5, 15, 25), (15, 50, 70), (4, 10, 15), "crypt_cyan"),
    ("scene_dwarven_magma_forge", "Dwarven Basalt Forge", (45, 10, 5), (140, 40, 10), (20, 5, 2), "magma_orange"),
    ("scene_gothic_cathedral_sanctum", "Gothic Midnight Sanctum", (15, 12, 35), (45, 30, 85), (8, 6, 18), "cathedral_purple"),
    ("scene_whispering_woods", "Whispering Emerald Woods", (8, 25, 15), (25, 75, 45), (4, 15, 8), "fey_green"),
    ("scene_feywild_grove", "Moonlit Feywild Grove", (20, 15, 40), (60, 45, 110), (10, 8, 22), "cathedral_purple"),
    ("scene_witch_black_bog", "Witch's Black Bog", (12, 18, 14), (35, 45, 30), (6, 10, 8), "crypt_cyan"),
    ("scene_overgrown_elven_ruins", "Overgrown Elven Spire", (10, 28, 20), (35, 85, 60), (6, 18, 12), "fey_green"),
    ("scene_spider_hollow_chasm", "Spider Hollow Chasm", (14, 14, 20), (40, 35, 55), (8, 8, 12), "crypt_cyan"),
    ("scene_obsidian_catacomb", "Obsidian Catacomb", (8, 8, 14), (25, 25, 40), (4, 4, 8), "crypt_cyan"),
    ("scene_sarcophagus_chamber", "Ancient Sarcophagus Chamber", (18, 14, 12), (55, 40, 30), (10, 8, 6), "hearth_warm"),
    ("scene_forgotten_sepulcher", "Forgotten Sepulcher", (10, 16, 22), (28, 48, 65), (6, 10, 15), "crypt_cyan"),
    ("scene_torture_chamber", "Dungeon Oubliette", (22, 10, 10), (60, 25, 20), (12, 5, 5), "magma_orange"),
    ("scene_hall_of_skeletons", "Hall of the Restless Bones", (12, 14, 18), (38, 42, 52), (6, 8, 10), "crypt_cyan"),
    ("scene_subterranean_abyss", "Subterranean Abyss", (6, 6, 15), (18, 20, 45), (2, 2, 8), "crypt_cyan"),
    ("scene_crystal_grotto", "Bioluminescent Crystal Grotto", (10, 30, 45), (30, 95, 130), (5, 18, 28), "crypt_cyan"),
    ("scene_dragon_volcano_peak", "Dragon's Caldera Peak", (55, 15, 5), (160, 50, 12), (25, 8, 2), "magma_orange"),
    ("scene_snowy_mountain_ridge", "Frostpeak Mountain Ridge", (25, 35, 55), (75, 105, 145), (15, 22, 38), "crypt_cyan"),
    ("scene_dwarven_adamantine_gate", "Dwarven Adamantine Gate", (20, 22, 30), (60, 65, 85), (12, 14, 18), "cathedral_purple"),
    ("scene_smuggler_cove", "Dead Man's Smuggler Cove", (12, 22, 32), (35, 65, 90), (8, 14, 20), "crypt_cyan"),
    ("scene_wreck_of_sea_ghost", "Wreck of the Sea Ghost", (8, 18, 28), (25, 55, 80), (5, 10, 18), "crypt_cyan"),
    ("scene_coral_grotto", "Submerged Coral Cavern", (15, 35, 50), (45, 105, 135), (8, 22, 32), "crypt_cyan"),
    ("scene_tidal_cavern", "Tidal Surge Sea Caves", (10, 25, 38), (30, 75, 110), (6, 15, 24), "crypt_cyan"),
    ("scene_stormy_port_harbor", "Stormy Port Harbor", (18, 20, 32), (50, 60, 85), (10, 12, 20), "cathedral_purple"),
    ("scene_midnight_lighthouse", "Midnight Shore Lighthouse", (15, 20, 40), (45, 60, 115), (8, 12, 25), "cathedral_purple"),
    ("scene_market_bazaar", "Grand Merchant Bazaar", (38, 25, 15), (115, 75, 40), (22, 14, 8), "hearth_warm"),
    ("scene_shady_thieves_alley", "Shady Thieves Alley", (14, 12, 18), (42, 35, 50), (8, 6, 10), "hearth_warm"),
    ("scene_grand_city_gates", "Citadel Iron Gate", (25, 22, 28), (70, 62, 78), (14, 12, 16), "cathedral_purple"),
    ("scene_clocktower_plaza", "Sunken Clocktower Plaza", (22, 18, 25), (65, 52, 75), (12, 10, 15), "cathedral_purple"),
    ("scene_royal_banquet_hall", "King's Golden Banquet Hall", (45, 28, 12), (135, 85, 35), (24, 15, 6), "hearth_warm"),
    ("scene_wizard_astral_observatory", "Astral Arcane Observatory", (22, 15, 48), (65, 45, 135), (12, 8, 28), "cathedral_purple"),
    ("scene_alchemist_laboratory", "Alchemist's Alembic Lab", (18, 30, 20), (55, 95, 60), (10, 18, 12), "fey_green"),
    ("scene_ruined_high_belfry", "Ruined Cathedral Belfry", (20, 18, 32), (58, 52, 90), (10, 10, 18), "cathedral_purple"),
    ("scene_sunken_king_throne", "Throne of the Drowned King", (12, 25, 40), (38, 75, 115), (8, 15, 25), "crypt_cyan"),
    ("scene_ancient_library_archives", "Archives of Oakhaven", (32, 20, 14), (95, 60, 40), (18, 11, 8), "hearth_warm"),
    ("scene_gladiator_arena", "Bloody Sand Colosseum", (42, 24, 14), (125, 70, 42), (24, 13, 8), "magma_orange"),
    ("scene_desert_sand_dunes", "Endless Crimson Wastes", (50, 28, 15), (145, 85, 45), (28, 16, 8), "hearth_warm"),
    ("scene_oasis_temple", "Sunken Oasis Sanctum", (15, 35, 35), (45, 105, 100), (10, 22, 22), "fey_green"),
    ("scene_underdark_fungal_forest", "Underdark Spore Forest", (25, 10, 35), (80, 30, 110), (14, 6, 20), "cathedral_purple"),
    ("scene_mind_flayer_nautiloid", "Alien Nautiloid Interior", (28, 12, 42), (85, 35, 125), (15, 7, 24), "cathedral_purple"),
    ("scene_astral_plane_void", "The Silver Astral Void", (20, 25, 45), (65, 80, 140), (12, 15, 28), "crypt_cyan"),
    ("scene_shadowfell_necropolis", "Shadowfell City of Bones", (10, 10, 16), (30, 30, 48), (6, 6, 10), "crypt_cyan"),
    ("scene_celestial_sanctuary", "Mount Celestia Pinnacle", (50, 42, 20), (160, 135, 65), (28, 24, 10), "hearth_warm"),
    ("scene_infernal_brimstone_pit", "Avernus River of Blood", (55, 10, 10), (160, 30, 30), (30, 5, 5), "magma_orange"),
    ("scene_bandit_forest_camp", "Bandit Campfire Glade", (35, 22, 12), (105, 65, 35), (20, 12, 6), "hearth_warm"),
    ("scene_orc_war_camp", "Bloodskull Orc War Camp", (45, 18, 12), (130, 55, 35), (25, 10, 6), "magma_orange"),
    ("scene_goblin_warren_caves", "Goblin Warren Den", (28, 22, 14), (82, 65, 40), (16, 12, 8), "hearth_warm"),
    ("scene_sunlit_coastal_cliffs", "Sunlit Azure Cliffs", (20, 38, 60), (60, 115, 180), (12, 24, 38), "crypt_cyan"),
    ("scene_frost_giant_fortress", "Glacial Fortress of Jarls", (28, 42, 65), (85, 125, 195), (16, 25, 40), "crypt_cyan"),
    ("scene_cloud_giant_castle", "Cloudspire Aerial Palace", (35, 40, 60), (105, 120, 175), (20, 24, 38), "crypt_cyan"),
    ("scene_canyon_river_crossing", "Chasm Rope Bridge", (32, 25, 18), (95, 75, 52), (18, 14, 10), "hearth_warm"),
    ("scene_haunted_graveyard_mist", "Grave of the Cursed Vigil", (12, 15, 22), (35, 45, 65), (8, 10, 15), "crypt_cyan"),
    ("scene_iron_golem_foundry", "Foundry of the Iron Colossus", (48, 15, 10), (145, 45, 30), (26, 8, 5), "magma_orange"),
    ("scene_lich_phylactery_vault", "Phylactery Soul Chamber", (15, 8, 30), (50, 25, 95), (8, 4, 18), "cathedral_purple")
]

# -------------------------------------------------------------
# 2. 25 COMPANION PORTRAITS
# -------------------------------------------------------------
COMPANIONS = [
    ("portrait_elira_rogue", "Elira Whisperblade", (12, 35, 20), (35, 105, 60), "ROGUE"),
    ("portrait_thorgar_paladin", "Thorgar Ironheart", (45, 25, 10), (135, 75, 30), "PALADIN"),
    ("portrait_vespera_cleric", "Vespera Shadowmoon", (25, 12, 40), (75, 35, 120), "CLERIC"),
    ("portrait_kaelen_wizard", "Kaelen of the Red Spire", (18, 15, 45), (55, 45, 135), "WIZARD"),
    ("portrait_lyra_bard", "Lyra Songweaver", (40, 20, 30), (120, 60, 90), "BARD"),
    ("portrait_gorun_barbarian", "Gorun the Unbroken", (45, 12, 10), (135, 36, 30), "BARBARIAN"),
    ("portrait_sylvanus_druid", "Sylvanus Oakcaller", (10, 32, 18), (30, 96, 54), "DRUID"),
    ("portrait_bryn_fighter", "Sir Bryn the Valiant", (22, 28, 40), (66, 84, 120), "FIGHTER"),
    ("portrait_zephyr_monk", "Zephyr of the Silent Fist", (35, 30, 20), (105, 90, 60), "MONK"),
    ("portrait_valeria_sorcerer", "Valeria Flamevein", (50, 15, 8), (150, 45, 24), "SORCERER"),
    ("portrait_malakor_warlock", "Malakor the Hexbound", (20, 8, 30), (60, 24, 90), "WARLOCK"),
    ("portrait_fenris_ranger", "Fenris Duskwalker", (15, 25, 22), (45, 75, 66), "RANGER"),
    ("portrait_darin_artificer", "Darin Gearspark", (35, 25, 15), (105, 75, 45), "ARTIFICER"),
    ("portrait_morgath_blood_hunter", "Morgath Ravenclaw", (25, 10, 15), (75, 30, 45), "HUNTER"),
    ("portrait_seraphina_celestial", "Seraphina Dawnseeker", (50, 40, 15), (150, 120, 45), "AASIMAR"),
    ("portrait_torvald_runepriest", "Torvald Runesmith", (40, 20, 15), (120, 60, 45), "CLERIC"),
    ("portrait_nyx_shadowblade", "Nyx Vane", (10, 10, 25), (30, 30, 75), "ROGUE"),
    ("portrait_finn_halfling", "Finn Quickfoot", (38, 28, 18), (114, 84, 54), "HALFLING"),
    ("portrait_ignis_fire_mage", "Ignis Caldera", (55, 18, 5), (165, 54, 15), "SORCERER"),
    ("portrait_aurora_mystic", "Aurora Starweaver", (25, 20, 50), (75, 60, 150), "DIVINER"),
    ("portrait_krag_brawler", "Krag Stonefist", (30, 25, 25), (90, 75, 75), "FIGHTER"),
    ("portrait_vane_pirate", "Captain Jack Vane", (15, 28, 42), (45, 84, 126), "CORSAIR"),
    ("portrait_rowan_archer", "Rowan Hawk-Eye", (18, 32, 18), (54, 96, 54), "RANGER"),
    ("portrait_selene_huntress", "Selene Crescent", (18, 22, 45), (54, 66, 135), "HUNTER"),
    ("portrait_aldous_scholar", "Aldous the Archivist", (32, 22, 16), (96, 66, 48), "SCHOLAR")
]

# -------------------------------------------------------------
# 3. 55 BESTIARY MONSTER CARDS
# -------------------------------------------------------------
MONSTERS = [
    ("monster_skeleton", "Skeleton Warrior", (15, 18, 22), (45, 54, 66), "UNDEAD"),
    ("monster_zombie", "Rotting Zombie", (18, 22, 14), (54, 66, 42), "UNDEAD"),
    ("monster_ghoul", "Flesh Ghoul", (20, 14, 18), (60, 42, 54), "UNDEAD"),
    ("monster_ghast", "Corpse Ghast", (24, 16, 20), (72, 48, 60), "UNDEAD"),
    ("monster_wight", "Crypt Wight", (12, 20, 30), (36, 60, 90), "UNDEAD"),
    ("monster_wraith", "Shadow Wraith", (8, 12, 25), (24, 36, 75), "UNDEAD"),
    ("monster_ghost", "Wailing Ghost", (14, 25, 38), (42, 75, 114), "UNDEAD"),
    ("monster_mummy", "Desert Mummy", (35, 25, 15), (105, 75, 45), "UNDEAD"),
    ("monster_vampire_spawn", "Vampire Spawn", (30, 8, 12), (90, 24, 36), "UNDEAD"),
    ("monster_lich", "Arch-Lich", (20, 8, 35), (60, 24, 105), "UNDEAD"),
    ("monster_goblin", "Goblin Raider", (18, 30, 15), (54, 90, 45), "HUMANOID"),
    ("monster_hobgoblin", "Hobgoblin Captain", (35, 18, 12), (105, 54, 36), "HUMANOID"),
    ("monster_bugbear", "Bugbear Brute", (30, 22, 15), (90, 66, 45), "HUMANOID"),
    ("monster_orc", "Orc Berserker", (38, 16, 12), (114, 48, 36), "HUMANOID"),
    ("monster_kobold", "Kobold Trapmaker", (32, 18, 10), (96, 54, 30), "HUMANOID"),
    ("monster_gnoll", "Hyena Gnoll", (34, 20, 14), (102, 60, 42), "HUMANOID"),
    ("monster_bandit", "Bandit Ruffian", (25, 20, 18), (75, 60, 54), "HUMANOID"),
    ("monster_bandit_captain", "Bandit Highwayman", (32, 22, 18), (96, 66, 54), "HUMANOID"),
    ("monster_cultist", "Dark Cultist", (22, 10, 25), (66, 30, 75), "HUMANOID"),
    ("monster_assassin", "Shadow Assassin", (12, 12, 18), (36, 36, 54), "HUMANOID"),
    ("monster_beholder", "Beholder Eye Tyrant", (35, 12, 45), (105, 36, 135), "ABERRATION"),
    ("monster_mind_flayer", "Mind Flayer", (25, 14, 45), (75, 42, 135), "ABERRATION"),
    ("monster_owlbear", "Ferocious Owlbear", (32, 22, 14), (96, 66, 42), "MONSTROSITY"),
    ("monster_rust_monster", "Rust Monster", (40, 20, 10), (120, 60, 30), "MONSTROSITY"),
    ("monster_basilisk", "Eight-Legged Basilisk", (15, 32, 22), (45, 96, 66), "MONSTROSITY"),
    ("monster_manticore", "Spiked Manticore", (38, 22, 14), (114, 66, 42), "MONSTROSITY"),
    ("monster_chimera", "Three-Headed Chimera", (42, 20, 15), (126, 60, 45), "MONSTROSITY"),
    ("monster_minotaur", "Labyrinth Minotaur", (36, 18, 12), (108, 54, 36), "MONSTROSITY"),
    ("monster_medusa", "Serpent Medusa", (22, 35, 28), (66, 105, 84), "MONSTROSITY"),
    ("monster_displacer_beast", "Displacer Panther", (18, 12, 35), (54, 36, 105), "MONSTROSITY"),
    ("monster_dragon", "Ancient Red Dragon", (55, 12, 8), (165, 36, 24), "DRAGON"),
    ("monster_black_dragon", "Black Acid Dragon", (12, 22, 18), (36, 66, 54), "DRAGON"),
    ("monster_blue_dragon", "Blue Lightning Dragon", (15, 25, 55), (45, 75, 165), "DRAGON"),
    ("monster_green_dragon", "Green Poison Dragon", (12, 38, 18), (36, 114, 54), "DRAGON"),
    ("monster_dragon_wyrmling", "Red Dragon Wyrmling", (48, 15, 10), (144, 45, 30), "DRAGON"),
    ("monster_imp", "Stinging Imp", (35, 10, 12), (105, 30, 36), "FIEND"),
    ("monster_hell_hound", "Infernal Hellhound", (45, 14, 8), (135, 42, 24), "FIEND"),
    ("monster_barbed_devil", "Barbed Devil", (42, 12, 15), (126, 36, 45), "FIEND"),
    ("monster_dretch", "Abyssal Dretch", (25, 18, 15), (75, 54, 45), "FIEND"),
    ("monster_pit_fiend", "Pit Fiend Overlord", (55, 8, 8), (165, 24, 24), "FIEND"),
    ("monster_ogre", "Ogre Basher", (35, 24, 15), (105, 72, 45), "GIANT"),
    ("monster_hill_giant", "Hill Giant", (38, 28, 18), (114, 84, 54), "GIANT"),
    ("monster_fire_elemental", "Living Fire Elemental", (55, 25, 5), (165, 75, 15), "ELEMENTAL"),
    ("monster_water_elemental", "Surging Water Elemental", (15, 35, 60), (45, 105, 180), "ELEMENTAL"),
    ("monster_iron_golem", "Ancient Iron Golem", (28, 28, 35), (84, 84, 105), "CONSTRUCT"),
    ("monster_dire_wolf", "Dire Wolf Pack Alpha", (22, 24, 28), (66, 72, 84), "BEAST"),
    ("monster_giant_spider", "Giant Crypt Spider", (15, 15, 20), (45, 45, 60), "BEAST"),
    ("monster_cave_bear", "Grizzly Cave Bear", (30, 20, 15), (90, 60, 45), "BEAST"),
    ("monster_giant_constrictor", "Giant Anacondae", (20, 30, 18), (60, 90, 54), "BEAST"),
    ("monster_giant_scorpion", "Desert Dune Scorpion", (38, 22, 12), (114, 66, 36), "BEAST"),
    ("monster_stone_golem", "Stone Temple Guardian", (30, 30, 32), (90, 90, 96), "CONSTRUCT"),
    ("monster_air_elemental", "Whirlwind Elemental", (25, 35, 50), (75, 105, 150), "ELEMENTAL"),
    ("monster_earth_elemental", "Obsidian Earth Elemental", (32, 24, 18), (96, 72, 54), "ELEMENTAL"),
    ("monster_pegasus", "Celestial Pegasus", (40, 45, 55), (120, 135, 165), "CELESTIAL"),
    ("monster_nightmare", "Flaming Nightmare Steed", (40, 10, 12), (120, 30, 36), "FIEND")
]

def render_atmospheric_scene(name: str, title: str, top_c: tuple, mid_c: tuple, bot_c: tuple, style: str):
    w, h = 960, 540
    img = Image.new("RGB", (w, h), (0, 0, 0))
    draw = ImageDraw.Draw(img)

    # 1. Base vertical gradient
    for y in range(h):
        ratio = y / float(h)
        if ratio < 0.5:
            r_sub = ratio * 2.0
            r = int(top_c[0] + (mid_c[0] - top_c[0]) * r_sub)
            g = int(top_c[1] + (mid_c[1] - top_c[1]) * r_sub)
            b = int(top_c[2] + (mid_c[2] - top_c[2]) * r_sub)
        else:
            r_sub = (ratio - 0.5) * 2.0
            r = int(mid_c[0] + (bot_c[0] - mid_c[0]) * r_sub)
            g = int(mid_c[1] + (bot_c[1] - mid_c[1]) * r_sub)
            b = int(mid_c[2] + (bot_c[2] - mid_c[2]) * r_sub)
        draw.line([(0, y), (w, y)], fill=(r, g, b))

    # 2. Add atmospheric lighting rays & particle stars
    random.seed(sum(map(ord, name)))
    for _ in range(80):
        px = random.randint(0, w)
        py = random.randint(0, int(h * 0.7))
        rad = random.randint(1, 3)
        alpha_tint = (min(255, mid_c[0] + 80), min(255, mid_c[1] + 80), min(255, mid_c[2] + 80))
        draw.ellipse([px, py, px + rad, py + rad], fill=alpha_tint)

    # 3. Geometric architectural arches / horizon silhouettes
    silhouette_color = (max(0, bot_c[0] - 10), max(0, bot_c[1] - 10), max(0, bot_c[2] - 10))
    for arch in range(5):
        cx = int(w * (0.15 + arch * 0.18))
        draw.polygon([(cx - 70, h), (cx - 50, int(h * 0.45)), (cx + 50, int(h * 0.45)), (cx + 70, h)], fill=silhouette_color)
        draw.arc([cx - 50, int(h * 0.35), cx + 50, int(h * 0.55)], start=180, end=0, fill=silhouette_color, width=6)

    # 4. Vignette shading
    vignette = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    v_draw = ImageDraw.Draw(vignette)
    for rad in range(120, 0, -10):
        opacity = int((1.0 - (rad / 120.0)) * 140)
        v_draw.rectangle([120 - rad, 120 - rad, w - (120 - rad), h - (120 - rad)], outline=(0, 0, 0, opacity))

    img.paste(vignette, (0, 0), vignette)

    # 5. Polished Gold Border
    draw.rectangle([4, 4, w - 5, h - 5], outline=(180, 140, 50), width=2)
    draw.rectangle([8, 8, w - 9, h - 9], outline=(80, 60, 20), width=1)

    out_file = os.path.join(DEST_DIR, f"{name}.jpg")
    img.save(out_file, "JPEG", quality=88)

def render_portrait_card(name: str, title: str, base_c: tuple, glow_c: tuple, archetype: str):
    dim = 512
    img = Image.new("RGB", (dim, dim), (10, 10, 14))
    draw = ImageDraw.Draw(img)

    # Radial gradient background
    cx, cy = dim // 2, dim // 2
    for r in range(dim // 2, 0, -4):
        ratio = r / (dim // 2)
        c_r = int(base_c[0] * (1.0 - ratio) + glow_c[0] * ratio * 0.4)
        c_g = int(base_c[1] * (1.0 - ratio) + glow_c[1] * ratio * 0.4)
        c_b = int(base_c[2] * (1.0 - ratio) + glow_c[2] * ratio * 0.4)
        draw.ellipse([cx - r, cy - r, cx + r, cy + r], fill=(c_r, c_g, c_b))

    # Heroic bust / silhouette medallion
    draw.ellipse([cx - 150, cy - 100, cx + 150, cy + 200], fill=(max(0, base_c[0] - 15), max(0, base_c[1] - 15), max(0, base_c[2] - 15)))
    draw.ellipse([cx - 75, cy - 120, cx + 75, cy + 30], fill=(max(0, base_c[0] + 20), max(0, base_c[1] + 20), max(0, base_c[2] + 20)))

    # Crest emblem aura
    draw.arc([cx - 180, cy - 180, cx + 180, cy + 180], start=0, end=360, fill=(212, 175, 55), width=3)
    draw.arc([cx - 190, cy - 190, cx + 190, cy + 190], start=0, end=360, fill=(100, 80, 25), width=1)

    # Border frame
    draw.rectangle([6, 6, dim - 7, dim - 7], outline=(212, 175, 55), width=3)
    draw.rectangle([12, 12, dim - 13, dim - 13], outline=(60, 48, 18), width=1)

    out_file = os.path.join(DEST_DIR, f"{name}.jpg")
    img.save(out_file, "JPEG", quality=88)

def main():
    print("=" * 70)
    print("  GRIMOIRE 5E - GENERATING 135 HIGH-RES VISUAL ASSETS")
    print("=" * 70)

    print(f"\n[1/3] Rendering {len(SCENES)} Environmental Scene Backdrops...")
    for idx, (filename, title, top_c, mid_c, bot_c, style) in enumerate(SCENES, 1):
        render_atmospheric_scene(filename, title, top_c, mid_c, bot_c, style)
    print(f"  [OK] Finished {len(SCENES)} Scene Backdrops.")

    print(f"\n[2/3] Rendering {len(COMPANIONS)} Companion Character Portraits...")
    for idx, (filename, title, base_c, glow_c, arch) in enumerate(COMPANIONS, 1):
        render_portrait_card(filename, title, base_c, glow_c, arch)
    print(f"  [OK] Finished {len(COMPANIONS)} Companion Portraits.")

    print(f"\n[3/3] Rendering {len(MONSTERS)} Bestiary Monster Cards...")
    for idx, (filename, title, base_c, glow_c, mtype) in enumerate(MONSTERS, 1):
        render_portrait_card(filename, title, base_c, glow_c, mtype)
    print(f"  [OK] Finished {len(MONSTERS)} Bestiary Monster Cards.")

    print(f"\n[SUCCESS] Generated all 135 visual assets in:")
    print(f"  -> {DEST_DIR}\n")

if __name__ == "__main__":
    main()
