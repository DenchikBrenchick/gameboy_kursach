package com.example.gameboy.game

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.random.Random

// Успадковує BaseGame — отримує score, highScore, isGameOver, isFullscreen безкоштовно
class SnakeGame(
    initialHighScore: Int = 0,
    onGameOver: (Int) -> Unit,
    private val onEat: () -> Unit,
    onGameStart: () -> Unit
) : BaseGame(initialHighScore, onGameOver, onGameStart) {

    private val snake = CopyOnWriteArrayList<SnakeSegment>()
    private var apple: Apple? = null

    private var currentDirection = InputAction.RIGHT
    private var nextDirection = InputAction.RIGHT

    private var gridWidth = 20
    private var gridHeight = 25
    private var cellSize = 0f

    private var frameCount = 0
    private val framesPerMove = 10

    private val paint = Paint().apply {
        color = Color.parseColor("#0F380F")
        isAntiAlias = true
    }

    override fun start() {
        resetBaseState() // Скидаємо score та isGameOver через BaseGame
        snake.clear()
        snake.add(SnakeSegment(10, gridHeight / 2))
        snake.add(SnakeSegment(9, gridHeight / 2))
        snake.add(SnakeSegment(8, gridHeight / 2))

        currentDirection = InputAction.RIGHT
        nextDirection = InputAction.RIGHT
        frameCount = 0
        apple = null
        spawnApple()
        onGameStart()
    }

    override fun handleInput(action: InputAction) {
        if (isGameOver && (action == InputAction.START || action == InputAction.ACTION_A)) {
            start(); return
        }
        when (action) {
            InputAction.UP    -> if (currentDirection != InputAction.DOWN)  nextDirection = InputAction.UP
            InputAction.DOWN  -> if (currentDirection != InputAction.UP)    nextDirection = InputAction.DOWN
            InputAction.LEFT  -> if (currentDirection != InputAction.RIGHT) nextDirection = InputAction.LEFT
            InputAction.RIGHT -> if (currentDirection != InputAction.LEFT)  nextDirection = InputAction.RIGHT
            else -> {}
        }
    }

    override fun update() {
        if (isGameOver) return
        frameCount++
        if (frameCount >= framesPerMove) {
            frameCount = 0
            moveSnake()
            checkCollisions()
        }
    }

    private fun moveSnake() {
        if (snake.isEmpty()) return
        currentDirection = nextDirection
        val head = snake.first()
        var newX = head.x
        var newY = head.y
        when (currentDirection) {
            InputAction.UP    -> newY--
            InputAction.DOWN  -> newY++
            InputAction.LEFT  -> newX--
            InputAction.RIGHT -> newX++
            else -> {}
        }
        snake.add(0, SnakeSegment(newX, newY))
        if (apple != null && newX == apple!!.x && newY == apple!!.y) {
            score += 10
            onEat()
            spawnApple()
        } else {
            if (snake.isNotEmpty()) snake.removeAt(snake.size - 1)
        }
    }

    private fun checkCollisions() {
        if (snake.isEmpty()) return
        val head = snake.first()
        if (head.x < 0 || head.x >= gridWidth || head.y < 0 || head.y >= gridHeight) {
            triggerGameOver(); return
        }
        for (i in 1 until snake.size) {
            if (head.x == snake[i].x && head.y == snake[i].y) {
                triggerGameOver(); break
            }
        }
    }

    private fun spawnApple() {
        var newX: Int; var newY: Int; var isOnSnake: Boolean
        do {
            newX = Random.nextInt(0, gridWidth)
            newY = Random.nextInt(2, gridHeight)
            isOnSnake = snake.any { it.x == newX && it.y == newY }
        } while (isOnSnake)
        apple = Apple(newX, newY)
    }

    override fun draw(canvas: Canvas) {
        canvas.drawColor(Color.parseColor("#9BBC0F"))
        cellSize = canvas.width.toFloat() / gridWidth
        gridHeight = (canvas.height / cellSize).toInt()
        if (apple != null && apple!!.y >= gridHeight) spawnApple()

        apple?.draw(canvas, paint, cellSize.toInt())
        for (segment in snake) segment.draw(canvas, paint, cellSize.toInt())

        if (isGameOver) {
            paint.textSize = cellSize * 2f
            paint.textAlign = Paint.Align.CENTER
            val cx = canvas.width / 2f; val cy = canvas.height / 2f
            val title = if (score == 0 && highScore == 0) GameType.SNAKE.id else "GAME OVER"
            canvas.drawText(title, cx, cy - cellSize * 3, paint)
            paint.textSize = cellSize * 1.5f
            canvas.drawText("Score: $score", cx, cy, paint)
            canvas.drawText("HI-SCORE: $highScore", cx, cy + cellSize * 2, paint)
            paint.textSize = cellSize * 1.2f
            canvas.drawText("Press START or A", cx, cy + cellSize * 5, paint)
        } else {
            paint.textSize = cellSize * 2.0f
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("Score: $score", 10f, cellSize * 2.0f, paint)
            paint.textAlign = Paint.Align.RIGHT
            val hiScoreX = if (isFullscreen) canvas.width - (cellSize * 4.0f) else canvas.width - 10f
            canvas.drawText("HI: $highScore", hiScoreX, cellSize * 2.0f, paint)
            paint.textAlign = Paint.Align.LEFT
        }
    }
}
