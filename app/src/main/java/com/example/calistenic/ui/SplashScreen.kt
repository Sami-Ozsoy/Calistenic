package com.example.calistenic.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.calistenic.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Uygulama açılışında ~2 sn boyunca gösterilen selamlama ekranı.
 * Sistem SplashScreen'inin bıraktığı yerden devralır: logoya fade-in + ölçek,
 * kısa tutma, ardından fade-out ile ana ekrana geçiş.
 *
 * @param onFinished Animasyon ve tutma süresi dolunca çağrılır; çağıran tarafta
 * splash kaldırılır.
 */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.85f) }
    // Arka plan rengini sistem splash temasıyla aynı tutarak geçişte göz atlaması önlenir.
    val backgroundColor = colorResource(R.color.splash_background)

    LaunchedEffect(Unit) {
        launch { alpha.animateTo(1f, tween(durationMillis = 600, easing = FastOutSlowInEasing)) }
        launch { scale.animateTo(1f, tween(durationMillis = 600, easing = FastOutSlowInEasing)) }
        delay(800) // logoyu göstermek için tutma
        alpha.animateTo(0f, tween(durationMillis = 600))
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale.value)
                    .alpha(alpha.value)
            )
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.alpha(alpha.value)
            )
        }
    }
}