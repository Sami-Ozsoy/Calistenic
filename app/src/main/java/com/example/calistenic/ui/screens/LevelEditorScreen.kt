package com.example.calistenic.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.calistenic.R
import com.example.calistenic.data.local.LevelExerciseEntity
import com.example.calistenic.ui.LevelsViewModel
import com.example.calistenic.ui.components.NumberField
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

private data class ExerciseDraft(
    val id: Int = 0,
    val exerciseName: String = "",
    val setCount: Int = 6,
    val restBetweenSetsSeconds: Int = 25,
    val restAfterSeconds: Int = 25,
    val imagePath: String? = null
)

@Composable
fun LevelEditorScreen(
    levelsViewModel: LevelsViewModel,
    levelId: Int?,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    var levelName by remember { mutableStateOf("") }
    val exercises = remember { mutableStateListOf<ExerciseDraft>() }
    var loaded by remember { mutableStateOf(levelId == null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Edit modunda mevcut seviye + hareketleri yükle
    LaunchedEffect(levelId) {
        if (levelId != null) {
            val pair = levelsViewModel.getLevelWithExercises(levelId)
            if (pair != null) {
                val (level, existingExercises) = pair
                levelName = level.name
                exercises.clear()
                exercises.addAll(existingExercises.map {
                    ExerciseDraft(
                        id = it.id,
                        exerciseName = it.exerciseName,
                        setCount = it.setCount,
                        restBetweenSetsSeconds = it.restBetweenSetsSeconds,
                        restAfterSeconds = it.restAfterSeconds,
                        imagePath = it.imagePath
                    )
                })
            }
        }
        loaded = true
    }

    fun save() {
        when {
            levelName.isBlank() -> {
                scope.launch { snackbarHostState.showSnackbar("Seviye adı boş bırakılamaz") }
            }
            exercises.isEmpty() -> {
                scope.launch { snackbarHostState.showSnackbar("En az bir hareket eklemelisin") }
            }
            exercises.any { it.exerciseName.isBlank() } -> {
                scope.launch { snackbarHostState.showSnackbar("Tüm hareketlerin adını gir") }
            }
            else -> {
                val entities = exercises.map {
                    LevelExerciseEntity(
                        id = if (levelId != null) it.id else 0,
                        levelId = levelId ?: 0,
                        orderIndex = 0,
                        exerciseName = it.exerciseName.trim(),
                        setCount = it.setCount,
                        restBetweenSetsSeconds = it.restBetweenSetsSeconds,
                        restAfterSeconds = it.restAfterSeconds,
                        imagePath = it.imagePath
                    )
                }
                levelsViewModel.saveLevel(levelId, levelName.trim(), entities) { onDone() }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (!loaded) {
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                OutlinedTextField(
                    value = levelName,
                    onValueChange = { levelName = it },
                    label = { Text(stringResource(R.string.level_name_label)) },
                    placeholder = { Text(stringResource(R.string.level_name_placeholder)) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
            ) {
                itemsIndexed(exercises) { index, draft ->
                    ExerciseEditorCard(
                        draft = draft,
                        index = index,
                        totalCount = exercises.size,
                        onNameChange = { exercises[index] = draft.copy(exerciseName = it) },
                        onSetCountChange = { exercises[index] = draft.copy(setCount = it) },
                        onRestBetweenSetsChange = { exercises[index] = draft.copy(restBetweenSetsSeconds = it) },
                        onRestAfterChange = { exercises[index] = draft.copy(restAfterSeconds = it) },
                        onImagePathChange = { exercises[index] = draft.copy(imagePath = it) },
                        onDelete = { exercises.removeAt(index) },
                        onMoveUp = {
                            if (index > 0) {
                                val tmp = exercises[index - 1]
                                exercises[index - 1] = exercises[index]
                                exercises[index] = tmp
                            }
                        },
                        onMoveDown = {
                            if (index < exercises.size - 1) {
                                val tmp = exercises[index + 1]
                                exercises[index + 1] = exercises[index]
                                exercises[index] = tmp
                            }
                        }
                    )
                }
                item {
                    OutlinedButton(
                        onClick = { exercises.add(ExerciseDraft()) },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.add_exercise))
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Button(
                    onClick = { save() },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.save_level), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun ExerciseEditorCard(
    draft: ExerciseDraft,
    index: Int,
    totalCount: Int,
    onNameChange: (String) -> Unit,
    onSetCountChange: (Int) -> Unit,
    onRestBetweenSetsChange: (Int) -> Unit,
    onRestAfterChange: (Int) -> Unit,
    onImagePathChange: (String?) -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hareket ${index + 1}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row {
                    IconButton(onClick = onMoveUp, enabled = index > 0) {
                        Icon(Icons.Filled.ArrowUpward, contentDescription = "Yukarı")
                    }
                    IconButton(onClick = onMoveDown, enabled = index < totalCount - 1) {
                        Icon(Icons.Filled.ArrowDownward, contentDescription = "Aşağı")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Sil",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            OutlinedTextField(
                value = draft.exerciseName,
                onValueChange = onNameChange,
                label = { Text(stringResource(R.string.exercise_name_label)) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Egzersiz görseli — galeri veya kamera ile ekle
            ExerciseImagePicker(
                imagePath = draft.imagePath,
                onImagePathChange = onImagePathChange
            )

            // Set sayısı stepper
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.exercise_set_count),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                StepperButtons(
                    value = draft.setCount,
                    onValueChange = onSetCountChange,
                    min = 1
                )
            }

            // Set arası dinlenme
            NumberField(
                value = draft.restBetweenSetsSeconds.toString(),
                onValueChange = { onRestBetweenSetsChange(it.toIntOrNull()?.coerceAtLeast(0) ?: 0) },
                label = stringResource(R.string.rest_between_sets_short),
                modifier = Modifier.fillMaxWidth()
            )

            // Hareket sonrası dinlenme + preset'ler
            NumberField(
                value = draft.restAfterSeconds.toString(),
                onValueChange = { onRestAfterChange(it.toIntOrNull()?.coerceAtLeast(0) ?: 0) },
                label = stringResource(R.string.rest_after_exercise),
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = { onRestAfterChange(25) },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    Text(stringResource(R.string.preset_rest_short), style = MaterialTheme.typography.labelSmall)
                }
                OutlinedButton(
                    onClick = { onRestAfterChange(180) },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    Text(stringResource(R.string.preset_rest_long), style = MaterialTheme.typography.labelSmall)
                }
                OutlinedButton(
                    onClick = { onRestAfterChange(0) },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    Text(stringResource(R.string.preset_rest_none), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun StepperButtons(
    value: Int,
    onValueChange: (Int) -> Unit,
    min: Int = 0
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = { onValueChange((value - 1).coerceAtLeast(min)) },
            enabled = value > min,
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.size(36.dp)
        ) {
            Text("−", style = MaterialTheme.typography.titleMedium)
        }
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        OutlinedButton(
            onClick = { onValueChange(value + 1) },
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.size(36.dp)
        ) {
            Text("+", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun ExerciseImagePicker(
    imagePath: String?,
    onImagePathChange: (String?) -> Unit
) {
    val context = LocalContext.current
    // Kameradan çekilen geçici dosya URI'si — launcher tetiklenmeden önce set edilir.
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Galeri picker launcher'ı
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val destFile = File(context.filesDir, "exercise_images/${UUID.randomUUID()}.jpg")
            destFile.parentFile?.mkdirs()
            context.contentResolver.openInputStream(uri)?.use { input ->
                destFile.outputStream().use { output -> input.copyTo(output) }
            }
            onImagePathChange(destFile.absolutePath)
        }
    }

    // Kamera launcher'ı — success = true ise pendingCameraUri'deki dosya yazılmış demektir
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = pendingCameraUri
        if (success && uri != null) {
            val srcFile = File(uri.path ?: return@rememberLauncherForActivityResult)
            val destFile = File(context.filesDir, "exercise_images/${UUID.randomUUID()}.jpg")
            destFile.parentFile?.mkdirs()
            srcFile.renameTo(destFile)
            onImagePathChange(destFile.absolutePath)
        } else {
            // İptal edildiyse temp dosyayı sil
            pendingCameraUri?.path?.let { File(it).delete() }
        }
        pendingCameraUri = null
    }

    // Kamera izni iste — kullanıcı kamera butonuna basınca kontrol edilir, gerekirse iste
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchCamera(context, pendingCameraUri, onUriSet = { pendingCameraUri = it }, cameraLauncher)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Preview — imagePath varsa göster
        if (imagePath != null) {
            AsyncImage(
                model = File(imagePath),
                contentDescription = "Egzersiz görseli",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedButton(
                onClick = {
                    galleryLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
            ) {
                Text("Galeri", style = MaterialTheme.typography.labelSmall)
            }
            OutlinedButton(
                onClick = {
                    val granted = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                    if (granted) {
                        launchCamera(context, pendingCameraUri, onUriSet = { pendingCameraUri = it }, cameraLauncher)
                    } else {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
            ) {
                Text("Kamera", style = MaterialTheme.typography.labelSmall)
            }
            if (imagePath != null) {
                OutlinedButton(
                    onClick = { onImagePathChange(null) },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Text("Kaldır", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

private fun launchCamera(
    context: android.content.Context,
    currentPendingUri: Uri?,
    onUriSet: (Uri) -> Unit,
    cameraLauncher: androidx.activity.result.ActivityResultLauncher<Uri>
) {
    val tmpFile = File(context.filesDir, "camera_tmp/${UUID.randomUUID()}.jpg")
    tmpFile.parentFile?.mkdirs()
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        tmpFile
    )
    onUriSet(uri)
    cameraLauncher.launch(uri)
}