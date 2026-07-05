package com.example.calistenic.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.calistenic.ui.screens.AddWorkoutScreen
import com.example.calistenic.ui.screens.SettingsScreen
import com.example.calistenic.ui.screens.WorkoutHistoryScreen

import androidx.compose.ui.res.stringResource
import com.example.calistenic.R

private sealed class Screen(val route: String, val labelRes: Int, val icon: ImageVector) {
    object Add : Screen("add", R.string.tab_add, Icons.Filled.Add)
    object History : Screen("history", R.string.tab_history, Icons.Filled.History)
    object Settings : Screen("settings", R.string.tab_settings, Icons.Filled.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalistenicApp(
    workoutViewModel: WorkoutViewModel,
    timerViewModel: TimerViewModel
) {
    val navController = rememberNavController()
    val screens = listOf(Screen.Add, Screen.History, Screen.Settings)

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
            startDestination = Screen.Add.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Add.route) {
                AddWorkoutScreen(
                    workoutViewModel = workoutViewModel,
                    timerViewModel = timerViewModel
                )
            }
            composable(Screen.History.route) {
                WorkoutHistoryScreen(
                    workoutViewModel = workoutViewModel
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    workoutViewModel = workoutViewModel
                )
            }
        }
    }
}

