package com.tang.prm.ui.animation.core

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun rememberIsResumed(): State<Boolean> {
    val lifecycleOwner = LocalLifecycleOwner.current
    val isResumed = remember {
        mutableStateOf(lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> isResumed.value = true
                Lifecycle.Event.ON_PAUSE -> isResumed.value = false
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    return isResumed
}

/**
 * 可暂停的无限循环动画（Float 类型）。
 *
 * @param key 重建 key，key 变化时 Animatable 会被重建。默认 [Unit] 表示不随调用方标识重建。
 * @param label 调试标签，透传给 [Animatable]，便于 Layout Inspector 定位。
 */
@Composable
private fun rememberPausableInfiniteFloat(
    initialValue: Float,
    targetValue: Float,
    animationSpec: AnimationSpec<Float>,
    key: Any? = Unit,
    label: String = "PausableInfiniteFloat"
): State<Float> {
    val isResumed by rememberIsResumed()
    val animatable = remember(key) { Animatable(initialValue, Float.VectorConverter, label = label) }
    LaunchedEffect(isResumed, key) {
        if (isResumed) {
            animatable.animateTo(targetValue, animationSpec = animationSpec)
        }
    }
    return animatable.asState()
}

/**
 * 可暂停的无限循环动画（任意类型）。
 *
 * @param key 重建 key，key 变化时 Animatable 会被重建。默认 [Unit] 表示不随调用方标识重建。
 * @param label 调试标签，透传给 [Animatable]，便于 Layout Inspector 定位。
 */
@Composable
fun <T> rememberPausableInfiniteValue(
    initialValue: T,
    targetValue: T,
    typeConverter: TwoWayConverter<T, *>,
    animationSpec: AnimationSpec<T>,
    key: Any? = Unit,
    label: String = "PausableInfiniteValue"
): State<T> {
    val isResumed by rememberIsResumed()
    val animatable = remember(key) { Animatable(initialValue, typeConverter, label = label) }
    LaunchedEffect(isResumed, key) {
        if (isResumed) {
            animatable.animateTo(targetValue, animationSpec = animationSpec)
        }
    }
    return animatable.asState()
}

/**
 * 可暂停的无限循环 Float 动画（loop 便捷封装）。
 *
 * @param key 重建 key，默认 [Unit]；如需随调用方标识重建可显式传入。
 * @param label 调试标签，透传给 [Animatable]。
 * @param repeatMode 默认 [RepeatMode.Restart]，适合扫描线/流光类原语；
 * 呼吸类原语请显式传 [RepeatMode.Reverse]。
 */
@Composable
fun rememberPausableInfiniteFloatLoop(
    initialValue: Float = 0f,
    targetValue: Float = 1f,
    durationMillis: Int = AnimationTokens.Cycle.normal,
    easing: androidx.compose.animation.core.Easing = AnimationTokens.Easing.linear,
    repeatMode: RepeatMode = RepeatMode.Restart,
    key: Any? = Unit,
    label: String = "PausableInfiniteFloatLoop"
): State<Float> {
    return rememberPausableInfiniteFloat(
        initialValue = initialValue,
        targetValue = targetValue,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = easing),
            repeatMode = repeatMode
        ),
        key = key,
        label = label
    )
}

/**
 * 呼吸类动画的语义化封装：标准缓动 + [RepeatMode.Reverse]。
 *
 * 适合缩放/透明度/位移的"呼吸感"循环（如脉冲、悬浮卡片微动）。
 * 相比 [rememberPausableInfiniteFloatLoop]，调用方仅需关注初值/目标值/时长/标签，
 * 缓动与重复模式由本函数统一为呼吸语义。
 */
@Composable
fun rememberBreathingFloatLoop(
    initialValue: Float,
    targetValue: Float,
    durationMillis: Int,
    label: String
): State<Float> = rememberPausableInfiniteFloatLoop(
    initialValue = initialValue,
    targetValue = targetValue,
    durationMillis = durationMillis,
    easing = AnimationTokens.Easing.standard,
    repeatMode = RepeatMode.Reverse,
    label = label
)
