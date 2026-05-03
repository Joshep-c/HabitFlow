package com.app.habitflow.data.repository

import com.app.habitflow.data.local.Habit
import com.app.habitflow.data.local.HabitDao
import com.app.habitflow.data.local.HabitLog
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitRepository @Inject constructor(
    private val habitDao: HabitDao
) {
    fun getAllHabits(): Flow<List<Habit>> = habitDao.getAllHabits()

    fun getLogsForHabit(habitId: Int): Flow<List<HabitLog>> =
        habitDao.getLogsForHabit(habitId)

    fun getLogsByDate(date: Long): Flow<List<HabitLog>> =
        habitDao.getLogsByDate(date)

    suspend fun addHabit(habit: Habit) = habitDao.insertHabit(habit)

    suspend fun deleteHabit(habit: Habit) = habitDao.deleteHabit(habit)

    suspend fun toggleLog(habitId: Int, date: Long, isCompleted: Boolean) {
        val log = HabitLog(habitId = habitId, date = date)
        if (isCompleted) habitDao.deleteLog(log) else habitDao.insertLog(log)
    }

    fun calculateStreak(logs: List<HabitLog>): Int {
        if (logs.isEmpty()) return 0
        val today = truncateToMidnight(System.currentTimeMillis())
        val sortedDates = logs.map { it.date }.sortedDescending()
        var streak = 0
        var expected = today
        for (date in sortedDates) {
            if (date == expected) {
                streak++
                expected -= 86_400_000L
            } else break
        }
        return streak
    }

    fun truncateToMidnight(timestamp: Long): Long {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.timeInMillis = timestamp
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
