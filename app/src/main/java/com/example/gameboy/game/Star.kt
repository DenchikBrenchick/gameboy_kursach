package com.example.gameboy.game

import android.graphics.Canvas
import android.graphics.Paint

// Зірка у фоні SpaceShooter — успадковує ScalableGameObject
class Star(
    x: Float, y: Float,
    val speed: Float,
    val size: Float
) : ScalableGameObject(x, y) {

    override fun draw(canvas: Canvas, paint: Paint, scale: Float) {
        canvas.drawRect(
            (x - size) * scale, (y - size) * scale,
            (x + size) * scale, (y + size) * scale,
            paint
        )
    }
}
