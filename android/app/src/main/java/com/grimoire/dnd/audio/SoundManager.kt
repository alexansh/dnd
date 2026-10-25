package com.grimoire.dnd.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import com.grimoire.dnd.R

class SoundManager private constructor(context: Context) {
    private val appContext = context.applicationContext
    private var soundPool: SoundPool? = null
    private var mediaPlayer: MediaPlayer? = null

    private var sfxDiceRoll: Int = 0
    private var sfxDiceCrit: Int = 0
    private var sfxSwordHit: Int = 0
    private var sfxSpellCast: Int = 0
    private var sfxPotionDrink: Int = 0
    private var sfxLevelUp: Int = 0

    var isSfxEnabled: Boolean = true
    var isMusicEnabled: Boolean = true

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(6)
            .setAudioAttributes(audioAttributes)
            .build().apply {
                sfxDiceRoll = load(appContext, R.raw.sfx_dice_roll, 1)
                sfxDiceCrit = load(appContext, R.raw.sfx_dice_crit, 1)
                sfxSwordHit = load(appContext, R.raw.sfx_sword_hit, 1)
                sfxSpellCast = load(appContext, R.raw.sfx_spell_cast, 1)
                sfxPotionDrink = load(appContext, R.raw.sfx_potion_drink, 1)
                sfxLevelUp = load(appContext, R.raw.sfx_level_up, 1)
            }
    }

    fun playDiceRoll() {
        if (!isSfxEnabled) return
        soundPool?.play(sfxDiceRoll, 0.85f, 0.85f, 1, 0, 1.0f)
    }

    fun playDiceCrit() {
        if (!isSfxEnabled) return
        soundPool?.play(sfxDiceCrit, 1.0f, 1.0f, 2, 0, 1.0f)
    }

    fun playSwordHit() {
        if (!isSfxEnabled) return
        soundPool?.play(sfxSwordHit, 0.9f, 0.9f, 1, 0, 1.0f)
    }

    fun playSpellCast() {
        if (!isSfxEnabled) return
        soundPool?.play(sfxSpellCast, 0.8f, 0.8f, 1, 0, 1.0f)
    }

    fun playPotionDrink() {
        if (!isSfxEnabled) return
        soundPool?.play(sfxPotionDrink, 0.9f, 0.9f, 1, 0, 1.0f)
    }

    fun playLevelUp() {
        if (!isSfxEnabled) return
        soundPool?.play(sfxLevelUp, 1.0f, 1.0f, 2, 0, 1.0f)
    }

    private var currentTrackRes: Int = R.raw.ambient_music
    private var currentAssetTrack: String? = null

    fun playMusic(trackRes: Int) {
        if (!isMusicEnabled) return
        try {
            if (mediaPlayer != null && currentTrackRes == trackRes && currentAssetTrack == null && mediaPlayer!!.isPlaying) {
                return
            }
            mediaPlayer?.stop()
            mediaPlayer?.release()
            currentTrackRes = trackRes
            currentAssetTrack = null
            mediaPlayer = MediaPlayer.create(appContext, trackRes).apply {
                isLooping = true
                setVolume(0.4f, 0.4f)
                start()
            }
        } catch (_: Exception) {}
    }

    /**
     * Dynamically switches between the 50+ procedural scene soundtracks based on current environment.
     */
    fun playSceneSoundtrack(sceneTag: String, variationIndex: Int = 1) {
        if (!isMusicEnabled) return
        val normalizedTag = sceneTag.lowercase()
        val themePrefix = when {
            normalizedTag.contains("tavern") || normalizedTag.contains("inn") -> "tavern"
            normalizedTag.contains("crypt") || normalizedTag.contains("tomb") || normalizedTag.contains("grave") -> "crypt"
            normalizedTag.contains("forge") || normalizedTag.contains("fire") || normalizedTag.contains("lava") -> "forge"
            normalizedTag.contains("cathedral") || normalizedTag.contains("temple") || normalizedTag.contains("altar") -> "cathedral"
            normalizedTag.contains("forest") || normalizedTag.contains("wood") || normalizedTag.contains("grove") -> "forest"
            normalizedTag.contains("cavern") || normalizedTag.contains("cave") || normalizedTag.contains("mine") -> "cavern"
            normalizedTag.contains("boss") || normalizedTag.contains("lord") || normalizedTag.contains("dragon") -> "boss"
            normalizedTag.contains("combat") || normalizedTag.contains("battle") || normalizedTag.contains("fight") -> "combat"
            normalizedTag.contains("rest") || normalizedTag.contains("camp") -> "rest"
            else -> "mystery"
        }

        val maxVars = if (themePrefix == "mystery" || themePrefix == "rest") 3 else 6
        val selectedIdx = ((variationIndex - 1).coerceAtLeast(0) % maxVars) + 1
        val assetFileName = "track_${themePrefix}_${String.format("%02d", selectedIdx)}.wav"

        if (currentAssetTrack == assetFileName && mediaPlayer?.isPlaying == true) {
            return
        }

        try {
            val afd = appContext.assets.openFd("audio/$assetFileName")
            mediaPlayer?.stop()
            mediaPlayer?.release()
            currentAssetTrack = assetFileName
            currentTrackRes = 0

            mediaPlayer = MediaPlayer().apply {
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                prepare()
                isLooping = true
                setVolume(0.45f, 0.45f)
                start()
            }
        } catch (_: Exception) {
            // Fallback to bundled raw track if asset streaming fails
            startAmbientMusic()
        }
    }

    fun startAmbientMusic() = playMusic(R.raw.ambient_music)
    fun playBattleMusic() = playSceneSoundtrack("combat", 1)
    fun playDungeonMusic() = playSceneSoundtrack("crypt", 1)
    fun playBossMusic() = playSceneSoundtrack("boss", 1)

    fun pauseAmbientMusic() {
        try {
            mediaPlayer?.pause()
        } catch (_: Exception) {}
    }

    fun release() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            soundPool?.release()
            soundPool = null
        } catch (_: Exception) {}
    }

    companion object {
        @Volatile
        private var instance: SoundManager? = null

        fun getInstance(context: Context): SoundManager {
            return instance ?: synchronized(this) {
                instance ?: SoundManager(context).also { instance = it }
            }
        }
    }
}
