package com.example.gameboy.game

interface ISoundManager {
    fun setMusicVolume(volumePercent: Int)
    fun setSfxVolume(volumePercent: Int)
    fun playTheme()
    fun playDeathTheme()
    fun pauseAllMusic()
    fun playEat()
    fun playDieSfx()
    fun playClick()
    fun getMusicVolume(): Int
    fun getSfxVolume(): Int
    fun release()
}