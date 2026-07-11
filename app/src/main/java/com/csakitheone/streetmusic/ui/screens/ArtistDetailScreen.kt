package com.csakitheone.streetmusic.ui.screens

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import com.csakitheone.streetmusic.R
import com.csakitheone.streetmusic.data.LocalRepository
import com.csakitheone.streetmusic.navigation.LocalNavBackStack
import com.csakitheone.streetmusic.navigation.LocalSharedTransitionContext
import com.csakitheone.streetmusic.ui.components.EventCard
import com.csakitheone.streetmusic.ui.components.FavoritesIndicator
import com.csakitheone.streetmusic.ui.components.YouTubeEmbed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailScreen(artistSlug: String) {
    val context = LocalContext.current
    val sharedTransition = LocalSharedTransitionContext.current
    val repository = LocalRepository.current
    val backStack = LocalNavBackStack.current
    val artist by repository.getArtist(artistSlug).collectAsState(initial = null)
    val events by repository.getEventsByArtist(artistSlug).collectAsState(initial = emptyList())

    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    var selectedTabIndex by rememberSaveable(events) {
        mutableIntStateOf(if (events.isEmpty()) 1 else 0)
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Box {
                if (repository.shouldShowImage() && !artist?.image.isNullOrBlank()) {
                    with(sharedTransition.sharedTransitionScope) {
                        AsyncImage(
                            modifier = Modifier
                                .matchParentSize()
                                .alpha(1f - scrollBehavior.state.collapsedFraction)
                                .sharedElement(
                                    sharedTransition.sharedTransitionScope.rememberSharedContentState(
                                        "image-$artistSlug"
                                    ),
                                    sharedTransition.animatedVisibilityScope,
                                ),
                            model = artist?.image,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                        )
                    }
                }
                LargeTopAppBar(
                    title = {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = .75f),
                            ),
                        ) {
                            SelectionContainer {
                                Text(
                                    modifier = Modifier.padding(4.dp),
                                    text = artist?.name ?: "Artist Details",
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        FilledIconButton(
                            onClick = { backStack.removeLastOrNull() },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                            )
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_arrow_back),
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        artist?.let {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                ),
                            ) {
                                FavoritesIndicator(slug = it.slug)
                            }
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = .5f),
                    ),
                )
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    icon = {
                        Icon(
                            painterResource(R.drawable.shortcut_events),
                            contentDescription = "Performances"
                        )
                    },
                    label = { Text("Performances") }
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
            Column(
                modifier = Modifier
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (selectedTabIndex) {
                    0 -> {
                        if (events.isEmpty()) {
                            Text("Waiting for Utcazene to upload events...")
                        } else {
                            events.forEach { event ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = event.startTime.substring(8, 10),
                                        style = MaterialTheme.typography.labelLarge,
                                    )
                                    EventCard(
                                        modifier = Modifier.weight(1f),
                                        event = event,
                                    )
                                }
                            }
                        }
                    }

                    1 -> {
                        artist?.let {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_map),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = it.country,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                it.tags.forEach { tag ->
                                    SuggestionChip(
                                        onClick = { },
                                        label = { Text(tag) }
                                    )
                                }
                            }
                        }

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
                            SelectionContainer {
                                Text(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    text = (artist?.description
                                        ?: "").ifBlank { "Waiting for Utcazene to finish artist details..." },
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
