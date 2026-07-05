package com.example.calistenic.data.repository

import com.example.calistenic.data.local.WorkoutDao
import com.example.calistenic.data.local.WorkoutEntity
import kotlinx.coroutines.flow.Flow

class WorkoutRepository(private val workoutDao: WorkoutDao) {
    val allWorkouts: Flow<List<WorkoutEntity>> = workoutDao.getAllWorkouts()

    suspend fun insertWorkout(workout: WorkoutEntity) {
        workoutDao.insertWorkout(workout)
    }

    suspend fun deleteWorkout(workout: WorkoutEntity) {
        workoutDao.deleteWorkout(workout)
    }

    suspend fun deleteAllWorkouts() {
        workoutDao.deleteAllWorkouts()
    }
}

