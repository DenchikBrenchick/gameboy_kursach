package com.example.gameboy.game

import android.graphics.Canvas
import android.graphics.Paint

// Лазер (гравець або ворог) — успадковує ScalableGameObject
class Laser(
    x: Float, y: Float,
    val dx: Float, val dy: Float,
    val isEnemy: Boolean = false
) : ScalableGameObject(x, y) {

    override fun draw(canvas: Canvas, paint: Paint, scale: Float) {
        if (isEnemy) {
            // Ворожі лазери — маленькі кульки
            canvas.drawCircle(x * scale, y * scale, 1.5f * scale, paint)
        } else {
            // Лазери гравця — смужки
            canvas.drawRect(
                (x - 0.5f) * scale, (y - 2f) * scale,
                (x + 0.5f) * scale, (y + 2f) * scale,
                paint
            )
        }
    }
}
