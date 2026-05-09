package com.example.gameboy.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.gameboy.GameBoyApplication
import com.example.gameboy.game.ISoundManager

abstract class BaseActivity : AppCompatActivity() {

    protected lateinit var soundManager: ISoundManager
    protected val styleProvider: IStyleProvider = StyleManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        soundManager = (applicationContext as GameBoyApplication).soundManager
        hideSystemUI()
    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    override fun onResume() {
        super.onResume()
        // Застосовуємо стиль при кожному поверненні на екран
        styleProvider.applyStyle(this)
        playScreenMusic()
    }

    override fun onPause() {
        super.onPause()
        soundManager.pauseAllMusic()
    }

    protected open fun playScreenMusic() {
        soundManager.playTheme()
    }
}
