package com.example.calistenic

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TimerService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var timerJob: Job? = null
    private var mediaPlayer: MediaPlayer? = null

    private val vibrator: Vibrator
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(VibratorManager::class.java)).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val seconds = intent.getIntExtra(EXTRA_DURATION_SECONDS, 25).coerceAtLeast(1)
                startCountdown(seconds)
            }
            ACTION_SET_DURATION -> {
                val seconds = intent.getIntExtra(EXTRA_DURATION_SECONDS, 25).coerceAtLeast(1)
                setDurationOnly(seconds)
            }
            ACTION_PAUSE -> pause()
            ACTION_RESUME -> resume()
            ACTION_RESET -> reset()
            ACTION_STOP_ALARM -> stopAlarm()
            ACTION_STOP -> {
                timerJob?.cancel()
                stopAlarm()
                TimerStateHolder.set(TimerUiState())
                ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
        }
        return START_NOT_STICKY
    }

    private fun setDurationOnly(seconds: Int) {
        if (TimerStateHolder.state.value.isRunning) return
        timerJob?.cancel()
        stopAlarm()
        TimerStateHolder.set(
            TimerUiState(
                selectedDurationSeconds = seconds,
                remainingSeconds = seconds
            )
        )
    }

    private fun startCountdown(seconds: Int) {
        timerJob?.cancel()
        TimerStateHolder.set(
            TimerUiState(
                selectedDurationSeconds = seconds,
                remainingSeconds = seconds,
                isRunning = true
            )
        )
        startForegroundWithNotification()
        runCountdown()
    }

    private fun runCountdown() {
        timerJob = scope.launch {
            while (TimerStateHolder.state.value.remainingSeconds > 0 &&
                TimerStateHolder.state.value.isRunning) {
                delay(1_000)
                val next = (TimerStateHolder.state.value.remainingSeconds - 1).coerceAtLeast(0)
                TimerStateHolder.update { it.copy(remainingSeconds = next) }
                updateNotification()
            }
            if (TimerStateHolder.state.value.remainingSeconds == 0 &&
                TimerStateHolder.state.value.isRunning) {
                TimerStateHolder.update { it.copy(isRunning = false, isFinished = true) }
                updateNotification(finished = true)
                startAlarm()
            }
        }
    }

    private fun pause() {
        if (!TimerStateHolder.state.value.isRunning) return
        timerJob?.cancel()
        TimerStateHolder.update { it.copy(isRunning = false, isPaused = true) }
        updateNotification()
    }

    private fun resume() {
        if (!TimerStateHolder.state.value.isPaused ||
            TimerStateHolder.state.value.remainingSeconds <= 0) return
        TimerStateHolder.update { it.copy(isRunning = true, isPaused = false) }
        updateNotification()
        runCountdown()
    }

    private fun reset() {
        timerJob?.cancel()
        stopAlarm()
        val duration = TimerStateHolder.state.value.selectedDurationSeconds
        TimerStateHolder.set(
            TimerUiState(
                selectedDurationSeconds = duration,
                remainingSeconds = duration
            )
        )
        updateNotification()
    }

    private fun startAlarm() {
        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ?: return

        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            setDataSource(this@TimerService, alarmUri)
            isLooping = true
            prepare()
            start()
        }
        vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 300, 500), 0))
    }

    private fun stopAlarm() {
        mediaPlayer?.run {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null
        vibrator.cancel()
    }

    private fun startForegroundWithNotification() {
        ensureChannel(this)
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            buildNotification(),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            } else 0
        )
    }

    private fun updateNotification(finished: Boolean = false) {
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(NOTIFICATION_ID, buildNotification(finished))
    }

    private fun buildNotification(finished: Boolean = false): Notification {
        val mainIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pi = PendingIntent.getActivity(
            this, 0, mainIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val title = when {
            finished -> "Dinlenme bitti!"
            TimerStateHolder.state.value.isFinished -> "Dinlenme bitti!"
            TimerStateHolder.state.value.isPaused -> "Dinlenme duraklatıldı"
            TimerStateHolder.state.value.isRunning -> "Dinlenme devam ediyor"
            else -> "Dinlenme sayacı"
        }
        val content = if (finished || TimerStateHolder.state.value.isFinished) {
            "Alarmı kapatmak için dokun"
        } else {
            "Kalan süre: ${formatSeconds(TimerStateHolder.state.value.remainingSeconds)}"
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(content)
            .setContentIntent(pi)
            .setOngoing(
                TimerStateHolder.state.value.isRunning ||
                TimerStateHolder.state.value.isPaused ||
                TimerStateHolder.state.value.isFinished
            )
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
        scope.cancel()
        stopAlarm()
    }

    companion object {
        private const val CHANNEL_ID = "calistenic_timer_channel"
        private const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.example.calistenic.timer.START"
        const val ACTION_SET_DURATION = "com.example.calistenic.timer.SET_DURATION"
        const val ACTION_PAUSE = "com.example.calistenic.timer.PAUSE"
        const val ACTION_RESUME = "com.example.calistenic.timer.RESUME"
        const val ACTION_RESET = "com.example.calistenic.timer.RESET"
        const val ACTION_STOP_ALARM = "com.example.calistenic.timer.STOP_ALARM"
        const val ACTION_STOP = "com.example.calistenic.timer.STOP"
        const val EXTRA_DURATION_SECONDS = "duration_seconds"

        fun ensureChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val nm = context.getSystemService(NotificationManager::class.java)
                if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                    nm.createNotificationChannel(
                        NotificationChannel(
                            CHANNEL_ID,
                            "Dinlenme Sayacı",
                            NotificationManager.IMPORTANCE_HIGH
                        ).apply {
                            description = "Dinlenme sayacı bildirimleri ve alarm"
                            setSound(null, null)
                            enableVibration(false)
                        }
                    )
                }
            }
        }

        private fun formatSeconds(totalSeconds: Int): String {
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%02d:%02d".format(minutes, seconds)
        }
    }
}