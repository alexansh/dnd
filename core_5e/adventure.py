"""
Grimoire 5E - Adventure Engine & Hierarchical State Machine
Handles cyclic node navigation, re-entrant room memory, skill check choices,
conditional path gating, and state persistence.
"""

from dataclasses import dataclass, field
from enum import Enum
import json
from typing import Any, Callable, Dict, List, Optional, Tuple

from core_5e.character import Character
from core_5e.dice import DiceEngine, RollOutcome, RollType
from core_5e.mechanics import Skill
from core_5e.database import DatabaseManager


@dataclass
class SkillCheckRequirement:
    skill: Skill
    dc: int
    success_node_id: str
    failure_node_id: str
    failure_damage_formula: Optional[str] = None
    success_message: str = "Success!"
    failure_message: str = "Failure."


@dataclass
class NodeChoice:
    id: str
    text: str
    target_node_id: Optional[str] = None
    skill_check: Optional[SkillCheckRequirement] = None
    required_flag: Optional[Tuple[str, str]] = None  # (flag_key, required_value)
    set_flag_on_select: Optional[Tuple[str, str]] = None  # (flag_key, new_value)
    give_item_id: Optional[str] = None


@dataclass
class AdventureNode:
    node_id: str
    campaign_id: str
    title: str
    narration: str
    return_narration: Optional[str] = None
    choices: List[NodeChoice] = field(default_factory=list)
    monster_ids: List[str] = field(default_factory=list)
    is_rest_area: bool = False
    reward_xp: int = 0
    reward_gold: int = 0


@dataclass
class TransitionResult:
    next_node_id: str
    narrative_log: str
    roll_outcome: Optional[RollOutcome] = None
    is_combat_triggered: bool = False
    monsters_encountered: List[str] = field(default_factory=list)


class AdventureEngine:
    """
    Executes an authored campaign using a Cyclic Hierarchical State Machine.
    Supports backtracking, room state memory, automated 5e DC adjudication, and flag gating.
    """

    def __init__(self, db: DatabaseManager, dice_engine: DiceEngine):
        self.db = db
        self.dice = dice_engine
        self.nodes: Dict[str, AdventureNode] = {}
        self.current_node_id: Optional[str] = None
        self.visited_nodes: Set[str] = set()

    def register_node(self, node: AdventureNode) -> None:
        self.nodes[node.node_id] = node

    def start_adventure(self, save_id: str, starting_node_id: str) -> AdventureNode:
        self.current_node_id = starting_node_id
        # Load visited nodes from database
        visited_raw = self.db.get_quest_flag(save_id, "visited_nodes", "[]")
        self.visited_nodes = set(json.loads(visited_raw))
        return self.get_current_node()

    def get_current_node(self) -> AdventureNode:
        if not self.current_node_id or self.current_node_id not in self.nodes:
            raise ValueError(f"Invalid current node: {self.current_node_id}")
        return self.nodes[self.current_node_id]

    def get_current_narration(self) -> str:
        """Returns fresh narration for new rooms, or return narration if revisited."""
        node = self.get_current_node()
        if node.node_id in self.visited_nodes and node.return_narration:
            return node.return_narration
        return node.narration

    def get_available_choices(self, save_id: str) -> List[NodeChoice]:
        """Filters choices based on character state and quest flags."""
        node = self.get_current_node()
        available: List[NodeChoice] = []

        for choice in node.choices:
            if choice.required_flag:
                key, val = choice.required_flag
                current_val = self.db.get_quest_flag(save_id, key)
                if current_val != val:
                    continue  # Gate blocked
            available.append(choice)

        return available

    def execute_choice(
        self,
        save_id: str,
        choice_id: str,
        character: Character
    ) -> TransitionResult:
        """
        Executes a player choice.
        If a skill check is present, automatically rolls against character modifiers.
        Updates quest flags and returns the transition result.
        """
        node = self.get_current_node()
        matched = next((c for c in node.choices if c.id == choice_id), None)
        if not matched:
            raise ValueError(f"Choice {choice_id} not available at node {node.node_id}")

        # Mark current node as visited
        self.visited_nodes.add(node.node_id)
        self.db.set_quest_flag(save_id, "visited_nodes", json.dumps(list(self.visited_nodes)))

        # Handle flag updates
        if matched.set_flag_on_select:
            f_key, f_val = matched.set_flag_on_select
            self.db.set_quest_flag(save_id, f_key, f_val)

        roll_outcome: Optional[RollOutcome] = None
        narrative_log = ""
        target_node = matched.target_node_id

        # 1. Adjudicate Skill Check if required
        if matched.skill_check:
            check = matched.skill_check
            skill_mod = character.get_skill_modifier(check.skill)
            roll_outcome = self.dice.roll_d20(modifier=skill_mod)

            is_success = roll_outcome.total >= check.dc or roll_outcome.is_critical_hit
            if roll_outcome.is_critical_miss:
                is_success = False

            if is_success:
                target_node = check.success_node_id
                narrative_log = f"[{check.skill.value} Check: {roll_outcome.summary()} vs DC {check.dc}] {check.success_message}"
            else:
                target_node = check.failure_node_id
                damage_taken = 0
                if check.failure_damage_formula:
                    dmg_roll = self.dice.parse_and_roll(check.failure_damage_formula)
                    damage_taken = character.apply_damage(dmg_roll.total)
                    narrative_log = (
                        f"[{check.skill.value} Check: {roll_outcome.summary()} vs DC {check.dc}] "
                        f"{check.failure_message} You take {damage_taken} damage ({dmg_roll.formula})."
                    )
                else:
                    narrative_log = f"[{check.skill.value} Check: {roll_outcome.summary()} vs DC {check.dc}] {check.failure_message}"

        # 2. Advance to the target node
        if not target_node or target_node not in self.nodes:
            raise ValueError(f"Target node {target_node} does not exist.")

        self.current_node_id = target_node
        next_node = self.nodes[target_node]

        # 3. Check for Combat Trigger
        is_combat = bool(next_node.monster_ids)

        # 4. Save updated character state
        self.db.save_character(character)

        return TransitionResult(
            next_node_id=target_node,
            narrative_log=narrative_log,
            roll_outcome=roll_outcome,
            is_combat_triggered=is_combat,
            monsters_encountered=next_node.monster_ids
        )
