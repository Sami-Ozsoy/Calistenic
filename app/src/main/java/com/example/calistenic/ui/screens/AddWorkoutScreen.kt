package com.example.calistenic.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.unit.dp
import com.example.calistenic.R
import com.example.calistenic.ui.TimerViewModel
import com.example.calistenic.ui.WorkoutViewModel
import com.example.calistenic.ui.components.NumberField
import com.example.calistenic.ui.components.SectionHeader
import com.example.calistenic.ui.components.TimerSection
import com.example.calistenic.ui.components.WorkoutDateSelector
import kotlinx.coroutines.launch
import java.time.Instant
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
    val workouts by workoutViewModel.workouts.collectAsState()
    var exerciseName by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    val setValues = remember(settings.defaultReps) {
        mutableStateListOf<String>().apply {
            repeat(6) { add(settings.defaultReps.toString()) }
        }
    }

    var restBetweenSets by remember(settings.restBetweenSetsSeconds) {
        mutableStateOf(settings.restBetweenSetsSeconds.toString())
    }
    var restBetweenExercises by remember(settings.restBetweenExercisesSeconds) {
        mutableStateOf(settings.restBetweenExercisesSeconds.toString())
    }
    var completedSets by remember { mutableIntStateOf(0) }
    var suggestedRestSeconds by remember { mutableIntStateOf(0) }
    var showRestSuggestion by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val totalReps = setValues.sumOf { it.toIntOrNull() ?: 0 }
    val activeSet = (completedSets + 1).coerceAtMost(setValues.size)
    val selectedDateWorkoutCount = workouts.count {
        Instant.ofEpochMilli(it.createdAt)
            .atZone(ZoneId.systemDefault())
            .toLocalDate() == selectedDate
    }

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
                WorkoutDateSelector(
                    selectedDate = selectedDate,
                    workoutCount = selectedDateWorkoutCount,
                    onDateSelected = { selectedDate = it }
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

            itemsIndexed(setValues) { index, value ->
                SetCard(
                    setNumber = index + 1,
                    value = value,
                    isCompleted = index < completedSets,
                    isActive = index == completedSets,
                    onValueChange = { setValues[index] = it }
                )
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
                TotalRepsCard(totalReps = totalReps)
            }

            item {
                SectionHeader(title = "Dinlenme Süreleri")
            }

            item {
                NumberField(
                    value = restBetweenSets,
                    onValueChange = { restBetweenSets = it },
                    label = stringResource(R.string.rest_between_sets_label),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                NumberField(
                    value = restBetweenExercises,
                    onValueChange = { restBetweenExercises = it },
                    label = stringResource(R.string.rest_between_exercises_label),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Button(
                    onClick = {
                        if (completedSets < setValues.size) {
                            completedSets++
                            suggestedRestSeconds = if (completedSets == setValues.size) {
                                restBetweenExercises.toIntOrNull()?.coerceAtLeast(1) ?: 120
                            } else {
                                restBetweenSets.toIntOrNull()?.coerceAtLeast(1) ?: 25
                            }
                            showRestSuggestion = true
                        }
                    },
                    enabled = completedSets < setValues.size,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (completedSets < setValues.size) {
                            stringResource(R.string.set_completed_format, completedSets + 1)
                        } else {
                            stringResource(R.string.all_sets_completed_label)
                        }
                    )
                }
            }

            if (completedSets > 0) {
                item {
                    OutlinedButton(
                        onClick = { completedSets = 0 },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.reset_set_tracking))
                    }
                }
            }

            item {
                SectionHeader(title = "Dinlenme Sayacı")
            }

            item {
                TimerSection(
                    timerViewModel = timerViewModel,
                    defaultDurationSeconds = restBetweenSets.toIntOrNull()?.coerceAtLeast(1) ?: 25
                )
            }

            item {
                Button(
                    onClick = {
                        val sets = setValues.map { it.toIntOrNull() ?: 0 }
                        val setRest = restBetweenSets.toIntOrNull()
                        val exerciseRest = restBetweenExercises.toIntOrNull()

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
                            setRest == null || setRest <= 0 ||
                                exerciseRest == null || exerciseRest <= 0 -> {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Dinlenme süreleri 0'dan büyük olmalı")
                                }
                            }
                            else -> {
                                val createdAt = selectedDate
                                    .atTime(LocalTime.now())
                                    .atZone(ZoneId.systemDefault())
                                    .toInstant()
                                    .toEpochMilli()

                                workoutViewModel.saveWorkout(
                                    exerciseName = exerciseName,
                                    sets = sets,
                                    restBetweenSetsSeconds = setRest,
                                    restBetweenExercisesSeconds = exerciseRest,
                                    createdAt = createdAt
                                ) {
                                    exerciseName = ""
                                    completedSets = 0
                                    setValues.clear()
                                    repeat(6) { setValues.add(settings.defaultReps.toString()) }
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            "Antrenman seçilen güne kaydedildi"
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
private fun TotalRepsCard(totalReps: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(R.string.total_reps),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Bu antrenman için",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = totalReps.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
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
    onValueChange: (String) -> Unit
) {
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
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.elevatedCardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = if (isActive) 4.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Set numarası badge
            Card(
                shape = RoundedCornerShape(12.dp),
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
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(R.string.set_label_format, setNumber) +
                        if (isCompleted) " " + stringResource(R.string.done_check) else "",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                NumberField(
                    value = value,
                    onValueChange = onValueChange,
                    label = stringResource(R.string.reps_label),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}