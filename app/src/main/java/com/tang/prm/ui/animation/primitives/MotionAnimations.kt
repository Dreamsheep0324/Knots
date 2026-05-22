package com.tang.prm.ui.animation.primitives

import androidx.compose.animation.core.RepeatMode
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.animation.core.rememberPausableInfiniteFloatLoop
import kotlinx.coroutines.delay

enum class RotationDirection {
    Clockwise,
    CounterClockwise
}

@Composable
fun rememberFloatingOffset(
    range: Dp = 4.dp,
    cycleDuration: Int = AnimationTokens.Cycle.ambient
): State<Dp> {
    val floatValue = rememberPausableInfiniteFloatLoop(
        initialValue = -1f,
        targetValue = 1f,
        durationMillis = cycleDuration,
        easing = AnimationTokens.Easing.emphasis,
        repeatMode = RepeatMode.Reverse,
        label = "FloatingOffset"
    )
    return produceState(
        initialValue = 0.dp,
        key1 = floatValue.value
    ) {
        value = (floatValue.value * range.value).dp
    }
}

@Composable
fun rememberContinuousRotation(
    speed: Float = 1f,
    cycleDuration: Int = AnimationTokens.Cycle.slow,
    direction: RotationDirection = RotationDirection.Clockwise
): State<Float> {
    val multiplier = if (direction == RotationDirection.Clockwise) 1f else -1f
    return produceState(initialValue = 0f) {
        val degreesPerMs = 360f * speed / cycleDuration
        var lastTime = System.currentTimeMillis()
        var accumulated = 0f
        while (true) {
            val now = System.currentTimeMillis()
            val delta = now - lastTime
            lastTime = now
            accumulated = (accumulated + degreesPerMs * delta * multiplier) % 360f
            value = accumulated
            delay(16)
        }
    }
}

@Composable
fun rememberScanLineOffset(
    cycleDuration: Int = AnimationTokens.Cycle.slow
): State<Float> {
    return rememberPausableInfiniteFloatLoop(
        initialValue = 0f,
        targetValue = 1f,
        durationMillis = cycleDuration,
        easing = AnimationTokens.Easing.linear,
        repeatMode = RepeatMode.Restart,
        label = "ScanLineOffset"
    )
}

@Composable
fun rememberShimmerPhase(
    cycleDuration: Int = AnimationTokens.Cycle.ambient
): State<Float> {
    return rememberPausableInfiniteFloatLoop(
        initialValue = 0f,
        targetValue = 1f,
        durationMillis = cycleDuration,
        easing = AnimationTokens.Easing.linear,
        repeatMode = RepeatMode.Restart,
        label = "ShimmerPhase"
    )
}
