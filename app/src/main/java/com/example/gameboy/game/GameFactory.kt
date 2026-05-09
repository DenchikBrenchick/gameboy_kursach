package com.example.gameboy.game

object GameFactory {
    fun create(
        gameType: GameType,
        initialHighScore: Int = 0,
        onGameOver: (Int) -> Unit,
        onEat: () -> Unit,
        onClick: () -> Unit,
        onGameStart: () -> Unit
    ): IGame {
        return when (gameType) {
            GameType.ARKANOID -> ArkanoidGame(
                initialHighScore = initialHighScore,
                onGameOver = onGameOver,
                onBrickHit = onEat,
                onGameStart = onGameStart
            )
            GameType.SPACE_SHOOTER -> SpaceShooterGame(
                initialHighScore = initialHighScore,
                onGameOver = onGameOver,
                onShoot = onClick,
                onExplosion = onEat,
                onGameStart = onGameStart
            )
            GameType.SNAKE -> SnakeGame(
                initialHighScore = initialHighScore,
                onGameOver = onGameOver,
                onEat = onEat,
                onGameStart = onGameStart
            )
        }
    }
}
