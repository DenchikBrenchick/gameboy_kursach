package com.example.gameboy.game

import android.graphics.Canvas
import android.graphics.Paint

// Базовий клас для всіх об'єктів на екрані (Успадкування)
abstract class GameObject(var x: Int, var y: Int) {

    // Кожен об'єкт, який успадкує цей клас, зобов'язаний пояснити, як саме його малювати
    abstract fun draw(canvas: Canvas, paint: Paint, cellSize: Int)
}