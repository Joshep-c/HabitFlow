# HabitFlow

Aplicación Android para el seguimiento y gestión de hábitos diarios. Permite crear hábitos personalizados, marcarlos como completados cada día, visualizar rachas y revisar el historial mensual.

---

## Índice

- [Capturas de pantalla](#capturas-de-pantalla)
- [Funcionalidades](#funcionalidades)
- [Arquitectura](#arquitectura)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Stack tecnológico](#stack-tecnológico)
- [Base de datos](#base-de-datos)
- [Navegación](#navegación)
- [Instalación y configuración](#instalación-y-configuración)
- [Migraciones de base de datos](#migraciones-de-base-de-datos)

---

## Funcionalidades

- **Crear hábitos** con nombre, descripción, color e ícono personalizados
- **Marcar hábitos** como completados por día con animación de color
- **Rachas (streaks)** calculadas automáticamente en base al historial
- **Mini calendario mensual** en el detalle del hábito con días completados resaltados
- **Historial de 30 días** en vista de grid circular
- **Filtros** por estado: Todos / Completados / Pendientes (persistido entre sesiones)
- **Timeline semanal** en la pantalla principal mostrando el día actual
- **Eliminar hábito** con confirmación, eliminando también su historial completo
- **Selector de ícono** con 10 opciones vectoriales de un solo color
- **Selector de color** con 12 colores predefinidos y animación de selección

---

## Arquitectura

El proyecto sigue el patrón **MVVM (Model-View-ViewModel)** con una arquitectura de capas limpia:

```
UI Layer
    └── Screens (Composables)
    └── ViewModel (HabitViewModel)

Domain / Data Layer
    └── Repository (HabitRepository)
    └── DAO (HabitDao)
    └── Entities (Habit, HabitLog)
    └── PreferencesManager (DataStore)

DI Layer
    └── DatabaseModule (Hilt)
    └── RepositoryModule (Hilt)
```

### Flujo de datos

```
Room DB ──► DAO ──► Repository ──► ViewModel ──► StateFlow ──► Composable
DataStore ──────────────────────────────────────────────────────────────►
```

Todo el estado de la UI se expone como `StateFlow` desde el ViewModel y se recolecta en los Composables con `collectAsState()`. Los datos de Room se exponen como `Flow` y se combinan en el ViewModel con `combine()`.

---

## Estructura del proyecto

```
com.app.habitflow/
│
├── HabitFlowApplication.kt          # Entry point, @HiltAndroidApp
├── MainActivity.kt                  # Activity principal, NavController
│
├── data/
│   ├── local/
│   │   ├── Habit.kt                 # Entidad Room: hábito
│   │   ├── HabitLog.kt              # Entidad Room: registro diario
│   │   ├── HabitDatabase.kt         # RoomDatabase, versión 3
│   │   ├── Dao.kt                   # Queries Room
│   │   ├── PreferencesManager.kt    # DataStore: filtro activo
│   │   └── HabitRecord.kt
│   └── repository/
│       └── HabitRepository.kt       # Fuente única de verdad
│
├── di/
│   ├── DatabaseModule.kt            # Provee Room DB y DAO
│   └── RepositoryModule.kt          # Provee HabitRepository
│
├── navigation/
│   └── HabitNavHost.kt              # Rutas de navegación
│
├── ui/
│   ├── screens/
│   │   ├── HomeScreen.kt            # Lista de hábitos + timeline
│   │   ├── AddHabitScreen.kt        # Crear nuevo hábito
│   │   └── HabitDetailScreen.kt     # Detalle, calendario, racha
│   ├── viewmodel/
│   │   └── HabitViewModel.kt        # Estado y lógica de UI
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
```

---

## Stack tecnológico

| Tecnología | Uso |
|---|---|
| **Kotlin** | Lenguaje principal |
| **Jetpack Compose** | UI declarativa |
| **Material3** | Componentes y tema |
| **Room** | Persistencia local (SQLite) |
| **DataStore Preferences** | Persistencia de preferencias |
| **Hilt** | Inyección de dependencias |
| **Navigation Compose** | Navegación entre pantallas |
| **ViewModel + StateFlow** | Gestión de estado |
| **KSP** | Procesador de anotaciones (Room + Hilt) |
| **Kotlin Coroutines** | Operaciones asíncronas |

### Versiones principales

| Parámetro | Valor |
|---|---|
| `compileSdk` | 36 |
| `minSdk` | 26 (Android 8.0) |
| `targetSdk` | 36 |
| Compose BOM | 2024.09.00 |
| Base de datos Room | versión 3 |

---

## Base de datos

### Entidad `Habit`

```kotlin
@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String = "",
    val themeColorHex: String = "#4CAF50",
    val createdAt: Long = System.currentTimeMillis(),
    val iconKey: String = "target"
)
```

### Entidad `HabitLog`

```kotlin
@Entity(
    tableName = "habit_logs",
    foreignKeys = [ForeignKey(
        entity = Habit::class,
        parentColumns = ["id"],
        childColumns = ["habitId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["habitId", "date"], unique = true)]
)
data class HabitLog(
    @PrimaryKey(autoGenerate = true) val logId: Int = 0,
    val habitId: Int,
    val date: Long   // timestamp UTC truncado a medianoche
)
```

El índice único `(habitId, date)` garantiza que un hábito solo pueda marcarse una vez por día. La clave foránea con `CASCADE` elimina automáticamente los logs cuando se elimina el hábito padre.

### Queries principales (DAO)

```kotlin
getAllHabits(): Flow<List<Habit>>
getLogsForHabit(habitId: Int): Flow<List<HabitLog>>
getLogsByDate(date: Long): Flow<List<HabitLog>>
insertHabit(habit: Habit)
insertLog(log: HabitLog)
deleteLog(log: HabitLog)
deleteHabitById(habitId: Int)
deleteLogsForHabit(habitId: Int)
```

### Cálculo de racha

```kotlin
fun calculateStreak(logs: List<HabitLog>): Int {
    // Ordena fechas descendentemente
    // Compara cada fecha con el día esperado (hoy, ayer, anteayer...)
    // Corta al primer día faltante
}
```

---

## Navegación

```
home
 ├──► add_habit          (navController.navigate("add_habit"))
 └──► detail/{habitId}   (navController.navigate("detail/$habitId"))
          └──► home       (navController.popBackStack())
```

### Rutas

| Ruta | Pantalla | Parámetros |
|---|---|---|
| `home` | `HomeScreen` | — |
| `add_habit` | `AddHabitScreen` | — |
| `detail/{habitId}` | `HabitDetailScreen` | `habitId: Int` |

El `habitId` se pasa como argumento de tipo `NavType.IntType` y se extrae con `backStackEntry.arguments!!.getInt("habitId")`.

---

## Instalación y configuración

### Requisitos

- Android Studio Hedgehog o superior
- JDK 11
- Android SDK 36

### Pasos

```bash
# 1. Clonar el repositorio
git clone https://github.com/tu-usuario/habitflow.git

# 2. Abrir en Android Studio
# File > Open > seleccionar carpeta del proyecto

# 3. Sincronizar Gradle
# Android Studio mostrará "Sync Now" automáticamente

# 4. Ejecutar
# Run > Run 'app' o Shift+F10
```

### Dependencias clave en `app/build.gradle`

```kotlin
dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.material3)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.datastore.preferences)
    implementation(libs.navigation.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
}
```

> **Nota:** No agregar `material-icons-extended` — es incompatible con el BOM `2024.09.00`. Los íconos usados pertenecen al conjunto core de Material Icons.

---

## Migraciones de base de datos

| Versión | Cambio |
|---|---|
| 1 → 2 | Creación inicial de tablas `habits` y `habit_logs` |
| 2 → 3 | Adición de columna `iconKey TEXT NOT NULL DEFAULT 'target'` a tabla `habits` |

La migración 2→3 está definida en `DatabaseModule.kt`:

```kotlin
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE habits ADD COLUMN iconKey TEXT NOT NULL DEFAULT 'target'")
    }
}
```

---

## Íconos disponibles

| `iconKey` | Ícono Material |
|---|---|
| `target` | DateRange |
| `running` | PlayArrow |
| `water` | Refresh |
| `book` | Info |
| `meditation` | Favorite |
| `food` | Menu |
| `sleep` | Home |
| `music` | Notifications |
| `code` | Build |
| `gym` | ThumbUp |

---

## Color principal

```kotlin
val AppTeal = Color(0xFF00C9A7)
```

Los 12 colores disponibles para hábitos:

`#F44336` `#E91E63` `#9C27B0` `#3F51B5` `#2196F3` `#00BCD4` `#00C9A7` `#4CAF50` `#8BC34A` `#FFC107` `#FF9800` `#795548`
