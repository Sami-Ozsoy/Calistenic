package com.example.calistenic.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calistenic.ui.TimerViewModel

@Composable
fun TimerSection(
    timerViewModel: TimerViewModel,
    defaultDurationSeconds: Int,
    modifier: Modifier = Modifier
) {
    val timerState by timerViewModel.uiState.collectAsState()
    var durationText by remember(defaultDurationSeconds) {
        mutableStateOf(defaultDurationSeconds.toString())
    }

    LaunchedEffect(defaultDurationSeconds) {
        if (!timerState.isRunning && !timerState.isPaused) {
            timerViewModel.setDuration(defaultDurationSeconds)
        }
    }

    val progress = if (timerState.selectedDurationSeconds > 0) {
        timerState.remainingSeconds.toFloat() / timerState.selectedDurationSeconds.toFloat()
    } else 1f

    val ringColor by animateColorAsState(
        targetValue = when {
            timerState.isFinished -> MaterialTheme.colorScheme.error
            timerState.isPaused -> MaterialTheme.colorScheme.tertiary
            timerState.isRunning -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.outline
        },
        label = "ringColor"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Dinlenme Sayacı",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            CircularTimerDisplay(
                remainingSeconds = timerState.remainingSeconds,
                progress = progress,
                ringColor = ringColor,
                isFinished = timerState.isFinished
            )

            NumberField(
                value = durationText,
                onValueChange = {
                    durationText = it
                    it.toIntOrNull()?.let(timerViewModel::setDuration)
                },
                label = "Sayaç süresi (saniye)",
                modifier = Modifier.fillMaxWidth()
            )

            if (timerState.isFinished) {
                Text(
                    text = "Dinlenme bitti",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = { timerViewModel.dismissFinished() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Alarmı Kapat")
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val seconds = durationText.toIntOrNull()?.coerceAtLeast(1) ?: 1
                            timerViewModel.start(seconds)
                        },
                        enabled = !timerState.isRunning,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (timerState.isPaused) "Yeniden Başlat" else "Başlat")
                    }

                    if (timerState.isRunning) {
                        OutlinedButton(
                            onClick = timerViewModel::pause,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Duraklat")
                        }
                    } else if (timerState.isPaused) {
                        OutlinedButton(
                            onClick = timerViewModel::resume,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Devam Et")
                        }
                    }
                }

                OutlinedButton(
                    onClick = timerViewModel::reset,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sıfırla")
                }
            }
        }
    }
}

@Composable
private fun CircularTimerDisplay(
    remainingSeconds: Int,
    progress: Float,
    ringColor: Color,
    isFinished: Boolean
) {
    Box(
        modifier = Modifier.size(180.dp),
        contentAlignment = Alignment.Center
    ) {
        ProgressRing(
            modifier = Modifier.size(180.dp),
            progress = progress,
            color = ringColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Text(
            text = formatSeconds(remainingSeconds),
            fontSize = 52.sp,
            fontWeight = FontWeight.Bold,
            color = if (isFinished) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun formatSeconds(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}