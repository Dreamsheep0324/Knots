package com.tang.prm.ui.animation.composites

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
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

private const val ANIM_BG_ALPHA_VISIBLE = 0.55f
private const val ANIM_CARD_SCALE_HIDDEN = 0.75f
private const val ANIM_CARD_OFFSET_Y_HIDDEN = 120f
private const val ANIM_CARD_ROTATION_HIDDEN = -8f
private const val SPRING_SCALE_DAMPING = 0.75f
private const val SPRING_SCALE_STIFFNESS = 350f
private const val SPRING_OFFSET_DAMPING = 0.8f
private const val SPRING_OFFSET_STIFFNESS = 300f
private const val SPRING_ROTATION_DAMPING = 0.75f
private const val SPRING_ROTATION_STIFFNESS = 250f
private const val TWEEN_ALPHA_DURATION = 500
private const val TWEEN_ALPHA_DELAY = 80
private const val TWEEN_SCALE_DURATION = 250
private const val CAMERA_DISTANCE_FACTOR = 16f
private const val SHADOW_ELEVATION_FACTOR = 24f
private const val SHADOW_ALPHA_THRESHOLD = 0.01f

@Immutable
data class HolographicConfig(
    val enableFlip: Boolean = true,
    val enableScanLine: Boolean = true,
    val enableShimmer: Boolean = true,
    val enableFloat: Boolean = true,
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

@Immutable
private data class HolographicAnimState(
    val bgAlpha: State<Float>,
    val cardScale: State<Float>,
    val cardAlpha: State<Float>,
    val cardOffsetY: State<Float>,
    val cardRotation: State<Float>,
    val rotationY: State<Float>
)

@Immutable
private data class HolographicOffsets(
    val floatOffset: State<Float>,
    val scanLineOffset: State<Float>,
    val shimmerOffset: State<Float>
)

@Composable
private fun HolographicCardAnimations(
    visible: Boolean,
    dismissRequested: Boolean,
    isFlipped: Boolean,
    config: HolographicConfig,
    onClose: () -> Unit
): HolographicAnimState {
    val bgAlpha = animateFloatAsState(
        targetValue = if (visible && !dismissRequested) ANIM_BG_ALPHA_VISIBLE else 0f,
        animationSpec = tween(400, easing = AnimationTokens.Easing.standard),
        label = "holo_bg"
    )
    val cardScale = animateFloatAsState(
        targetValue = if (visible && !dismissRequested) 1f else ANIM_CARD_SCALE_HIDDEN,
        animationSpec = spring(
            dampingRatio = SPRING_SCALE_DAMPING,
            stiffness = SPRING_SCALE_STIFFNESS
        ),
        label = "holo_scale"
    )
    val cardAlpha = animateFloatAsState(
        targetValue = if (visible && !dismissRequested) 1f else 0f,
        animationSpec = if (visible && !dismissRequested) {
            tween(TWEEN_ALPHA_DURATION, delayMillis = TWEEN_ALPHA_DELAY, easing = AnimationTokens.Easing.standard)
        } else {
            tween(TWEEN_SCALE_DURATION, easing = AnimationTokens.Easing.exit)
        },
        finishedListener = {
            if (dismissRequested && it == 0f) onClose()
        },
        label = "holo_alpha"
    )
    val cardOffsetY = animateFloatAsState(
        targetValue = if (visible && !dismissRequested) 0f else ANIM_CARD_OFFSET_Y_HIDDEN,
        animationSpec = spring(
            dampingRatio = SPRING_OFFSET_DAMPING,
            stiffness = SPRING_OFFSET_STIFFNESS
        ),
        label = "holo_offset_y"
    )
    val cardRotation = animateFloatAsState(
        targetValue = if (visible && !dismissRequested) 0f else ANIM_CARD_ROTATION_HIDDEN,
        animationSpec = spring(
            dampingRatio = SPRING_ROTATION_DAMPING,
            stiffness = SPRING_ROTATION_STIFFNESS
        ),
        label = "holo_rotation"
    )
    val rotationY = animateFloatAsState(
        targetValue = if (isFlipped && config.enableFlip) 180f else 0f,
        animationSpec = tween(config.flipDuration, easing = AnimationTokens.Easing.standard),
        label = "holo_flip"
    )

    return HolographicAnimState(
        bgAlpha = bgAlpha,
        cardScale = cardScale,
        cardAlpha = cardAlpha,
        cardOffsetY = cardOffsetY,
        cardRotation = cardRotation,
        rotationY = rotationY
    )
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

    val anim = HolographicCardAnimations(
        visible = visible,
        dismissRequested = dismissRequested,
        isFlipped = isFlipped,
        config = config,
        onClose = onClose
    )
    val bgAlpha by anim.bgAlpha
    val cardScale by anim.cardScale
    val cardAlpha by anim.cardAlpha
    val cardOffsetY by anim.cardOffsetY
    val cardRotation by anim.cardRotation
    val rotationY by anim.rotationY

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
                    cameraDistance = CAMERA_DISTANCE_FACTOR * density
                    shadowElevation = if (cardAlpha > SHADOW_ALPHA_THRESHOLD) SHADOW_ELEVATION_FACTOR * cardScale else 0f
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
private fun HolographicAnimationOffsets(config: HolographicConfig): HolographicOffsets {
    val floatOffset = if (config.enableFloat) {
        rememberPausableInfiniteFloatLoop(
            initialValue = -config.floatRange.value,
            targetValue = config.floatRange.value,
            durationMillis = config.floatDuration,
            easing = AnimationTokens.Easing.emphasis,
            repeatMode = RepeatMode.Reverse,
            label = "holo_float"
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    val scanLineOffset = if (config.enableScanLine) {
        rememberPausableInfiniteFloatLoop(
            initialValue = 0f,
            targetValue = 1f,
            durationMillis = config.scanDuration,
            easing = AnimationTokens.Easing.linear,
            repeatMode = RepeatMode.Restart,
            label = "holo_scan"
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    val shimmerOffset = if (config.enableShimmer) {
        rememberPausableInfiniteFloatLoop(
            initialValue = -1f,
            targetValue = 2f,
            durationMillis = config.shimmerDuration,
            easing = AnimationTokens.Easing.linear,
            repeatMode = RepeatMode.Restart,
            label = "holo_shimmer"
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    return HolographicOffsets(
        floatOffset = floatOffset,
        scanLineOffset = scanLineOffset,
        shimmerOffset = shimmerOffset
    )
}

@Composable
private fun HolographicCardContent(
    config: HolographicConfig,
    onFlip: () -> Unit,
    content: @Composable () -> Unit
) {
    val offsets = HolographicAnimationOffsets(config)
    val floatOffset by offsets.floatOffset
    val scanLineOffset by offsets.scanLineOffset
    val shimmerOffset by offsets.shimmerOffset

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

@Composable
private fun HolographicScanLineOverlay(
    offset: Float,
    color: Color
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = 0.3f + offset * 0.4f
            }
    ) {
        val y = size.height * offset
        val lineThickness = 2.dp.toPx()
        val glowHeight = 20.dp.toPx()
        drawRect(
            color = color.copy(alpha = 0.15f),
            topLeft = androidx.compose.ui.geometry.Offset(0f, y - glowHeight / 2),
            size = androidx.compose.ui.geometry.Size(size.width, glowHeight)
        )
        drawRect(
            color = color,
            topLeft = androidx.compose.ui.geometry.Offset(0f, y - lineThickness / 2),
            size = androidx.compose.ui.geometry.Size(size.width, lineThickness)
        )
    }
}

@Composable
private fun HolographicShimmerOverlay(
    offset: Float,
    color: Color
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = if (offset in 0f..1f) offset * 0.15f else 0f
            }
    ) {
        val x = size.width * offset
        val bandWidth = 60.dp.toPx()
        drawRect(
            color = color.copy(alpha = 0.08f),
            topLeft = androidx.compose.ui.geometry.Offset(x - bandWidth / 2, 0f),
            size = androidx.compose.ui.geometry.Size(bandWidth, size.height)
        )
    }
}
