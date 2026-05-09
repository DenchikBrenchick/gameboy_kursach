package com.example.gameboy.ui

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import com.example.gameboy.Constants
import com.example.gameboy.R
import com.example.gameboy.data.AppDatabase
import com.example.gameboy.data.ScoreEntity
import com.example.gameboy.game.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

class GameActivity : BaseActivity() {

    private lateinit var gameView: GameView
    private lateinit var currentGame: IGame   // Поліморфізм: тип IGame, не конкретна гра
    private var isInclusiveMode = false
    private var selectedOverlayOption = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        val db = AppDatabase.getDatabase(this)
        val scoreDao = db.scoreDao()
        val sharedPref = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        val currentPlayer = sharedPref.getString(Constants.PREF_PLAYER_NAME, Constants.DEFAULT_PLAYER_NAME)
            ?: Constants.DEFAULT_PLAYER_NAME
        val gameType = GameType.fromId(intent.getStringExtra(Constants.EXTRA_GAME_TYPE))

        // Фабричний метод: створюємо потрібну гру, далі — лише через IGame
        currentGame = GameFactory.create(
            gameType = gameType,
            initialHighScore = 0,
            onGameOver = { score -> handleGameOver(score, gameType, currentPlayer, scoreDao) },
            onEat = { soundManager.playEat() },
            onClick = { soundManager.playClick() },
            onGameStart = { runOnUiThread { playScreenMusic() } }
        )

        // Завантажуємо рекорд — більше не потрібні is/as касти
        lifecycleScope.launch(Dispatchers.IO) {
            val savedHighScore = scoreDao.getPersonalBest(currentPlayer, gameType.id) ?: 0
            withContext(Dispatchers.Main) {
                currentGame.updateHighScore(savedHighScore) // Поліморфізм!
            }
        }

        gameView = GameView(this, currentGame)
        val screenContainer = findViewById<FrameLayout>(R.id.gameScreenContainer)
        screenContainer.addView(gameView, 0)

