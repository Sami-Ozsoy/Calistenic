package com.example.calistenic.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LevelSessionExerciseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: LevelSessionExerciseEntity): Long

    @Update
    suspend fun updateExercise(exercise: LevelSessionExerciseEntity)

    @Query("SELECT * FROM level_session_exercises WHERE sessionId = :sessionId ORDER BY orderIndex")
    fun getExercisesForSession(sessionId: Int): Flow<List<LevelSessionExerciseEntity>>

    @Query("SELECT * FROM level_session_exercises WHERE sessionId = :sessionId ORDER BY orderIndex")
    suspend fun getExercisesForSessionOnce(sessionId: Int): List<LevelSessionExerciseEntity>
}