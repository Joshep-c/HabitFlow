package com.app.habitflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.app.habitflow.data.local.HabitLog
import com.app.habitflow.ui.viewmodel.HabitViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    viewModel: HabitViewModel,
    habitId: Int,
    onBack: () -> Unit
) {
    val allHabits by viewModel.filteredHabits.collectAsState()
    val habit = allHabits.firstOrNull { it.id == habitId }
    val logs by viewModel.getLogsForHabit(habitId).collectAsState(initial = emptyList())
    val completedIds by viewModel.completedTodayIds.collectAsState()
    val isCompletedToday = habitId in completedIds
    val streak = viewModel.getStreakForHabit(logs)

    val today = viewModel.run {
        Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val last30Days = (0..29).map { today - it * 86_400_000L }
    val completedDates = logs.map { it.date }.toSet()
    val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(habit?.title ?: "Detalle") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Racha
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Racha actual", style = MaterialTheme.typography.titleMedium)
                    Text("🔥 $streak días", style = MaterialTheme.typography.headlineSmall)
                }
            }

            // Botón marcar/desmarcar hoy
            Button(
                onClick = { viewModel.toggleHabit(habitId, isCompletedToday) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCompletedToday) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                )
            ) {
                Text(if (isCompletedToday) "✓ Completado hoy" else "Marcar hoy")
            }

            // Historial últimos 30 días
            Text("Historial — últimos 30 días", style = MaterialTheme.typography.titleSmall)

            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(last30Days) { dayMillis ->
                    val done = dayMillis in completedDates
                    val color = if (done)
                        habit?.themeColorHex?.let { Color(android.graphics.Color.parseColor(it)) }
                            ?: Color(0xFF4CAF50)
                    else Color(0xFF2A2A2A)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(sdf.format(Date(dayMillis)), style = MaterialTheme.typography.bodyMedium)
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }
            }
        }
    }
}

