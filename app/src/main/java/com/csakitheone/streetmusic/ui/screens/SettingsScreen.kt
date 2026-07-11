package com.csakitheone.streetmusic.ui.screens

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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
fun SettingsScreen() {
    val context = LocalContext.current
    val repository = LocalRepository.current
    val coroutineScope = rememberCoroutineScope()
    val backStack = LocalNavBackStack.current

    val showImagesOnMetered by repository.showImagesOnMetered.collectAsState(initial = false)
    val nickname by repository.nickname.collectAsState()

    val artists by repository.artists.collectAsState(initial = emptyList())
    val events by repository.events.collectAsState(initial = emptyList())
    val userFavorites by repository.userFavorites.collectAsState()

    var favoritesString by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Profile for nearby friends",
                        style = MaterialTheme.typography.titleMedium
                    )
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = nickname,
                        onValueChange = { repository.setNickname(it) },
                        label = { Text("Nickname") },
                        placeholder = { Text("Friend") },
                        singleLine = true,
                    )
                    Text(
                        text = "Your nickname will be visible to nearby devices.",
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }

            Card(
                onClick = {
                    repository.setShowImagesOnMetered(!showImagesOnMetered)
                },
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        modifier = Modifier.padding(8.dp),
                        checked = showImagesOnMetered,
                        onCheckedChange = {
                            repository.setShowImagesOnMetered(it)
                        }
                    )
                    Text(
                        modifier = Modifier.padding(8.dp),
                        text = "Show images and videos on metered connections",
                    )
                }
            }

            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(text = "Data", style = MaterialTheme.typography.titleMedium)

                    AnimatedContent(repository.isDownloading) {
                        if (it) {
                            LoadingIndicator(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .align(Alignment.CenterHorizontally),
                            )
                        } else {
                            Text(text = "${artists.size} artists\n${events.size} events")
                        }
                    }

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            backStack.add(com.csakitheone.streetmusic.navigation.Destination.DataSync)
                        },
                    ) {
                        Icon(
                            modifier = Modifier.padding(end = ButtonDefaults.IconSpacing),
                            painter = painterResource(R.drawable.ic_connect_without_contact),
                            contentDescription = null,
                        )
                        Text("Send data to another device")
                    }

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !repository.isDownloading,
                        onClick = {
                            coroutineScope.launch(Dispatchers.IO) {
                                repository.tryDownloadData(force = true)
                            }
                        },
                    ) {
                        Icon(
                            modifier = Modifier.padding(end = ButtonDefaults.IconSpacing),
                            painter = painterResource(R.drawable.ic_download),
                            contentDescription = null,
                        )
                        Text("Redownload data")
                    }

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !repository.isDownloading && (artists.isNotEmpty() || events.isNotEmpty()),
                        onClick = {
                            coroutineScope.launch(Dispatchers.IO) {
                                repository.deleteDatabase()
                            }
                            backStack.removeLastOrNull()
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
                        Text("Remove data from device")
                    }
                }
            }

            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = "Favorites",
                        style = MaterialTheme.typography.titleMedium
                    )

                    PrimaryTabRow(
                        selectedTabIndex = if (favoritesString == userFavorites.joinToString(",")) 0 else 1,
                        containerColor = Color.Transparent,
                        divider = {},
                    ) {
                        Tab(
                            selected = favoritesString == userFavorites.joinToString(","),
                            onClick = { favoritesString = userFavorites.joinToString(",") },
                            text = { Text("My favorites") },
                        )
                        Tab(
                            selected = favoritesString != userFavorites.joinToString(","),
                            onClick = { favoritesString = "" },
                            text = { Text("Import favorites") },
                        )
                    }

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
                                    .filter { slugs.contains(CombinedRepository.getSlugForAny(it)) }
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

            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = "Raw data for debugging",
                        style = MaterialTheme.typography.titleMedium
                    )

                    userFavorites.sorted().forEach { slug ->
                        ListItem(
                            trailingContent = {
                                IconButton(onClick = { repository.setFavorite(slug, false) }) {
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
                        enabled = !repository.isDownloading && (artists.isNotEmpty() || events.isNotEmpty()),
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
    }
}
