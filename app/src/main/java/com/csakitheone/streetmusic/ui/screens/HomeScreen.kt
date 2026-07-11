package com.csakitheone.streetmusic.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalGridApi
import androidx.compose.foundation.layout.Grid
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.carousel.CarouselDefaults
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.csakitheone.streetmusic.data.CombinedRepository
import com.csakitheone.streetmusic.ui.components.ArtistCard
import com.csakitheone.streetmusic.ui.components.CombinedDisplay
import com.csakitheone.streetmusic.ui.components.EventCard
import com.csakitheone.streetmusic.data.nearby.NearbyManager
import com.csakitheone.streetmusic.ui.components.NearbyConnectionsDisplay
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalGridApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen() {
    val repository = LocalRepository.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val backStack = LocalNavBackStack.current
    val hasData by repository.hasData.collectAsState(initial = false)
    val nearbyFeatures by repository.nearbyFeatures.collectAsState()

    val appUpdateManager = remember { AppUpdateManagerFactory.create(context) }
    var updateInfo by remember {
        mutableStateOf<com.google.android.play.core.appupdate.AppUpdateInfo?>(
            null
        )
    }
    val updateLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode != android.app.Activity.RESULT_OK) {
            Toast.makeText(context, "Update failed or cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                updateInfo = info
            } else if (info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                appUpdateManager.startUpdateFlowForResult(
                    info,
                    updateLauncher,
                    AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                )
            }
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.size(padding.calculateTopPadding()))

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

            updateInfo?.let { info ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_download),
                            contentDescription = null,
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Update available",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = "A new version of UZ App is available. Download it to stay up to date with the events.",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        Button(
                            onClick = {
                                appUpdateManager.startUpdateFlowForResult(
                                    info,
                                    updateLauncher,
                                    AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                                )
                            },
                        ) {
                            Text("Update")
                        }
                    }
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
                        Text("You are on a metered connection or Utcazene hasn't uploaded this year's musicians yet. Would you like to download the event data now or sync from another device?")
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
                            Text("Retry / Download using mobile data")
                        }
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                backStack.add(Destination.DataSync)
                            },
                        ) {
                            Icon(
                                modifier = Modifier.padding(end = ButtonDefaults.IconSpacing),
                                painter = painterResource(R.drawable.ic_connect_without_contact),
                                contentDescription = null,
                            )
                            Text("Sync from another device")
                        }
                    }
                }

                ToggleNearbyFriendsCard(
                    modifier = Modifier.align(Alignment.End),
                )
            } else {
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
                    ToggleNearbyFriendsCard(
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            AnimatedVisibility(nearbyFeatures) {
                Card {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(16.dp)
                                .weight(1f),
                            text = "Nearby friends",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        NearbyConnectionsDisplay()
                    }
                }
            }

            HomeSectionToday(repository)

            HomeSectionTomorrow(repository)

            HomeSectionThisYear(repository)

            Text(text = "Parallel events", style = MaterialTheme.typography.titleLarge)

            Card(
                onClick = {
                    backStack.add(Destination.Imu)
                },
            ) {
                ListItem(
                    leadingContent = {
                        Image(
                            modifier = Modifier.size(64.dp),
                            painter = painterResource(R.drawable.imu),
                            contentDescription = null,
                        )
                    },
                    content = { Text("Íródeák Művészeti Udvar") },
                    supportingContent = { Text("Full schedule") },
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        headlineColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        supportingColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                )
            }

            OutlinedCard(
                onClick = {
                    backStack.add(Destination.Gyarkert)
                },
            ) {
                ListItem(
                    leadingContent = {
                        Image(
                            modifier = Modifier.size(64.dp),
                            painter = painterResource(R.drawable.gyarkert_logo),
                            contentDescription = null,
                        )
                    },
                    content = { Text("Gyárkert") },
                    supportingContent = { Text("Pont Ott Parti and more") },
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        headlineColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        supportingColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    ),
                )
            }

            OutlinedCard(
                onClick = {
                    backStack.add(Destination.UnlockFest)
                },
            ) {
                Box(
                    contentAlignment = Alignment.BottomStart,
                ) {
                    Image(
                        painter = painterResource(R.drawable.unlock_fest_banner),
                        contentDescription = null,
                    )
                    Card(
                        modifier = Modifier
                            .padding(8.dp)
                            .alpha(.9f),
                    ) {
                        Text(
                            modifier = Modifier.padding(8.dp),
                            text = "Unlock Fest Vol. VII - TEREM",
                        )
                    }
                }
            }

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
                Text(
                    text = "With excitement since 2023",
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
            ) {
                Image(
                    modifier = Modifier
                        .size(64.dp)
                        .padding(end = ButtonDefaults.IconSpacing),
                    painter = painterResource(R.drawable.miku_hi),
                    contentDescription = null,
                )
                Button(
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

            Spacer(modifier = Modifier.size(padding.calculateBottomPadding()))
        }
    }
}

