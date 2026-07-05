package com.example.calistenic.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

data class WorkoutSettings(
    val defaultReps: Int = 6,
    val restBetweenSetsSeconds: Int = 25,
    val restBetweenExercisesSeconds: Int = 120
)

class SettingsRepository(private val context: Context) {
    private object Keys {
        val defaultReps = intPreferencesKey("default_reps")
        val restBetweenSets = intPreferencesKey("rest_between_sets")
        val restBetweenExercises = intPreferencesKey("rest_between_exercises")
    }

    val settings: Flow<WorkoutSettings> = context.settingsDataStore.data.map { preferences ->
        WorkoutSettings(
            defaultReps = preferences[Keys.defaultReps] ?: 6,
            restBetweenSetsSeconds = preferences[Keys.restBetweenSets] ?: 25,
            restBetweenExercisesSeconds = preferences[Keys.restBetweenExercises] ?: 120
        )
    }

    suspend fun saveSettings(settings: WorkoutSettings) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.defaultReps] = settings.defaultReps
            preferences[Keys.restBetweenSets] = settings.restBetweenSetsSeconds
            preferences[Keys.restBetweenExercises] = settings.restBetweenExercisesSeconds
        }
    }
}

