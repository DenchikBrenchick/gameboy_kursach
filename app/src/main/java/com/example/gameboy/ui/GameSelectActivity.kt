package com.example.gameboy.ui

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.example.gameboy.Constants
import com.example.gameboy.R
import com.example.gameboy.game.GameType

class GameSelectActivity : BaseActivity() {

    private var selectedIndex = 0
    private var menuItems: List<TextView>? = null
    private val gamesList = GameType.all

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_select)

        val tv1 = findViewById<TextView>(R.id.tvGameSnake)
        val tv2 = findViewById<TextView>(R.id.tvGameArkanoid)
        val tv3 = findViewById<TextView>(R.id.tvGameShooter)

        if (tv1 != null && tv2 != null && tv3 != null) {
            menuItems = listOf(tv1, tv2, tv3)
        }

        updateMenuUI()
        setupControls()
    }

    private fun setupControls() {
        val dPadBase = findViewById<View>(R.id.dPadBase)

        findViewById<View>(R.id.btnDown)?.addDpadRockingEffect(dPadBase, -15f, 0f) {
            soundManager.playClick()
            selectedIndex = (selectedIndex + 1) % gamesList.size
            updateMenuUI()
        }

        findViewById<View>(R.id.btnUp)?.addDpadRockingEffect(dPadBase, 15f, 0f) {
            soundManager.playClick()
            selectedIndex = (selectedIndex - 1 + gamesList.size) % gamesList.size
            updateMenuUI()
        }

        findViewById<View>(R.id.btnActionA)?.addBounceEffect {
            soundManager.playClick()
            val intent = Intent(this@GameSelectActivity, GameActivity::class.java)
            intent.putExtra(Constants.EXTRA_GAME_TYPE, gamesList[selectedIndex].id)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        val goBack = { soundManager.playClick(); finish(); overridePendingTransition(0, 0) }
        findViewById<View>(R.id.btnActionB)?.addBounceEffect(goBack)
        findViewById<View>(R.id.btnSelect)?.addBounceEffect(goBack)

        findViewById<View>(R.id.btnOptions)?.addBounceEffect {
            soundManager.playClick()
            startActivity(Intent(this@GameSelectActivity, OptionsActivity::class.java))
            overridePendingTransition(0, 0)
        }

        findViewById<View>(R.id.btnLeft)?.addDpadRockingEffect(dPadBase, 0f, -15f) { soundManager.playClick() }
        findViewById<View>(R.id.btnRight)?.addDpadRockingEffect(dPadBase, 0f, 15f) { soundManager.playClick() }
    }

    private fun updateMenuUI() {
        menuItems?.forEachIndexed { i, textView ->
            if (i == selectedIndex) {
                textView.text = "> ${gamesList[i].id}"
                textView.setTypeface(null, Typeface.BOLD)
            } else {
                textView.text = "  ${gamesList[i].id}"
                textView.setTypeface(null, Typeface.NORMAL)
            }
        }
    }
}
