package com.example.gameboy.game

import android.graphics.Canvas
import android.graphics.Paint

// Базовий клас для ігрових об'єктів у логічній системі координат (Arkanoid, SpaceShooter)
// Успадкування: Ball, Brick, Laser, Enemy, Star, PowerUp розширюють цей клас
abstract class ScalableGameObject(var x: Float, var y: Float) {

    // Кожен нащадок зобов'язаний описати власну логіку малювання
    abstract fun draw(canvas: Canvas, paint: Paint, scale: Float)
}
