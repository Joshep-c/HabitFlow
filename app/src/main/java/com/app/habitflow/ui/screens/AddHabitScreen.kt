package com.app.habitflow.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.habitflow.ui.viewmodel.HabitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitScreen(
    viewModel: HabitViewModel,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#00C9A7") }
    var selectedIconKey by remember { mutableStateOf("target") }

    val wheelColors = listOf(
        "#F44336", "#E91E63", "#9C27B0", "#3F51B5",
        "#2196F3", "#00BCD4", "#00C9A7", "#4CAF50",
        "#8BC34A", "#FFC107", "#FF9800", "#795548"
    )

    val icons: List<Pair<String, ImageVector>> = listOf(
        "target"     to Icons.Default.DateRange,
        "running"    to Icons.Default.PlayArrow,
        "water"      to Icons.Default.Refresh,
        "book"       to Icons.Default.Info,
        "meditation" to Icons.Default.Favorite,
        "food"       to Icons.Default.Menu,
        "sleep"      to Icons.Default.Home,
        "music"      to Icons.Default.Notifications,
        "code"       to Icons.Default.Build,
        "gym"        to Icons.Default.ThumbUp
    )

    Scaffold(
        containerColor = Color(0xFFF5F5F5),
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Hábito", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = AppTeal)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Define Hábito
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(4.dp)) {
                    TextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("Nombre", color = Color.LightGray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            cursorColor = AppTeal,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        )
                    )
                    HorizontalDivider(color = Color(0xFFEEEEEE))
                    TextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = { Text("Descripción", color = Color.LightGray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            cursorColor = AppTeal,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        )
                    )
                }
            }

            // Icon Tag
            Text("Ícono del Hábito", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                modifier = Modifier.height(150.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                userScrollEnabled = false
            ) {
                items(icons) { (key: String, vector: ImageVector) ->
                    val isSelected = selectedIconKey == key
                    val colorHex = try {
                        Color(android.graphics.Color.parseColor(selectedColor))
                    } catch (e: Exception) { AppTeal }
                    Card(
                        modifier = Modifier
                            .size(64.dp)
                            .clickable { selectedIconKey = key },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) colorHex.copy(alpha = 0.2f) else Color.White
                        ),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = vector,
                                contentDescription = key,
                                tint = if (isSelected) colorHex else Color(0xFFBBBBBB),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }

            // Color
            Text("Color", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier.height(160.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        userScrollEnabled = false
                    ) {
                        items(wheelColors) { hex ->
                            val c = try {
                                Color(android.graphics.Color.parseColor(hex))
                            } catch (e: Exception) { Color.Gray }
                            val isSelected = selectedColor == hex
                            val size by animateFloatAsState(
                                targetValue = if (isSelected) 48f else 42f,
                                animationSpec = tween(200),
                                label = "colorSize"
                            )
                            Box(
                                modifier = Modifier
                                    .size(size.dp)
                                    .clip(CircleShape)
                                    .background(c)
                                    .border(if (isSelected) 3.dp else 0.dp, Color.White, CircleShape)
                                    .border(if (isSelected) 5.dp else 0.dp, c, CircleShape)
                                    .clickable { selectedColor = hex }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val previewIcon: ImageVector = icons.firstOrNull { it.first == selectedIconKey }?.second
                            ?: Icons.Default.Star
                        val color = try {
                            Color(android.graphics.Color.parseColor(selectedColor))
                        } catch (e: Exception) { AppTeal }
                        Icon(
                            imageVector = previewIcon,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(selectedColor.uppercase(), fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Botón Guardar
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        viewModel.addHabit(title, description, selectedColor, selectedIconKey)
                        onBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppTeal),
                enabled = title.isNotBlank()
            ) {
                Text("Guardar hábito", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            TextButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar", color = Color.Gray, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}