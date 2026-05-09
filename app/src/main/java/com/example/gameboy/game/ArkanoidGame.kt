package com.example.gameboy.game

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.random.Random

// Успадковує BaseGame — score, highScore, isGameOver, isFullscreen вже є
class ArkanoidGame(
    initialHighScore: Int = 0,
    onGameOver: (Int) -> Unit,
    private val onBrickHit: () -> Unit,
    onGameStart: () -> Unit
) : BaseGame(initialHighScore, onGameOver, onGameStart) {

    private val logicalWidth = 100f
    private var logicalHeight = 125f
    private var scale = 1f

    private var paddleX = 50f
    private var paddleY = 115f
    private val basePaddleWidth = 24f
    private val expandedPaddleWidth = 38f
    private var paddleWidth = basePaddleWidth
    private val paddleHeight = 4f
    private val paddleSpeed = 1f
    private var paddleExpandFrames = 0

    private val balls   = CopyOnWriteArrayList<Ball>()
    private val powerUps = CopyOnWriteArrayList<PowerUp>()
    private val bricks  = CopyOnWriteArrayList<Brick>()
    private val ballSize = 3f

    private var rowSpawnFrames = 0
    private val framesToSpawnRow = 1200
    private val brickW = 10f; private val brickH = 5f; private val padding = 2f

    private val paint = Paint().apply { color = Color.parseColor("#0F380F"); isAntiAlias = true }

    override fun start() {
        resetBaseState()
        paddleX = 50f; paddleWidth = basePaddleWidth
        paddleExpandFrames = 0; rowSpawnFrames = 0
        powerUps.clear(); bricks.clear()
        resetBall(); generateInitialBricks(); onGameStart()
    }

    private fun generateInitialBricks() {
        bricks.clear()
        for (r in 0 until 5) addBrickRow(20f + r * (brickH + padding))
    }

    private fun addBrickRow(yPosition: Float) {
        val cols = 8
        val startX = (logicalWidth - (cols * (brickW + padding))) / 2f + (brickW / 2f)
        for (c in 0 until cols) {
            val bx = startX + c * (brickW + padding)
            bricks.add(Brick(RectF(bx - brickW / 2, yPosition - brickH / 2, bx + brickW / 2, yPosition + brickH / 2)))
        }
    }

    private fun resetBall() {
        balls.clear()
        balls.add(Ball(paddleX, paddleY - paddleHeight / 2 - ballSize / 2, 1.2f, -1.2f, true))
    }

    // Реалізація onTouchMove з IGame — повноекранне керування (Поліморфізм)
    override fun onTouchMove(percentX: Float) {
        paddleX = percentX * logicalWidth
        if (paddleX < paddleWidth / 2) paddleX = paddleWidth / 2
        if (paddleX > logicalWidth - paddleWidth / 2) paddleX = logicalWidth - paddleWidth / 2
        balls.forEach { if (it.isGlued) it.x = paddleX }
    }

    override fun handleInput(action: InputAction) {
        if (isGameOver && (action == InputAction.START || action == InputAction.ACTION_A)) {
            start(); return
        }
        if (!isGameOver) {
            when (action) {
                InputAction.LEFT -> {
                    paddleX -= paddleSpeed
                    if (paddleX < paddleWidth / 2) paddleX = paddleWidth / 2
                    balls.forEach { if (it.isGlued) it.x = paddleX }
                }
                InputAction.RIGHT -> {
                    paddleX += paddleSpeed
                    if (paddleX > logicalWidth - paddleWidth / 2) paddleX = logicalWidth - paddleWidth / 2
                    balls.forEach { if (it.isGlued) it.x = paddleX }
                }
                InputAction.ACTION_A, InputAction.ACTION_B -> balls.forEach { it.isGlued = false }
                else -> {}
            }
        }
    }

    override fun update() {
        if (isGameOver) return
        val isPlaying = balls.any { !it.isGlued }
        if (!isPlaying) return

        if (paddleExpandFrames > 0) { paddleExpandFrames--; paddleWidth = expandedPaddleWidth } else paddleWidth = basePaddleWidth

        rowSpawnFrames++
        if (rowSpawnFrames >= framesToSpawnRow) {
            rowSpawnFrames = 0
            val shiftY = brickH + padding
            for (brick in bricks) brick.rect.offset(0f, shiftY)
            addBrickRow(20f)
            if (bricks.any { it.rect.bottom >= paddleY - paddleHeight / 2 }) { triggerGameOver(); return }
        }

        for (p in powerUps) {
            p.y += 0.6f
            if (p.y + 3f >= paddleY - paddleHeight / 2 && p.y - 3f <= paddleY + paddleHeight / 2) {
                if (p.x >= paddleX - paddleWidth / 2 && p.x <= paddleX + paddleWidth / 2) {
                    applyPowerUp(p.type); onBrickHit(); powerUps.remove(p)
                }
            } else if (p.y > logicalHeight) powerUps.remove(p)
        }

        for (ball in balls) {
            if (ball.isGlued) continue
            ball.x += ball.dx; ball.y += ball.dy

            if (ball.x <= ballSize / 2) { ball.x = ballSize / 2f; ball.dx *= -1; onBrickHit() }
            if (ball.x >= logicalWidth - ballSize / 2) { ball.x = logicalWidth - ballSize / 2f; ball.dx *= -1; onBrickHit() }
            if (ball.y <= ballSize / 2) { ball.y = ballSize / 2f; ball.dy *= -1; onBrickHit() }
            if (ball.y >= logicalHeight) { balls.remove(ball); continue }

            if (ball.dy > 0 && ball.y + ballSize / 2 >= paddleY - paddleHeight / 2 && ball.y <= paddleY + paddleHeight / 2) {
                if (ball.x in (paddleX - paddleWidth / 2 - ballSize / 2)..(paddleX + paddleWidth / 2 + ballSize / 2)) {
                    ball.dy *= -1
                    ball.dx = (ball.x - paddleX) * 0.15f
                    ball.y = paddleY - paddleHeight / 2 - ballSize / 2
                    onBrickHit()
                }
            }

            val ballRect = RectF(ball.x - ballSize, ball.y - ballSize, ball.x + ballSize, ball.y + ballSize)
            for (brick in bricks) {
                if (RectF.intersects(ballRect, brick.rect)) {
                    bricks.remove(brick); ball.dy *= -1; score += 15; onBrickHit()
                    if (Random.nextFloat() < 0.05f) {
                        val type = if (Random.nextBoolean()) PowerUpType.EXTRA_BALL else PowerUpType.EXPAND_PADDLE
                        powerUps.add(PowerUp(brick.rect.centerX(), brick.rect.centerY(), type))
                    }
                    if (bricks.isEmpty()) generateInitialBricks()
                    break
                }
            }
        }

        if (balls.isEmpty()) triggerGameOver()
    }

    private fun applyPowerUp(type: PowerUpType) {
        when (type) {
            PowerUpType.EXTRA_BALL -> balls.add(Ball(paddleX, paddleY - paddleHeight, (Random.nextFloat() * 2 - 1) * 1.5f, -1.2f, false))
            PowerUpType.EXPAND_PADDLE -> paddleExpandFrames = 1200
            else -> {}
        }
    }

    override fun draw(canvas: Canvas) {
        canvas.drawColor(Color.parseColor("#9BBC0F"))
        scale = canvas.width / logicalWidth
        logicalHeight = canvas.height / scale
        paddleY = logicalHeight - 10f

        canvas.drawRect((paddleX - paddleWidth / 2) * scale, (paddleY - paddleHeight / 2) * scale,
            (paddleX + paddleWidth / 2) * scale, (paddleY + paddleHeight / 2) * scale, paint)

        for (ball in balls) ball.draw(canvas, paint, scale, ballSize)
        for (p in powerUps) p.draw(canvas, paint, scale)
        for (brick in bricks) brick.draw(canvas, paint, scale)

        if (isGameOver) {
            paint.textSize = scale * 6f; paint.textAlign = Paint.Align.CENTER
            val cx = canvas.width / 2f; val cy = canvas.height / 2f
            val title = if (score == 0 && highScore == 0) GameType.ARKANOID.id else "GAME OVER"
            canvas.drawText(title, cx, cy - scale * 10, paint)
            paint.textSize = scale * 4f
            canvas.drawText("Score: $score", cx, cy, paint)
            canvas.drawText("HI-SCORE: $highScore", cx, cy + scale * 8, paint)
            paint.textSize = scale * 3f
            canvas.drawText("Press A or Touch to Start", cx, cy + scale * 20, paint)
        } else {
            paint.textSize = scale * 6f; paint.textAlign = Paint.Align.LEFT
            canvas.drawText("Score: $score", scale * 4f, scale * 8f, paint)
            paint.textAlign = Paint.Align.RIGHT
            val hiScoreX = if (isFullscreen) canvas.width - (scale * 25f) else canvas.width - (scale * 4f)
            canvas.drawText("HI: $highScore", hiScoreX, scale * 8f, paint)
            paint.textAlign = Paint.Align.LEFT
        }
    }
}
