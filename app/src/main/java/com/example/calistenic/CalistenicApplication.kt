package com.example.calistenic

import android.app.Application
import com.example.calistenic.data.local.WorkoutDatabase
import com.example.calistenic.data.repository.SettingsRepository
import com.example.calistenic.data.repository.WorkoutRepository

class CalistenicApplication : Application() {
    val database by lazy { WorkoutDatabase.getDatabase(this) }
    val workoutRepository by lazy { WorkoutRepository(database.workoutDao()) }
    val settingsRepository by lazy { SettingsRepository(this) }
}

