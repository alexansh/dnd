#!/usr/bin/env python3
"""
Grimoire 5E - Royalty-Free CC0 Procedural Fantasy Soundtrack Generator.

Generates 54 unique, atmospheric, loopable game audio tracks across
9 core fantasy RPG environments:
1. Tavern (6 tracks): Warm lute, rustic folk rhythms
2. Crypt (6 tracks): Dark subterranean drone, eerie minor intervals
3. Forge (6 tracks): Basalt anvil rhythms, deep magma hum
4. Cathedral (6 tracks): Sacred pipe organ chords, celestial choir resonance
5. Forest (6 tracks): Fey flute melodies, enchanted woodland winds
6. Cavern (6 tracks): Crystalline echoes, underground drip tension
7. Combat (6 tracks): War drums, frantic martial rhythm, adrenaline pace
8. Boss (6 tracks): Dramatic crescendos, dark titan orchestral pulses
9. Rest & Mystery (6 tracks): Midnight campfire peace, occult mystery harp

Saves output as 16-bit 22050Hz PCM WAV files directly to:
android/app/src/main/assets/audio/
"""

import os
import math
import wave
import struct

SAMPLE_RATE = 22050
DURATION_SEC = 6.0  # 6-second seamless looping ambient motif
TOTAL_SAMPLES = int(SAMPLE_RATE * DURATION_SEC)

DEST_DIR = os.path.join(
    os.path.dirname(__file__),
    "..", "android", "app", "src", "main", "assets", "audio"
)
DEST_DIR = os.path.abspath(DEST_DIR)
os.makedirs(DEST_DIR, exist_ok=True)

TRACK_THEMES = [
    # (prefix, count, base_freq, style)
    ("tavern", 6, 220.0, "lute_folk"),       # A3
    ("crypt", 6, 110.0, "dark_drone"),       # A2
    ("forge", 6, 146.83, "industrial_anvil"),# D3
    ("cathedral", 6, 130.81, "organ_choir"), # C3
    ("forest", 6, 293.66, "fey_woodland"),   # D4
    ("cavern", 6, 98.00, "crystal_echo"),    # G2
    ("combat", 6, 164.81, "war_drums"),      # E3
    ("boss", 6, 73.42, "titan_orchestra"),   # D2
    ("mystery", 3, 174.61, "occult_harp"),   # F3
    ("rest", 3, 261.63, "campfire_peace")    # C4
]

