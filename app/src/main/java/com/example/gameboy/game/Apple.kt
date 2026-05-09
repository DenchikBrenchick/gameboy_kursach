package com.example.gameboy.game

import android.graphics.Canvas
import android.graphics.Paint

// Яблуко успадковує GameObject і одразу отримує координати x та y
class Apple(x: Int, y: Int) : GameObject(x, y) {

    override fun draw(canvas: Canvas, paint: Paint, cellSize: Int) {
        // Яблуко малюватиметься як кружечок
        // Вираховуємо реальні пікселі на екрані на основі координат на сітці
        val cx = (x * cellSize) + (cellSize / 2f)
        val cy = (y * cellSize) + (cellSize / 2f)
        val radius = (cellSize / 2f) - 2f // Трохи менше за клітинку, щоб були відступи

        canvas.drawCircle(cx, cy, radius, paint)
    }
}