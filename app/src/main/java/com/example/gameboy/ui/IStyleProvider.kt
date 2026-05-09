package com.example.gameboy.ui

import android.app.Activity
import android.content.Context

interface IStyleProvider {
    val styles: List<String>

    fun getStyle(context: Context): String
    fun setStyle(context: Context, style: String)
    fun getStyleDisplayName(context: Context): String
    fun applyStyle(activity: Activity)
}
