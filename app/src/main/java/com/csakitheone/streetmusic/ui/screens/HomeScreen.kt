package com.csakitheone.streetmusic.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalGridApi
import androidx.compose.foundation.layout.Grid
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.csakitheone.streetmusic.R
import com.csakitheone.streetmusic.data.DataRepository
import com.csakitheone.streetmusic.data.LocalRepository
import com.csakitheone.streetmusic.navigation.Destination
import com.csakitheone.streetmusic.navigation.LocalNavBackStack
import com.csakitheone.streetmusic.ui.components.ArtistCard
import com.csakitheone.streetmusic.ui.components.EventCard
import com.csakitheone.streetmusic.data.nearby.NearbyManager
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

@OptIn(ExperimentalGridApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen() {
    val repository = LocalRepository.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val backStack = LocalNavBackStack.current
    val hasData by repository.hasData.collectAsState(initial = false)
    val nearbyFeatures by repository.nearbyFeatures.collectAsState()
    val connectedFriends by repository.nearbyManager.friends.connectedFriends.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            repository.setNearbyFeatures(true)
        } else {
            Toast.makeText(context, "Permissions required for nearby features", Toast.LENGTH_SHORT)
                .show()
        }
    }

    val nearbyPermissions = NearbyManager.REQUIRED_PERMISSIONS

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                contentAlignment = Alignment.TopEnd,
            ) {
                Image(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(5f / 2f)
                        .clip(MaterialTheme.shapes.large),
                    painter = painterResource(id = R.drawable.header_2026),
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                )
                FilledIconButton(
                    modifier = Modifier.padding(8.dp),
                    onClick = { backStack.add(Destination.Settings) },
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_settings),
                        contentDescription = null
                    )
                }
            }

            if (repository.isDownloading) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    LoadingIndicator()
                    Text(text = "Downloading events...")
                }
                return@Column
            } else if (!hasData) {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(text = "Download data?", style = MaterialTheme.typography.titleMedium)
                        Text("You are on a metered connection. Would you like to download the event data now or sync from another device?")
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                scope.launch {
                                    when (repository.tryDownloadData(true)) {
                                        DataRepository.DownloadResult.NO_CONNECTION -> {
                                            Toast.makeText(
                                                context,
                                                "No connection",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }

                                        else -> {}
                                    }
                                }
                            },
                        ) {
                            Icon(
                                modifier = Modifier.padding(end = ButtonDefaults.IconSpacing),
                                painter = painterResource(R.drawable.ic_download),
                                contentDescription = null,
                            )
                            Text("Download using mobile data")
                        }
                        OutlinedButton(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                backStack.add(Destination.DataSync)
                            },
                        ) {
                            Text("Sync from another device")
                        }
                    }
                }
                return@Column
            }

            Grid(
                config = {
                    column(.5f)
                    column(.5f)
                    columnGap(8.dp)
                    row(64.dp)
                    row(64.dp)
                    rowGap(8.dp)
                }
            ) {
                Button(
                    modifier = Modifier.fillMaxSize(),
                    onClick = { backStack.add(Destination.Calendar) },
                    shape = MaterialTheme.shapes.large,
                ) {
                    Icon(
                        modifier = Modifier.padding(end = ButtonDefaults.IconSpacing),
                        painter = painterResource(R.drawable.shortcut_events),
                        contentDescription = null
                    )
                    Text("Calendar")
                }
                Button(
                    modifier = Modifier.fillMaxSize(),
                    onClick = { backStack.add(Destination.Artists) },
                    shape = MaterialTheme.shapes.large,
                ) {
                    Icon(
                        modifier = Modifier.padding(end = ButtonDefaults.IconSpacing),
                        painter = painterResource(R.drawable.shortcut_musicians),
                        contentDescription = null
                    )
                    Text("Artists")
                }
                Button(
                    modifier = Modifier.fillMaxSize(),
                    onClick = { backStack.add(Destination.Places) },
                    shape = MaterialTheme.shapes.large,
                ) {
                    Icon(
                        modifier = Modifier.padding(end = ButtonDefaults.IconSpacing),
                        painter = painterResource(R.drawable.shortcut_places),
                        contentDescription = null
                    )
                    Text("Places")
                    FilledTonalIconButton(
                        modifier = Modifier.padding(start = ButtonDefaults.IconSpacing),
                        onClick = { backStack.add(Destination.Map) }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_map),
                            contentDescription = null
                        )
                    }
                }
                Card(
                    modifier = Modifier.fillMaxSize(),
                    shape = MaterialTheme.shapes.large,
                    onClick = {
                        if (!nearbyFeatures) {
                            permissionLauncher.launch(nearbyPermissions.toTypedArray())
                        } else {
                            repository.setNearbyFeatures(false)
                        }
                    },
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Switch(
                            modifier = Modifier
                                .scale(1.5f)
                                .alpha(.6f),
                            checked = nearbyFeatures,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    permissionLauncher.launch(nearbyPermissions.toTypedArray())
                                } else {
                                    repository.setNearbyFeatures(false)
                                }
                            },
                        )
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                modifier = Modifier.padding(end = ButtonDefaults.IconSpacing),
                                painter = painterResource(R.drawable.ic_connect_without_contact),
                                contentDescription = null
                            )
                            Text("The gang")
                        }
                    }
                }
            }

            AnimatedVisibility(nearbyFeatures) {
                Card {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = "Nearby friends",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        if (connectedFriends.isEmpty()) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(16.dp)
                                    .alpha(.5f),
                                strokeWidth = ProgressIndicatorDefaults.CircularStrokeWidth / 2,
                            )
                            Text(
                                text = "Waiting for others...",
                                style = MaterialTheme.typography.labelMedium,
                            )
                        } else {
                            connectedFriends.values.forEach { payload ->
                                val name = payload.nickname
                                Box(
                                    modifier = Modifier
                                        .padding(2.dp)
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                        .clickable {
                                            Toast.makeText(context, name, Toast.LENGTH_SHORT).show()
                                        },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = name.firstOrNull()?.toString() ?: "?",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            HomeSectionToday(repository)

            HomeSectionTomorrow(repository)

            HomeSectionThisYear(repository)

            Text(text = "Utcazene socials", style = MaterialTheme.typography.titleLarge)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                NavigationBarItem(
                    selected = false,
                    onClick = {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                "https://utcazene.hu/".toUri()
                            )
                        )
                    },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_web),
                            contentDescription = null,
                        )
                    },
                    label = { Text("Website") },
                )
                NavigationBarItem(
                    selected = false,
                    onClick = {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                "https://www.instagram.com/utcazene/".toUri()
                            )
                        )
                    },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_instagram),
                            contentDescription = null,
                        )
                    },
                    label = { Text("Instagram") },
                )
                NavigationBarItem(
                    selected = false,
                    onClick = {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                "https://www.facebook.com/utcazene/".toUri()
                            )
                        )
                    },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_facebook),
                            contentDescription = null,
                        )
                    },
                    label = { Text("Facebook") },
                )
            }

            Column {
                Text(text = "UZ App was made by Csáki", style = MaterialTheme.typography.titleLarge)
                Text(text = "With excitement since 2023", style = MaterialTheme.typography.labelMedium)
            }

            TextButton(
                onClick = {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            "https://www.instagram.com/csakitheone/".toUri()
                        )
                    )
                },
            ) {
                Icon(
                    modifier = Modifier.padding(end = ButtonDefaults.IconSpacing),
                    painter = painterResource(R.drawable.ic_instagram),
                    contentDescription = null,
                )
                Text("@csakitheone")
            }
        }
    }
}

