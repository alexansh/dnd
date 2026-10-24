# Grimoire 5E: The Tabletop Chronicle

A 100% offline, commercial-grade D&D 5th Edition Android game built natively with **Kotlin & Jetpack Compose**.

---

## 📱 Android Game APK

The compiled Android application is ready to install:

* **File:** [`Grimoire5E.apk`](file:///C:/Users/saras/OneDrive/Desktop/project/dnd/Grimoire5E.apk) (9.6 MB)
* **Direct Build Path:** `android/app/build/outputs/apk/debug/app-debug.apk`

### How to Install & Play on your Android Phone:

1. Connect your Android phone via USB (or send `Grimoire5E.apk` to your phone via Google Drive, WhatsApp, or Quick Share).
2. Tap the `.apk` file on your phone and select **Install** (allow "Install from unknown sources" if prompted).
3. Open **Grimoire 5E** and enjoy 100% offline solo D&D!

---

## 🌟 Key Features

1. **Native Android UI (Jetpack Compose)**
   * **Portrait One-Handed Layout:** Clean, dark fantasy aesthetic with brass and parchment motifs.
   * **Live Vitals HUD:** Dynamic animated health bar, AC shield rating, speed, proficiency bonus, and gold tracking.
   * **Tactile Dice Roller Tray:** Integrated modal rolling `d4`, `d6`, `d8`, `d10`, `d12`, `d20`, `d100` with **vibrating haptic feedback** on every roll and critical hit.

2. **Deterministic 5E Rules Adjudicator (`engine/`)**
   * **Math & Modifiers:** Exact 5E formulas: $\lfloor(\text{Score} - 10) / 2\rfloor$, proficiency bonus scaling across levels 1–20 ($\lceil 1 + \text{Level} / 4\rceil$).
   * **Death Saves:** 3 successes/failures, Natural 20 instant revival with 1 HP, and Natural 1 double failures.
   * **Advantage & Disadvantage:** Automatic discard of the lower/higher die.

3. **Hierarchical State Machine (Adventure Engine)**
   * **Cyclic Exploration:** Full support for backtracking to towns, hubs, and previously visited rooms.
   * **Room State Memory:** Dynamic atmospheric narration that changes when revisiting cleared or explored rooms.
   * **Automated 5E Skill Checks:** Dynamic DC checks against character ability and skill modifiers.
   * **Flag-Gated Secrets:** Unlock special choices and shortcuts when discovering keys, relics, or clues.

4. **Campaign 1: The Sunken King's Legacy (The Drowned Crypt)**
   * Full multi-room adventure with town NPC dialogue, graveyard exploration, puzzle-solving, traps, and boss encounters against the Wight Lord.

---

## 🛠️ Project Structure

```
dnd/
├── Grimoire5E.apk             # Ready-to-install Android APK
├── play.py                    # Terminal CLI runner for quick testing
├── core_5e/                   # Python reference 5E engine
├── tests/                     # 24 automated unit tests
└── android/                   # Native Android Studio Project (Kotlin + Compose)
    ├── app/
    │   └── src/main/java/com/grimoire/dnd/
    │       ├── MainActivity.kt
    │       ├── engine/        # Kotlin 5E rules, dice & character engine
    │       ├── data/          # Campaign 1 story graph
    │       └── ui/            # Jetpack Compose UI & Theme
```
