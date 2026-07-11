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
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigation3.ui.NavDisplay
import androidx.room.Room
import com.csakitheone.streetmusic.data.DataRepository
import com.csakitheone.streetmusic.data.LocalRepository
import com.csakitheone.streetmusic.data.api.UtcazeneApi
import com.csakitheone.streetmusic.data.local.AppDatabase
import com.csakitheone.streetmusic.data.nearby.NearbyManager
import com.csakitheone.streetmusic.navigation.Destination
import com.csakitheone.streetmusic.navigation.LocalNavBackStack
import com.csakitheone.streetmusic.navigation.LocalSharedTransitionContext
import com.csakitheone.streetmusic.navigation.SharedTransitionContext
import com.csakitheone.streetmusic.ui.screens.ArtistDetailScreen
import com.csakitheone.streetmusic.ui.screens.ArtistsScreen
import com.csakitheone.streetmusic.ui.screens.CalendarScreen
import com.csakitheone.streetmusic.ui.screens.DataSyncScreen
import com.csakitheone.streetmusic.ui.screens.EventDetailScreen
import com.csakitheone.streetmusic.ui.screens.FavoritesSyncScreen
import com.csakitheone.streetmusic.ui.screens.GyarkertScreen
import com.csakitheone.streetmusic.ui.screens.HomeScreen
import com.csakitheone.streetmusic.ui.screens.ImuScreen
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

                val startDestination = when (intent.action) {
                    "com.csakitheone.streetmusic.ACTION_CALENDAR" -> Destination.Calendar
                    "com.csakitheone.streetmusic.ACTION_ARTISTS" -> Destination.Artists
                    "com.csakitheone.streetmusic.ACTION_PLACES" -> Destination.Places
                    else -> null
                }
                if (startDestination != null) {
                    backStack.add(startDestination)
                }
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

            SharedTransitionLayout {
                CompositionLocalProvider(
                    LocalNavBackStack provides backStack,
                    LocalRepository provides repository,
                ) {
                    UtcazeneTheme {
                        NavDisplay(
                            backStack = backStack,
                            onBack = {
                                if (backStack.size > 1) {
                                    backStack.removeAt(backStack.lastIndex)
                                } else {
                                    finish()
                                }
                            },
                            entryDecorators = listOf(
                                rememberSaveableStateHolderNavEntryDecorator(),
                            ),
                            entryProvider = { key ->
                                when (key) {
                                    Destination.Home -> NavEntry(key) {
                                        CompositionLocalProvider(
                                            LocalSharedTransitionContext provides SharedTransitionContext(
                                                sharedTransitionScope = this,
                                                animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                                            )
                                        ) {
                                            HomeScreen()
                                        }
                                    }

                                    Destination.Calendar -> NavEntry(key) {
                                        CompositionLocalProvider(
                                            LocalSharedTransitionContext provides SharedTransitionContext(
                                                sharedTransitionScope = this,
                                                animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                                            )
                                        ) {
                                            CalendarScreen()
                                        }
                                    }

                                    Destination.Artists -> NavEntry(key) {
                                        CompositionLocalProvider(
                                            LocalSharedTransitionContext provides SharedTransitionContext(
                                                sharedTransitionScope = this,
                                                animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                                            )
                                        ) {
                                            ArtistsScreen()
                                        }
                                    }

                                    Destination.Places -> NavEntry(key) {
                                        CompositionLocalProvider(
                                            LocalSharedTransitionContext provides SharedTransitionContext(
                                                sharedTransitionScope = this,
                                                animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                                            )
                                        ) {
                                            PlacesScreen()
                                        }
                                    }

                                    Destination.Map -> NavEntry(key) {
                                        CompositionLocalProvider(
                                            LocalSharedTransitionContext provides SharedTransitionContext(
                                                sharedTransitionScope = this,
                                                animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                                            )
                                        ) {
                                            MapScreen()
                                        }
                                    }

                                    Destination.Settings -> NavEntry(key) {
                                        CompositionLocalProvider(
                                            LocalSharedTransitionContext provides SharedTransitionContext(
                                                sharedTransitionScope = this,
                                                animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                                            )
                                        ) {
                                            SettingsScreen()
                                        }
                                    }

                                    Destination.DataSync -> NavEntry(key) {
                                        CompositionLocalProvider(
                                            LocalSharedTransitionContext provides SharedTransitionContext(
                                                sharedTransitionScope = this,
                                                animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                                            )
                                        ) {
                                            DataSyncScreen()
                                        }
                                    }

                                    Destination.FavoritesSync -> NavEntry(key) {
                                        CompositionLocalProvider(
                                            LocalSharedTransitionContext provides SharedTransitionContext(
                                                sharedTransitionScope = this,
                                                animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                                            )
                                        ) {
                                            FavoritesSyncScreen()
                                        }
                                    }

                                    is Destination.ArtistDetail -> NavEntry(key) {
                                        CompositionLocalProvider(
                                            LocalSharedTransitionContext provides SharedTransitionContext(
                                                sharedTransitionScope = this,
                                                animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                                            )
                                        ) {
                                            ArtistDetailScreen(key.artistSlug)
                                        }
                                    }

                                    is Destination.EventDetail -> NavEntry(key) {
                                        CompositionLocalProvider(
                                            LocalSharedTransitionContext provides SharedTransitionContext(
                                                sharedTransitionScope = this,
                                                animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                                            )
                                        ) {
                                            EventDetailScreen(key.eventId)
                                        }
                                    }

                                    is Destination.UnlockFest -> NavEntry(key) {
                                        CompositionLocalProvider(
                                            LocalSharedTransitionContext provides SharedTransitionContext(
                                                sharedTransitionScope = this,
                                                animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                                            )
                                        ) {
                                            UnlockFestScreen()
                                        }
                                    }

                                    Destination.Gyarkert -> NavEntry(key) {
                                        CompositionLocalProvider(
                                            LocalSharedTransitionContext provides SharedTransitionContext(
                                                sharedTransitionScope = this,
                                                animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                                            )
                                        ) {
                                            GyarkertScreen()
                                        }
                                    }

                                    Destination.Imu -> NavEntry(key) {
                                        CompositionLocalProvider(
                                            LocalSharedTransitionContext provides SharedTransitionContext(
                                                sharedTransitionScope = this,
                                                animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                                            )
                                        ) {
                                            ImuScreen()
                                        }
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
