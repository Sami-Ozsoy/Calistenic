package com.example.calistenic.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.calistenic.ui.screens.LevelEditorScreen
import com.example.calistenic.ui.screens.LevelSessionScreen
import com.example.calistenic.ui.screens.SeviyelerScreen
import com.example.calistenic.ui.screens.WorkoutHistoryScreen

import androidx.compose.ui.res.stringResource
import com.example.calistenic.R

private sealed class Screen(val route: String, val labelRes: Int, val icon: ImageVector) {
    object Seviyeler : Screen("seviyeler", R.string.tab_seviyeler, Icons.Filled.FitnessCenter)
    object History : Screen("history", R.string.tab_history, Icons.Filled.History)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalistenicApp(
    workoutViewModel: WorkoutViewModel,
    timerViewModel: TimerViewModel,
    levelsViewModel: LevelsViewModel
) {
    val navController = rememberNavController()
    val screens = listOf(Screen.Seviyeler, Screen.History)

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.app_name)) })
        },
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                screens.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(stringResource(screen.labelRes)) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Seviyeler.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Seviyeler.route) {
                SeviyelerScreen(
                    levelsViewModel = levelsViewModel,
                    onNavigateToEditor = {
                        navController.navigate("seviyeler/editor")
                    },
                    onNavigateToEditorWithId = { levelId ->
                        navController.navigate("seviyeler/editor/$levelId")
                    },
                    onNavigateToSession = {
                        navController.navigate("seviyeler/session")
                    }
                )
            }
            composable("seviyeler/editor") {
                LevelEditorScreen(
                    levelsViewModel = levelsViewModel,
                    levelId = null,
                    onDone = { navController.popBackStack() }
                )
            }
            composable(
                route = "seviyeler/editor/{levelId}",
                arguments = listOf(navArgument("levelId") { type = NavType.IntType })
            ) { backStackEntry ->
                val levelId = backStackEntry.arguments?.getInt("levelId") ?: return@composable
                LevelEditorScreen(
                    levelsViewModel = levelsViewModel,
                    levelId = levelId,
                    onDone = { navController.popBackStack() }
                )
            }
            composable("seviyeler/session") {
                LevelSessionScreen(
                    levelsViewModel = levelsViewModel,
                    timerViewModel = timerViewModel,
                    onDone = { navController.popBackStack() },
                    onNavigateToHistory = {
                        navController.navigate(Screen.History.route) {
                            popUpTo(Screen.Seviyeler.route) { inclusive = false }
                        }
                    }
                )
            }
            composable(Screen.History.route) {
                WorkoutHistoryScreen(
                    workoutViewModel = workoutViewModel,
                    levelsViewModel = levelsViewModel
                )
            }
        }
    }
}

