package com.example.gameboy.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scores")
data class ScoreEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val playerName: String,
    val gameName: String,
    val score: Int,
    val date: Long = System.currentTimeMillis()
)