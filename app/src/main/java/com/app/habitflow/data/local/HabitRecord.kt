package com.app.habitflow.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habit_records")
data class HabitRecord(
    @PrimaryKey(autoGenerate = true) val recordId: Int = 0,
    val habitId: Int,
    val completionDate: Long, // Formato Epoch Milli (UTC)
    val isCompleted: Boolean
)

