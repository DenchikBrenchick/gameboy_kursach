package com.example.gameboy.game

import android.graphics.Canvas
import android.graphics.Paint

// Успадковує ScalableGameObject — отримує координати x, y та абстрактний draw()
class Ball(
    x: Float, y: Float,
    var dx: Float, var dy: Float,
    var isGlued: Boolean = false
) : ScalableGameObject(x, y) {

    override fun draw(canvas: Canvas, paint: Paint, scale: Float) {
        val ballSize = 3f
        canvas.drawRect(
            (x - ballSize / 2) * scale, (y - ballSize / 2) * scale,
            (x + ballSize / 2) * scale, (y + ballSize / 2) * scale,
            paint
        )
    }

    // Зручний draw з кастомним розміром для ArkanoidGame
    fun draw(canvas: Canvas, paint: Paint, scale: Float, ballSize: Float) {
        canvas.drawRect(
            (x - ballSize / 2) * scale, (y - ballSize / 2) * scale,
            (x + ballSize / 2) * scale, (y + ballSize / 2) * scale,
            paint
        )
    }
}
