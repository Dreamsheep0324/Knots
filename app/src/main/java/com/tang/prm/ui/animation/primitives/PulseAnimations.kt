package com.tang.prm.ui.animation.primitives

import androidx.compose.animation.core.RepeatMode
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.animation.core.rememberPausableInfiniteFloatLoop

@Composable
fun rememberBreathingPulse(
    minAlpha: Float = AnimationTokens.Alpha.dim,
    maxAlpha: Float = AnimationTokens.Alpha.full,
    cycleDuration: Int = AnimationTokens.Cycle.slow,
    easing: androidx.compose.animation.core.Easing = AnimationTokens.Easing.emphasis
): State<Float> {
    return rememberPausableInfiniteFloatLoop(
        initialValue = minAlpha,
        targetValue = maxAlpha,
        durationMillis = cycleDuration,
        easing = easing,
        repeatMode = RepeatMode.Reverse,
        label = "BreathingPulse"
    )
}

@Composable
fun rememberBlinkingAlpha(
    onDuration: Int = 530,
    offDuration: Int = 530
): State<Float> {
    val totalCycle = onDuration + offDuration
    val phase = rememberPausableInfiniteFloatLoop(
        initialValue = 0f,
        targetValue = totalCycle.toFloat(),
        durationMillis = totalCycle,
        easing = AnimationTokens.Easing.linear,
        repeatMode = RepeatMode.Restart,
        label = "BlinkingAlpha"
    )
    return produceState(
        initialValue = 1f,
        key1 = phase.value
    ) {
        val currentPhase = phase.value % totalCycle
        value = if (currentPhase < onDuration) 1f else 0f
    }
}

@Composable
fun rememberFadePulse(
    cycleDuration: Int = AnimationTokens.Cycle.normal
): State<Float> {
    return rememberPausableInfiniteFloatLoop(
        initialValue = 0f,
        targetValue = 1f,
        durationMillis = cycleDuration,
        easing = AnimationTokens.Easing.emphasis,
        repeatMode = RepeatMode.Reverse,
        label = "FadePulse"
    )
}
