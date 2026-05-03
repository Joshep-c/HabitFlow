package com.app.habitflow.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.app.habitflow.data.local.Habit
import com.app.habitflow.ui.viewmodel.HabitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HabitViewModel,
    onAddHabitClick: () -> Unit,
    onHabitClick: (Int) -> Unit
) {
    val habits by viewModel.filteredHabits.collectAsState()
    val completedIds by viewModel.completedTodayIds.collectAsState()
    val activeFilter by viewModel.activeFilter.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("HabitFlow") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddHabitClick) {
                Icon(Icons.Default.Add, contentDescription = "Agregar hábito")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Filtros
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("ALL" to "Todos", "DONE" to "Completados", "PENDING" to "Pendientes")
                    .forEach { (key, label) ->
                        FilterChip(
                            selected = activeFilter == key,
                            onClick = { viewModel.setFilter(key) },
                            label = { Text(label) }
                        )
                    }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(habits) { habit ->
                    val isCompleted = habit.id in completedIds
                    HabitCard(
                        habit = habit,
                        isCompleted = isCompleted,
                        onClick = { onHabitClick(habit.id) },
                        onToggle = { viewModel.toggleHabit(habit.id, isCompleted) }
                    )
                }
            }
        }
    }
}

@Composable
fun HabitCard(
    habit: Habit,
    isCompleted: Boolean,
    onClick: () -> Unit,
    onToggle: () -> Unit
) {
    val cardBackground by animateColorAsState(
        targetValue = if (isCompleted)
            Color(android.graphics.Color.parseColor(habit.themeColorHex)).copy(alpha = 0.25f)
        else Color(0xFF1E1E1E),
        animationSpec = tween(500),
        label = "cardBg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isCompleted)
            Color(android.graphics.Color.parseColor(habit.themeColorHex)).copy(alpha = 0.6f)
        else Color(0xFF3A3A3A),
        animationSpec = tween(500),
        label = "borderColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(BorderStroke(if (isCompleted) 1.5.dp else 1.dp, borderColor), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(habit.title, style = MaterialTheme.typography.titleMedium, color = Color.White)
                if (habit.description.isNotBlank())
                    Text(habit.description, style = MaterialTheme.typography.bodySmall, color = Color(0xFFAAAAAA))
            }
            IconButton(onClick = onToggle) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Marcar",
                    tint = if (isCompleted) Color(android.graphics.Color.parseColor(habit.themeColorHex))
                           else Color(0xFF555555)
                )
            }
        }
    }
}
