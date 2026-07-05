package com.example.calistenic.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [WorkoutEntity::class],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class WorkoutDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao

    companion object {
        @Volatile
        private var instance: WorkoutDatabase? = null

        // İleride şema değişiklikleri için migration listesi.
        // Örnek:
        // val MIGRATION_1_2 = object : Migration(1, 2) {
        //     override fun migrate(db: SupportSQLiteDatabase) {
        //         db.execSQL("ALTER TABLE workouts ADD COLUMN note TEXT")
        //     }
        // }
        private val MIGRATIONS: Array<Migration> = arrayOf(
            // Buraya yeni migration'lar eklenecek
        )

        fun getDatabase(context: Context): WorkoutDatabase {
            return instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    WorkoutDatabase::class.java,
                    "calistenic_database"
                )
                    .addMigrations(*MIGRATIONS)
                    // Şema değişikliklerinde otomatik sıfırlama için true yapıyoruz
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { instance = it }
            }
        }
    }
}