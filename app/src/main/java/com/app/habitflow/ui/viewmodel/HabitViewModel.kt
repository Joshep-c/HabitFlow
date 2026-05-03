package com.app.habitflow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.habitflow.data.local.Habit
import com.app.habitflow.data.local.HabitLog
import com.app.habitflow.data.local.PreferencesManager
import com.app.habitflow.data.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class HabitViewModel @Inject constructor(
    private val repository: HabitRepository,
    private val prefs: PreferencesManager
) : ViewModel() {

    private val today: Long get() = repository.truncateToMidnight(System.currentTimeMillis())

    val activeFilter: StateFlow<String> = prefs.activeFilter
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "ALL")

    private val allHabits: Flow<List<Habit>> = repository.getAllHabits()
    private val todayLogs: Flow<List<HabitLog>> = repository.getLogsByDate(today)

    val filteredHabits: StateFlow<List<Habit>> =
        combine(allHabits, todayLogs, activeFilter) { habits, logs, filter ->
            val completedIds = logs.map { it.habitId }.toSet()
            when (filter) {
                "DONE"    -> habits.filter { it.id in completedIds }
                "PENDING" -> habits.filter { it.id !in completedIds }
                else      -> habits
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedTodayIds: StateFlow<Set<Int>> = todayLogs
        .map { logs -> logs.map { it.habitId }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    fun setFilter(filter: String) {
        viewModelScope.launch { prefs.saveFilter(filter) }
    }

    fun addHabit(title: String, description: String, colorHex: String, iconKey: String) {
        viewModelScope.launch {
            repository.addHabit(
                Habit(title = title, description = description, themeColorHex = colorHex, iconKey = iconKey)
            )
        }
    }

    fun deleteHabit(habitId: Int) {
        viewModelScope.launch {
            repository.deleteHabit(habitId)
        }
    }

    fun toggleHabit(habitId: Int, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.toggleLog(habitId, today, isCompleted)
        }
    }

    fun getLogsForHabit(habitId: Int): Flow<List<HabitLog>> =
        repository.getLogsForHabit(habitId)

    fun getStreakForHabit(logs: List<HabitLog>): Int =
        repository.calculateStreak(logs)
}
