package com.example.calistenic

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TimerUiState(
    val selectedDurationSeconds: Int = 25,
    val remainingSeconds: Int = 25,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val isFinished: Boolean = false
)

/**
 * Process geneli timer durumu. TimerService yazarken okur,
 * TimerViewModel hem okur hem komut gönderir. Service çalışmıyorken
 * bile duration ayarlanabilir.
 */
object TimerStateHolder {
    private val _state = MutableStateFlow(TimerUiState())
    val state: StateFlow<TimerUiState> = _state.asStateFlow()

    fun set(state: TimerUiState) {
        _state.value = state
    }

    fun update(transform: (TimerUiState) -> TimerUiState) {
        _state.value = transform(_state.value)
    }
}