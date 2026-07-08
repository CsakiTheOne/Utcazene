package com.csakitheone.streetmusic.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.csakitheone.streetmusic.R
import com.csakitheone.streetmusic.data.LocalRepository
import com.csakitheone.streetmusic.navigation.LocalNavBackStack
import com.csakitheone.streetmusic.ui.components.FavoritesIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailScreen(artistSlug: String) {
    val repository = LocalRepository.current
    val backStack = LocalNavBackStack.current
    val artist by repository.getArtist(artistSlug).collectAsState(initial = null)

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
                        FavoritesIndicator(
                            slug = it.slug,
                            isStarred = it.isStarred,
                            onToggle = { repository.toggleFavorite(it.slug) }
                        )
                    }
                }
            )
        }
    ) { padding ->
        Text(
            modifier = Modifier.padding(padding),
            text = "Coming soon: ${artist?.name}"
        )
    }
}
