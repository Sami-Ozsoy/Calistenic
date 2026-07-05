package com.example.calistenic.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LevelDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLevel(level: LevelEntity): Long

    @Update
    suspend fun updateLevel(level: LevelEntity)

    @Delete
    suspend fun deleteLevel(level: LevelEntity)

    @Query("SELECT * FROM levels ORDER BY createdAt DESC")
    fun getAllLevels(): Flow<List<LevelEntity>>

    @Query("SELECT * FROM levels WHERE id = :id")
    suspend fun getLevelById(id: Int): LevelEntity?

    @Query("SELECT * FROM levels WHERE id = :id")
    fun getLevelByIdFlow(id: Int): Flow<LevelEntity?>
}