package com.app.habitflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.habitflow.ui.viewmodel.HabitViewModel
import java.text.SimpleDateFormat
import java.util.*

val AppTealDetail = Color(0xFF00C9A7)

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

    var showDeleteDialog by remember { mutableStateOf(false) }

    val habitColorText = habit?.themeColorHex ?: "#4CAF50"
    val habitColor = try {
        Color(android.graphics.Color.parseColor(habitColorText))
    } catch (e: Exception) { Color(0xFF4CAF50) }

    val todayCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }
    val todayMillis = todayCal.timeInMillis

    val last30Days = (0..29).map { todayMillis - it * 86_400_000L }.reversed()
    val completedDates = logs.map { it.date }.toSet()

    // Lógica del mini calendario
    val currentMonthCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    // Resetear a medianoche
    currentMonthCal.set(Calendar.HOUR_OF_DAY, 0)
    currentMonthCal.set(Calendar.MINUTE, 0)
    currentMonthCal.set(Calendar.SECOND, 0)
    currentMonthCal.set(Calendar.MILLISECOND, 0)

    val currentMonthText = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(currentMonthCal.time)
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

    val daysInMonth = currentMonthCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val todayDayOfMonth = currentMonthCal.get(Calendar.DAY_OF_MONTH)

    // Obtener en qué día de la semana empieza el mes (1 = Domingo, 2 = Lunes, ...)
    currentMonthCal.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfWeek = currentMonthCal.get(Calendar.DAY_OF_WEEK)

    // Ajustar para que Lunes sea el inicio
    // Domingo = 1 -> offset 6
    // Lunes = 2   -> offset 0
    val offset = if (firstDayOfWeek == 1) 6 else firstDayOfWeek - 2

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color.White, Color(0xFFBBDEFB))))
    ) {
        if (habit == null) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
            return@Box
        }

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Detalle: ${habit.title}",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = AppTealDetail)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Card de racha
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val iconVector = habitIconVector(habit.iconKey)
                        Icon(imageVector = iconVector, contentDescription = null, tint = habitColor, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "$streak",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.Black
                            )
                            Text(
                                habit.description.ifBlank { "Pequeños pasos, grandes cambios" },
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                        Text("🏅", fontSize = 28.sp)
                    }
                }

                // Mini Calendario Mensual
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text("Este mes", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(currentMonthText, fontSize = 12.sp, color = Color.Gray)
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val daysOfWeek = listOf("L", "M", "X", "J", "V", "S", "D")
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                            daysOfWeek.forEach { day ->
                                Text(day, fontSize = 10.sp, color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(7),
                            userScrollEnabled = false,
                            modifier = Modifier.heightIn(max = 200.dp)
                        ) {
                            items(offset) {
                                Box(modifier = Modifier.size(32.dp).padding(2.dp))
                            }

                            items(daysInMonth) { dayIndex ->
                                val dayNum = dayIndex + 1

                                val c = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                                c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0)
                                c.set(Calendar.DAY_OF_MONTH, dayNum)
                                val millis = c.timeInMillis

                                val isDone = completedDates.contains(millis)
                                val isToday = dayNum == todayDayOfMonth
                                val isPast = dayNum < todayDayOfMonth
                                val isFuture = dayNum > todayDayOfMonth

                                Box(
                                    modifier = Modifier.padding(2.dp).size(32.dp).clip(CircleShape).then(
                                        when {
                                            isDone -> Modifier.background(habitColor)
                                            isToday -> Modifier.border(2.dp, habitColor, CircleShape)
                                            else -> Modifier
                                        }
                                    ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$dayNum",
                                        fontSize = 12.sp,
                                        fontWeight = if (isDone || isToday) FontWeight.Bold else FontWeight.Normal,
                                        color = when {
                                            isDone -> Color.White
                                            isToday -> habitColor
                                            isFuture -> Color(0xFFDDDDDD)
                                            isPast -> Color(0xFFCCCCCC)
                                            else -> Color.Black
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Grid últimos 30 días
                Text("Historial", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(top = 8.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(10),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    userScrollEnabled = false,
                    modifier = Modifier.heightIn(max = 100.dp)
                ) {
                    items(last30Days) { dayMillis ->
                        val done = dayMillis in completedDates
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(if (done) habitColor else Color(0xFFDDDDDD))
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Botón Marcar / Desmarcar
                Button(
                    onClick = { viewModel.toggleHabit(habitId, isCompletedToday) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCompletedToday) habitColor else Color.White
                    ),
                    border = if (!isCompletedToday) BorderStroke(1.5.dp, habitColor) else null,
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text(
                        if (isCompletedToday) "✓ Completado hoy" else "Marcar como hecho",
                        color = if (isCompletedToday) Color.White else habitColor,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp
                    )
                }

                // Botón Eliminar
                TextButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Eliminar hábito", color = Color(0xFFE53935), fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("¿Eliminar hábito?") },
            text = { Text("Esta acción eliminará el hábito y todo su historial. No se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteHabit(habitId); onBack() }) {
                    Text("Eliminar", color = Color(0xFFE53935))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        )
    }
}

