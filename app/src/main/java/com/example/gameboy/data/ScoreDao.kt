package com.example.gameboy.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ScoreDao {
    @Query("SELECT MAX(score) FROM scores WHERE playerName = :name AND gameName = :game")
    suspend fun getPersonalBest(name: String, game: String): Int?

    @Insert
    suspend fun insertScore(score: ScoreEntity)

    @Query("SELECT * FROM scores WHERE gameName = :game ORDER BY score DESC LIMIT 10")
    suspend fun getGlobalTopScores(game: String): List<ScoreEntity>
}