@Composable
fun HomeSectionToday(repository: DataRepository) {
    val events by repository.events.collectAsState(initial = emptyList())
    val allFavs by repository.allStarredSlugs.collectAsState(initial = emptySet())

    val now = LocalDateTime.now()
    val today = LocalDate.now().toString()

    val todayEvents = events.filter { it.startTime.startsWith(today) }
    val upcomingStarred = todayEvents.filter {
        allFavs.contains("${it.artistSlug} at ${it.startTime}") && LocalDateTime.parse(it.startTime)
            .isAfter(now)
    }
        .sortedBy { it.startTime }
    val nowPlaying = todayEvents.filter {
        LocalDateTime.parse(it.startTime).isBefore(now) && LocalDateTime.parse(it.endTime)
            .isAfter(now)
    }

    if (upcomingStarred.isEmpty() && nowPlaying.isEmpty()) return

    Text(text = "Today", style = MaterialTheme.typography.titleLarge)

    if (upcomingStarred.isNotEmpty()) {
        Text(text = "Today's plan", style = MaterialTheme.typography.titleMedium)
        upcomingStarred.forEach { event ->
            EventCard(event = event)
        }
    }

    if (nowPlaying.isNotEmpty()) {
        Text(text = "Now playing", style = MaterialTheme.typography.titleMedium)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(nowPlaying) { event ->
                EventCard(modifier = Modifier.width(280.dp), event = event)
            }
        }
    }
}

@Composable
fun HomeSectionTomorrow(repository: DataRepository) {
    val events by repository.events.collectAsState(initial = emptyList())
    val tomorrow = LocalDate.now().plusDays(1).toString()
    val allFavs by repository.allStarredSlugs.collectAsState(initial = emptySet())

    val tomorrowStarred =
        events.filter { it.startTime.startsWith(tomorrow) && allFavs.contains("${it.artistSlug} at ${it.startTime}") }
            .sortedBy { it.startTime }

    if (tomorrowStarred.isEmpty()) return

    Text(text = "Tomorrow", style = MaterialTheme.typography.titleLarge)

    Text(text = "Tomorrow's plan", style = MaterialTheme.typography.titleMedium)

    tomorrowStarred.forEach { event ->
        EventCard(event = event)
    }
}

@Composable
fun HomeSectionThisYear(repository: DataRepository) {
    val artists by repository.artists.collectAsState(initial = emptyList())
    val allStarredSlugs by repository.allStarredSlugs.collectAsState(initial = emptySet())

    val favoriteArtists = artists.filter { allStarredSlugs.contains(it.slug) }
    val headliners = artists.filter { it.tags.contains("headliner") }
    val competitors = artists.filter { it.tags.contains("competitor") }

    if (favoriteArtists.isEmpty() && headliners.isEmpty() && competitors.isEmpty()) return

    Text(text = "This year on Utcazene", style = MaterialTheme.typography.titleLarge)

    if (favoriteArtists.isNotEmpty()) {
        Text(text = "Favorite artists", style = MaterialTheme.typography.titleMedium)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(favoriteArtists) { artist ->
                ArtistCard(modifier = Modifier.width(280.dp), artist = artist)
            }
        }
    }

    if (headliners.isNotEmpty()) {
        Text(text = "From around the world", style = MaterialTheme.typography.titleMedium)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(headliners) { artist ->
                ArtistCard(modifier = Modifier.width(280.dp), artist = artist)
            }
        }
    }

    if (competitors.isNotEmpty()) {
        Text(text = "This year's competitors", style = MaterialTheme.typography.titleMedium)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(competitors) { artist ->
                ArtistCard(modifier = Modifier.width(280.dp), artist = artist)
            }
        }
    }
}
