"""
Grimoire 5E - Dice Engine & Roll Adjudicator
Handles polyhedral dice, advantage/disadvantage, critical hits, and formula parsing.
"""

from dataclasses import dataclass, field
from enum import Enum
import random
import re
from typing import List, Optional, Tuple


class RollType(Enum):
    NORMAL = "normal"
    ADVANTAGE = "advantage"
    DISADVANTAGE = "disadvantage"


@dataclass
class DieResult:
    die_type: int  # 4, 6, 8, 10, 12, 20, 100
    value: int
    is_discarded: bool = False


@dataclass
class RollOutcome:
    total: int
    dice_results: List[DieResult]
    modifier: int = 0
    roll_type: RollType = RollType.NORMAL
    is_critical_hit: bool = False
    is_critical_miss: bool = False
    formula: str = ""

    def summary(self) -> str:
        kept_dice = [str(d.value) for d in self.dice_results if not d.is_discarded]
        discarded = [f"({d.value} discarded)" for d in self.dice_results if d.is_discarded]
        dice_str = ", ".join(kept_dice + discarded)
        mod_str = f" + {self.modifier}" if self.modifier > 0 else (f" - {abs(self.modifier)}" if self.modifier < 0 else "")
        crit_str = " [CRITICAL HIT!]" if self.is_critical_hit else (" [NATURAL 1 FAIL!]" if self.is_critical_miss else "")
        return f"Roll: [{dice_str}]{mod_str} = {self.total}{crit_str}"


class DiceEngine:
    """Deterministic Polyhedral Dice Simulator adhering to 5th Edition SRD rules."""

    def __init__(self, rng_seed: Optional[int] = None):
        self._rng = random.Random(rng_seed)

    def roll_die(self, sides: int) -> int:
        if sides < 1:
            raise ValueError("Dice must have at least 1 side.")
        return self._rng.randint(1, sides)

    def roll_d20(self, modifier: int = 0, roll_type: RollType = RollType.NORMAL) -> RollOutcome:
        """
        Adjudicates a d20 roll under Normal, Advantage, or Disadvantage conditions.
        Automatically evaluates Natural 20 (Crit) and Natural 1 (Fumble).
        """
        r1 = self.roll_die(20)

        if roll_type == RollType.NORMAL:
            is_crit = (r1 == 20)
            is_fumble = (r1 == 1)
            return RollOutcome(
                total=r1 + modifier,
                dice_results=[DieResult(20, r1)],
                modifier=modifier,
                roll_type=roll_type,
                is_critical_hit=is_crit,
                is_critical_miss=is_fumble,
                formula=f"1d20{'+' if modifier >= 0 else ''}{modifier}"
            )

        r2 = self.roll_die(20)
        if roll_type == RollType.ADVANTAGE:
            kept = max(r1, r2)
            d1 = DieResult(20, r1, is_discarded=(r1 < r2))
            d2 = DieResult(20, r2, is_discarded=(r2 < r1 if r1 != r2 else False))
            # If tied, discard one arbitrarily
            if r1 == r2:
                d2.is_discarded = True
        else:  # DISADVANTAGE
            kept = min(r1, r2)
            d1 = DieResult(20, r1, is_discarded=(r1 > r2))
            d2 = DieResult(20, r2, is_discarded=(r2 > r1 if r1 != r2 else False))
            if r1 == r2:
                d2.is_discarded = True

        is_crit = (kept == 20)
        is_fumble = (kept == 1)

        return RollOutcome(
            total=kept + modifier,
            dice_results=[d1, d2],
            modifier=modifier,
            roll_type=roll_type,
            is_critical_hit=is_crit,
            is_critical_miss=is_fumble,
            formula=f"1d20 ({roll_type.value}){'+' if modifier >= 0 else ''}{modifier}"
        )

    def roll_damage(self, dice_count: int, sides: int, modifier: int = 0, is_critical: bool = False) -> RollOutcome:
        """
        Adjudicates a damage roll.
        On a critical hit, all damage dice are doubled; flat modifiers remain unchanged.
        """
        total_dice = dice_count * 2 if is_critical else dice_count
        results = [DieResult(sides, self.roll_die(sides)) for _ in range(total_dice)]
        total = sum(d.value for d in results) + modifier

        return RollOutcome(
            total=max(0, total),
            dice_results=results,
            modifier=modifier,
            is_critical_hit=is_critical,
            formula=f"{total_dice}d{sides}{'+' if modifier >= 0 else ''}{modifier}"
        )

    def parse_and_roll(self, formula: str, is_critical: bool = False) -> RollOutcome:
        """
        Parses standard D&D dice notation e.g., '2d6 + 3', '1d8 + 1d4 + 2', '1d20 - 1'.
        """
        cleaned = formula.replace(" ", "").lower()
        tokens = re.findall(r"([+-]?\d*d\d+|[+-]?\d+)", cleaned)

        results: List[DieResult] = []
        static_mod = 0

        for token in tokens:
            sign = -1 if token.startswith("-") else 1
            raw = token.lstrip("+-")

            if "d" in raw:
                parts = raw.split("d")
                count = int(parts[0]) if parts[0] else 1
                sides = int(parts[1])
                actual_count = count * 2 if is_critical else count
                for _ in range(actual_count):
                    val = self.roll_die(sides) * sign
                    results.append(DieResult(sides, val))
            else:
                static_mod += int(raw) * sign

        dice_total = sum(d.value for d in results)
        total = dice_total + static_mod

        return RollOutcome(
            total=total,
            dice_results=results,
            modifier=static_mod,
            is_critical_hit=is_critical,
            formula=formula
        )
