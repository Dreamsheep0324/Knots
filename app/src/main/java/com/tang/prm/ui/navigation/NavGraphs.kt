package com.tang.prm.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.navigation.NavBackStackEntry
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut

internal fun navTransitions(bottomRoutes: Set<String>): NavTransitions {
    return NavTransitions(
        enterTransition = {
            if (targetState.destination.route in bottomRoutes && initialState.destination.route in bottomRoutes) {
                fadeIn(tween(0))
            } else {
                fadeIn(tween(450, delayMillis = 50, easing = FastOutSlowInEasing)) +
                    scaleIn(
                        animationSpec = tween(450, delayMillis = 50, easing = FastOutSlowInEasing),
                        initialScale = 0.96f
                    )
            }
        },
        exitTransition = {
            if (targetState.destination.route in bottomRoutes && initialState.destination.route in bottomRoutes) {
                fadeOut(tween(0))
            } else {
                fadeOut(tween(200)) +
                    scaleOut(
                        animationSpec = tween(200),
                        targetScale = 0.96f
                    )
            }
        },
        popEnterTransition = {
            if (targetState.destination.route in bottomRoutes && initialState.destination.route in bottomRoutes) {
                fadeIn(tween(0))
            } else {
                fadeIn(tween(450, delayMillis = 50, easing = FastOutSlowInEasing)) +
                    scaleIn(
                        animationSpec = tween(450, delayMillis = 50, easing = FastOutSlowInEasing),
                        initialScale = 0.96f
                    )
            }
        },
        popExitTransition = {
            if (targetState.destination.route in bottomRoutes && initialState.destination.route in bottomRoutes) {
                fadeOut(tween(0))
            } else {
                fadeOut(tween(200)) +
                    scaleOut(
                        animationSpec = tween(200),
                        targetScale = 0.96f
                    )
            }
        }
    )
}

internal data class NavTransitions(
    val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition,
    val exitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition,
    val popEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition,
    val popExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition
)