def generate_track_samples(theme: str, track_index: int) -> list:
    samples = []
    base_f = 220.0
    for prefix, count, freq, style in TRACK_THEMES:
        if theme.startswith(prefix):
            base_f = freq * (1.0 + (track_index * 0.05))
            break

    for i in range(TOTAL_SAMPLES):
        t = float(i) / SAMPLE_RATE
        env = 1.0 - 0.1 * math.cos(2.0 * math.pi * t / DURATION_SEC) # Seamless loop envelope

        if "tavern" in theme:
            # Plucked acoustic lute pattern with warm 5th harmony
            beat = math.floor(t * 3.0) % 4
            f_offset = [1.0, 1.25, 1.5, 1.33][beat]
            sig = (math.sin(2.0 * math.pi * base_f * f_offset * t) +
                   0.5 * math.sin(4.0 * math.pi * base_f * f_offset * t))
            decay = math.exp(-3.0 * ((t * 3.0) % 1.0))
            val = sig * decay * 0.6 + 0.15 * math.sin(2.0 * math.pi * (base_f / 2) * t)

        elif "crypt" in theme:
            # Deep ominous sub-bass drone with dissonant minor 2nd beating
            drone = math.sin(2.0 * math.pi * base_f * t)
            beating = 0.5 * math.sin(2.0 * math.pi * (base_f * 1.06) * t)
            pulse = math.sin(2.0 * math.pi * 0.25 * t)
            val = (drone + beating) * (0.6 + 0.4 * pulse)

        elif "forge" in theme:
            # Rhythmic hammer strikes on anvil with volcanic sub-rumble
            strike = math.exp(-12.0 * ((t * 2.0) % 1.0))
            clang = math.sin(2.0 * math.pi * 880.0 * t) + 0.5 * math.sin(2.0 * math.pi * 1760.0 * t)
            magma = 0.4 * math.sin(2.0 * math.pi * 55.0 * t)
            val = (clang * strike * 0.7) + magma

        elif "cathedral" in theme:
            # Organ chord (root, major third, fifth, octave) with slow vibrato
            vibrato = 1.0 + 0.01 * math.sin(2.0 * math.pi * 4.0 * t)
            f = base_f * vibrato
            chord = (math.sin(2.0 * math.pi * f * t) +
                     0.7 * math.sin(2.0 * math.pi * (f * 1.25) * t) +
                     0.6 * math.sin(2.0 * math.pi * (f * 1.5) * t) +
                     0.4 * math.sin(2.0 * math.pi * (f * 2.0) * t))
            val = chord * 0.35

        elif "forest" in theme:
            # Fey wooden flute with breathy modulation
            flute_freq = base_f * (1.0 + 0.25 * math.sin(2.0 * math.pi * 0.5 * t))
            flute = math.sin(2.0 * math.pi * flute_freq * t) + 0.2 * math.sin(4.0 * math.pi * flute_freq * t)
            wind = 0.15 * math.sin(2.0 * math.pi * 120.0 * t) * math.sin(2.0 * math.pi * 0.3 * t)
            val = (flute * 0.5) + wind

        elif "cavern" in theme:
            # Echoing mineral drip and crystalline harmonics
            drip_time = (t * 1.5) % 1.0
            drip = math.sin(2.0 * math.pi * (base_f * 4.0) * t) * math.exp(-15.0 * drip_time)
            abyss = 0.4 * math.sin(2.0 * math.pi * (base_f * 0.5) * t)
            val = drip * 0.6 + abyss

        elif "combat" in theme:
            # Pounding war drum polyrhythm with adrenaline brass pulses
            drum_t = (t * 4.0) % 1.0
            drum = math.sin(2.0 * math.pi * 70.0 * (1.0 - drum_t * 0.5) * t) * math.exp(-8.0 * drum_t)
            brass = 0.35 * math.sin(2.0 * math.pi * (base_f * 2.0) * t) * (1.0 if (t % 1.0) < 0.25 else 0.0)
            val = (drum * 0.7) + brass

        elif "boss" in theme:
            # Heavy titan march with dark choir octave drops
            t_march = (t * 2.0) % 1.0
            impact = math.sin(2.0 * math.pi * 50.0 * t) * math.exp(-6.0 * t_march)
            titan = 0.4 * (math.sin(2.0 * math.pi * base_f * t) +
                           math.sin(2.0 * math.pi * (base_f * 1.05) * t))
            val = (impact * 0.6) + titan

        elif "mystery" in theme:
            # Eerie plucked harp in a harmonic minor progression
            step = math.floor(t * 2.0) % 4
            f_arp = [1.0, 1.2, 1.4, 1.6][step]
            harp = math.sin(2.0 * math.pi * base_f * f_arp * t) * math.exp(-4.0 * ((t * 2.0) % 1.0))
            pad = 0.2 * math.sin(2.0 * math.pi * (base_f / 2) * t)
            val = harp * 0.6 + pad

        else: # "rest"
            # Peaceful acoustic chord progression
            chord = (math.sin(2.0 * math.pi * base_f * t) +
                     0.5 * math.sin(2.0 * math.pi * (base_f * 1.25) * t) +
                     0.4 * math.sin(2.0 * math.pi * (base_f * 1.5) * t))
            gentle_swell = 0.5 + 0.3 * math.sin(2.0 * math.pi * 0.3 * t)
            val = chord * gentle_swell * 0.4

        sample_clamped = max(-0.95, min(0.95, val * env))
        sample_int = int(sample_clamped * 32767.0)
        samples.append(sample_int)

    return samples

def main():
    print("=" * 65)
    print("  GRIMOIRE 5E - GENERATING 54 SOUNDTRACK LOOPS")
    print("=" * 65)

    created_tracks = []
    track_counter = 1

    for prefix, count, freq, style in TRACK_THEMES:
        for i in range(1, count + 1):
            track_name = f"track_{prefix}_{i:02d}.wav"
            file_path = os.path.join(DEST_DIR, track_name)

            samples = generate_track_samples(prefix, i)

            with wave.open(file_path, "wb") as wav_file:
                wav_file.setnchannels(1)       # Mono
                wav_file.setsampwidth(2)      # 16-bit
                wav_file.setframerate(SAMPLE_RATE)
                packed_data = struct.pack(f"<{len(samples)}h", *samples)
                wav_file.writeframes(packed_data)

            created_tracks.append((track_counter, track_name, prefix))
            track_counter += 1

    print(f"\n[SUCCESS] Successfully generated {len(created_tracks)} soundtrack audio loops in:")
    print(f"  -> {DEST_DIR}\n")
    print("Sample generated tracks:")
    for num, name, pfx in created_tracks[:12]:
        print(f"  #{num:02d} [{pfx.upper()}]: {name}")
    print(f"  ... and {len(created_tracks) - 12} more tracks.")

if __name__ == "__main__":
    main()
