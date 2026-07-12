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
    data object FavoritesSync : Destination
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

fun Destination.label(): String = when (this) {
    Destination.Home -> "Home"
    Destination.Calendar -> "Calendar"
    Destination.Artists -> "Artists"
    Destination.Places -> "Places"
    Destination.Map -> "Map"
    Destination.Settings -> "Settings"
    Destination.DataSync -> "Data Sync"
    Destination.FavoritesSync -> "Favorites Sync"
    is Destination.ArtistDetail -> "Artist Detail"
    is Destination.EventDetail -> "Event Detail"
    Destination.UnlockFest -> "Unlock Festival"
    Destination.Gyarkert -> "Gyárkert"
    Destination.Imu -> "Imu"
}

val LocalNavBackStack = staticCompositionLocalOf<NavBackStack<Destination>> {
    error("No NavBackStack provided")
}
