# Grimoire 5E: The AI Tabletop Chronicle

A production-grade, 100% offline-ready solo D&D 5th Edition game built natively for Android using **Kotlin & Jetpack Compose**, featuring an AI Dungeon Master architecture governed strictly by deterministic 5E rules.

---

## 🏛️ Core Architectural Principle: "The DM Proposes, the Engine Disposes"

In Grimoire 5E, the Large Language Model (LLM) **never edits game state directly**.
Instead, the player writes in free text what their character attempts in-world, and the turn executes through a hardened, unidirectional pipeline:

```
[Player Free-Text Attempt]
         │
         ▼
[Intent Parser] ── (Sanitizes input, strips injection delimiters, classifies approach)
         │
         ▼
[Context Builder] ── (Retrieves visible entities, active clocks, and canon ledger)
         │
         ▼
[DM Proposer] ── (Emits structured proposal: narration + tool calls + proposed canon facts)
         │
         ▼
[Rule Legality Validator] ── (Validates DC bounds 5-30, legal 5E abilities, inventory & spell slots;
                              auto-retries LLM on failure up to 2 times)
         │
         ▼
[Deterministic 5E Engine] ── (Executes dice rolls, tactical movement, damage, conditions, rests)
         │
         ▼
[Canon Auditor] ── (Filters narrative hallucinations and contradictory assertions)
         │
         ▼
[State Commit & Event Log] ── (Appends to Room/SQLite database with full replayability)
         │
         ▼
[Jetpack Compose UI] ── (Presents narration, transparent spoiler-safe roll results, living chronicle)
```

---

## 🌟 Key Subsystems & Features

### 1. Free-Text Exploration & Prompt-Injection Defense
- **Free-Text Actions:** Players describe actions in their own words ("I slip into the alcove and look for tripwires").
- **Delimiters & Prompt Fencing:** Player attempts are wrapped inside `<in_fiction_player_attempt>` fences. Delimiter manipulation and prompt injections ("ignore previous instructions", "I kill the dragon instantly") are neutralized and treated as in-fiction attempts or rejected by the engine.

### 2. Deterministic 5E SRD 5.1 Rules Engine
- **Character Progression:** Complete SRD 5.1 level 1–10+ progression, including hit dice, proficiency bonus scaling, extra attacks, and spell slot pools across 1st through 5th level.
- **Combat & Condition Gating:** Full tactical combat arena enforcing action economy (Action, Bonus Action, Reaction), condition immunity/penalties (Prone, Restrained, Blinded, Poisoned), concentration checks on taking damage (`max(10, damage / 2)`), and death saving throws.
- **Python Oracle Parity:** 400+ golden vectors generated from `core_5e/` and verified against the Kotlin engine on every build.

### 3. Persistent World-State & Canon Consistency
- **Room / SQLite Single Source of Truth:**
  - Entities (NPCs, Locations, Factions, Items, Quests)
  - Relationships with affinity scores
  - Progress Clocks with automated advancement triggers
  - DM Secrets layer (facts hidden until discovered)
  - Append-only Event Log supporting full deterministic replay
- **Canon Ledger & Auditor:** Every fact stated by the DM is cataloged. Before state commitment, the Canon Auditor verifies that newly proposed facts do not contradict previously established canon.

### 4. Companion Party Mode with Bounded Autonomy
- **AI Companions (1–3):** Each companion has a distinct moral alignment, goals, bonds, and backstory.
- **Dynamic Approval:** Companions approve or disapprove of player choices (Heroic, Ruthless, Mercenary, Mercy).
- **Mutiny / Desertion:** If companion approval drops to critical levels ($\le 15$), companions protest and abandon the party in disgust.

### 5. Living Chronicle & Presentation
- **Living Journal:** Real-time modal inspecting countdown clocks, quest logs, NPC Codex, and canon history.
- **Atmospheric Visual Scenes:** Dynamic scene card artwork with fantasy environments (tavern, crypt, dwarven forge, cathedral).
- **Tactile Feedback:** Multi-stage haptic vibration on dice rolls and critical strikes.

---

## 🧪 Testing & Verification Suite

Grimoire 5E maintains rigorous quality and regression gates:

- **Android Unit & Parity Tests:** 42 automated tests spanning dice mechanics, golden vectors, Room database operations, legality validation, turn pipelines, combat, and companions.
  ```bash
  cd android
  ./gradlew testDebugUnitTest
  ```
- **Applied LLM Evaluation Harness (`scripts/eval_harness.py`):**
  Evaluates schema conformance, rule violation rejections, prompt-injection containment, and canon contradiction defense.
  ```bash
  python scripts/eval_harness.py
  ```
- **Python Reference Engine Tests:**
  ```bash
  pytest tests/test_engine.py
  ```

---

## 🔒 Security & Privacy

- **100% Offline-Capable:** Ships with `DeterministicFallbackProvider` and the seeded `SunkenKingModule` for full offline gameplay with zero network dependency.
- **Zero API Keys in APK:** No cloud provider credentials or sensitive tokens are stored in the client APK.
- **Local Storage Only:** All campaign progress, transcripts, and logs remain strictly within the device's local SQLite database.

---

## ⚖️ Legal & Attribution Notice

This work includes material taken from the **System Reference Document 5.1 ("SRD 5.1")** by Wizards of the Coast LLC, available at [https://dnd.wizards.com/resources/systems-reference-document](https://dnd.wizards.com/resources/systems-reference-document). The SRD 5.1 is licensed under the **Creative Commons Attribution 4.0 International License (CC-BY-4.0)**, available at [https://creativecommons.org/licenses/by/4.0/legalcode](https://creativecommons.org/licenses/by/4.0/legalcode).

All mechanics, stat blocks, and classes within this software conform strictly to SRD 5.1. No non-SRD copyrighted content (such as D&D Beyond or supplemental trademarked books) is incorporated.
