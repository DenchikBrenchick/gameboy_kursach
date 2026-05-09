package com.example.gameboy.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.example.gameboy.Constants
import com.example.gameboy.R

class ProfileActivity : BaseActivity() {

    private var etPlayerName: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        etPlayerName = findViewById(R.id.etPlayerName)
        val prefs = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        etPlayerName?.setText(prefs.getString(Constants.PREF_PLAYER_NAME, Constants.DEFAULT_PLAYER_NAME))

        setupButtons()
    }

    private fun setupButtons() {
        findViewById<View>(R.id.btnActionA)?.addBounceEffect {
            soundManager.playClick()
            val name = etPlayerName?.text.toString().trim()
            if (name.isNotEmpty()) {
                getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE).edit().putString(Constants.PREF_PLAYER_NAME, name).apply()
                Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        val goBack = { soundManager.playClick(); finish(); overridePendingTransition(0, 0) }
        findViewById<View>(R.id.btnActionB)?.addBounceEffect(goBack)
        findViewById<View>(R.id.btnSelect)?.addBounceEffect(goBack)
    }
}
