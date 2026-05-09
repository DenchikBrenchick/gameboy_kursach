package com.example.gameboy

import android.app.Application
import com.example.gameboy.game.ISoundManager
import com.example.gameboy.game.SoundManager

class GameBoyApplication : Application() {
    // Інкапсуляція: доступ тільки для читання
    lateinit var soundManager: ISoundManager
        private set

    override fun onCreate() {
        super.onCreate()
        // Впровадження залежностей: створюємо один екземпляр на весь життєвий цикл додатка
        soundManager = SoundManager(this)
    }

    override fun onTerminate() {
        if (::soundManager.isInitialized) {
            soundManager.release()
        }
        super.onTerminate()
    }
}
