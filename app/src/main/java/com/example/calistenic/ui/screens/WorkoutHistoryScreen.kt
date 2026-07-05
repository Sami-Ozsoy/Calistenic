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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.calistenic.data.local.WorkoutEntity
import com.example.calistenic.ui.WorkoutViewModel
import com.example.calistenic.ui.components.SectionHeader
import com.example.calistenic.ui.components.formatWorkoutDate
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val CardShape = RoundedCornerShape(18.dp)

@Composable
fun WorkoutHistoryScreen(
    workoutViewModel: WorkoutViewModel,
    modifier: Modifier = Modifier
) {
    val workouts by workoutViewModel.workouts.collectAsState()
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var expandedDates by remember { mutableStateOf(emptySet<LocalDate>()) }
    val today = remember { LocalDate.now() }
    // groupBy + sıralama her recomposition'ta tekrar hesaplanmasın; yalnızca workouts değişince.
    val workoutsByDate = remember(workouts) {
        workouts
            .groupBy { workout ->
                Instant.ofEpochMilli(workout.createdAt)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            }
            .toSortedMap(compareByDescending { it })
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
                    subtitle = if (workouts.isEmpty()) null
                    else "${workouts.size} kayıt"
                )
                if (workouts.isNotEmpty()) {
                    TextButton(onClick = { showDeleteAllDialog = true }) {
                        Text("Tümünü Sil")
                    }
                }
            }
        }

        if (workouts.isEmpty()) {
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
            workoutsByDate.forEach { (date, dayWorkouts) ->
                item(key = "date-$date") {
                    WorkoutDayHeader(
                        date = date,
                        today = today,
                        workoutCount = dayWorkouts.size,
                        totalReps = dayWorkouts.sumOf { it.totalReps },
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
                        items = dayWorkouts,
                        key = { it.id }
                    ) { workout ->
                        WorkoutHistoryCard(
                            workout = workout,
                            onDelete = { workoutViewModel.deleteWorkout(workout) }
                        )
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
    // Gölge (elevation) renderı kaydırmada pahalı; tonalElevation renk tonu için yeterli.
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
    // Tarih formatlamayı her recompose'ta tekrar yapma; createdAt sabit olduğu için cache'le.
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

            // Set'ler görsel çip olarak. Gölge maliyeti olmayan Box + clip kullanılır.
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

private fun formatTime(timestamp: Long): String {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    return Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .format(formatter)
}