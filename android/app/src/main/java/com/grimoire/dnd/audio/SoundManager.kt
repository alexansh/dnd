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

    fun playMusic(trackRes: Int) {
        if (!isMusicEnabled) return
        try {
            if (mediaPlayer != null && currentTrackRes == trackRes && mediaPlayer!!.isPlaying) {
                return
            }
            mediaPlayer?.stop()
            mediaPlayer?.release()
            currentTrackRes = trackRes
            mediaPlayer = MediaPlayer.create(appContext, trackRes).apply {
                isLooping = true
                setVolume(0.4f, 0.4f)
                start()
            }
        } catch (_: Exception) {}
    }

    fun startAmbientMusic() = playMusic(R.raw.ambient_music)
    fun playBattleMusic() = playMusic(R.raw.music_battle)
    fun playDungeonMusic() = playMusic(R.raw.music_dungeon)
    fun playBossMusic() = playMusic(R.raw.music_boss)

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
