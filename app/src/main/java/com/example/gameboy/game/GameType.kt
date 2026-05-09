package com.example.gameboy.game

enum class GameType(val id: String) {
    SNAKE("SNAKE"),
    ARKANOID("ARKANOID"),
    SPACE_SHOOTER("SPACE SHOOTER");

    companion object {
        val all: List<GameType>
            get() = values().toList()

        fun fromId(id: String?): GameType =
            values().firstOrNull { it.id == id } ?: SNAKE
    }
}