@Composable
fun ToggleNearbyFriendsCard(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val repository = LocalRepository.current

    val nearbyFeatures by repository.nearbyFeatures.collectAsState()

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

    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        onClick = {
            if (!nearbyFeatures) {
                permissionLauncher.launch(NearbyManager.REQUIRED_PERMISSIONS.toTypedArray())
            } else {
                repository.setNearbyFeatures(false)
            }
        },
    ) {
        Box(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center,
        ) {
            Switch(
                modifier = Modifier
                    .scale(1.5f)
                    .alpha(.6f),
                checked = nearbyFeatures,
                onCheckedChange = { checked ->
                    if (checked) {
                        permissionLauncher.launch(NearbyManager.REQUIRED_PERMISSIONS.toTypedArray())
                    } else {
                        repository.setNearbyFeatures(false)
                    }
                },
            )
            Row(
                modifier = Modifier
                    .height(64.dp)
                    .padding(16.dp),
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

@Composable
fun HomeSectionToday(repository: DataRepository) {
    val events by repository.events.collectAsState(initial = emptyList())
    val allFavs by repository.allFavorites.collectAsState(initial = emptySet())

    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30.seconds)
            now = LocalDateTime.now()
        }
    }

    val upcomingStarred by remember(now, events, allFavs) {
        derivedStateOf {
            val todayDay = now.dayOfMonth
            CombinedRepository.getCombinedEventsForDay(todayDay, events)
                .filter {
                    allFavs.contains(CombinedRepository.getSlugForAny(it)) &&
                            CombinedRepository.getIsAfterAny(it, now)
                }
        }
    }
    val nowPlaying by remember(now) {
        derivedStateOf {
            val today = now.toLocalDate().toString()
            events.filter { it.startTime.startsWith(today) }
                .filter {
                    LocalDateTime.parse(it.startTime)
                        .isBefore(now) && LocalDateTime.parse(it.endTime)
                        .isAfter(now)
                }
        }
    }

    if (upcomingStarred.isEmpty() && nowPlaying.isEmpty()) return

    Text(text = "Today", style = MaterialTheme.typography.titleLarge)

    if (upcomingStarred.isNotEmpty()) {
        Text(text = "Today's plan", style = MaterialTheme.typography.titleMedium)
        upcomingStarred.forEach { event ->
            CombinedDisplay(data = event)
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
    val allFavs by repository.allFavorites.collectAsState(initial = emptySet())

    val tomorrowStarred by remember(events, allFavs) {
        derivedStateOf {
            val tomorrowDay = LocalDate.now().plusDays(1).dayOfMonth
            CombinedRepository.getCombinedEventsForDay(tomorrowDay, events)
                .filter { allFavs.contains(CombinedRepository.getSlugForAny(it)) }
        }
    }

    if (tomorrowStarred.isEmpty()) return

    Text(text = "Tomorrow", style = MaterialTheme.typography.titleLarge)

    Text(text = "Tomorrow's plan", style = MaterialTheme.typography.titleMedium)

    tomorrowStarred.forEach { event ->
        CombinedDisplay(data = event)
    }
}

@Composable
fun HomeSectionThisYear(repository: DataRepository) {
    val context = LocalContext.current
    val backStack = LocalNavBackStack.current
    val artists by repository.artists.collectAsState(initial = emptyList())
    val allStarredSlugs by repository.allFavorites.collectAsState(initial = emptySet())

    val favoriteArtists by remember {
        derivedStateOf { artists.filter { allStarredSlugs.contains(it.slug) } }
    }
    val headliners by remember {
        derivedStateOf { artists.filter { it.tags.contains("headliner") } }
    }
    val competitors by remember {
        derivedStateOf { artists.filter { it.tags.contains("competitor") } }
    }

    if (favoriteArtists.isEmpty() && headliners.isEmpty() && competitors.isEmpty()) return

    Text(text = "This year on Utcazene", style = MaterialTheme.typography.titleLarge)

    Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            val randomYouTubeId =
                artists.filter { !it.youtubeEmbed.isNullOrBlank() }.random().youtubeEmbed
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    "https://www.youtube.com/watch?v=$randomYouTubeId".toUri()
                )
            )
        },
    ) {
        Icon(
            modifier = Modifier.padding(end = ButtonDefaults.IconSpacing),
            painter = painterResource(R.drawable.ic_youtube),
            contentDescription = null,
        )
        Text("I'm feeling lucky")
    }

    if (favoriteArtists.isNotEmpty()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "Favorite artists", style = MaterialTheme.typography.titleMedium)

            TextButton(
                onClick = { backStack.add(Destination.FavoritesSync) },
            ) {
                Text("Manage/Export")
            }
        }
        HorizontalMultiBrowseCarousel(
            state = rememberCarouselState(initialItem = 0) { favoriteArtists.size },
            preferredItemWidth = 280.dp,
            itemSpacing = 8.dp,
            flingBehavior = CarouselDefaults.noSnapFlingBehavior(),
        ) { index ->
            val artist = favoriteArtists[index]
            ArtistCard(artist = artist)
        }
    }

    if (headliners.isNotEmpty()) {
        Text(text = "From around the world", style = MaterialTheme.typography.titleMedium)
        HorizontalMultiBrowseCarousel(
            state = rememberCarouselState(initialItem = 0) { headliners.size },
            preferredItemWidth = 280.dp,
            itemSpacing = 8.dp,
            flingBehavior = CarouselDefaults.noSnapFlingBehavior(),
        ) { index ->
            val artist = headliners[index]
            ArtistCard(artist = artist)
        }
    }

    if (competitors.isNotEmpty()) {
        Text(text = "This year's competitors", style = MaterialTheme.typography.titleMedium)
        HorizontalMultiBrowseCarousel(
            state = rememberCarouselState(initialItem = 0) { competitors.size },
            preferredItemWidth = 280.dp,
            itemSpacing = 8.dp,
            flingBehavior = CarouselDefaults.noSnapFlingBehavior(),
        ) { index ->
            val artist = competitors[index]
            ArtistCard(artist = artist)
        }
    }
}
