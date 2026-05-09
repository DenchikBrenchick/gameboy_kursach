package com.example.gameboy.game

import android.graphics.Canvas
import android.graphics.Paint

// Ворог у SpaceShooter — успадковує ScalableGameObject
class Enemy(
    x: Float, y: Float,
    val type: Int,
    val pattern: Int,
    val startX: Float = x,
    var shootCooldown: Int = 60
) : ScalableGameObject(x, y) {

    override fun draw(canvas: Canvas, paint: Paint, scale: Float) {
        val ex = x * scale
        val ey = y * scale
        val es = 4f * scale

        when (type) {
            1 -> { // Звичайний
                canvas.drawRect(ex - es, ey - es, ex - es + scale, ey + es, paint)
                canvas.drawRect(ex + es - scale, ey - es, ex + es, ey + es, paint)
                canvas.drawRect(ex - es / 2, ey - scale, ex + es / 2, ey + scale, paint)
            }
            2 -> { // Жирний
                canvas.drawRect(ex - es, ey - es / 2, ex + es, ey + es / 2, paint)
                canvas.drawRect(ex - es / 2, ey - es, ex + es / 2, ey + es, paint)
            }
            3 -> { // Стрілок (У-подібний)
                canvas.drawRect(ex - es, ey - es, ex - es / 2, ey + es, paint)
                canvas.drawRect(ex + es / 2, ey - es, ex + es, ey + es, paint)
                canvas.drawRect(ex - es / 2, ey - es, ex + es / 2, ey, paint)
            }
        }
    }
}
