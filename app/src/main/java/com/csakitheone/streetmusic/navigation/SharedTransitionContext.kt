package com.csakitheone.streetmusic.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.staticCompositionLocalOf

data class SharedTransitionContext(
    val sharedTransitionScope: SharedTransitionScope,
    val animatedVisibilityScope: AnimatedContentScope,
)

val LocalSharedTransitionContext = staticCompositionLocalOf<SharedTransitionContext?> {
    null
}
