package com.app.habitflow.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String = "",
    val themeColorHex: String = "#4CAF50",
    val createdAt: Long = System.currentTimeMillis(),
    val iconKey: String = "target"
)
