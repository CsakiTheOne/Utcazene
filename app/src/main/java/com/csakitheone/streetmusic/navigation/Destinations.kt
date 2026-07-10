package com.csakitheone.streetmusic.navigation

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Destination : NavKey {
    @Serializable
    data object Home : Destination
    @Serializable
    data object Calendar : Destination
    @Serializable
    data object Artists : Destination
    @Serializable
    data object Places : Destination
    @Serializable
    data object Map : Destination
    @Serializable
    data object Settings : Destination
    @Serializable
    data object DataSync : Destination
    @Serializable
    data class ArtistDetail(val artistSlug: String) : Destination
    @Serializable
    data class EventDetail(val eventId: Int) : Destination
    @Serializable
    data object UnlockFest : Destination
    @Serializable
    data object Gyarkert : Destination
    @Serializable
    data object Imu : Destination
}

val LocalNavBackStack = staticCompositionLocalOf<NavBackStack<Destination>> {
    error("No NavBackStack provided")
}
