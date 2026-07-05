package com.example.calistenic.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.calistenic.data.local.LevelSessionEntity
import com.example.calistenic.data.local.LevelSessionExerciseEntity
import com.example.calistenic.data.local.WorkoutEntity
import com.example.calistenic.ui.LevelsViewModel
import com.example.calistenic.ui.WorkoutViewModel
import com.example.calistenic.ui.components.SectionHeader
import com.example.calistenic.ui.components.formatWorkoutDate
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val CardShape = RoundedCornerShape(18.dp)

private sealed interface HistoryItem {
    val date: LocalDate
    val timestamp: Long

    data class Single(
        val workout: WorkoutEntity,
        override val date: LocalDate,
        override val timestamp: Long
    ) : HistoryItem

    data class LevelSession(
        val session: LevelSessionEntity,
        val exercises: List<LevelSessionExerciseEntity>,
        override val date: LocalDate,
        override val timestamp: Long
    ) : HistoryItem
}

@Composable
fun WorkoutHistoryScreen(
    workoutViewModel: WorkoutViewModel,
    levelsViewModel: LevelsViewModel,
    modifier: Modifier = Modifier
) {
    val workouts by workoutViewModel.workouts.collectAsState()
    val completedSessions by levelsViewModel.completedSessions.collectAsState()
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var expandedDates by remember { mutableStateOf(emptySet<LocalDate>()) }
    var expandedSessions by remember { mutableStateOf(emptySet<Int>()) }
    val today = remember { LocalDate.now() }

    // Seviye session'larını exercises ile zenginleştir
    val sessionExercisesMap = remember { mutableStateMapOf<Int, List<LevelSessionExerciseEntity>>() }
    LaunchedEffect(completedSessions) {
        completedSessions.forEach { session ->
            if (session.id !in sessionExercisesMap) {
                val pair = levelsViewModel.getSessionWithExercises(session.id)
                if (pair != null) {
                    sessionExercisesMap[session.id] = pair.second
                }
            }
        }
    }
    val sessionItems = remember(completedSessions, sessionExercisesMap.toMap()) {
        completedSessions.map { session ->
            val date = Instant.ofEpochMilli(session.completedAt ?: session.startedAt)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            HistoryItem.LevelSession(
                session = session,
                exercises = sessionExercisesMap[session.id] ?: emptyList(),
                date = date,
                timestamp = session.completedAt ?: session.startedAt
            )
        }
    }

    // Workout ve seviye session'larını tarihe göre birleştir
    val itemsByDate = remember(workouts, sessionItems) {
        val all: List<HistoryItem> = workouts.map { w ->
            val date = Instant.ofEpochMilli(w.createdAt)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            HistoryItem.Single(w, date, w.createdAt)
        } + sessionItems

        all.groupBy { it.date }.toSortedMap(compareByDescending { it })
    }

    // Bir gün içindeki toplam antrenman + toplam tekrar
    fun dayStats(items: List<HistoryItem>): Pair<Int, Int> {
        var count = 0
        var reps = 0
        items.forEach { item ->
            when (item) {
                is HistoryItem.Single -> {
                    count++
                    reps += item.workout.totalReps
                }
                is HistoryItem.LevelSession -> {
                    count++
                    reps += item.session.totalReps
                }
            }
        }
        return count to reps
    }

    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text("Tüm kayıtlar silinsin mi?") },
            text = { Text("Bu işlem geri alınamaz.") },
            confirmButton = {
                Button(
                    onClick = {
                        workoutViewModel.deleteAllWorkouts()
                        showDeleteAllDialog = false
                    }
                ) {
                    Text("Tümünü Sil")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
                    Text("Vazgeç")
                }
            }
        )
    }

    val totalItems = workouts.size + completedSessions.size

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 32.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader(
                    title = "Antrenman Geçmişi",
                    subtitle = if (totalItems == 0) null else "$totalItems kayıt"
                )
                if (totalItems > 0) {
                    TextButton(onClick = { showDeleteAllDialog = true }) {
                        Text("Tümünü Sil")
                    }
                }
            }
        }

        if (totalItems == 0) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CardShape,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Henüz antrenman yok",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "İlk antrenmanını eklemek için Ekle sekmesine geç.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            itemsByDate.forEach { (date, dayItems) ->
                val (count, reps) = dayStats(dayItems)
                item(key = "date-$date") {
                    WorkoutDayHeader(
                        date = date,
                        today = today,
                        workoutCount = count,
                        totalReps = reps,
                        isExpanded = date in expandedDates,
                        onClick = {
                            expandedDates = if (date in expandedDates) {
                                expandedDates - date
                            } else {
                                expandedDates + date
                            }
                        }
                    )
                }

                if (date in expandedDates) {
                    items(
                        items = dayItems.sortedBy { it.timestamp },
                        key = { item ->
                            when (item) {
                                is HistoryItem.Single -> "w-${item.workout.id}"
                                is HistoryItem.LevelSession -> "s-${item.session.id}"
                            }
                        }
                    ) { item ->
                        when (item) {
                            is HistoryItem.Single -> WorkoutHistoryCard(
                                workout = item.workout,
                                onDelete = { workoutViewModel.deleteWorkout(item.workout) }
                            )
                            is HistoryItem.LevelSession -> LevelSessionHistoryCard(
                                session = item.session,
                                exercises = item.exercises,
                                isExpanded = item.session.id in expandedSessions,
                                onToggle = {
                                    expandedSessions = if (item.session.id in expandedSessions) {
                                        expandedSessions - item.session.id
                                    } else {
                                        expandedSessions + item.session.id
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutDayHeader(
    date: LocalDate,
    today: LocalDate,
    workoutCount: Int,
    totalReps: Int,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                val dateText = remember(date, today) {
                    if (date == today) "Bugün · ${formatWorkoutDate(date)}"
                    else formatWorkoutDate(date)
                }
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$workoutCount antrenman · $totalReps tekrar",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
            Icon(
                imageVector = if (isExpanded) Icons.Filled.ExpandLess
                else Icons.Filled.ExpandMore,
                contentDescription = if (isExpanded) "Kapat" else "Aç",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun WorkoutHistoryCard(
    workout: WorkoutEntity,
    onDelete: () -> Unit
) {
    val formattedTime = remember(workout.createdAt) { formatTime(workout.createdAt) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = workout.exerciseName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                workout.sets.forEach { reps ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = reps.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Toplam",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${workout.totalReps} tekrar",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Set arası dinlenme",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${workout.restBetweenSetsSeconds} sn",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Hareket arası dinlenme",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${workout.restBetweenExercisesSeconds} sn",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            OutlinedButton(
                onClick = onDelete,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text("Kaydı Sil")
            }
        }
    }
}

@Composable
private fun LevelSessionHistoryCard(
    session: LevelSessionEntity,
    exercises: List<LevelSessionExerciseEntity>,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val formattedTime = remember(session.startedAt) { formatTime(session.startedAt) }

    Card(
        onClick = onToggle,
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = session.levelName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "$formattedTime · ${exercises.size} hareket · ${session.totalReps} tekrar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.ExpandLess
                    else Icons.Filled.ExpandMore,
                    contentDescription = if (isExpanded) "Kapat" else "Aç",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isExpanded && exercises.isNotEmpty()) {
                exercises.forEach { exercise ->
                    ExerciseHistoryRow(exercise = exercise)
                }
            } else if (isExpanded && exercises.isEmpty()) {
                Text(
                    text = "Hareket detayları yüklenemedi",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ExerciseHistoryRow(exercise: LevelSessionExerciseEntity) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = exercise.exerciseName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${exercise.sets.sum()} tekrar",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(vertical = 2.dp)
        ) {
            exercise.sets.forEach { reps ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = reps.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    return Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .format(formatter)
}