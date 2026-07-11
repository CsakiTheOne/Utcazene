package com.csakitheone.streetmusic.ui.screens

import android.content.ClipData
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.csakitheone.streetmusic.R
import com.csakitheone.streetmusic.data.CombinedRepository
import com.csakitheone.streetmusic.data.LocalRepository
import com.csakitheone.streetmusic.navigation.LocalNavBackStack
import com.csakitheone.streetmusic.ui.components.CombinedDisplay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FavoritesSyncScreen() {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val clipboard = LocalClipboard.current

    val repository = LocalRepository.current
    val backStack = LocalNavBackStack.current

    val artists by repository.artists.collectAsState(initial = emptyList())
    val events by repository.events.collectAsState(initial = emptyList())
    val userFavorites by repository.userFavorites.collectAsState()

    val userFavoritesString by remember(userFavorites) {
        derivedStateOf {
            userFavorites.joinToString(
                ","
            )
        }
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var favoritesString by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favorites Sync") },
                navigationIcon = {
                    IconButton(onClick = { backStack.removeLastOrNull() }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = "Back"
                        )
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_star),
                            contentDescription = null
                        )
                    },
                    label = { Text("Manage my favs") },
                )
                NavigationBarItem(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_download),
                            contentDescription = null
                        )
                    },
                    label = { Text("Import favs") },
                )
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            when (selectedTabIndex) {
                0 -> {
                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            Text(
                                text = "Manage / Export",
                                style = MaterialTheme.typography.titleMedium,
                            )

                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = userFavoritesString,
                                onValueChange = {},
                                label = { Text("My favorites string") },
                                placeholder = { Text("Comma-separated slugs") },
                                minLines = 3,
                                readOnly = true,
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End),
                            ) {
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            clipboard.setClipEntry(
                                                ClipEntry(
                                                    ClipData.newPlainText(
                                                        "Favorites string",
                                                        userFavoritesString
                                                    )
                                                )
                                            )
                                        }
                                    },
                                ) {
                                    Icon(
                                        modifier = Modifier.padding(end = ButtonDefaults.IconSpacing),
                                        painter = painterResource(R.drawable.ic_content_copy),
                                        contentDescription = null,
                                    )
                                    Text("Copy")
                                }
                                Button(
                                    onClick = {
                                        context.startActivity(
                                            Intent.createChooser(
                                                Intent(Intent.ACTION_SEND)
                                                    .setType("text/plain")
                                                    .putExtra(
                                                        Intent.EXTRA_TEXT,
                                                        userFavoritesString
                                                    ),
                                                "Share favorites"
                                            )
                                        )
                                    },
                                ) {
                                    Icon(
                                        modifier = Modifier.padding(end = ButtonDefaults.IconSpacing),
                                        painter = painterResource(R.drawable.ic_share),
                                        contentDescription = null,
                                    )
                                    Text("Export")
                                }
                            }

                            AnimatedContent(targetState = userFavoritesString) { targetFavoritesString ->
                                val favorites by remember(targetFavoritesString) {
                                    derivedStateOf {
                                        val slugs = targetFavoritesString.split(",")
                                        (CombinedRepository.getCombinedEvents(events) + artists)
                                            .filter {
                                                slugs.contains(
                                                    CombinedRepository.getSlugForAny(
                                                        it
                                                    )
                                                )
                                            }
                                    }
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    OutlinedCard {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(16.dp),
                                        ) {
                                            if (targetFavoritesString.isBlank()) {
                                                Text("Browse the artists and find your favorites.")
                                            }
                                            favorites.forEach {
                                                CombinedDisplay(data = it)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            Text(
                                text = "Manage raw favorites",
                                style = MaterialTheme.typography.titleMedium
                            )

                            userFavorites.sorted().forEach { slug ->
                                ListItem(
                                    trailingContent = {
                                        IconButton(onClick = {
                                            repository.setFavorite(
                                                slug,
                                                false
                                            )
                                        }) {
                                            Icon(
                                                painter = painterResource(R.drawable.ic_delete_forever),
                                                contentDescription = "Remove",
                                                tint = MaterialTheme.colorScheme.error,
                                            )
                                        }
                                    }
                                ) {
                                    Text(text = slug, style = MaterialTheme.typography.bodySmall)
                                }
                            }

                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                enabled = userFavorites.isNotEmpty(),
                                onClick = {
                                    coroutineScope.launch(Dispatchers.IO) {
                                        repository.clearFavorites()
                                    }
                                    Toast.makeText(
                                        context,
                                        "All favorites removed",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError,
                                ),
                            ) {
                                Icon(
                                    modifier = Modifier.padding(end = ButtonDefaults.IconSpacing),
                                    painter = painterResource(R.drawable.ic_delete_forever),
                                    contentDescription = null,
                                )
                                Text("Remove all favorites")
                            }
                        }
                    }
                }

                1 -> {
                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            Text(
                                text = "Import",
                                style = MaterialTheme.typography.titleMedium
                            )

                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = favoritesString,
                                onValueChange = { favoritesString = it },
                                label = { Text("Favorites string") },
                                placeholder = { Text("Comma-separated slugs") },
                                minLines = 3,
                            )

                            AnimatedContent(targetState = favoritesString) { targetFavoritesString ->
                                val favorites by remember(targetFavoritesString) {
                                    derivedStateOf {
                                        val slugs = targetFavoritesString.split(",")
                                        (CombinedRepository.getCombinedEvents(events) + artists)
                                            .filter {
                                                slugs.contains(
                                                    CombinedRepository.getSlugForAny(
                                                        it
                                                    )
                                                )
                                            }
                                    }
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    if (targetFavoritesString != userFavorites.joinToString(",") && favorites.isNotEmpty()) {
                                        Button(
                                            modifier = Modifier.fillMaxWidth(),
                                            onClick = {
                                                coroutineScope.launch {
                                                    favorites.forEach {
                                                        repository.setFavorite(
                                                            CombinedRepository.getSlugForAny(
                                                                it
                                                            ), true
                                                        )
                                                    }
                                                    Toast.makeText(
                                                        context,
                                                        "Favorites imported",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        ) {
                                            Icon(
                                                modifier = Modifier.padding(end = ButtonDefaults.IconSpacing),
                                                painter = painterResource(R.drawable.ic_download),
                                                contentDescription = null,
                                            )
                                            Text("Add all to favorites")
                                        }
                                    }

                                    OutlinedCard {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(16.dp),
                                        ) {
                                            if (targetFavoritesString.isBlank()) {
                                                Text("Ask a friend to copy their favorites string and paste it here.")
                                            }
                                            favorites.forEach {
                                                CombinedDisplay(data = it)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
