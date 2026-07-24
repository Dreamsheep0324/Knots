package com.tang.prm.ui.animation.primitives

import androidx.compose.animation.core.RepeatMode
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.animation.core.rememberIsResumed
import com.tang.prm.ui.animation.core.rememberPausableInfiniteFloatLoop

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
    val isResumed by rememberIsResumed()
    val speeds = remember {
        floatArrayOf(
            360f / scanCycleDuration,
            360f / rotateCycleDuration,
            360f / particleCycleDuration,
            360f / crosshairCycleDuration
        )
    }
    return produceState(initialValue = OrbitalRotationState(), isResumed) {
        if (!isResumed) return@produceState
        var lastTime = System.nanoTime()
        var scan = 0f; var rotate = 0f; var particle = 0f; var crosshair = 0f
        while (true) {
            withFrameNanos { frameTime ->
                val delta = (frameTime - lastTime) / 1_000_000f
                lastTime = frameTime
                scan = (scan + speeds[0] * delta) % 360f
                rotate = (rotate + speeds[1] * delta) % 360f
                particle = (particle + speeds[2] * delta) % 360f
                crosshair = (crosshair + speeds[3] * delta) % 360f
                value = OrbitalRotationState(scan, rotate, particle, crosshair)
            }
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
    // 用 derivedStateOf 替代 produceState(key1 = floatValue.value)，
    // 避免每帧重建协程；derivedStateOf 自动追踪 floatValue 变化
    return remember { derivedStateOf { (floatValue.value * range.value).dp } }
}

@Composable
fun rememberContinuousRotation(
    speed: Float = 1f,
    cycleDuration: Int = AnimationTokens.Cycle.slow,
    direction: RotationDirection = RotationDirection.Clockwise
): State<Float> {
    val isResumed by rememberIsResumed()
    val multiplier = if (direction == RotationDirection.Clockwise) 1f else -1f
    return produceState(initialValue = 0f, isResumed) {
        if (!isResumed) return@produceState
        val degreesPerMs = 360f * speed / cycleDuration
        var lastTime = System.nanoTime()
        var accumulated = 0f
        while (true) {
            withFrameNanos { frameTime ->
                val delta = (frameTime - lastTime) / 1_000_000f
                lastTime = frameTime
                accumulated = (accumulated + degreesPerMs * delta * multiplier) % 360f
                value = accumulated
            }
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
