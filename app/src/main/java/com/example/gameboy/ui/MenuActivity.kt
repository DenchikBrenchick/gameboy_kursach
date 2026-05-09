package com.example.gameboy.ui

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.example.gameboy.R

class MenuActivity : BaseActivity() {

    private var selectedIndex = 0
    private var menuItems: List<TextView>? = null
    private val menuTexts = listOf("PLAY", "PROFILE", "RECORDS")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val tvPlay = findViewById<TextView>(R.id.tvMenuPlay)
        val tvProfile = findViewById<TextView>(R.id.tvMenuProfile)
        val tvRecords = findViewById<TextView>(R.id.tvMenuRecords)

        if (tvPlay != null && tvProfile != null && tvRecords != null) {
            menuItems = listOf(tvPlay, tvProfile, tvRecords)
        }

        updateMenuUI()
        setupControls()
    }

    private fun setupControls() {
        val dPadBase = findViewById<View>(R.id.dPadBase)

        findViewById<View>(R.id.btnDown)?.addDpadRockingEffect(dPadBase, -15f, 0f) {
            soundManager.playClick()
            selectedIndex = (selectedIndex + 1) % menuTexts.size
            updateMenuUI()
        }

        findViewById<View>(R.id.btnUp)?.addDpadRockingEffect(dPadBase, 15f, 0f) {
            soundManager.playClick()
            selectedIndex = (selectedIndex - 1 + menuTexts.size) % menuTexts.size
            updateMenuUI()
        }

        findViewById<View>(R.id.btnActionA)?.addBounceEffect {
            soundManager.playClick()
            handleSelection()
        }

        findViewById<View>(R.id.btnOptions)?.addBounceEffect {
            soundManager.playClick()
            startActivity(Intent(this@MenuActivity, OptionsActivity::class.java))
            overridePendingTransition(0, 0)
        }

        findViewById<View>(R.id.btnActionB)?.addBounceEffect { soundManager.playClick() }
        findViewById<View>(R.id.btnSelect)?.addBounceEffect { soundManager.playClick() }
    }

    private fun updateMenuUI() {
        menuItems?.forEachIndexed { i, textView ->
            if (i == selectedIndex) {
                textView.text = "> ${menuTexts[i]}"
                textView.setTypeface(null, Typeface.BOLD)
            } else {
                textView.text = "  ${menuTexts[i]}"
                textView.setTypeface(null, Typeface.NORMAL)
            }
        }
    }

    private fun handleSelection() {
        val targetClass = when (selectedIndex) {
            0 -> GameSelectActivity::class.java
            1 -> ProfileActivity::class.java
            else -> RecordsActivity::class.java
        }
        val intent = Intent(this@MenuActivity, targetClass)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        startActivity(intent)
        overridePendingTransition(0, 0)
    }
}