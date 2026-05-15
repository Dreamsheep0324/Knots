package com.tang.prm.ui.animation.primitives

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.tang.prm.ui.animation.core.AnimationTokens

@Composable
fun Modifier.staggeredAppear(
    index: Int,
    staggerMs: Int = 30,
    durationMs: Int = AnimationTokens.Duration.fast
): Modifier {
    var visible by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay((index * staggerMs).toLong())
        visible = 1
    }
    val alpha by animateFloatAsState(
        targetValue = if (visible == 1) 1f else 0f,
        animationSpec = tween(durationMs, easing = AnimationTokens.Easing.enter),
        label = "stagger_alpha_$index"
    )
    val offsetY by animateFloatAsState(
        targetValue = if (visible == 1) 0f else 12f,
        animationSpec = tween(durationMs, easing = AnimationTokens.Easing.enter),
        label = "stagger_offset_$index"
    )
    return this.graphicsLayer {
        this.alpha = alpha
        this.translationY = offsetY
    }
}
