package com.csakitheone.streetmusic.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import com.csakitheone.streetmusic.R
import com.csakitheone.streetmusic.data.LocalRepository
import com.csakitheone.streetmusic.navigation.LocalNavBackStack
import com.csakitheone.streetmusic.ui.components.EventCard
import com.csakitheone.streetmusic.ui.components.FavoritesIndicator
import com.csakitheone.streetmusic.ui.components.YouTubeEmbed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailScreen(artistSlug: String) {
    val context = LocalContext.current
    val repository = LocalRepository.current
    val backStack = LocalNavBackStack.current
    val artist by repository.getArtist(artistSlug).collectAsState(initial = null)
    val events by repository.getEventsByArtist(artistSlug).collectAsState(initial = emptyList())

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(artist?.name ?: "Artist Details") },
                navigationIcon = {
                    IconButton(onClick = { backStack.removeLastOrNull() }) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = "Back")
                    }
                },
                actions = {
                    artist?.let {
                        FavoritesIndicator(slug = it.slug)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    icon = {
                        Icon(
                            painterResource(R.drawable.shortcut_events),
                            contentDescription = "Events"
                        )
                    },
                    label = { Text("Events") }
                )
                NavigationBarItem(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    icon = {
                        Icon(
                            painterResource(R.drawable.ic_info),
                            contentDescription = "Info"
                        )
                    },
                    label = { Text("Info") }
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            if (repository.shouldShowImage() && !artist?.image.isNullOrBlank()) {
                Card(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(16.dp),
                        )
                        AsyncImage(
                            modifier = Modifier.fillMaxWidth(),
                            model = artist?.image,
                            contentDescription = null,
                        )
                    }
                }
                HorizontalDivider()
            }

            Column(
                modifier = Modifier
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (selectedTabIndex) {
                    0 -> {
                        events.forEach { event ->
                            EventCard(event = event)
                        }
                    }

                    1 -> {
                        if (!artist?.youtubeEmbed.isNullOrBlank()) {
                            if (repository.shouldShowImage()) {
                                YouTubeEmbed(videoId = artist!!.youtubeEmbed!!)
                            } else {
                                Button(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = {
                                        val url =
                                            "https://www.youtube.com/watch?v=${artist!!.youtubeEmbed}"
                                        context.startActivity(
                                            Intent(
                                                Intent.ACTION_VIEW,
                                                url.toUri()
                                            )
                                        )
                                    }
                                ) {
                                    Icon(
                                        modifier = Modifier.padding(end = ButtonDefaults.IconSpacing),
                                        painter = painterResource(R.drawable.ic_youtube),
                                        contentDescription = null,
                                    )
                                    Text("Watch on YouTube")
                                }
                            }
                        }

                        Card {
                            Text(
                                modifier = Modifier.padding(16.dp),
                                text = artist?.description ?: "",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
        }
    }
}
