package com.example.calistenic.ui

import android.app.Application
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.calistenic.TimerService
import com.example.calistenic.TimerStateHolder
import com.example.calistenic.TimerUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * Timer durumunu TimerStateHolder'dan okur ve komutları TimerService'e iletir.
 * Service foreground'da çalıştığı için timer arka planda da çalışır.
 * setDuration service başlatmadan state'i günceller (kullanıcı süre girerken).
 */
class TimerViewModel(application: Application) : AndroidViewModel(application) {

    val uiState: StateFlow<TimerUiState> = TimerStateHolder.state.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TimerStateHolder.state.value
    )

    private fun serviceIntent(action: String, duration: Int? = null): Intent =
        Intent(getApplication(), TimerService::class.java).apply {
            this.action = action
            duration?.let { putExtra(TimerService.EXTRA_DURATION_SECONDS, it) }
        }

    private fun startForeground(action: String, duration: Int? = null) {
        val intent = serviceIntent(action, duration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getApplication<Application>().startForegroundService(intent)
        } else {
            getApplication<Application>().startService(intent)
        }
    }

    private fun send(action: String, duration: Int? = null) {
        getApplication<Application>().startService(serviceIntent(action, duration))
    }

    fun setDuration(seconds: Int) {
        if (TimerStateHolder.state.value.isRunning) return
        // Service'i başlatmadan direkt state'i güncelle
        TimerStateHolder.set(
            TimerUiState(
                selectedDurationSeconds = seconds.coerceAtLeast(1),
                remainingSeconds = seconds.coerceAtLeast(1)
            )
        )
    }

    fun start(seconds: Int = uiState.value.selectedDurationSeconds) {
        val safeSeconds = seconds.coerceAtLeast(1)
        startForeground(TimerService.ACTION_START, safeSeconds)
    }

    fun pause() {
        if (!uiState.value.isRunning) return
        send(TimerService.ACTION_PAUSE)
    }

    fun resume() {
        if (!uiState.value.isPaused || uiState.value.remainingSeconds <= 0) return
        send(TimerService.ACTION_RESUME)
    }

    fun reset() {
        send(TimerService.ACTION_RESET)
    }

    fun dismissFinished() {
        send(TimerService.ACTION_STOP_ALARM)
        send(TimerService.ACTION_STOP)
    }
}