package com.example.gameboy.ui

import android.view.MotionEvent
import android.view.View

fun View.addBounceEffect(action: (() -> Unit)? = null) {
    this.setOnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                v.animate().scaleX(0.85f).scaleY(0.85f).setDuration(80).start()
            }
            MotionEvent.ACTION_UP -> {
                v.animate().scaleX(1f).scaleY(1f).setDuration(80).start()
                action?.invoke() // ТУТ ЗАПУСКАЄТЬСЯ ТВОЯ ДІЯ
            }
            MotionEvent.ACTION_CANCEL -> {
                v.animate().scaleX(1f).scaleY(1f).setDuration(80).start()
            }
        }
        true
    }
}

fun View.addDpadRockingEffect(dPadBase: View?, tiltX: Float, tiltY: Float, action: (() -> Unit)? = null) {
    this.setOnTouchListener { _, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                dPadBase?.animate()?.rotationX(tiltX)?.rotationY(tiltY)?.scaleX(0.97f)?.scaleY(0.97f)?.setDuration(80)?.start()
            }
            MotionEvent.ACTION_UP -> {
                dPadBase?.animate()?.rotationX(0f)?.rotationY(0f)?.scaleX(1f)?.scaleY(1f)?.setDuration(80)?.start()
                action?.invoke() // ТУТ ЗАПУСКАЄТЬСЯ ТВОЯ ДІЯ (РУХ МЕНЮ)
            }
            MotionEvent.ACTION_CANCEL -> {
                dPadBase?.animate()?.rotationX(0f)?.rotationY(0f)?.scaleX(1f)?.scaleY(1f)?.setDuration(80)?.start()
            }
        }
        true
    }
}