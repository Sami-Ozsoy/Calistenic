package com.example.calistenic.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.calistenic.R
import com.example.calistenic.ui.TimerViewModel
import com.example.calistenic.ui.WorkoutViewModel
import com.example.calistenic.ui.components.NumberField
import com.example.calistenic.ui.components.SectionHeader
import com.example.calistenic.ui.components.TimerSection
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

private val CardShape = RoundedCornerShape(18.dp)

@Composable
fun AddWorkoutScreen(
    workoutViewModel: WorkoutViewModel,
    timerViewModel: TimerViewModel,
    modifier: Modifier = Modifier
) {
    val settings by workoutViewModel.settings.collectAsState()
    var exerciseName by remember { mutableStateOf("") }

    val setValues = remember(settings.defaultReps) {
        mutableStateListOf<String>().apply {
            repeat(6) { add(settings.defaultReps.toString()) }
        }
    }

    var completedSets by remember { mutableIntStateOf(0) }
    var suggestedRestSeconds by remember { mutableIntStateOf(0) }
    var showRestSuggestion by remember { mutableStateOf(false) }
    var showTimerDialog by remember { mutableStateOf(false) }

    val timerState by timerViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val totalReps = setValues.sumOf { it.toIntOrNull() ?: 0 }
    val activeSet = (completedSets + 1).coerceAtMost(setValues.size)

    if (showRestSuggestion) {
        val isExerciseRest = completedSets == setValues.size
        AlertDialog(
            onDismissRequest = { showRestSuggestion = false },
            title = {
                Text(if (isExerciseRest) "Hareket tamamlandı" else "Set tamamlandı")
            },
            text = {
                Text(
                    if (isExerciseRest) {
                        "$suggestedRestSeconds saniyelik hareketler arası dinlenme başlatılsın mı?"
                    } else {
                        "$suggestedRestSeconds saniyelik setler arası dinlenme başlatılsın mı?"
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        timerViewModel.start(suggestedRestSeconds)
                        showRestSuggestion = false
                        showTimerDialog = true
                    }
                ) {
                    Text("Başlat")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestSuggestion = false }) {
                    Text("Şimdi Değil")
                }
            }
        )
    }

    if (showTimerDialog) {
        Dialog(
            onDismissRequest = { showTimerDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.padding(24.dp)
            ) {
                TimerSection(
                    timerViewModel = timerViewModel,
                    defaultDurationSeconds = settings.restBetweenSetsSeconds.coerceAtLeast(1),
                    modifier = Modifier.padding(top = 20.dp)
                )
                androidx.compose.material3.IconButton(
                    onClick = { showTimerDialog = false },
                    modifier = Modifier.align(androidx.compose.ui.Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.close)
                    )
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 32.dp)
        ) {
            item {
                SectionHeader(
                    title = stringResource(R.string.new_workout),
                    subtitle = "Tarih, hareket ve setlerini gir, sonra kaydet."
                )
            }

            item {
                OutlinedTextField(
                    value = exerciseName,
                    onValueChange = { exerciseName = it },
                    label = { Text(stringResource(R.string.exercise_name_label)) },
                    placeholder = { Text(stringResource(R.string.exercise_name_placeholder)) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                ActiveSetBanner(
                    activeSet = activeSet,
                    totalSets = setValues.size,
                    completed = completedSets
                )
            }

            val setRows = setValues.chunked(2)
            itemsIndexed(setRows) { rowIndex, row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SetCard(
                        setNumber = rowIndex * 2 + 1,
                        value = row[0],
                        isCompleted = rowIndex * 2 < completedSets,
                        isActive = rowIndex * 2 == completedSets,
                        onValueChange = { setValues[rowIndex * 2] = it },
                        modifier = Modifier.weight(1f)
                    )
                    if (row.size > 1) {
                        SetCard(
                            setNumber = rowIndex * 2 + 2,
                            value = row[1],
                            isCompleted = rowIndex * 2 + 1 < completedSets,
                            isActive = rowIndex * 2 + 1 == completedSets,
                            onValueChange = { setValues[rowIndex * 2 + 1] = it },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { setValues.add(settings.defaultReps.toString()) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.add_set))
                    }
                    if (setValues.size > 1) {
                        OutlinedButton(
                            onClick = {
                                setValues.removeAt(setValues.size - 1)
                                if (completedSets > setValues.size) {
                                    completedSets = setValues.size
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.remove_set))
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.total_reps),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = totalReps.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            if (completedSets < setValues.size) {
                                completedSets++
                                suggestedRestSeconds = if (completedSets == setValues.size) {
                                    settings.restBetweenExercisesSeconds.coerceAtLeast(1)
                                } else {
                                    settings.restBetweenSetsSeconds.coerceAtLeast(1)
                                }
                                showRestSuggestion = true
                            }
                        },
                        enabled = completedSets < setValues.size,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            if (completedSets < setValues.size) {
                                stringResource(R.string.set_completed_format, completedSets + 1)
                            } else {
                                stringResource(R.string.all_sets_completed_label)
                            }
                        )
                    }
                    if (completedSets > 0) {
                        IconButton(onClick = { completedSets = 0 }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = stringResource(R.string.reset_set_tracking)
                            )
                        }
                    }
                }
            }

            item {
                TimerStatusRow(
                    timerState = timerState,
                    onClick = { showTimerDialog = true }
                )
            }

            item {
                Button(
                    onClick = {
                        val sets = setValues.map { it.toIntOrNull() ?: 0 }

                        when {
                            exerciseName.isBlank() -> {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Hareket adı boş bırakılamaz")
                                }
                            }
                            setValues.any { it.isBlank() } -> {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Tüm set tekrarlarını girin")
                                }
                            }
                            else -> {
                                val createdAt = LocalDate.now()
                                    .atTime(LocalTime.now())
                                    .atZone(ZoneId.systemDefault())
                                    .toInstant()
                                    .toEpochMilli()

                                workoutViewModel.saveWorkout(
                                    exerciseName = exerciseName,
                                    sets = sets,
                                    restBetweenSetsSeconds = settings.restBetweenSetsSeconds,
                                    restBetweenExercisesSeconds = settings.restBetweenExercisesSeconds,
                                    createdAt = createdAt
                                ) {
                                    exerciseName = ""
                                    completedSets = 0
                                    setValues.clear()
                                    repeat(6) { setValues.add(settings.defaultReps.toString()) }
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            "Antrenman kaydedildi"
                                        )
                                    }
                                }
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.save_workout), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun ActiveSetBanner(activeSet: Int, totalSets: Int, completed: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (completed < totalSets) {
                        "Aktif Set: $activeSet/$totalSets"
                    } else {
                        stringResource(R.string.all_sets_completed)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$completed / $totalSets set tamamlandı",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = "$completed/$totalSets",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun TimerStatusRow(timerState: com.example.calistenic.TimerUiState, onClick: () -> Unit) {
    val minutes = timerState.remainingSeconds / 60
    val seconds = timerState.remainingSeconds % 60
    val statusText = when {
        timerState.isFinished -> "Dinlenme bitti"
        timerState.isRunning -> "Çalışıyor · %02d:%02d".format(minutes, seconds)
        timerState.isPaused -> "Duraklatıldı · %02d:%02d".format(minutes, seconds)
        else -> "Sayacı başlatmak için dokun"
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = CardShape,
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (timerState.isRunning || timerState.isPaused) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null
            )
        }
    }
}

