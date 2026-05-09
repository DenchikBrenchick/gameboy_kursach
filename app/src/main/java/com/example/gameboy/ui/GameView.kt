package com.example.gameboy.ui

import android.content.Context
import android.graphics.Canvas
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.example.gameboy.game.IGame

// SurfaceView дозволяє малювати графіку в окремому потоці, щоб інтерфейс не зависав
class GameView(context: Context, private val game: IGame) : SurfaceView(context), Runnable {
    @Volatile
    private var isPlaying = false
    private var thread: Thread? = null
    private val surfaceHolder: SurfaceHolder = holder

    init {
        game.start() // Запускаємо гру при створенні екрана
    }

    // Це і є наш Game Loop (Ігровий цикл)
    override fun run() {
        while (isPlaying) {
            update()
            draw()
            controlFPS() // Робимо паузу, щоб не перегріти процесор
        }
    }

    private fun update() {
        game.update()
    }

    private fun draw() {
        if (surfaceHolder.surface.isValid) {
            var canvas: Canvas? = null
            try {
                // Намагаємося отримати полотно для малювання
                canvas = surfaceHolder.lockCanvas()

                // Малюємо гру ТІЛЬКИ якщо полотно реально існує
                if (canvas != null) {
                    game.draw(canvas)
                }
            } catch (e: Exception) {
                e.printStackTrace() // Якщо сталася помилка, просто запишемо її, але не "вб'ємо" гру
            } finally {
                // Обов'язково віддаємо полотно назад системі, щоб воно показалося на екрані
                if (canvas != null) {
                    surfaceHolder.unlockCanvasAndPost(canvas)
                }
            }
        }
    }

    private fun controlFPS() {
        try {
            Thread.sleep(17) // Затримка 17 мілісекунд дає нам приблизно 60 FPS
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    // Методи для керування життєвим циклом (щоб гра ставилась на паузу, коли ти згортаєш додаток)
    @Synchronized
    fun resume() {
        if (isPlaying || thread?.isAlive == true) return
        isPlaying = true
        thread = Thread(this)
        thread?.start()
    }
    @Synchronized
    fun pause() {
        if (!isPlaying && thread?.isAlive != true) return
        try {
            isPlaying = false
            val localThread = thread
            if (localThread != null && localThread != Thread.currentThread()) {
                localThread.join()
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } finally {
            thread = null
        }
    }
}