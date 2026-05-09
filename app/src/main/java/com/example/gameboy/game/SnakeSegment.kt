package com.example.gameboy.game

import android.graphics.Canvas
import android.graphics.Paint

// Шматок тіла змійки також успадковує базовий клас і має координати x, y
class SnakeSegment(x: Int, y: Int) : GameObject(x, y) {

    override fun draw(canvas: Canvas, paint: Paint, cellSize: Int) {
        // Вираховуємо пікселі для малювання квадратика на екрані
        val left = x * cellSize.toFloat() + 1f // +1 та -2 потрібні, щоб між
        val top = y * cellSize.toFloat() + 1f  // сегментами змійки був маленький
        val right = left + cellSize.toFloat() - 2f // невидимий зазор (як в ретро іграх)
        val bottom = top + cellSize.toFloat() - 2f

        // Малюємо зафарбований прямокутник (квадрат)
        canvas.drawRect(left, top, right, bottom, paint)
    }
}