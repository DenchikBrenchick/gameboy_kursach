package com.example.gameboy.game

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF

// Блок Arkanoid — успадковує ScalableGameObject
// Використовує RectF замість точкових координат, тому x/y = центр блоку
class Brick(val rect: RectF) : ScalableGameObject(rect.centerX(), rect.centerY()) {

    override fun draw(canvas: Canvas, paint: Paint, scale: Float) {
        canvas.drawRect(
            rect.left * scale, rect.top * scale,
            rect.right * scale, rect.bottom * scale,
            paint
        )
    }
}
