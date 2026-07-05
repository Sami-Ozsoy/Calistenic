package com.example.calistenic.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.calistenic.R
import com.example.calistenic.TimerUiState
import com.example.calistenic.data.local.LevelSessionExerciseEntity
import com.example.calistenic.ui.LevelsViewModel
import com.example.calistenic.ui.TimerViewModel
import kotlin.math.ceil

@Composable
fun LevelSessionScreen(
    levelsViewModel: LevelsViewModel,
    timerViewModel: TimerViewModel,
    onDone: () -> Unit,
    onNavigateToHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeSession by levelsViewModel.activeSession.collectAsState()
    val timerState by timerViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showCancelDialog by remember { mutableStateOf(false) }
    var stepperValue by remember { mutableIntStateOf(6) }

    // Aktif session yoksa
    if (activeSession == null) {
        Box(
            modifier = modifier.fillMaxSize().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.session_no_active),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(onClick = onDone) { Text("Geri") }
            }
        }
        return
    }

    val sessionId = activeSession!!.id
    val sessionExercises by levelsViewModel.getExercisesForSessionFlow(sessionId)
        .collectAsState(initial = emptyList())

    // Mevcut pozisyonu türet: sets.size < setCount olan ilk exercise
    val currentExerciseIndex = sessionExercises.indexOfFirst { it.sets.size < it.setCount }
    val isComplete = currentExerciseIndex == -1 && sessionExercises.isNotEmpty()
    val currentExercise = sessionExercises.getOrNull(currentExerciseIndex)
    val currentSetIndex = currentExercise?.sets?.size ?: 0

    // Session tamamlandıysa → complete + history'ye git
    LaunchedEffect(isComplete) {
        if (isComplete) {
            levelsViewModel.completeSession(sessionId) {
                onNavigateToHistory()
            }
        }
    }

    // Stale finished timer varsa temizle (resume sonrası)
    LaunchedEffect(sessionId) {
        if (timerState.isFinished) {
            timerViewModel.dismissFinished()
        }
    }

    showCancelDialog.let {
        if (it) {
            AlertDialog(
                onDismissRequest = { showCancelDialog = false },
                title = { Text(stringResource(R.string.session_cancel)) },
                text = { Text(stringResource(R.string.session_cancel_confirm)) },
                confirmButton = {
                    TextButton(onClick = {
                        levelsViewModel.cancelSession(sessionId) {
                            timerViewModel.dismissFinished()
                            onDone()
                        }
                    }) { Text("İptal Et") }
                },
                dismissButton = {
                    TextButton(onClick = { showCancelDialog = false }) { Text("Vazgeç") }
                }
            )
        }
    }

    val isResting = timerState.isRunning || timerState.isPaused || timerState.isFinished

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Breadcrumb
            if (currentExercise != null) {
                BreadcrumbRow(
                    levelName = activeSession!!.levelName,
                    exerciseIndex = currentExerciseIndex + 1,
                    exerciseTotal = sessionExercises.size,
                    setIndex = currentSetIndex + 1,
                    setTotal = currentExercise.setCount
                )
            }

            // Mevcut set kartı (rest sırasında da görünür ama disabled)
            if (currentExercise != null) {
                CurrentSetCard(
                    exerciseName = currentExercise.exerciseName,
                    setIndex = currentSetIndex + 1,
                    setTotal = currentExercise.setCount,
                    stepperValue = stepperValue,
                    onStepperValueChange = { stepperValue = it },
                    onSetCompleted = {
                        // 1. Rep'i kaydet
                        levelsViewModel.appendRep(currentExercise, stepperValue)
                        // 2. Dinlenmeyi belirle
                        val isLastSet = currentSetIndex >= currentExercise.setCount - 1
                        val restSeconds = if (isLastSet) {
                            currentExercise.restAfterSeconds
                        } else {
                            currentExercise.restBetweenSetsSeconds
                        }
                        // 3. restAfterSeconds == 0 → session complete (LaunchedEffect isComplete yakalar)
                        //    Aksi → timer başlat
                        if (restSeconds > 0) {
                            timerViewModel.start(restSeconds)
                        }
                    },
                    enabled = !isResting
                )
            }

            // Sıradaki hareket önizlemesi
            if (currentExerciseIndex >= 0 && currentExerciseIndex + 1 < sessionExercises.size) {
                val nextEx = sessionExercises[currentExerciseIndex + 1]
                NextExercisePreview(
                    exerciseName = nextEx.exerciseName,
                    isGroupRest = currentExercise?.restAfterSeconds?.let { it >= 60 } == true
                )
            }

            // Timer kartı (rest sırasında)
            if (isResting) {
                RestTimerCard(
                    timerState = timerState,
                    onPause = { timerViewModel.pause() },
                    onResume = { timerViewModel.resume() },
                    onSkip = { timerViewModel.dismissFinished() }
                )
            }

            // İptal butonu
            OutlinedButton(
                onClick = { showCancelDialog = true },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.session_cancel))
            }
        }
    }
}

@Composable
private fun BreadcrumbRow(
    levelName: String,
    exerciseIndex: Int,
    exerciseTotal: Int,
    setIndex: Int,
    setTotal: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = levelName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Hareket $exerciseIndex/$exerciseTotal · Set $setIndex/$setTotal",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun CurrentSetCard(
    exerciseName: String,
    setIndex: Int,
    setTotal: Int,
    stepperValue: Int,
    onStepperValueChange: (Int) -> Unit,
    onSetCompleted: () -> Unit,
    enabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = exerciseName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Set $setIndex/$setTotal",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            // Stepper
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { onStepperValueChange((stepperValue - 1).coerceAtLeast(0)) },
                    enabled = enabled && stepperValue > 0,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(48.dp)
                ) {
                    Text("−", style = MaterialTheme.typography.headlineSmall)
                }
                Text(
                    text = stepperValue.toString(),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                OutlinedButton(
                    onClick = { onStepperValueChange(stepperValue + 1) },
                    enabled = enabled,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(48.dp)
                ) {
                    Text("+", style = MaterialTheme.typography.headlineSmall)
                }
            }
            Button(
                onClick = onSetCompleted,
                enabled = enabled,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.session_set_completed), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun NextExercisePreview(exerciseName: String, isGroupRest: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.session_next_exercise) + ":",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = exerciseName + if (isGroupRest) " (uzun dinlenme öncesi)" else "",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun RestTimerCard(
    timerState: TimerUiState,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onSkip: () -> Unit
) {
    val minutes = timerState.remainingSeconds / 60
    val seconds = timerState.remainingSeconds % 60
    val statusText = when {
        timerState.isFinished -> stringResource(R.string.session_rest_finished)
        timerState.isRunning -> "%02d:%02d".format(minutes, seconds)
        timerState.isPaused -> "Duraklatıldı · %02d:%02d".format(minutes, seconds)
        else -> "Hazır"
    }
    val containerColor = when {
        timerState.isFinished -> MaterialTheme.colorScheme.errorContainer
        timerState.isRunning -> MaterialTheme.colorScheme.primaryContainer
        timerState.isPaused -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.session_rest_label),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = statusText,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (timerState.isRunning) {
                    OutlinedButton(
                        onClick = onPause,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Duraklat") }
                }
                if (timerState.isPaused) {
                    OutlinedButton(
                        onClick = onResume,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Devam") }
                }
                if (timerState.isFinished) {
                    Button(
                        onClick = onSkip,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Devam Et")
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.padding(start = 4.dp))
                    }
                } else {
                    OutlinedButton(
                        onClick = onSkip,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Atla") }
                }
            }
        }
    }
}