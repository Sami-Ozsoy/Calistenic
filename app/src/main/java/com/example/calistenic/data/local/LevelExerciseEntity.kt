package com.example.calistenic.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "level_exercises",
    foreignKeys = [ForeignKey(
        entity = LevelEntity::class,
        parentColumns = ["id"],
        childColumns = ["levelId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("levelId")]
)
data class LevelExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val levelId: Int,
    val orderIndex: Int,
    val exerciseName: String,
    val setCount: Int,
    val restBetweenSetsSeconds: Int,
    // Bu haraketten sonraki dinlenme:
    // 25 = kısa (grup içi sonraki harekete geçiş),
    // 180 = uzun (grup sonu),
    // 0 = son hareket (session biter)
    val restAfterSeconds: Int
)