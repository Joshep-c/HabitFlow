package com.app.habitflow.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Habit::class, HabitLog::class],
    version = 3,
    exportSchema = false
)
abstract class HabitDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
}
