package com.csakitheone.streetmusic.ui.screens

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.core.content.FileProvider
import java.io.File
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.csakitheone.streetmusic.R
import com.csakitheone.streetmusic.data.LocalRepository
import com.csakitheone.streetmusic.navigation.LocalNavBackStack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = LocalRepository.current
    val backStack = LocalNavBackStack.current

    val showImagesOnMetered by repository.showImagesOnMetered.collectAsState(initial = false)
    val useHighPowerDiscovery by repository.useHighPowerDiscovery.collectAsState(initial = false)
    val nickname by repository.nickname.collectAsState()

    val artists by repository.artists.collectAsState(initial = emptyList())
    val events by repository.events.collectAsState(initial = emptyList())

    val incompleteArtistsCount by remember(artists) {
        derivedStateOf {
            artists.count { it.description.isEmpty() || it.image.isNullOrBlank() || it.youtubeEmbed.isNullOrBlank() }
        }
    }

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

            Card(
                onClick = {
                    repository.setUseHighPowerDiscovery(!useHighPowerDiscovery)
                    if (repository.isNearbyFriendsActive.value) {
                        Toast.makeText(
                            context,
                            "Restart nearby friends to apply changes",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        modifier = Modifier.padding(8.dp),
                        checked = useHighPowerDiscovery,
                        onCheckedChange = {
                            repository.setUseHighPowerDiscovery(it)
                        }
                    )
                    Column(
                        modifier = Modifier
                            .padding(8.dp)
                            .weight(1f)
                    ) {
                        Text(text = "High-power discovery")
                        Text(
                            text = "Faster discovery in crowded areas, but uses more battery.",
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
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
                            Text(text = "${artists.size} artists${if (incompleteArtistsCount > 0) " ($incompleteArtistsCount incomplete)" else ""}\n${events.size} events")
                        }
                    }

                    Text(text = "Sharing is caring", style = MaterialTheme.typography.titleSmall)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                shareApp(context, true)
                            },
                        ) {
                            Icon(
                                modifier = Modifier.padding(end = ButtonDefaults.IconSpacing),
                                painter = painterResource(R.drawable.ic_send),
                                contentDescription = null,
                            )
                            Text("Send app with Quick Share")
                        }
                        Button(
                            onClick = {
                                shareApp(context)
                            },
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_share),
                                contentDescription = null,
                            )
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
                        onClick = {
                            backStack.add(com.csakitheone.streetmusic.navigation.Destination.FavoritesSync)
                        },
                    ) {
                        Icon(
                            modifier = Modifier.padding(end = ButtonDefaults.IconSpacing),
                            painter = painterResource(R.drawable.ic_star),
                            contentDescription = null,
                        )
                        Text("Import/Export favorites")
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

                    Text(text = "Free up space", style = MaterialTheme.typography.titleMedium)

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

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            repository.clearMessages()
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
                        Text("Remove all messages from device")
                    }
                }
            }
        }
    }
}

private fun shareApp(context: Context, quickShareOnly: Boolean = false) {
    try {
        val appFile = File(context.applicationInfo.sourceDir)
        val cacheFile = File(context.cacheDir, "Utcazene.apk")

        // Copy the APK to cache to ensure it's accessible via FileProvider
        appFile.copyTo(cacheFile, overwrite = true)

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            cacheFile
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.android.package-archive"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        if (quickShareOnly) {
            // Explicitly send app to Quick Share
            context.startActivity(
                intent.setClassName(
                    "com.google.android.gms",
                    "com.google.android.gms.nearby.sharing.main.MainActivity"
                )
            )
        } else {
            context.startActivity(Intent.createChooser(intent, "Share app via"))
        }
    } catch (e: Exception) {
        Log.e("SettingsScreen", "Failed to share app", e)
        Toast.makeText(context, "Failed to share app: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
