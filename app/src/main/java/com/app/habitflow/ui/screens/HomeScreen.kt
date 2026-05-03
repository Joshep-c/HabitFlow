package com.app.habitflow.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.filled.*
import com.app.habitflow.data.local.Habit
import com.app.habitflow.ui.viewmodel.HabitViewModel
import java.text.SimpleDateFormat
import java.util.*

val AppTeal = Color(0xFF00C9A7)

fun habitIconVector(iconKey: String): ImageVector = when (iconKey) {
    "running"    -> Icons.Default.PlayArrow
    "water"      -> Icons.Default.Refresh
    "book"       -> Icons.Default.Info
    "meditation" -> Icons.Default.Favorite
    "food"       -> Icons.Default.Menu
    "sleep"      -> Icons.Default.Home
    "music"      -> Icons.Default.Notifications
    "code"       -> Icons.Default.Build
    "gym"        -> Icons.Default.ThumbUp
    else         -> Icons.Default.Star
}

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
        containerColor = Color(0xFFF5F5F5),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "HabitFlow",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = Color.Black
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddHabitClick,
                containerColor = AppTeal,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar", tint = Color.White)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            WeekTimeline()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("ALL" to "Todos", "DONE" to "Completados", "PENDING" to "Pendientes")
                    .forEach { (key, label) ->
                        FilterChip(
                            selected = activeFilter == key,
                            onClick = { viewModel.setFilter(key) },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AppTeal,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp, top = 8.dp)
            ) {
                items(habits) { habit ->
                    val isCompleted = habit.id in completedIds
                    BentoHabitCard(
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
fun WeekTimeline() {
    val calendar = Calendar.getInstance()
    val dayLabels = listOf("SU", "MO", "TU", "WE", "TH", "FR", "SA")
    val today = calendar.get(Calendar.DAY_OF_MONTH)

    val days = (0..6).map { i ->
        val c = Calendar.getInstance()
        c.set(Calendar.DAY_OF_WEEK, i + 1)
        Pair(dayLabels[i], c.get(Calendar.DAY_OF_MONTH))
    }

    Surface(color = Color.White, shadowElevation = 2.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            days.forEachIndexed { _, (label, day) ->
                val isToday = day == today
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        label,
                        fontSize = 11.sp,
                        color = if (isToday) AppTeal else Color.Gray,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(if (isToday) AppTeal else Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "$day",
                            fontSize = 12.sp,
                            color = if (isToday) Color.White else Color.DarkGray,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BentoHabitCard(
    habit: Habit,
    isCompleted: Boolean,
    onClick: () -> Unit,
    onToggle: () -> Unit
) {
    val habitColor = try {
        Color(android.graphics.Color.parseColor(habit.themeColorHex))
    } catch (e: Exception) { AppTeal }

    val cardBg by animateColorAsState(
        targetValue = if (isCompleted) habitColor.copy(alpha = 0.12f) else Color.White,
        animationSpec = tween(500),
        label = "bg"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(cardBg)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(habitColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                val iconVector = habitIconVector(habit.iconKey)
                Icon(
                    imageVector = iconVector,
                    contentDescription = habit.title,
                    tint = habitColor,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    habit.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                if (habit.description.isNotBlank()) {
                    Text(
                        habit.description,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                if (isCompleted) {
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = habitColor,
                        trackColor = habitColor.copy(alpha = 0.2f)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    if (isCompleted) "Completado Hoy" else "Incomplete",
                    fontSize = 11.sp,
                    color = if (isCompleted) habitColor else Color.Gray,
                    modifier = Modifier.align(Alignment.End)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = onToggle) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isCompleted) habitColor else Color(0xFFEEEEEE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Toggle",
                        tint = if (isCompleted) Color.White else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}