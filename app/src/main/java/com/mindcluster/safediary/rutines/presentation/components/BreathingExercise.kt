package com.mindcluster.safediary.rutines.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mindcluster.safediary.shared.presentation.components.*
import com.mindcluster.safediary.shared.presentation.theme.*
import kotlinx.coroutines.delay
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle

@Composable
fun BreathingExercise(id: String, minutes: Int, isActive: Boolean, onComplete: () -> Unit, onExit: () -> Unit) {
    val total = minutes * 60
    var remaining by rememberSaveable(id) { mutableIntStateOf(total) }
    var running by rememberSaveable(id) { mutableStateOf(true) }
    val currentOnComplete by rememberUpdatedState(onComplete)
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(running, isActive, lifecycle) {
        if (running && isActive) {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                while (remaining > 0) { delay(1000); remaining-- }
                currentOnComplete()
            }
        }
    }
    val phase = (total - remaining) % 14
    val instruction = when { phase < 4 -> "Inhala"; phase < 8 -> "Haz una pausa"; else -> "Exhala" }
    PatientHeading("Respiración profunda", "Sigue un ritmo que te resulte cómodo.")
    PatientCard {
        Column(Modifier.fillMaxWidth().padding(vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Box(Modifier.size(200.dp).background(PatientMint, CircleShape), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(progress = { 1f - remaining.toFloat() / total }, modifier = Modifier.size(180.dp), color = SecondaryTeal, strokeWidth = 6.dp, trackColor = SurfaceContainerLow)
                Text(if (running) instruction else "En pausa", style = MaterialTheme.typography.headlineSmall, color = SecondaryTeal)
            }
            Text("%02d:%02d".format(remaining / 60, remaining % 60), style = MaterialTheme.typography.displayLarge, color = PrimaryNavy)
            Text("Inhala 4 s · pausa 4 s · exhala 6 s", color = SlateSubtle, textAlign = TextAlign.Center)
            PatientInfo("Puedes pausar o salir cuando lo necesites.")
            PatientButton(if (running) "Pausar" else "Continuar", { running = !running }, Modifier.fillMaxWidth())
        }
    }
    PatientButton("Salir del ejercicio", onExit, Modifier.fillMaxWidth(), secondary = true)
}
