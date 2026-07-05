package com.example.calistenic.data.repository

import com.example.calistenic.data.local.LevelDao
import com.example.calistenic.data.local.LevelEntity
import com.example.calistenic.data.local.LevelExerciseDao
import com.example.calistenic.data.local.LevelExerciseEntity
import kotlinx.coroutines.flow.Flow

class LevelsRepository(
    private val levelDao: LevelDao,
    private val levelExerciseDao: LevelExerciseDao
) {
    val allLevels: Flow<List<LevelEntity>> = levelDao.getAllLevels()

    suspend fun getLevelWithExercises(levelId: Int): Pair<LevelEntity, List<LevelExerciseEntity>>? {
        val level = levelDao.getLevelById(levelId) ?: return null
        val exercises = levelExerciseDao.getExercisesForLevelOnce(levelId)
        return level to exercises
    }

    /**
     * Yeni seviye kaydeder veya mevcut seviyeyi günceller.
     * [levelId] null ise yeni kayıt, non-null ise update (önce mevcut hareketler silinir).
     */
    suspend fun saveLevel(levelId: Int?, name: String, exercises: List<LevelExerciseEntity>): Long {
        return if (levelId == null) {
            val newLevel = LevelEntity(name = name)
            val newId = levelDao.insertLevel(newLevel)
            exercises.forEachIndexed { index, ex ->
                levelExerciseDao.insertExercise(
                    ex.copy(levelId = newId.toInt(), orderIndex = index)
                )
            }
            newId
        } else {
            levelDao.updateLevel(levelDao.getLevelById(levelId)!!.copy(name = name))
            levelExerciseDao.deleteAllForLevel(levelId)
            exercises.forEachIndexed { index, ex ->
                levelExerciseDao.insertExercise(
                    ex.copy(levelId = levelId, orderIndex = index, id = 0)
                )
            }
            levelId.toLong()
        }
    }

    suspend fun deleteLevel(level: LevelEntity) {
        levelDao.deleteLevel(level)
    }
}