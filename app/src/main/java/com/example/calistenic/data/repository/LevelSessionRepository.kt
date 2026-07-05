package com.example.calistenic.data.repository

import com.example.calistenic.data.local.LevelEntity
import com.example.calistenic.data.local.LevelExerciseEntity
import com.example.calistenic.data.local.LevelSessionDao
import com.example.calistenic.data.local.LevelSessionEntity
import com.example.calistenic.data.local.LevelSessionExerciseDao
import com.example.calistenic.data.local.LevelSessionExerciseEntity
import kotlinx.coroutines.flow.Flow

class LevelSessionRepository(
    private val sessionDao: LevelSessionDao,
    private val sessionExerciseDao: LevelSessionExerciseDao
) {
    val activeSession: Flow<LevelSessionEntity?> = sessionDao.getActiveSession()
    val completedSessions: Flow<List<LevelSessionEntity>> = sessionDao.getCompletedSessions()

    /**
     * Yeni session başlatır: LevelSessionEntity (completedAt=null) +
     * level_exercises snapshot'ından LevelSessionExerciseEntity satırları (boş sets ile).
     */
    suspend fun startSession(level: LevelEntity, exercises: List<LevelExerciseEntity>): Long {
        val session = LevelSessionEntity(
            levelId = level.id,
            levelName = level.name,
            startedAt = System.currentTimeMillis()
        )
        val sessionId = sessionDao.insertSession(session)
        exercises.forEachIndexed { index, ex ->
            sessionExerciseDao.insertExercise(
                LevelSessionExerciseEntity(
                    sessionId = sessionId.toInt(),
                    orderIndex = index,
                    exerciseName = ex.exerciseName,
                    setCount = ex.setCount,
                    sets = emptyList(),
                    restBetweenSetsSeconds = ex.restBetweenSetsSeconds,
                    restAfterSeconds = ex.restAfterSeconds,
                    imagePath = ex.imagePath
                )
            )
        }
        return sessionId
    }

    /** Mevcut sete tekrar ekle (append). sessionExercise güncellenir. */
    suspend fun appendRep(sessionExercise: LevelSessionExerciseEntity, rep: Int) {
        sessionExerciseDao.updateExercise(
            sessionExercise.copy(sets = sessionExercise.sets + rep)
        )
    }

    /** Session'ı tamamlandı olarak işaretle, totalReps güncelle. */
    suspend fun completeSession(sessionId: Int) {
        val session = sessionDao.getSessionById(sessionId) ?: return
        val exercises = sessionExerciseDao.getExercisesForSessionOnce(sessionId)
        val totalReps = exercises.sumOf { it.sets.sum() }
        sessionDao.updateSession(
            session.copy(completedAt = System.currentTimeMillis(), totalReps = totalReps)
        )
    }

    /** Session'ı iptal et (sil). Cascade child exercise'leri de siler. */
    suspend fun cancelSession(sessionId: Int) {
        sessionDao.deleteSession(sessionId)
    }

    suspend fun getSessionWithExercises(sessionId: Int): Pair<LevelSessionEntity, List<LevelSessionExerciseEntity>>? {
        val session = sessionDao.getSessionById(sessionId) ?: return null
        val exercises = sessionExerciseDao.getExercisesForSessionOnce(sessionId)
        return session to exercises
    }

    suspend fun getExercisesForSessionOnce(sessionId: Int): List<LevelSessionExerciseEntity> =
        sessionExerciseDao.getExercisesForSessionOnce(sessionId)

    fun getExercisesForSession(sessionId: Int): Flow<List<LevelSessionExerciseEntity>> =
        sessionExerciseDao.getExercisesForSession(sessionId)
}