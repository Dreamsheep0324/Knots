package com.tang.prm.ui.animation.core

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing

object AnimationTokens {

    object Duration {
        const val instant = 150
        const val fast = 300
        const val normal = 500
        const val slow = 800
        const val dramatic = 1200
    }

    object Easing {
        val standard = FastOutSlowInEasing
        val enter = FastOutSlowInEasing
        val exit = FastOutLinearInEasing
        val emphasis = EaseInOutSine
        val linear = LinearEasing
    }

    object Cycle {
        const val fast = 600
        const val normal = 1500
        const val slow = 3000
        const val ambient = 8000
    }

    object Alpha {
        const val faint = 0.08f
        const val subtle = 0.12f
        const val dim = 0.3f
        const val half = 0.5f
        const val visible = 0.6f
        const val strong = 0.8f
        const val full = 1f
    }
}
