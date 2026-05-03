package com.app.habitflow.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.app.habitflow.ui.screens.AddHabitScreen
import com.app.habitflow.ui.screens.HomeScreen
import com.app.habitflow.ui.screens.HabitDetailScreen
import com.app.habitflow.ui.viewmodel.HabitViewModel

@Composable
fun HabitNavHost(
    navController: NavHostController,
    viewModel: HabitViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier
    ) {
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onAddHabitClick = { navController.navigate("add_habit") },
                onHabitClick = { habitId -> navController.navigate("detail/$habitId") }
            )
        }
        composable("add_habit") {
            AddHabitScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "detail/{habitId}",
            arguments = listOf(navArgument("habitId") { type = NavType.IntType })
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments!!.getInt("habitId")
            HabitDetailScreen(
                viewModel = viewModel,
                habitId = habitId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
