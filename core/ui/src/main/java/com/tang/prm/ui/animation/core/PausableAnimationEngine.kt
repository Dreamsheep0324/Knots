package com.tang.prm.ui.animation.core

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.TwoWayConverter
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

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

@Composable
fun rememberPausableInfiniteFloat(
    initialValue: Float,
    targetValue: Float,
    animationSpec: AnimationSpec<Float>,
    label: String = "PausableInfiniteFloat"
): State<Float> {
    val isResumed by rememberIsResumed()
    val animatable = remember(label) { Animatable(initialValue) }
    LaunchedEffect(isResumed, label) {
        if (isResumed) {
            animatable.animateTo(targetValue, animationSpec = animationSpec)
        } else {
            if (animatable.isRunning) animatable.stop()
        }
    }
    return animatable.asState()
}

@Composable
fun <T> rememberPausableInfiniteValue(
    initialValue: T,
    targetValue: T,
    typeConverter: TwoWayConverter<T, *>,
    animationSpec: AnimationSpec<T>,
    label: String = "PausableInfiniteValue"
): State<T> {
    val isResumed by rememberIsResumed()
    val animatable = remember(label) { Animatable(initialValue, typeConverter) }
    LaunchedEffect(isResumed, label) {
        if (isResumed) {
            animatable.animateTo(targetValue, animationSpec = animationSpec)
        } else {
            if (animatable.isRunning) animatable.stop()
        }
    }
    return animatable.asState()
}

@Composable
fun rememberPausableInfiniteFloatLoop(
    initialValue: Float = 0f,
    targetValue: Float = 1f,
    durationMillis: Int = AnimationTokens.Cycle.normal,
    easing: androidx.compose.animation.core.Easing = AnimationTokens.Easing.linear,
    repeatMode: RepeatMode = RepeatMode.Reverse,
    label: String = "PausableInfiniteFloatLoop"
): State<Float> {
    return rememberPausableInfiniteFloat(
        initialValue = initialValue,
        targetValue = targetValue,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = easing),
            repeatMode = repeatMode
        ),
        label = label
    )
}
