package com.example.gameboy.game

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.sin
import kotlin.random.Random

// Успадковує BaseGame — score, highScore, isGameOver, isFullscreen вже є
class SpaceShooterGame(
    initialHighScore: Int = 0,
    onGameOver: (Int) -> Unit,
    private val onShoot: () -> Unit,
    private val onExplosion: () -> Unit,
    onGameStart: () -> Unit
) : BaseGame(initialHighScore, onGameOver, onGameStart) {

    private val logicalWidth = 100f
    private var logicalHeight = 125f
    private var scale = 1f

    private var hp = 3
    private val maxHp = 3
    private var invulnerableFrames = 0

    private var shipX = 50f
    private var shipY = 110f
    private val shipSpeed = 1.5f
    private val shipWidth = 8f
    private val shipHeight = 8f
    private var isShooting = false
    private var shootCooldown = 0
    private var weaponLevel = 1

    private val stars    = CopyOnWriteArrayList<Star>()
    private val lasers   = CopyOnWriteArrayList<Laser>()
    private val enemies  = CopyOnWriteArrayList<Enemy>()
    private val powerUps = CopyOnWriteArrayList<PowerUp>()

    private var frameCount = 0
    private var waveFrames = 0
    private val framesToWave = 1800

    private val paint = Paint().apply { color = Color.parseColor("#0F380F"); isAntiAlias = true }
    private val paintLight = Paint().apply { color = Color.parseColor("#0F380F"); alpha = 50; isAntiAlias = true }

    override fun start() {
        resetBaseState()
        hp = 3; weaponLevel = 1; shipX = 50f
        frameCount = 0; waveFrames = 0; invulnerableFrames = 0
        lasers.clear(); enemies.clear(); powerUps.clear(); stars.clear()
        for (i in 0..40) {
            stars.add(Star(Random.nextFloat() * logicalWidth, Random.nextFloat() * 125f,
                Random.nextFloat() * 0.5f + 0.1f, Random.nextFloat() * 1.5f + 0.5f))
        }
        onGameStart()
    }

    // Реалізація onTouchMove з IGame — повноекранне керування (Поліморфізм)
    override fun onTouchMove(percentX: Float) {
        shipX = percentX * logicalWidth
        if (shipX < shipWidth / 2) shipX = shipWidth / 2
        if (shipX > logicalWidth - shipWidth / 2) shipX = logicalWidth - shipWidth / 2
        isShooting = true
    }

    // Реалізація onTouchRelease з IGame — зупиняємо стрільбу (Поліморфізм)
    override fun onTouchRelease() {
        isShooting = false
    }

    override fun handleInput(action: InputAction) {
        if (isGameOver && (action == InputAction.START || action == InputAction.ACTION_A)) { start(); return }
        if (!isGameOver) {
            when (action) {
                InputAction.LEFT -> { shipX -= shipSpeed; if (shipX < shipWidth / 2) shipX = shipWidth / 2 }
                InputAction.RIGHT -> { shipX += shipSpeed; if (shipX > logicalWidth - shipWidth / 2) shipX = logicalWidth - shipWidth / 2 }
                InputAction.ACTION_A, InputAction.ACTION_B -> isShooting = true
                else -> {}
            }
        }
    }

    override fun update() {
        if (isGameOver) return
        frameCount++; waveFrames++
        if (invulnerableFrames > 0) invulnerableFrames--

        for (star in stars) {
            star.y += star.speed
            if (star.y > logicalHeight) { star.y = 0f; star.x = Random.nextFloat() * logicalWidth }
        }

        if (shootCooldown > 0) shootCooldown--
        if (isShooting && shootCooldown == 0) {
            when (weaponLevel) {
                1 -> lasers.add(Laser(shipX, shipY - shipHeight / 2, 0f, -3f))
                2 -> { lasers.add(Laser(shipX - 2f, shipY - shipHeight / 2, 0f, -3f)); lasers.add(Laser(shipX + 2f, shipY - shipHeight / 2, 0f, -3f)) }
                3 -> { lasers.add(Laser(shipX, shipY - shipHeight / 2, 0f, -3f)); lasers.add(Laser(shipX - 2f, shipY - shipHeight / 2, -0.8f, -3f)); lasers.add(Laser(shipX + 2f, shipY - shipHeight / 2, 0.8f, -3f)) }
                else -> { lasers.add(Laser(shipX - 2f, shipY - shipHeight / 2, 0f, -3f)); lasers.add(Laser(shipX + 2f, shipY - shipHeight / 2, 0f, -3f)); lasers.add(Laser(shipX - 4f, shipY - shipHeight / 2, -1.2f, -3f)); lasers.add(Laser(shipX + 4f, shipY - shipHeight / 2, 1.2f, -3f)) }
            }
            onShoot(); shootCooldown = 12
        }

        val shipRect = RectF(shipX - shipWidth / 2, shipY - shipHeight / 2, shipX + shipWidth / 2, shipY + shipHeight / 2)

        for (laser in lasers) {
            laser.x += laser.dx; laser.y += laser.dy
            if (laser.y < 0 || laser.x < 0 || laser.x > logicalWidth || laser.y > logicalHeight) { lasers.remove(laser); continue }
            if (laser.isEnemy && invulnerableFrames == 0) {
                val lr = RectF(laser.x - 1.5f, laser.y - 1.5f, laser.x + 1.5f, laser.y + 1.5f)
                if (RectF.intersects(lr, shipRect)) {
                    lasers.remove(laser); hp--; onExplosion(); invulnerableFrames = 30
                    if (hp <= 0) { triggerGameOver(); return }
                }
            }
        }

        val spawnRate = if (score > 500) 25 else if (score > 200) 35 else 50
        if (frameCount % spawnRate == 0) {
            val r = Random.nextFloat()
            val enemyType = when { frameCount > 1800 && r > 0.7f -> 3; r > 0.8f -> 2; else -> 1 }
            enemies.add(Enemy(Random.nextFloat() * (logicalWidth - 10f) + 5f, -10f, enemyType, if (Random.nextFloat() > 0.7f) 1 else 0, shootCooldown = Random.nextInt(30, 90)))
        }

        if (waveFrames >= framesToWave) {
            waveFrames = 0; val cy = -10f
            enemies.add(Enemy(50f, cy, 1, 0)); enemies.add(Enemy(38f, cy - 12f, 1, 0)); enemies.add(Enemy(62f, cy - 12f, 1, 0))
            enemies.add(Enemy(26f, cy - 24f, 2, 0)); enemies.add(Enemy(74f, cy - 24f, 2, 0))
        }

        for (p in powerUps) {
            p.y += 0.8f
            val pr = RectF(p.x - 3.5f, p.y - 3.5f, p.x + 3.5f, p.y + 3.5f)
            if (RectF.intersects(shipRect, pr)) {
                if (p.type == PowerUpType.WEAPON_UPGRADE && weaponLevel < 4) weaponLevel++
                if (p.type == PowerUpType.HEALTH && hp < maxHp) hp++
                onExplosion(); powerUps.remove(p)
            } else if (p.y > logicalHeight) powerUps.remove(p)
        }

        for (enemy in enemies) {
            enemy.y += if (enemy.type == 1) 0.8f else 0.5f
            if (enemy.pattern == 1) {
                enemy.x = enemy.startX + sin(enemy.y * 0.1f) * 15f
                if (enemy.x < 4f) enemy.x = 4f; if (enemy.x > logicalWidth - 4f) enemy.x = logicalWidth - 4f
            }
            if (enemy.type == 3) {
                if (enemy.shootCooldown > 0) enemy.shootCooldown--
                if (enemy.shootCooldown <= 0) { lasers.add(Laser(enemy.x, enemy.y + 4f, 0f, 1.8f, isEnemy = true)); enemy.shootCooldown = 100 }
            }

            val er = RectF(enemy.x - 4f, enemy.y - 4f, enemy.x + 4f, enemy.y + 4f)
            if (RectF.intersects(shipRect, er)) { hp = 0; triggerGameOver(); return }
            if (enemy.y > shipY + shipHeight / 2) { hp = 0; triggerGameOver(); return }

            for (laser in lasers) {
                if (laser.isEnemy) continue
                val lr = RectF(laser.x - 1f, laser.y - 2f, laser.x + 1f, laser.y + 2f)
                if (RectF.intersects(lr, er)) {
                    lasers.remove(laser); enemies.remove(enemy); score += if (enemy.type == 1) 10 else 25; onExplosion()
                    val drop = Random.nextFloat()
                    if (drop < 0.04f && weaponLevel < 4) powerUps.add(PowerUp(enemy.x, enemy.y, PowerUpType.WEAPON_UPGRADE))
                    else if (drop in 0.04f..0.06f && hp < maxHp) powerUps.add(PowerUp(enemy.x, enemy.y, PowerUpType.HEALTH))
                    break
                }
            }
        }
    }

    override fun draw(canvas: Canvas) {
        canvas.drawColor(Color.parseColor("#9BBC0F"))
        scale = canvas.width / logicalWidth; logicalHeight = canvas.height / scale; shipY = logicalHeight - 15f

        for (star in stars) star.draw(canvas, paintLight, scale)
        for (laser in lasers) laser.draw(canvas, paint, scale)
        for (p in powerUps) p.draw(canvas, paint, scale)
        for (enemy in enemies) enemy.draw(canvas, paint, scale)

        if (invulnerableFrames % 8 < 4) {
            val sx = shipX * scale; val sy = shipY * scale; val sw = (shipWidth / 2) * scale; val sh = (shipHeight / 2) * scale
            canvas.drawRect(sx - scale, sy - sh, sx + scale, sy + sh, paint)
            canvas.drawRect(sx - sw, sy, sx + sw, sy + sh, paint)
        }

        if (isGameOver) {
            paint.textSize = scale * 6f; paint.textAlign = Paint.Align.CENTER
            val cx = canvas.width / 2f; val cy = canvas.height / 2f
            val title = if (score == 0 && highScore == 0) GameType.SPACE_SHOOTER.id else "GAME OVER"
            canvas.drawText(title, cx, cy - scale * 10, paint)
            paint.textSize = scale * 4f
            canvas.drawText("Score: $score", cx, cy, paint)
            canvas.drawText("HI-SCORE: $highScore", cx, cy + scale * 8, paint)
            paint.textSize = scale * 3f
            canvas.drawText("Press A or Touch to Start", cx, cy + scale * 20, paint)
        } else {
            paint.textSize = scale * 5f; paint.textAlign = Paint.Align.LEFT
            canvas.drawText("Score: $score", scale * 4f, scale * 6f, paint)
            canvas.drawText("HP: $hp", scale * 4f, scale * 12f, paint)
            paint.textAlign = Paint.Align.RIGHT
            val hiScoreX = if (isFullscreen) canvas.width - (scale * 25f) else canvas.width - (scale * 4f)
            canvas.drawText("HI: $highScore", hiScoreX, scale * 6f, paint)
            paint.textAlign = Paint.Align.LEFT
        }
    }
}
