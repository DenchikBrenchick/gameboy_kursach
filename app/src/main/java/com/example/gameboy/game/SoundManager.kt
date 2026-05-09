package com.example.gameboy.game

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import com.example.gameboy.Constants
import com.example.gameboy.R

class SoundManager(private val context: Context) : ISoundManager {
    private var themePlayer: MediaPlayer? = null
    private var deathThemePlayer: MediaPlayer? = null
    private var soundPool: SoundPool? = null

    private var eatSoundId: Int = 0
    private var dieSoundId: Int = 0
    private var clickSoundId: Int = 0

    private var currentMusicVolume = 1.0f
    private var currentSfxVolume = 1.0f

    init {
        val appCtx = context.applicationContext
        val prefs = appCtx.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        currentMusicVolume = prefs.getInt(Constants.PREF_MUSIC_VOLUME, 100) / 100f
        currentSfxVolume = prefs.getInt(Constants.PREF_SFX_VOLUME, 100) / 100f

        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(attributes)
            .build()

        val appSoundPool = soundPool
        if (appSoundPool != null) {
            eatSoundId = appSoundPool.load(appCtx, R.raw.eat, 1)
            dieSoundId = appSoundPool.load(appCtx, R.raw.die, 1)
            clickSoundId = appSoundPool.load(appCtx, R.raw.click, 1)
        }

        themePlayer = MediaPlayer.create(appCtx, R.raw.theme)
        themePlayer?.isLooping = true
        themePlayer?.setVolume(currentMusicVolume, currentMusicVolume)
    }

    override fun setMusicVolume(volumePercent: Int) {
        currentMusicVolume = volumePercent / 100f
        context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(Constants.PREF_MUSIC_VOLUME, volumePercent)
            .apply()

        themePlayer?.setVolume(currentMusicVolume, currentMusicVolume)
        deathThemePlayer?.setVolume(currentMusicVolume, currentMusicVolume)
    }

    override fun setSfxVolume(volumePercent: Int) {
        currentSfxVolume = volumePercent / 100f
        context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(Constants.PREF_SFX_VOLUME, volumePercent)
            .apply()
    }

    override fun playTheme() {
        stopDeathTheme()
        if (themePlayer?.isPlaying != true) themePlayer?.start()
    }

    override fun playDeathTheme() {
        themePlayer?.pause()
        stopDeathTheme()
        deathThemePlayer = MediaPlayer.create(context.applicationContext, R.raw.death_theme)?.apply {
            isLooping = false
            setVolume(currentMusicVolume, currentMusicVolume)
            setOnCompletionListener { player ->
                player.release()
                if (deathThemePlayer === player) {
                    deathThemePlayer = null
                }
            }
            setOnErrorListener { player, _, _ ->
                player.release()
                if (deathThemePlayer === player) {
                    deathThemePlayer = null
                }
                true
            }
            start()
        }
    }

    override fun pauseAllMusic() {
        themePlayer?.pause()
        stopDeathTheme()
    }

    override fun playEat() {
        soundPool?.play(eatSoundId, currentSfxVolume, currentSfxVolume, 1, 0, 1f)
    }

    override fun playDieSfx() {
        soundPool?.play(dieSoundId, currentSfxVolume, currentSfxVolume, 1, 0, 1f)
    }

    override fun playClick() {
        soundPool?.play(clickSoundId, currentSfxVolume, currentSfxVolume, 1, 0, 1f)
    }

    override fun getMusicVolume() = (currentMusicVolume * 100).toInt()

    override fun getSfxVolume() = (currentSfxVolume * 100).toInt()

    private fun stopDeathTheme() {
        deathThemePlayer?.let { player ->
            if (player.isPlaying) player.stop()
            player.release()
        }
        deathThemePlayer = null
    }

    override fun release() {
        themePlayer?.release()
        deathThemePlayer?.release()
        soundPool?.release()
        themePlayer = null
        deathThemePlayer = null
        soundPool = null
    }
}
