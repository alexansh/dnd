"""
Grimoire 5E - Campaign 1: The Sunken King's Legacy (Act I: The Drowned Crypt)
Authoring complete cyclic adventure nodes with skill checks, backtracking,
secret loot, and tactical encounters.
"""

from typing import List
from core_5e.adventure import AdventureNode, NodeChoice, SkillCheckRequirement
from core_5e.mechanics import Skill


def build_campaign_1_nodes() -> List[AdventureNode]:
    return [
        # Node 1: The Drunken Dragon Tavern
        AdventureNode(
            node_id="c1_tavern",
            campaign_id="campaign_1",
            title="The Drunken Dragon Tavern",
            narration=(
                "A crackling hearth warms the smoke-filled taproom of Riverfall. "
                "Townsfolk whisper anxiously in shadowed booths. At the corner table sits "
                "Elder Judith, clutching a worn map with trembling hands. The ancient catacombs "
                "beneath the marsh have begun leaking brackish water—and worse, restless dead."
            ),
            return_narration=(
                "You step back into the warmth of the Drunken Dragon Tavern. The scent of roasted "
                "ale and pine smoke offers a welcome respite from the damp marsh outside."
            ),
            is_rest_area=True,
            choices=[
                NodeChoice(
                    id="talk_judith",
                    text="Speak with Elder Judith about the flooded crypt.",
                    target_node_id="c1_tavern_judith",
                    set_flag_on_select=("knows_crypt_lore", "true")
                ),
                NodeChoice(
                    id="depart_graveyard",
                    text="Leave the tavern and march toward the Riverfall Cemetery.",
                    target_node_id="c1_graveyard"
                )
            ]
        ),

        # Node 1b: Talking to Judith
        AdventureNode(
            node_id="c1_tavern_judith",
            campaign_id="campaign_1",
            title="A Dire Warning from Elder Judith",
            narration=(
                "Elder Judith leans in, her voice hushed: 'The tomb belongs to King Kar-Drak, "
                "an ancient warlord who bargained with dark waters. A heavy bronze gate seals the "
                "inner crypt. Our ancestors hid an iron skeleton key somewhere in the cemetery mausoleums. "
                "Find it, or you'll have to break your way inside.'"
            ),
            return_narration="Elder Judith nods solemnly at your return. 'May the light guide your blade, adventurer.'",
            is_rest_area=True,
            choices=[
                NodeChoice(
                    id="leave_for_graveyard",
                    text="Take your leave and head to the Riverfall Cemetery.",
                    target_node_id="c1_graveyard"
                ),
                NodeChoice(
                    id="return_tavern_floor",
                    text="Step back to the main taproom.",
                    target_node_id="c1_tavern"
                )
            ]
        ),

        # Node 2: Riverfall Graveyard
        AdventureNode(
            node_id="c1_graveyard",
            campaign_id="campaign_1",
            title="Riverfall Weeping Graveyard",
            narration=(
                "Fog blankets the moss-eaten tombstones. Ravens caw from weeping willows. "
                "At the center of the graveyard stands a monumental granite archway framing "
                "the submerged stairs that lead into the sunken crypt."
            ),
            return_narration=(
                "You return to the damp graveyard. The fog swirls across the silent headstones."
            ),
            choices=[
                NodeChoice(
                    id="approach_crypt_doors",
                    text="Descend the stone stairs toward the heavy crypt doors.",
                    target_node_id="c1_crypt_doors"
                ),
                NodeChoice(
                    id="search_mausoleums",
                    text="Search the ancient family mausoleums for the hidden key (Investigation DC 12).",
                    skill_check=SkillCheckRequirement(
                        skill=Skill.INVESTIGATION,
                        dc=12,
                        success_node_id="c1_found_key",
                        failure_node_id="c1_graveyard_failed_search",
                        success_message="You discover an iron key tucked behind a weathered gargoyle!",
                        failure_message="You find only rotted moss and mud among the inscriptions."
                    )
                ),
                NodeChoice(
                    id="backtrack_to_tavern",
                    text="Retreat back to the safety of Riverfall Tavern.",
                    target_node_id="c1_tavern"
                )
            ]
        ),

        # Node 2b: Found the Key!
        AdventureNode(
            node_id="c1_found_key",
            campaign_id="campaign_1",
            title="Ancient Bronze Key Discovered",
            narration=(
                "Behind the crumbling stonework of the riverfall founders' tomb, your fingers brush "
                "cold metal. You pull free a heavy, tarnished iron skeleton key inscribed with a trident sigil."
            ),
            choices=[
                NodeChoice(
                    id="go_to_doors_with_key",
                    text="Head to the crypt doors with your newly found key.",
                    target_node_id="c1_crypt_doors",
                    set_flag_on_select=("has_crypt_key", "true")
                )
            ]
        ),

        # Node 2c: Failed search
        AdventureNode(
            node_id="c1_graveyard_failed_search",
            campaign_id="campaign_1",
            title="Empty Mausoleum",
            narration="You scrape your knuckles against cold granite, finding nothing of note. A chilly wind moans through the trees.",
            choices=[
                NodeChoice(
                    id="return_graveyard",
                    text="Look around the cemetery grounds again.",
                    target_node_id="c1_graveyard"
                )
            ]
        ),

        # Node 3: The Crypt Doors
        AdventureNode(
            node_id="c1_crypt_doors",
            campaign_id="campaign_1",
            title="The Sunken Bronze Doors",
            narration=(
                "At the bottom of the wet stairwell, a massive pair of bronze-banded doors blocks your way. "
                "Murky water pools around your boots. A heavy iron padlock holds the chains fast."
            ),
            return_narration="You stand before the bronze crypt doors.",
            choices=[
                NodeChoice(
                    id="use_key",
                    text="[Key] Unlock the padlock using the tarnished iron key.",
                    target_node_id="c1_crypt_foyer",
                    required_flag=("has_crypt_key", "true"),
                    set_flag_on_select=("crypt_unlocked", "true")
                ),
                NodeChoice(
                    id="pick_lock",
                    text="Pick the lock using Thieves' Tools (Sleight of Hand DC 12).",
                    skill_check=SkillCheckRequirement(
                        skill=Skill.SLEIGHT_OF_HAND,
                        dc=12,
                        success_node_id="c1_crypt_foyer",
                        failure_node_id="c1_crypt_doors_failed_pick",
                        success_message="With a sharp click, the ancient tumblers align and the chain drops free!",
                        failure_message="Your pick slips on the rusted tumbler with an agonizing squeal."
                    )
                ),
                NodeChoice(
                    id="force_doors",
                    text="Batter the rusted gate open with sheer force (Athletics DC 14).",
                    skill_check=SkillCheckRequirement(
                        skill=Skill.ATHLETICS,
                        dc=14,
                        success_node_id="c1_crypt_foyer",
                        failure_node_id="c1_crypt_doors_failed_force",
                        failure_damage_formula="1d4",
                        success_message="You slam your shoulder against the seam—the rusted hinges tear from the stone wall!",
                        failure_message="The bronze barrier reverberates painfully through your shoulder bones."
                    )
                ),
                NodeChoice(
                    id="backtrack_graveyard",
                    text="Climb back up the stairs to the cemetery.",
                    target_node_id="c1_graveyard"
                )
            ]
        ),

        # Node 3b: Failed pick
        AdventureNode(
            node_id="c1_crypt_doors_failed_pick",
            campaign_id="campaign_1",
            title="Stubborn Lock",
            narration="The corroded padlock resists your tool. It will take another attempt or brute force.",
            choices=[
                NodeChoice(id="retry_doors", text="Step back to reassess the doors.", target_node_id="c1_crypt_doors")
            ]
        ),

        # Node 3c: Failed force
        AdventureNode(
            node_id="c1_crypt_doors_failed_force",
            campaign_id="campaign_1",
            title="Bruised Shoulder",
            narration="Pain shoots down your arm as the bronze slab refuses to budge. The water around your boots ripples eerily.",
            choices=[
                NodeChoice(id="retry_doors", text="Recover your breath and try another approach.", target_node_id="c1_crypt_doors")
            ]
        ),

        # Node 4: The Crypt Foyer (Trapped Hallway)
        AdventureNode(
            node_id="c1_crypt_foyer",
            campaign_id="campaign_1",
            title="The Drowned Antechamber",
            narration=(
                "Beyond the heavy doors lies an arched subterranean gallery. Cold water sloshes knee-deep. "
                "Alabaster statues of forgotten sea-priests line the walls, weeping lime-scale tears. "
                "Ahead, the water deepens into an altar room, but the flagstones beneath the water seem uneven."
            ),
            return_narration="You wade back into the arched entrance corridor. The cold water ripples around the stone statues.",
            choices=[
                NodeChoice(
                    id="wade_cautiously",
                    text="Search for submerged pressure plates as you wade forward (Perception DC 12).",
                    skill_check=SkillCheckRequirement(
                        skill=Skill.PERCEPTION,
                        dc=12,
                        success_node_id="c1_sanctum_entrance",
                        failure_node_id="c1_triggered_dart_trap",
                        failure_damage_formula="1d6",
                        success_message="You spot a taut wire under the murky waterline and step carefully over it!",
                        failure_message="Your boot catches a hidden wire! A volley of rusty iron darts fires from the wall!"
                    )
                ),
                NodeChoice(
                    id="backtrack_to_entrance",
                    text="Backtrack out of the crypt to the surface stairs.",
                    target_node_id="c1_crypt_doors"
                )
            ]
        ),

        # Node 4b: Triggered Trap
        AdventureNode(
            node_id="c1_triggered_dart_trap",
            campaign_id="campaign_1",
            title="Spring Trap Sprung!",
            narration="You wrench the darts from your armor as murky water splashes across the floor. Ahead, the corridor opens into the boss chamber.",
            choices=[
                NodeChoice(id="enter_sanctum", text="Press forward into the inner tomb.", target_node_id="c1_sanctum_entrance")
            ]
        ),

        # Node 5: The Altar Sanctum (Combat Encounter)
        AdventureNode(
            node_id="c1_sanctum_entrance",
            campaign_id="campaign_1",
            title="The Altar of the Sunken King",
            narration=(
                "You step onto a raised dry dais in a grand vaulted hall. "
                "Atop an ornate sarcophagus sits the dried, armored corpse of King Kar-Drak—a Wight! "
                "Two skeleton guards raise their notched scimitars as unholy blue fire ignites within their eye sockets!"
            ),
            return_narration="The grand tomb is quiet now. The fallen bones and dark remains of the Wight rest peacefully.",
            is_rest_area=False,
            monster_ids=["skeleton", "wight"],
            choices=[
                NodeChoice(
                    id="engage_boss",
                    text="Draw your weapons and engage the undead lord in combat!",
                    target_node_id="c1_boss_victory"
                ),
                NodeChoice(
                    id="retreat_foyer",
                    text="Make a fighting retreat back down the flooded corridor.",
                    target_node_id="c1_crypt_foyer"
                )
            ]
        ),

        # Node 6: Boss Victory!
        AdventureNode(
            node_id="c1_boss_victory",
            campaign_id="campaign_1",
            title="Victory in the Drowned Crypt",
            narration=(
                "With a final, desperate stroke, the Wight Lord collapses into a pile of ancient dust and salt! "
                "The blue flame leaves the skeletons, and their bones clatter harmlessly across the stone floor. "
                "The sarcophagus slides open, revealing gleaming ancestral treasures: 150 gold pieces, a gleaming Silver Longsword, "
                "and the Sacred Sigil of Riverfall!"
            ),
            reward_xp=500,
            reward_gold=150,
            is_rest_area=True,
            choices=[
                NodeChoice(
                    id="return_triumphant",
                    text="Gather the royal relics and return triumphant to Riverfall Tavern.",
                    target_node_id="c1_tavern",
                    set_flag_on_select=("campaign_1_complete", "true")
                )
            ]
        )
    ]
