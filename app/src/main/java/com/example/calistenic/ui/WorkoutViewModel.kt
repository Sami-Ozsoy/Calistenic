package com.example.calistenic.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.calistenic.data.local.WorkoutEntity
import com.example.calistenic.data.repository.SettingsRepository
import com.example.calistenic.data.repository.WorkoutRepository
import com.example.calistenic.data.repository.WorkoutSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WorkoutViewModel(
    private val workoutRepository: WorkoutRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    val workouts = workoutRepository.allWorkouts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    val settings = settingsRepository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = WorkoutSettings()
    )

    fun saveWorkout(
        exerciseName: String,
        sets: List<Int>,
        restBetweenSetsSeconds: Int,
        restBetweenExercisesSeconds: Int,
        createdAt: Long,
        onSaved: () -> Unit
    ) {
        viewModelScope.launch {
            workoutRepository.insertWorkout(
                WorkoutEntity(
                    exerciseName = exerciseName.trim(),
                    sets = sets,
                    totalReps = sets.sum(),
                    restBetweenSetsSeconds = restBetweenSetsSeconds,
                    restBetweenExercisesSeconds = restBetweenExercisesSeconds,
                    createdAt = createdAt
                )
            )
            onSaved()
        }
    }

    fun deleteWorkout(workout: WorkoutEntity) {
        viewModelScope.launch {
            workoutRepository.deleteWorkout(workout)
        }
    }

    fun deleteAllWorkouts() {
        viewModelScope.launch {
            workoutRepository.deleteAllWorkouts()
        }
    }

    fun saveSettings(settings: WorkoutSettings, onSaved: () -> Unit) {
        viewModelScope.launch {
            settingsRepository.saveSettings(settings)
            onSaved()
        }
    }

    class Factory(
        private val workoutRepository: WorkoutRepository,
        private val settingsRepository: SettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return WorkoutViewModel(workoutRepository, settingsRepository) as T
        }
    }
}
