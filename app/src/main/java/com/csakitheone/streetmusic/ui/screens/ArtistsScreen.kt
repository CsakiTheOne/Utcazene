package com.csakitheone.streetmusic.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.csakitheone.streetmusic.R
import com.csakitheone.streetmusic.data.LocalRepository
import com.csakitheone.streetmusic.navigation.LocalNavBackStack
import com.csakitheone.streetmusic.ui.components.ArtistCard
import com.csakitheone.streetmusic.ui.components.NearbyConnectionsDisplay

@Composable
fun ArtistsScreen(
    onRequestSearch: () -> Unit,
) {
    val context = LocalContext.current
    val repository = LocalRepository.current
    val backStack = LocalNavBackStack.current
    val artists by repository.artists.collectAsState(initial = emptyList())
    val allStarredSlugs by repository.allFavorites.collectAsState(initial = emptySet())
    var showOnlyStarred by rememberSaveable { mutableStateOf(false) }
    var selectedTag by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedCountry by rememberSaveable { mutableStateOf<String?>(null) }

    val availableCountries by remember(artists) {
        derivedStateOf {
            artists.groupBy { it.country }
                .filter { it.value.size >= 2 }
                .keys
                .sorted()
        }
    }

    val filteredArtists by remember(
        artists,
        showOnlyStarred,
        allStarredSlugs,
        selectedTag,
        selectedCountry
    ) {
        derivedStateOf {
            artists.filter { artist ->
                val matchesStarred =
                    if (showOnlyStarred) allStarredSlugs.contains(artist.slug) else true
                val matchesTag = selectedTag?.let { artist.tags.contains(it) } ?: true
                val matchesCountry = selectedCountry?.let { artist.country == it } ?: true
                matchesStarred && matchesTag && matchesCountry
            }.sortedBy { it.name }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Artists") },
                navigationIcon = {
                    IconButton(onClick = { backStack.removeLastOrNull() }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    NearbyConnectionsDisplay()
                    IconButton(
                        onClick = { onRequestSearch() }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_search),
                            contentDescription = "Search"
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("I'm feeling lucky") },
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_youtube),
                        contentDescription = null
                    )
                },
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
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(
                top = paddingValues.calculateTopPadding() + 16.dp,
                bottom = paddingValues.calculateBottomPadding() + 16.dp
            ),
        ) {
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    contentPadding = PaddingValues(horizontal = 16.dp),
                ) {
                    item {
                        FilterChip(
                            selected = showOnlyStarred,
                            onClick = { showOnlyStarred = !showOnlyStarred },
                            label = { Text("Starred") },
                            leadingIcon = if (showOnlyStarred) {
                                {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_star),
                                        contentDescription = null
                                    )
                                }
                            } else null
                        )
                    }
                    item {
                        Text("-")
                    }
                    items(artists.flatMap { it.tags }.distinct().sorted()) { tag ->
                        FilterChip(
                            selected = selectedTag == tag,
                            onClick = {
                                selectedTag = if (selectedTag != tag) tag else null
                            },
                            label = { Text(tag) },
                        )
                    }
                    item {
                        Text("-")
                    }
                    items(availableCountries) { country ->
                        FilterChip(
                            selected = selectedCountry == country,
                            onClick = {
                                selectedCountry = if (selectedCountry != country) country else null
                            },
                            label = { Text(country) },
                        )
                    }
                }
            }
            items(filteredArtists, key = { it.slug }) { artist ->
                ArtistCard(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    artist = artist
                )
            }
        }
    }
}
