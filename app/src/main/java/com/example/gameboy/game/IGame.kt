package com.example.gameboy.game

import android.graphics.Canvas

// Загальний шаблон для будь-якої гри (Абстракція)
interface IGame {

    // ── Стан гри (спільне для всіх) ──
    val isGameOver: Boolean
    var isFullscreen: Boolean

    // Викликається один раз при запуску картриджа
    fun start()

    // Викликається 30-60 разів на секунду для розрахунку логіки (фізика, рух)
    fun update()

    // Викликається 30-60 разів на секунду для малювання картинки
    fun draw(canvas: Canvas)

    // Поліморфізм: гравець натиснув кнопку — гра сама вирішує що робити
    fun handleInput(action: InputAction)

    // Оновити рекорд з бази даних
    fun updateHighScore(newHighScore: Int)

    // Повноекранний режим: палець рухається по екрану (Arkanoid/SpaceShooter)
    fun onTouchMove(percentX: Float) { /* За замовчуванням — ігнорується */ }

    // Повноекранний режим: палець відпущено (SpaceShooter зупиняє стрільбу)
    fun onTouchRelease() { /* За замовчуванням — ігнорується */ }
}
