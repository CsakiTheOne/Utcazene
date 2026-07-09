package com.csakitheone.streetmusic

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.room.Room
import com.csakitheone.streetmusic.data.DataRepository
import com.csakitheone.streetmusic.data.LocalRepository
import com.csakitheone.streetmusic.data.api.UtcazeneApi
import com.csakitheone.streetmusic.data.local.AppDatabase
import com.csakitheone.streetmusic.data.nearby.NearbyManager
import com.csakitheone.streetmusic.navigation.Destination
import com.csakitheone.streetmusic.navigation.LocalNavBackStack
import com.csakitheone.streetmusic.ui.screens.ArtistDetailScreen
import com.csakitheone.streetmusic.ui.screens.ArtistsScreen
import com.csakitheone.streetmusic.ui.screens.CalendarScreen
import com.csakitheone.streetmusic.ui.screens.DataSyncScreen
import com.csakitheone.streetmusic.ui.screens.EventDetailScreen
import com.csakitheone.streetmusic.ui.screens.HomeScreen
import com.csakitheone.streetmusic.ui.screens.MapScreen
import com.csakitheone.streetmusic.ui.screens.PlacesScreen
import com.csakitheone.streetmusic.ui.screens.SettingsScreen
import com.csakitheone.streetmusic.ui.screens.UnlockFestScreen
import com.csakitheone.streetmusic.ui.theme.UtcazeneTheme
import kotlinx.coroutines.awaitCancellation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val repository = remember {
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "utcazene.db"
                ).fallbackToDestructiveMigration(true).build()
                val connectivityManager =
                    context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
                val prefs = context.getSharedPreferences("favorites", Context.MODE_PRIVATE)
                val nearbyManager = NearbyManager(context.applicationContext, lifecycleScope)
                DataRepository(UtcazeneApi(), db, connectivityManager, prefs, nearbyManager)
            }

            val backStack = rememberNavBackStack(Destination.Home) as NavBackStack<Destination>

            LaunchedEffect(Unit) {
                repository.tryDownloadData()
            }

            LaunchedEffect(Unit) {
                lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    val nearbyFeatures = repository.nearbyFeatures.value
                    repository.nearbyManager.setNearbyActive(nearbyFeatures)
                    try {
                        awaitCancellation()
                    } finally {
                        repository.nearbyManager.setNearbyActive(false)
                    }
                }
            }

            CompositionLocalProvider(
                LocalNavBackStack provides backStack,
                LocalRepository provides repository
            ) {
                UtcazeneTheme {
                    SharedTransitionLayout {
                        NavDisplay(
                            backStack = backStack,
                            onBack = {
                                if (backStack.size > 1) {
                                    backStack.removeAt(backStack.lastIndex)
                                } else {
                                    finish()
                                }
                            },
                            entryProvider = { key ->
                                when (key) {
                                    Destination.Home -> NavEntry(key) {
                                        HomeScreen()
                                    }

                                    Destination.Calendar -> NavEntry(key) {
                                        CalendarScreen()
                                    }

                                    Destination.Artists -> NavEntry(key) { ArtistsScreen() }

                                    Destination.Places -> NavEntry(key) {
                                        PlacesScreen()
                                    }

                                    Destination.Map -> NavEntry(key) { MapScreen() }

                                    Destination.Settings -> NavEntry(key) {
                                        SettingsScreen()
                                    }

                                    Destination.DataSync -> NavEntry(key) {
                                        DataSyncScreen()
                                    }

                                    is Destination.ArtistDetail -> NavEntry(key) {
                                        ArtistDetailScreen(key.artistSlug)
                                    }

                                    is Destination.EventDetail -> NavEntry(key) {
                                        EventDetailScreen(key.eventId)
                                    }

                                    is Destination.UnlockFest -> NavEntry(key) {
                                        UnlockFestScreen()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
