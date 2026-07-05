package com.example.calistenic.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.calistenic.data.repository.WorkoutSettings
import com.example.calistenic.ui.WorkoutViewModel
import com.example.calistenic.ui.components.NumberField
import com.example.calistenic.ui.components.SectionHeader
import kotlinx.coroutines.launch

private val CardShape = RoundedCornerShape(18.dp)

@Composable
fun SettingsScreen(
    workoutViewModel: WorkoutViewModel,
    modifier: Modifier = Modifier
) {
    val settings by workoutViewModel.settings.collectAsState()
    var defaultReps by remember { mutableStateOf(settings.defaultReps.toString()) }
    var setRest by remember { mutableStateOf(settings.restBetweenSetsSeconds.toString()) }
    var exerciseRest by remember {
        mutableStateOf(settings.restBetweenExercisesSeconds.toString())
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(settings) {
        defaultReps = settings.defaultReps.toString()
        setRest = settings.restBetweenSetsSeconds.toString()
        exerciseRest = settings.restBetweenExercisesSeconds.toString()
    }

    androidx.compose.material3.Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionHeader(
                title = "Ayarlar",
                subtitle = "Buradaki değerler yeni antrenmanlarda varsayılan olarak kullanılır."
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape,
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Varsayılan Değerler",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    NumberField(
                        value = defaultReps,
                        onValueChange = { defaultReps = it },
                        label = "Varsayılan tekrar sayısı",
                        modifier = Modifier.fillMaxWidth()
                    )
                    NumberField(
                        value = setRest,
                        onValueChange = { setRest = it },
                        label = "Setler arası dinlenme (saniye)",
                        modifier = Modifier.fillMaxWidth()
                    )
                    NumberField(
                        value = exerciseRest,
                        onValueChange = { exerciseRest = it },
                        label = "Hareketler arası dinlenme (saniye)",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Button(
                onClick = {
                    val reps = defaultReps.toIntOrNull()
                    val betweenSets = setRest.toIntOrNull()
                    val betweenExercises = exerciseRest.toIntOrNull()

                    if (
                        reps == null || reps <= 0 ||
                        betweenSets == null || betweenSets <= 0 ||
                        betweenExercises == null || betweenExercises <= 0
                    ) {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                "Tüm değerler 0'dan büyük olmalı"
                            )
                        }
                    } else {
                        workoutViewModel.saveSettings(
                            WorkoutSettings(
                                defaultReps = reps,
                                restBetweenSetsSeconds = betweenSets,
                                restBetweenExercisesSeconds = betweenExercises
                            )
                        ) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Ayarlar kaydedildi")
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
                Text("Ayarları Kaydet", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}