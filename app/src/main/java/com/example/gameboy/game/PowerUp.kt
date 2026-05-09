package com.example.gameboy.game

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

class PowerUp(
    x: Float,
    y: Float,
    val type: PowerUpType
) : ScalableGameObject(x, y) {

    override fun draw(canvas: Canvas, paint: Paint, scale: Float) {
        canvas.drawRect(
            (x - 3.5f) * scale, (y - 3.5f) * scale,
            (x + 3.5f) * scale, (y + 3.5f) * scale,
            paint
        )

        val oldColor = paint.color
        paint.color = Color.parseColor("#9BBC0F")
        paint.textSize = scale * 6f
        paint.textAlign = Paint.Align.CENTER

        val text = when (type) {
            PowerUpType.EXTRA_BALL -> "B"
            PowerUpType.EXPAND_PADDLE -> "W"
            PowerUpType.WEAPON_UPGRADE -> "P"
            PowerUpType.HEALTH -> "H"
        }

        canvas.drawText(text, x * scale, (y + 2f) * scale, paint)
        paint.color = oldColor
    }
}

enum class PowerUpType {
    EXTRA_BALL,
    EXPAND_PADDLE,
    WEAPON_UPGRADE,
    HEALTH
}
