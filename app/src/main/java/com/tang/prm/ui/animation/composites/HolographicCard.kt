package com.tang.prm.ui.animation.composites

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.animation.core.rememberPausableInfiniteFloatLoop
import com.tang.prm.ui.animation.primitives.rememberScanLineOffset
import com.tang.prm.ui.animation.primitives.rememberShimmerPhase

data class HolographicConfig(
    val enableFlip: Boolean = true,
    val enableScanLine: Boolean = true,
    val enableShimmer: Boolean = true,
    val enableFloat: Boolean = true,
    val enableWaveform: Boolean = false,
    val enablePulse: Boolean = true,
    val scanLineColor: Color = Color.Unspecified,
    val shimmerColor: Color = Color.Unspecified,
    val borderColor: Color = Color.Unspecified,
    val borderAlpha: Float = 0.5f,
    val borderWidth: Dp = 2.dp,
    val cornerRadius: Dp = 2.dp,
    val cardWidth: Dp = 340.dp,
    val cardHeight: Dp = 476.dp,
    val shadowElevation: Dp = 20.dp,
    val floatRange: Dp = 2.dp,
    val floatDuration: Int = 2000,
    val scanDuration: Int = 3000,
    val shimmerDuration: Int = 2500,
    val pulseMinAlpha: Float = 0.2f,
    val pulseMaxAlpha: Float = 0.6f,
    val pulseDuration: Int = 1500,
    val flipDuration: Int = 600
) {
    companion object {
        val default = HolographicConfig()
    }
}

@Composable
fun HolographicCardOverlay(
    isFlipped: Boolean,
    onFlip: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    config: HolographicConfig = HolographicConfig.default,
    frontContent: @Composable () -> Unit,
    backContent: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    var dismissRequested by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val bgAlpha by animateFloatAsState(
        targetValue = if (visible && !dismissRequested) 0.55f else 0f,
        animationSpec = tween(400, easing = AnimationTokens.Easing.standard),
        label = "holo_bg"
    )
    val cardScale by animateFloatAsState(
        targetValue = if (visible && !dismissRequested) 1f else 0.75f,
        animationSpec = spring(
            dampingRatio = 0.75f,
            stiffness = 350f
        ),
        label = "holo_scale"
    )
    val cardAlpha by animateFloatAsState(
        targetValue = if (visible && !dismissRequested) 1f else 0f,
        animationSpec = if (visible && !dismissRequested) {
            tween(500, delayMillis = 80, easing = AnimationTokens.Easing.standard)
        } else {
            tween(250, easing = AnimationTokens.Easing.exit)
        },
        finishedListener = {
            if (dismissRequested && it == 0f) onClose()
        },
        label = "holo_alpha"
    )
    val cardOffsetY by animateFloatAsState(
        targetValue = if (visible && !dismissRequested) 0f else 120f,
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = 300f
        ),
        label = "holo_offset_y"
    )
    val cardRotation by animateFloatAsState(
        targetValue = if (visible && !dismissRequested) 0f else -8f,
        animationSpec = spring(
            dampingRatio = 0.75f,
            stiffness = 250f
        ),
        label = "holo_rotation"
    )
    val rotationY by animateFloatAsState(
        targetValue = if (isFlipped && config.enableFlip) 180f else 0f,
        animationSpec = tween(config.flipDuration, easing = AnimationTokens.Easing.standard),
        label = "holo_flip"
    )

    val showFront = rotationY < 90f

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = bgAlpha))
                .clickable {
                    dismissRequested = true
                    visible = false
                }
        )

        Column(
            modifier = Modifier
                .width(config.cardWidth)
                .graphicsLayer {
                    this.scaleX = cardScale
                    this.scaleY = cardScale
                    this.alpha = cardAlpha
                    this.translationY = cardOffsetY
                    this.rotationZ = cardRotation
                    this.rotationY = rotationY
                    cameraDistance = 16f * density
                    shadowElevation = if (cardAlpha > 0.01f) 24f * cardScale else 0f
                    shape = RoundedCornerShape(config.cornerRadius)
                    clip = true
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (showFront) {
                HolographicCardContent(
                    config = config,
                    onFlip = onFlip,
                    content = frontContent
                )
            } else {
                Box(modifier = Modifier.graphicsLayer { scaleX = -1f }) {
                    HolographicCardContent(
                        config = config,
                        onFlip = {},
                        content = backContent
                    )
                }
            }
        }
    }
}

@Composable
private fun HolographicCardContent(
    config: HolographicConfig,
    onFlip: () -> Unit,
    content: @Composable () -> Unit
) {
    val floatOffset by rememberPausableInfiniteFloatLoop(
        initialValue = if (config.enableFloat) -config.floatRange.value else 0f,
        targetValue = if (config.enableFloat) config.floatRange.value else 0f,
        durationMillis = if (config.enableFloat) config.floatDuration else Int.MAX_VALUE,
        easing = AnimationTokens.Easing.emphasis,
        repeatMode = RepeatMode.Reverse,
        label = "holo_float"
    )

    val _pulseAlpha by rememberPausableInfiniteFloatLoop(
        initialValue = if (config.enablePulse) config.pulseMinAlpha else 1f,
        targetValue = if (config.enablePulse) config.pulseMaxAlpha else 1f,
        durationMillis = if (config.enablePulse) config.pulseDuration else Int.MAX_VALUE,
        easing = AnimationTokens.Easing.linear,
        repeatMode = RepeatMode.Reverse,
        label = "holo_pulse"
    )

    val scanLineOffset by rememberPausableInfiniteFloatLoop(
        initialValue = 0f,
        targetValue = if (config.enableScanLine) 1f else 0f,
        durationMillis = if (config.enableScanLine) config.scanDuration else Int.MAX_VALUE,
        easing = AnimationTokens.Easing.linear,
        repeatMode = RepeatMode.Restart,
        label = "holo_scan"
    )

    val shimmerOffset by rememberPausableInfiniteFloatLoop(
        initialValue = if (config.enableShimmer) -1f else 0f,
        targetValue = if (config.enableShimmer) 2f else 0f,
        durationMillis = if (config.enableShimmer) config.shimmerDuration else Int.MAX_VALUE,
        easing = AnimationTokens.Easing.linear,
        repeatMode = RepeatMode.Restart,
        label = "holo_shimmer"
    )

    Box(
        modifier = Modifier
            .width(config.cardWidth)
            .height(config.cardHeight)
            .graphicsLayer { translationY = floatOffset },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .width(config.cardWidth)
                .height(config.cardHeight)
                .clickable { onFlip() },
            shape = RoundedCornerShape(config.cornerRadius),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(config.borderWidth, config.borderColor.copy(alpha = config.borderAlpha)),
            shadowElevation = config.shadowElevation
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                content()

                if (config.enableScanLine) {
                    HolographicScanLineOverlay(
                        offset = scanLineOffset,
                        color = config.scanLineColor
                    )
                }

                if (config.enableShimmer) {
                    HolographicShimmerOverlay(
                        offset = shimmerOffset,
                        color = config.shimmerColor
                    )
                }
            }
        }
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
private fun HolographicScanLineOverlay(
    offset: Float,
    color: Color
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = 0.3f + offset * 0.4f
            }
    )
}

@Suppress("UNUSED_PARAMETER")
@Composable
private fun HolographicShimmerOverlay(
    offset: Float,
    color: Color
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = if (offset in 0f..1f) offset * 0.15f else 0f
            }
    )
}
