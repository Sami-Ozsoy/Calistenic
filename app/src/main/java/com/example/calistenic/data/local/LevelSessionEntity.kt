package com.example.calistenic.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "level_sessions")
data class LevelSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val levelId: Int,
    val levelName: String,
    val startedAt: Long,
    // null = devam ediyor, non-null = bitmiş (history'de görünür)
    val completedAt: Long? = null,
    val totalReps: Int = 0
)