        setupButtons()
        setupInclusiveMode()
        setupOptionsMenu()
    }

    private fun handleGameOver(finalScore: Int, gameType: GameType, currentPlayer: String, scoreDao: com.example.gameboy.data.ScoreDao) {
        runOnUiThread {
            soundManager.pauseAllMusic()
            soundManager.playDieSfx()
            lifecycleScope.launch {
                delay(1000)
                if (currentGame.isGameOver) {
                    soundManager.playDeathTheme()
                }
            }
        }
        lifecycleScope.launch(Dispatchers.IO) {
            scoreDao.insertScore(ScoreEntity(playerName = currentPlayer, gameName = gameType.id, score = finalScore))
            val newMax = scoreDao.getPersonalBest(currentPlayer, gameType.id) ?: 0
            withContext(Dispatchers.Main) {
                currentGame.updateHighScore(newMax) // Поліморфізм — без кастів!
            }
        }
    }

    private fun setupButtons() {
        val optionsOverlay = findViewById<View>(R.id.optionsOverlay)
        val dPadBase = findViewById<View>(R.id.dPadBase)

        fun handleDpad(action: InputAction, tiltX: Float, tiltY: Float, viewId: Int) {
            val view = findViewById<View>(viewId)
            var job: Job? = null
            view?.setOnTouchListener { _, event ->
                if (optionsOverlay?.visibility == View.VISIBLE) {
                    if (event.action == MotionEvent.ACTION_UP) { dPadBase?.animate()?.rotationX(0f)?.rotationY(0f)?.scaleX(1f)?.scaleY(1f)?.setDuration(80)?.start(); handleOptionsInput(action) }
                    else if (event.action == MotionEvent.ACTION_DOWN) dPadBase?.animate()?.rotationX(tiltX)?.rotationY(tiltY)?.scaleX(0.97f)?.scaleY(0.97f)?.setDuration(80)?.start()
                    return@setOnTouchListener true
                }
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        dPadBase?.animate()?.rotationX(tiltX)?.rotationY(tiltY)?.scaleX(0.97f)?.scaleY(0.97f)?.setDuration(80)?.start()
                        job = lifecycleScope.launch { while (true) { currentGame.handleInput(action); delay(16) } }
                        true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        dPadBase?.animate()?.rotationX(0f)?.rotationY(0f)?.scaleX(1f)?.scaleY(1f)?.setDuration(80)?.start()
                        job?.cancel(); true
                    }
                    else -> false
                }
            }
        }

        handleDpad(InputAction.LEFT, 0f, -15f, R.id.btnLeft)
        handleDpad(InputAction.RIGHT, 0f, 15f, R.id.btnRight)
        handleDpad(InputAction.UP, 15f, 0f, R.id.btnUp)
        handleDpad(InputAction.DOWN, -15f, 0f, R.id.btnDown)

        findViewById<View>(R.id.btnActionA)?.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> { v.animate().scaleX(0.85f).scaleY(0.85f).setDuration(80).start(); if (optionsOverlay?.visibility != View.VISIBLE) { soundManager.playClick(); currentGame.handleInput(InputAction.ACTION_A) }; true }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { v.animate().scaleX(1f).scaleY(1f).setDuration(80).start(); if (event.action == MotionEvent.ACTION_UP) currentGame.onTouchRelease(); true }
                else -> false
            }
        }

        findViewById<View>(R.id.btnActionB)?.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate().scaleX(0.85f).scaleY(0.85f).setDuration(80).start()
                    if (optionsOverlay?.visibility != View.VISIBLE) { soundManager.playClick(); currentGame.handleInput(InputAction.ACTION_B) }
                    else { soundManager.playClick(); optionsOverlay.visibility = View.GONE }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { v.animate().scaleX(1f).scaleY(1f).setDuration(80).start(); true }
                else -> false
            }
        }

        val goBack = { soundManager.playClick(); finish(); overridePendingTransition(0, 0) }
        findViewById<View>(R.id.btnSelect)?.addBounceEffect(goBack)
    }

    private fun handleOptionsInput(action: InputAction) {
        val tvMusic = findViewById<TextView>(R.id.tvMusicVol)
        val tvSound = findViewById<TextView>(R.id.tvSoundVol)
        fun updateTexts() {
            if (selectedOverlayOption == 0) { tvMusic?.text = "> MUSIC: ${soundManager.getMusicVolume()}%"; tvSound?.text = "  SOUND: ${soundManager.getSfxVolume()}%" }
            else { tvMusic?.text = "  MUSIC: ${soundManager.getMusicVolume()}%"; tvSound?.text = "> SOUND: ${soundManager.getSfxVolume()}%" }
        }
        when (action) {
            InputAction.UP -> { soundManager.playClick(); selectedOverlayOption = 0; updateTexts() }
            InputAction.DOWN -> { soundManager.playClick(); selectedOverlayOption = 1; updateTexts() }
            InputAction.LEFT -> { if (selectedOverlayOption == 0) soundManager.setMusicVolume((soundManager.getMusicVolume() - 10).coerceAtLeast(0)) else { soundManager.setSfxVolume((soundManager.getSfxVolume() - 10).coerceAtLeast(0)); soundManager.playEat() }; soundManager.playClick(); updateTexts() }
            InputAction.RIGHT -> { if (selectedOverlayOption == 0) soundManager.setMusicVolume((soundManager.getMusicVolume() + 10).coerceAtMost(100)) else { soundManager.setSfxVolume((soundManager.getSfxVolume() + 10).coerceAtMost(100)); soundManager.playEat() }; soundManager.playClick(); updateTexts() }
            else -> {}
        }
    }

    private fun setupOptionsMenu() {
        val optionsOverlay = findViewById<View>(R.id.optionsOverlay)
        val tvMusic = findViewById<TextView>(R.id.tvMusicVol)
        val tvSound = findViewById<TextView>(R.id.tvSoundVol)

        findViewById<View>(R.id.btnOptions)?.addBounceEffect {
            if (currentGame.isGameOver) {
                soundManager.playClick(); selectedOverlayOption = 0
                tvMusic?.text = "> MUSIC: ${soundManager.getMusicVolume()}%"
                tvSound?.text = "  SOUND: ${soundManager.getSfxVolume()}%"
                optionsOverlay?.visibility = View.VISIBLE
            } else Toast.makeText(this, "Wait until the game is over!", Toast.LENGTH_SHORT).show()
        }
        findViewById<View>(R.id.btnCloseOptions)?.addBounceEffect { soundManager.playClick(); optionsOverlay?.visibility = View.GONE }
        findViewById<Button>(R.id.btnMusicPlus)?.addBounceEffect { soundManager.setMusicVolume((soundManager.getMusicVolume() + 10).coerceAtMost(100)); tvMusic?.text = if (selectedOverlayOption == 0) "> MUSIC: ${soundManager.getMusicVolume()}%" else "  MUSIC: ${soundManager.getMusicVolume()}%" }
        findViewById<Button>(R.id.btnMusicMinus)?.addBounceEffect { soundManager.setMusicVolume((soundManager.getMusicVolume() - 10).coerceAtLeast(0)); tvMusic?.text = if (selectedOverlayOption == 0) "> MUSIC: ${soundManager.getMusicVolume()}%" else "  MUSIC: ${soundManager.getMusicVolume()}%" }
    }

    private fun setupInclusiveMode() {
        val root = findViewById<FrameLayout>(R.id.main)
        val gameboyBody = findViewById<View>(R.id.gameboyBody)
        val screenBezel = findViewById<View>(R.id.screenBezel)
        val gameContainer = findViewById<FrameLayout>(R.id.gameScreenContainer)
        val tvDotMatrix = findViewById<View>(R.id.tvDotMatrix)
        val tvBattery = findViewById<View>(R.id.tvBattery)
        val batteryIndicator = findViewById<View>(R.id.batteryIndicator)
        val logoText = findViewById<TextView>(R.id.logoText)
        val bottomControls = findViewById<View>(R.id.bottomControls)
        val btnExitInclusive = findViewById<View>(R.id.btnExitInclusive)
        val btnToggleFullscreen = findViewById<Button>(R.id.btnToggleFullscreen)

        fun toggleMode() {
            isInclusiveMode = !isInclusiveMode
            btnToggleFullscreen?.text = if (isInclusiveMode) "FULLSCREEN: ON" else "FULLSCREEN: OFF"
            currentGame.isFullscreen = isInclusiveMode // Поліморфізм — без is/as!

            if (isInclusiveMode) {
                root?.setBackgroundColor(Color.parseColor("#9BBC0F"))
                gameboyBody?.setPadding(0, 0, 0, 0)
                screenBezel?.setBackgroundColor(Color.TRANSPARENT)
                tvDotMatrix?.visibility = View.GONE; tvBattery?.visibility = View.GONE
                batteryIndicator?.visibility = View.GONE; logoText?.visibility = View.GONE
                bottomControls?.visibility = View.GONE; btnExitInclusive?.visibility = View.VISIBLE

                val bezelParams = screenBezel?.layoutParams as? android.widget.LinearLayout.LayoutParams
                if (bezelParams != null) { bezelParams.height = android.widget.LinearLayout.LayoutParams.MATCH_PARENT; bezelParams.weight = 1f; screenBezel.layoutParams = bezelParams }
                val params = gameContainer?.layoutParams as? ConstraintLayout.LayoutParams
                if (params != null) { params.dimensionRatio = null; params.setMargins(0, 0, 0, 0); params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID; gameContainer.layoutParams = params }
            } else {
                styleProvider.applyStyle(this)
                val dp16 = (16 * resources.displayMetrics.density).toInt()
                gameboyBody?.setPadding(dp16, dp16, dp16, dp16)
                tvDotMatrix?.visibility = View.VISIBLE; tvBattery?.visibility = View.VISIBLE
                batteryIndicator?.visibility = View.VISIBLE; logoText?.visibility = View.VISIBLE
                bottomControls?.visibility = View.VISIBLE; btnExitInclusive?.visibility = View.GONE

                val bezelParams = screenBezel?.layoutParams as? android.widget.LinearLayout.LayoutParams
                if (bezelParams != null) { bezelParams.height = android.widget.LinearLayout.LayoutParams.WRAP_CONTENT; bezelParams.weight = 0f; screenBezel.layoutParams = bezelParams }
                val params = gameContainer?.layoutParams as? ConstraintLayout.LayoutParams
                if (params != null) { params.dimensionRatio = "20:25"; val dp8 = (8 * resources.displayMetrics.density).toInt(); params.setMargins(dp8, dp8, dp8, 0); params.topToTop = ConstraintLayout.LayoutParams.UNSET; params.topToBottom = R.id.batteryIndicator; gameContainer.layoutParams = params }
            }
        }

        val modeToggleListener = View.OnClickListener {
            if (!currentGame.isGameOver) { Toast.makeText(this@GameActivity, "Зміна екрану доступна лише між іграми!", Toast.LENGTH_SHORT).show(); return@OnClickListener }
            toggleMode(); gameView.resume()
        }
        btnExitInclusive?.setOnClickListener(modeToggleListener)
        btnToggleFullscreen?.setOnClickListener(modeToggleListener)

        var startX = 0f; var startY = 0f

        gameView.setOnTouchListener { view, event ->
            if (!isInclusiveMode) return@setOnTouchListener false

            // Поліморфізм: onTouchMove/onTouchRelease — гра сама вирішує що робити
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    val percentX = event.x / view.width
                    currentGame.onTouchMove(percentX)
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        startX = event.x; startY = event.y
                        currentGame.handleInput(InputAction.ACTION_A)
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    currentGame.onTouchRelease()
                    val dx = event.x - startX; val dy = event.y - startY
                    if (abs(dx) > 50 || abs(dy) > 50) {
                        if (abs(dx) > abs(dy)) { if (dx > 0) currentGame.handleInput(InputAction.RIGHT) else currentGame.handleInput(InputAction.LEFT) }
                        else { if (dy > 0) currentGame.handleInput(InputAction.DOWN) else currentGame.handleInput(InputAction.UP) }
                    } else currentGame.handleInput(InputAction.ACTION_A)
                }
            }
            true
        }
    }

    override fun onResume() { super.onResume(); gameView.resume() }
    override fun onPause() { super.onPause(); gameView.pause() }
    override fun playScreenMusic() {
        if (!currentGame.isGameOver) soundManager.playTheme()
    }
}
