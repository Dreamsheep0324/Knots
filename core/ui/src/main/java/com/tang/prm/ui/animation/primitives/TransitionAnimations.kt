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

/**
 * 错落入场动画。
 *
 * @param index 同一组中卡片的位置索引，用于计算错落延迟（index * staggerMs）
 * @param triggerKey 触发键。当此值变化时（如 Tab 切换、数据刷新）重新播放动画。
 *                   传路由名可让每次进入界面都重新触发；传数据 id 可让滚动复用时也触发。
 *                   默认 Unit 保持向后兼容（仅首次组合触发）。
 * @param staggerMs 每个卡片之间的错落延迟毫秒数
 * @param durationMs 单个卡片的入场动画时长
 */
@Composable
fun Modifier.staggeredAppear(
    index: Int,
    triggerKey: Any? = Unit,
    staggerMs: Int = 30,
    durationMs: Int = AnimationTokens.Duration.fast
): Modifier {
    var visible by remember(triggerKey) { mutableIntStateOf(0) }
    LaunchedEffect(triggerKey) {
        kotlinx.coroutines.delay((index * staggerMs).toLong())
        visible = 1
    }
    val alpha by animateFloatAsState(
        targetValue = if (visible == 1) 1f else 0f,
        animationSpec = tween(durationMs, easing = AnimationTokens.Easing.standard),
        label = "stagger_alpha_$index"
    )
    val offsetY by animateFloatAsState(
        targetValue = if (visible == 1) 0f else 12f,
        animationSpec = tween(durationMs, easing = AnimationTokens.Easing.standard),
        label = "stagger_offset_$index"
    )
    return this.graphicsLayer {
        this.alpha = alpha
        this.translationY = offsetY
    }
}
