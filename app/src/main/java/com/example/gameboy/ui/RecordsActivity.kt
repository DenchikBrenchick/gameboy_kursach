package com.example.gameboy.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.example.gameboy.R
import com.example.gameboy.data.AppDatabase
import com.example.gameboy.game.GameType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecordsActivity : BaseActivity() {

    private var tvGameTitle: TextView? = null
    private var tvRecordsContent: TextView? = null
    private val gamesList = GameType.all
    private var currentGameIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_records)

        tvGameTitle = findViewById(R.id.tvGameTitle)
        tvRecordsContent = findViewById(R.id.tvRecordsContent)

        setupControls()
        updateUI()
    }

    private fun setupControls() {
        val dPadBase = findViewById<View>(R.id.dPadBase)

        findViewById<View>(R.id.btnLeft)?.addDpadRockingEffect(dPadBase, 0f, -15f) {
            soundManager.playClick()
            currentGameIndex = (currentGameIndex - 1 + gamesList.size) % gamesList.size
            updateUI()
        }

        findViewById<View>(R.id.btnRight)?.addDpadRockingEffect(dPadBase, 0f, 15f) {
            soundManager.playClick()
            currentGameIndex = (currentGameIndex + 1) % gamesList.size
            updateUI()
        }

        val goBack = { soundManager.playClick(); finish(); overridePendingTransition(0, 0) }
        findViewById<View>(R.id.btnActionB)?.addBounceEffect(goBack)
        findViewById<View>(R.id.btnSelect)?.addBounceEffect(goBack)
    }

    private fun updateUI() {
        val currentGame = gamesList[currentGameIndex]
        tvGameTitle?.text = "< ${currentGame.id} >"
        loadRecords(currentGame)
    }

    private fun loadRecords(gameType: GameType) {
        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch(Dispatchers.IO) {
            val records = db.scoreDao().getGlobalTopScores(gameType.id)
            withContext(Dispatchers.Main) {
                tvRecordsContent?.text = if (records.isEmpty()) "\n\n  No records yet." else {
                    records.mapIndexed { i, s -> "${i + 1}. ${s.playerName.padEnd(8).take(8)} ${s.score}" }.joinToString("\n")
                }
            }
        }
    }
}
