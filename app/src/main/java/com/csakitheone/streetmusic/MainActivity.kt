package com.csakitheone.streetmusic

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.csakitheone.streetmusic.data.LocalRepository
import com.csakitheone.streetmusic.navigation.Destination
import com.csakitheone.streetmusic.navigation.LocalNavBackStack
import com.csakitheone.streetmusic.navigation.label
import com.csakitheone.streetmusic.notifications.NotificationHelper
import com.csakitheone.streetmusic.ui.components.UniversalSearchOverlay
import com.csakitheone.streetmusic.ui.screens.ArtistDetailScreen
import com.csakitheone.streetmusic.ui.screens.ArtistsScreen
import com.csakitheone.streetmusic.ui.screens.CalendarScreen
import com.csakitheone.streetmusic.ui.screens.ChatScreen
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
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDateTime

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val repository = remember { (context.applicationContext as UZApp).repository }

            val backStack = rememberNavBackStack(Destination.Home) as NavBackStack<Destination>

            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) {}

            var isUniversalSearchOverlayVisible by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                NotificationHelper.createNotificationChannel(context)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            android.Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                repository.tryDownloadData()

                Log.d("MainActivity", "Received intent action: ${intent.action}")
                val startDestination = when (intent.action) {
                    "com.csakitheone.streetmusic.ACTION_CALENDAR" -> Destination.Calendar
                    "com.csakitheone.streetmusic.ACTION_ARTISTS" -> Destination.Artists
                    "com.csakitheone.streetmusic.ACTION_PLACES" -> Destination.Places
                    "com.csakitheone.streetmusic.ACTION_EVENT_DETAIL" -> {
                        val eventId = intent.getIntExtra("eventId", -1)
                        Log.d("MainActivity", "Deep link to event: $eventId")
                        if (eventId != -1) Destination.EventDetail(eventId) else null
                    }

                    else -> null
                }
                if (startDestination != null) {
                    Log.d("MainActivity", "Navigating to: $startDestination")
                    backStack.add(startDestination)
                }
            }

            LaunchedEffect(Unit) {
                lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    val nearbyFeatures = repository.isNearbyFriendsActive.value
                    repository.nearbyManager.setNearbyFriendsActive(nearbyFeatures)
                    try {
                        awaitCancellation()
                    } finally {
                        repository.nearbyManager.setNearbyFriendsActive(false)
                    }
                }
            }

            LaunchedEffect(backStack.lastOrNull()) {
                val currentScreen = backStack.lastOrNull() ?: Destination.Home
                when (currentScreen) {
                    is Destination.ArtistDetail -> {
                        repository.getArtist(currentScreen.artistSlug).collectLatest { artist ->
                            val label =
                                if (artist != null) "Artist: ${artist.name}" else "Artist Detail"
                            repository.nearbyManager.updateLocalScreen(label)
                        }
                    }

                    is Destination.EventDetail -> {
                        repository.getEvent(currentScreen.eventId).collectLatest { event ->
                            val startTime = LocalDateTime.parse(
                                event?.startTime ?: LocalDateTime.now().toString()
                            )
                            val label =
                                if (event != null) "Event: ${event.artistName} on ${startTime.dayOfMonth} at ${startTime.toLocalTime()}" else "Event Detail"
                            repository.nearbyManager.updateLocalScreen(label)
                        }
                    }

                    else -> {
                        repository.nearbyManager.updateLocalScreen(currentScreen.label())
                    }
                }
            }

            CompositionLocalProvider(
                LocalNavBackStack provides backStack,
                LocalRepository provides repository,
            ) {
                UtcazeneTheme {
                    Box {
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
                                        HomeScreen(
                                            onRequestSearch = {
                                                isUniversalSearchOverlayVisible = true
                                            },
                                        )
                                    }

                                    Destination.Calendar -> NavEntry(key) {
                                        CalendarScreen(
                                            onRequestSearch = {
                                                isUniversalSearchOverlayVisible = true
                                            },
                                        )
                                    }

                                    Destination.Artists -> NavEntry(key) {
                                        ArtistsScreen(
                                            onRequestSearch = {
                                                isUniversalSearchOverlayVisible = true
                                            },
                                        )
                                    }

                                    Destination.Places -> NavEntry(key) {
                                        PlacesScreen(
                                            onRequestSearch = {
                                                isUniversalSearchOverlayVisible = true
                                            },
                                        )
                                    }

                                    Destination.Map -> NavEntry(key) {
                                        MapScreen()
                                    }

                                    Destination.Settings -> NavEntry(key) {
                                        SettingsScreen()
                                    }

                                    Destination.DataSync -> NavEntry(key) {
                                        DataSyncScreen()
                                    }

                                    Destination.FavoritesSync -> NavEntry(key) {
                                        FavoritesSyncScreen()
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

                                    Destination.Gyarkert -> NavEntry(key) {
                                        GyarkertScreen()
                                    }

                                    Destination.Imu -> NavEntry(key) {
                                        ImuScreen()
                                    }

                                    is Destination.Chat -> NavEntry(key) {
                                        ChatScreen(initialRootNodeId = key.rootNodeId)
                                    }
                                }
                            }
                        )
                        AnimatedVisibility(
                            visible = isUniversalSearchOverlayVisible,
                            enter = fadeIn(),
                            exit = fadeOut(),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .clickable { isUniversalSearchOverlayVisible = false },
                            ) {
                                UniversalSearchOverlay(
                                    modifier = Modifier
                                        .systemBarsPadding()
                                        .imePadding()
                                        .padding(8.dp)
                                        .fillMaxWidth(),
                                    onDismissRequest = {
                                        isUniversalSearchOverlayVisible = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

        }
    }
}
