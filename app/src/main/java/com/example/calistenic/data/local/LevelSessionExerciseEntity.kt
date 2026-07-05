package com.example.calistenic.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "level_session_exercises",
    foreignKeys = [ForeignKey(
        entity = LevelSessionEntity::class,
        parentColumns = ["id"],
        childColumns = ["sessionId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("sessionId")]
)
data class LevelSessionExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val sessionId: Int,
    val orderIndex: Int,
    val exerciseName: String,
    val setCount: Int,           // level_exercises'ten snapshot
    val sets: List<Int>,         // Converters (CSV) — başta boş, set bittikçe append
    val restBetweenSetsSeconds: Int,
    val restAfterSeconds: Int,
    // level_exercises'ten snapshot — app-internal storage absolute path. Null = görsel yok.
    val imagePath: String? = null
)