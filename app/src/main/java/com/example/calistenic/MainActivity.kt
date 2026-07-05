package com.example.calistenic

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calistenic.ui.CalistenicApp
import com.example.calistenic.ui.LevelsViewModel
import com.example.calistenic.ui.SplashScreen
import com.example.calistenic.ui.TimerViewModel
import com.example.calistenic.ui.WorkoutViewModel
import com.example.calistenic.ui.theme.CalistenicTheme

class MainActivity : ComponentActivity() {
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* sonuç önemli değil; kullanıcı reddetse service yine çalışır ama bildirim görünmez */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Modern açılış ekranı — içerik hazırlanana kadar logo gösterilir.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        maybeRequestNotificationPermission()

        val application = application as CalistenicApplication

        setContent {
            CalistenicTheme {
                var showSplash by remember { mutableStateOf(true) }

                if (showSplash) {
                    SplashScreen(onFinished = { showSplash = false })
                } else {
                    val workoutViewModel: WorkoutViewModel = viewModel(
                        factory = WorkoutViewModel.Factory(
                            workoutRepository = application.workoutRepository,
                            settingsRepository = application.settingsRepository
                        )
                    )
                    val timerViewModel: TimerViewModel = viewModel()
                    val levelsViewModel: LevelsViewModel = viewModel(
                        factory = LevelsViewModel.Factory(
                            levelsRepository = application.levelsRepository,
                            levelSessionRepository = application.levelSessionRepository
                        )
                    )

                    CalistenicApp(
                        workoutViewModel = workoutViewModel,
                        timerViewModel = timerViewModel,
                        levelsViewModel = levelsViewModel
                    )
                }
            }
        }
    }

    private fun maybeRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}