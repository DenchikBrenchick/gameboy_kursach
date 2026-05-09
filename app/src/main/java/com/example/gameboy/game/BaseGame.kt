package com.example.gameboy.game

import android.graphics.Canvas

// Абстрактний базовий клас для всіх ігор (Абстракція + Успадкування)
// Містить спільну логіку: очки, рекорд, стан гри, повноекранний режим
// SnakeGame, ArkanoidGame, SpaceShooterGame успадковують цей клас
abstract class BaseGame(
    initialHighScore: Int = 0,
    protected val onGameOver: (finalScore: Int) -> Unit,
    protected val onGameStart: () -> Unit
) : IGame {

    // ── Спільний стан (Інкапсуляція: зміна лише через методи нащадків) ──
    protected var score: Int = 0
    protected var highScore: Int = initialHighScore

    // Реалізуємо властивості з IGame
    override var isGameOver: Boolean = true
        protected set

    override var isFullscreen: Boolean = false

    // ── Реалізація updateHighScore з IGame ──
    override fun updateHighScore(newHighScore: Int) {
        if (newHighScore > highScore) {
            highScore = newHighScore
        }
    }

    // ── Скидання спільного стану при старті нової гри ──
    protected fun resetBaseState() {
        score = 0
        isGameOver = false
    }

    // ── Фіксуємо програш і передаємо фінальний рахунок назовні ──
    protected fun triggerGameOver() {
        isGameOver = true
        onGameOver(score)
    }
}