@Composable
private fun SetCard(
    setNumber: Int,
    value: String,
    isCompleted: Boolean,
    isActive: Boolean,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isManualEntry by remember { mutableStateOf(false) }
    val containerColor = when {
        isCompleted -> MaterialTheme.colorScheme.primaryContainer
        isActive -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surface
    }
    val contentColor = when {
        isCompleted -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }
    Card(
        modifier = modifier,
        shape = CardShape,
        colors = CardDefaults.elevatedCardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = if (isActive) 4.dp else 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Set numarası badge (Tamamlandıysa ✓)
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = if (isCompleted) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                    },
                    contentColor = if (isCompleted) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSecondary
                    }
                )
            ) {
                Text(
                    text = if (isCompleted) "✓" else setNumber.toString(),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            if (isManualEntry) {
                NumberField(
                    value = value,
                    onValueChange = onValueChange,
                    label = stringResource(R.string.reps_label),
                    modifier = Modifier.fillMaxWidth()
                )
                TextButton(
                    onClick = { isManualEntry = false },
                    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = stringResource(R.string.use_stepper),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            } else {
                RepsStepper(value = value, onValueChange = onValueChange)
                TextButton(
                    onClick = { isManualEntry = true },
                    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = stringResource(R.string.manual_entry),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun RepsStepper(
    value: String,
    onValueChange: (String) -> Unit
) {
    val current = value.toIntOrNull() ?: 0
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = { onValueChange((current - 1).coerceAtLeast(0).toString()) },
            enabled = current > 0,
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.size(36.dp)
        ) {
            Text("−", style = MaterialTheme.typography.titleMedium)
        }
        Text(
            text = current.toString(),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        OutlinedButton(
            onClick = { onValueChange((current + 1).toString()) },
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.size(36.dp)
        ) {
            Text("+", style = MaterialTheme.typography.titleMedium)
        }
    }
}