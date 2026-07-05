package com.example.calistenic.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val exerciseName: String,
    val sets: List<Int>,
    val totalReps: Int,
    val restBetweenSetsSeconds: Int,
    val restBetweenExercisesSeconds: Int,
    val createdAt: Long = System.currentTimeMillis()
)
