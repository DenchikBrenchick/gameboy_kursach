package com.example.gameboy.ui

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.widget.ImageView
import android.widget.TextView
import android.view.View
import com.example.gameboy.Constants
import com.example.gameboy.R

object StyleManager : IStyleProvider {

    const val STYLE_WHITE   = "WHITE"
    const val STYLE_BLACK   = "BLACK"
    const val STYLE_CLASSIC = "CLASSIC"

    override val styles = listOf(STYLE_WHITE, STYLE_BLACK, STYLE_CLASSIC)

    private val COLOR_WHITE   = Color.parseColor("#EBECEB")
    private val COLOR_BLACK   = Color.parseColor("#1E1E1E")
    private val COLOR_CLASSIC = Color.parseColor("#1004A6")

    override fun getStyle(context: Context): String =
        context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
            .getString(Constants.PREF_STYLE, STYLE_WHITE) ?: STYLE_WHITE

    override fun setStyle(context: Context, style: String) {
        context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(Constants.PREF_STYLE, style).apply()
    }

    override fun getStyleDisplayName(context: Context): String =
        when (getStyle(context)) {
            STYLE_BLACK -> "BLACK"
            STYLE_CLASSIC -> "CLASSIC"
            else -> "WHITE"
        }

    override fun applyStyle(activity: Activity) {
        val style = getStyle(activity)

        val bodyColor = when (style) {
            STYLE_BLACK   -> COLOR_BLACK
            STYLE_CLASSIC -> COLOR_CLASSIC
            else          -> COLOR_WHITE
        }
        val isYellow = style == STYLE_BLACK || style == STYLE_CLASSIC

        // Колір тексту UI (поза екраном)
        val uiTextColor = when (style) {
            STYLE_BLACK   -> Color.WHITE
            STYLE_CLASSIC -> Color.BLACK
            else          -> Color.parseColor("#1E1E1E")
        }

        // ── Корпус ──
        (activity.findViewById<View>(R.id.main)
            ?: activity.window.decorView.rootView)
            .setBackgroundColor(bodyColor)
        activity.findViewById<View>(R.id.gameboyBody)
            ?.setBackgroundColor(bodyColor)

        // ── Рамка екрану ──
        activity.findViewById<View>(R.id.screenBezel)
            ?.setBackgroundResource(R.drawable.bg_screen_shape)

        // ── Текст у шапці екрану ──
        activity.findViewById<TextView>(R.id.tvDotMatrix)?.setTextColor(uiTextColor)
        activity.findViewById<TextView>(R.id.tvBattery)?.setTextColor(uiTextColor)

        // ── Nintendo GAME BOY™ логотип ──
        activity.findViewById<TextView>(R.id.logoText)?.setTextColor(uiTextColor)

        // ── Підписи під кнопками (всі TextView в bottomControls) ──
        // Знаходимо btnStartContainer і міняємо всі TextView всередині
        val btnContainer = activity.findViewById<View>(R.id.btnStartContainer)
        if (btnContainer is android.view.ViewGroup) {
            setAllTextViewColors(btnContainer, uiTextColor)
        }

        // ── D-pad ──
        activity.findViewById<View>(R.id.dPadBase)?.setBackgroundResource(
            if (isYellow) R.drawable.ic_dpad_yellow else R.drawable.ic_dpad
        )

        // ── Кнопки A і B ──
        val roundRes = if (isYellow) R.drawable.ic_btn_round_yellow else R.drawable.ic_btn_round
        val btnLabelColor = if (style == STYLE_CLASSIC) Color.BLACK else Color.WHITE
        listOf(R.id.btnActionA, R.id.btnActionB).forEach { btnId ->
            val btn = activity.findViewById<View>(btnId)
            btn?.setBackgroundResource(roundRes)
            if (btn is android.view.ViewGroup) setAllTextViewColors(btn, btnLabelColor)
        }

        // ── Овальні кнопки ──
        val ovalRes = if (isYellow) R.drawable.ic_btn_oval_yellow else R.drawable.ic_btn_oval
        activity.findViewById<View>(R.id.btnSelect)?.setBackgroundResource(ovalRes)
        activity.findViewById<View>(R.id.btnOptions)?.setBackgroundResource(ovalRes)

        // ── LED індикатор ──
        activity.findViewById<View>(R.id.batteryIndicator)?.setBackgroundResource(
            if (isYellow) R.drawable.ic_led_dot_yellow else R.drawable.ic_led_dot
        )

        // ── Спікер ──
        activity.findViewById<ImageView>(R.id.imgSpeakerHoles)
            ?.setImageResource(R.drawable.ic_speaker_holes)
    }

    private fun setAllTextViewColors(group: android.view.ViewGroup, color: Int) {
        for (i in 0 until group.childCount) {
            when (val child = group.getChildAt(i)) {
                is TextView -> child.setTextColor(color)
                is android.view.ViewGroup -> setAllTextViewColors(child, color)
            }
        }
    }
}
