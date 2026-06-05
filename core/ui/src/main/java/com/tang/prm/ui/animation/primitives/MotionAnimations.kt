package com.tang.prm.ui.animation.primitives

import androidx.compose.animation.core.RepeatMode
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.animation.core.rememberPausableInfiniteFloatLoop
import kotlinx.coroutines.delay

enum class RotationDirection {
    Clockwise,
    CounterClockwise
}

/**
 * Combined rotation state for orbital calendar — single coroutine drives all angles.
 * Reduces recompositions from N×60fps to 1×60fps.
 */
data class OrbitalRotationState(
    val scanAngle: Float = 0f,
    val rotateAngle: Float = 0f,
    val particleAngle: Float = 0f,
    val crosshairAngle: Float = 0f
)

@Composable
fun rememberOrbitalRotations(
    scanCycleDuration: Int = 10000,
    rotateCycleDuration: Int = 60000,
    particleCycleDuration: Int = 90000,
    crosshairCycleDuration: Int = 120000
): State<OrbitalRotationState> {
    val speeds = remember {
        floatArrayOf(
            360f / scanCycleDuration,
            360f / rotateCycleDuration,
            360f / particleCycleDuration,
            360f / crosshairCycleDuration
        )
    }
    return produceState(initialValue = OrbitalRotationState()) {
        var lastTime = System.currentTimeMillis()
        var scan = 0f; var rotate = 0f; var particle = 0f; var crosshair = 0f
        while (true) {
            val now = System.currentTimeMillis()
            val delta = now - lastTime
            lastTime = now
            scan = (scan + speeds[0] * delta) % 360f
            rotate = (rotate + speeds[1] * delta) % 360f
            particle = (particle + speeds[2] * delta) % 360f
            crosshair = (crosshair + speeds[3] * delta) % 360f
            value = OrbitalRotationState(scan, rotate, particle, crosshair)
            delay(16)
        }
    }
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
