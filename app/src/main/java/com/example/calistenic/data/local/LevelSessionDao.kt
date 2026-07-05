package com.example.calistenic.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LevelSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: LevelSessionEntity): Long

    @Update
    suspend fun updateSession(session: LevelSessionEntity)

    @Query("SELECT * FROM level_sessions WHERE completedAt IS NULL LIMIT 1")
    fun getActiveSession(): Flow<LevelSessionEntity?>

    @Query("SELECT * FROM level_sessions WHERE completedAt IS NOT NULL ORDER BY completedAt DESC")
    fun getCompletedSessions(): Flow<List<LevelSessionEntity>>

    @Query("SELECT * FROM level_sessions WHERE id = :id")
    suspend fun getSessionById(id: Int): LevelSessionEntity?

    @Query("DELETE FROM level_sessions WHERE id = :id")
    suspend fun deleteSession(id: Int)
}