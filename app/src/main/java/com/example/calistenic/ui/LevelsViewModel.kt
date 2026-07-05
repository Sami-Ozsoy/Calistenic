package com.example.calistenic.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.calistenic.data.local.LevelEntity
import com.example.calistenic.data.local.LevelExerciseEntity
import com.example.calistenic.data.local.LevelSessionEntity
import com.example.calistenic.data.local.LevelSessionExerciseEntity
import com.example.calistenic.data.repository.LevelSessionRepository
import com.example.calistenic.data.repository.LevelsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LevelsViewModel(
    private val levelsRepository: LevelsRepository,
    private val levelSessionRepository: LevelSessionRepository
) : ViewModel() {

    val levels: StateFlow<List<LevelEntity>> = levelsRepository.allLevels.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    val activeSession: StateFlow<LevelSessionEntity?> = levelSessionRepository.activeSession.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null
    )

    val completedSessions: StateFlow<List<LevelSessionEntity>> =
        levelSessionRepository.completedSessions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    // ---- Level CRUD ----

    fun saveLevel(
        levelId: Int?,
        name: String,
        exercises: List<LevelExerciseEntity>,
        onSaved: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val id = levelsRepository.saveLevel(levelId, name, exercises)
            onSaved(id)
        }
    }

    fun deleteLevel(level: LevelEntity) {
        viewModelScope.launch {
            levelsRepository.deleteLevel(level)
        }
    }

    suspend fun getLevelWithExercises(levelId: Int): Pair<LevelEntity, List<LevelExerciseEntity>>? =
        levelsRepository.getLevelWithExercises(levelId)

    // ---- Session lifecycle ----

    fun startSession(level: LevelEntity, exercises: List<LevelExerciseEntity>, onStarted: (Long) -> Unit) {
        viewModelScope.launch {
            // Zaten aktif session varsa onu iptal et (aynı anda tek session)
            activeSession.value?.let { levelSessionRepository.cancelSession(it.id) }
            val sessionId = levelSessionRepository.startSession(level, exercises)
            onStarted(sessionId)
        }
    }

    /** Seviye id'den exercises yükle + session başlat. UI kolaylığı için. */
    fun startSessionById(levelId: Int, onStarted: (Long) -> Unit, onError: () -> Unit = {}) {
        viewModelScope.launch {
            val pair = levelsRepository.getLevelWithExercises(levelId)
            if (pair == null) {
                onError()
            } else {
                val (level, exercises) = pair
                if (exercises.isEmpty()) {
                    onError()
                } else {
                    activeSession.value?.let { levelSessionRepository.cancelSession(it.id) }
                    val sessionId = levelSessionRepository.startSession(level, exercises)
                    onStarted(sessionId)
                }
            }
        }
    }

    fun appendRep(sessionExercise: LevelSessionExerciseEntity, rep: Int) {
        viewModelScope.launch {
            levelSessionRepository.appendRep(sessionExercise, rep)
        }
    }

    fun completeSession(sessionId: Int, onCompleted: () -> Unit = {}) {
        viewModelScope.launch {
            levelSessionRepository.completeSession(sessionId)
            onCompleted()
        }
    }

    fun cancelSession(sessionId: Int, onCancelled: () -> Unit = {}) {
        viewModelScope.launch {
            levelSessionRepository.cancelSession(sessionId)
            onCancelled()
        }
    }

    suspend fun getSessionWithExercises(sessionId: Int): Pair<LevelSessionEntity, List<LevelSessionExerciseEntity>>? =
        levelSessionRepository.getSessionWithExercises(sessionId)

    fun getExercisesForSessionFlow(sessionId: Int) = levelSessionRepository.getExercisesForSession(sessionId)

    class Factory(
        private val levelsRepository: LevelsRepository,
        private val levelSessionRepository: LevelSessionRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LevelsViewModel(levelsRepository, levelSessionRepository) as T
        }
    }
}