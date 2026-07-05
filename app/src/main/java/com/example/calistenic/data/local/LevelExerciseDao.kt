package com.example.calistenic.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LevelExerciseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: LevelExerciseEntity): Long

    @Update
    suspend fun updateExercise(exercise: LevelExerciseEntity)

    @Delete
    suspend fun deleteExercise(exercise: LevelExerciseEntity)

    @Query("SELECT * FROM level_exercises WHERE levelId = :levelId ORDER BY orderIndex")
    fun getExercisesForLevel(levelId: Int): Flow<List<LevelExerciseEntity>>

    @Query("SELECT * FROM level_exercises WHERE levelId = :levelId ORDER BY orderIndex")
    suspend fun getExercisesForLevelOnce(levelId: Int): List<LevelExerciseEntity>

    @Query("DELETE FROM level_exercises WHERE levelId = :levelId")
    suspend fun deleteAllForLevel(levelId: Int)
}