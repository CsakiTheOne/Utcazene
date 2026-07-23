package com.csakitheone.streetmusic.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.pdf.models.ListItem
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.csakitheone.streetmusic.R
import com.csakitheone.streetmusic.data.DataRepository
import com.csakitheone.streetmusic.data.LocalRepository
import com.csakitheone.streetmusic.navigation.Destination
import com.csakitheone.streetmusic.navigation.LocalNavBackStack
import com.csakitheone.streetmusic.ui.components.NearbyConnectionsDisplay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = LocalRepository.current
    val backStack = LocalNavBackStack.current

    val showImagesOnMetered by repository.showImagesOnMetered.collectAsState(initial = false)
    val autoUpdateMode by repository.autoUpdateMode.collectAsState()
    val useHighPowerDiscovery by repository.useHighPowerDiscovery.collectAsState(initial = false)
    val isBatterySaverEnabled by repository.isBatterySaverEnabled.collectAsState()
    val nickname by repository.nickname.collectAsState()

    val artists by repository.getAllArtistEntities().collectAsState(initial = emptyList())
    val events by repository.getAllEventEntities().collectAsState(initial = emptyList())
    val venues by repository.getAllVenueEntities().collectAsState(initial = emptyList())

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
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = "Battery",
                    style = MaterialTheme.typography.titleMedium
                )

                if (isBatterySaverEnabled) {
                    Text(
                        modifier = Modifier.padding(16.dp),
                        text = "Battery saver is active. Some features are disabled to save energy.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                ListItem(
                    onClick = {
                        repository.setUseHighPowerDiscovery(false)
                        repository.setIsNearbyFriendsActive(false)
                        repository.setIsNearbyBackgroundEnabled(false)
                        if (autoUpdateMode == DataRepository.AutoUpdateMode.ALWAYS) {
                            repository.setAutoUpdateMode(DataRepository.AutoUpdateMode.NEVER)
                        }
                        repository.setShowImagesOnMetered(false)
                        Toast.makeText(
                            context,
                            "Settings applied for best battery life",
                            Toast.LENGTH_LONG
                        ).show()
                    },
                    leadingContent = {
                        Icon(
                            painter = painterResource(R.drawable.ic_battery_android_frame_plus),
                            contentDescription = null,
                        )
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = Color.Transparent,
                        headlineColor = MaterialTheme.colorScheme.primary,
                        leadingIconColor = MaterialTheme.colorScheme.primary,
                    )
                ) {
                    Text("Apply settings best for battery")
                }

                ListItem(
                    onClick = {
                        context.startActivity(Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS))
                    },
                    leadingContent = {
                        Icon(
                            painter = painterResource(R.drawable.ic_settings),
                            contentDescription = null,
                        )
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = Color.Transparent,
                    )
                ) {
                    Text("Battery saver settings")
                }
            }

            Card {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        modifier = Modifier
                            .padding(16.dp)
                            .weight(1f),
                        text = "Nearby features",
                        style = MaterialTheme.typography.titleMedium
                    )
                    NearbyConnectionsDisplay()
                }
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    value = nickname,
                    onValueChange = { repository.setNickname(it) },
                    label = { Text("Nickname") },
                    placeholder = { Text("Friend") },
                    singleLine = true,
                )
                val isNearbyFriendsActive by repository.isNearbyFriendsActive.collectAsState()
                val isNearbyBackgroundEnabled by repository.isNearbyBackgroundEnabled.collectAsState()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            repository.setIsNearbyFriendsActive(!isNearbyFriendsActive)
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(text = "Nearby Friends")
                        Text(
                            text = "Discover and connect with nearby friends to see their favorites and chat.",
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                    Switch(
                        checked = isNearbyFriendsActive,
                        onCheckedChange = {
                            repository.setIsNearbyFriendsActive(it)
                        },
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isBatterySaverEnabled) {
                            repository.setIsNearbyBackgroundEnabled(!isNearbyBackgroundEnabled)
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        modifier = Modifier
                            .weight(1f)
                            .alpha(if (isBatterySaverEnabled) 0.5f else 1f),
                        text = "Keep turned on when app is closed"
                    )
                    Checkbox(
                        enabled = !isBatterySaverEnabled,
                        checked = isNearbyBackgroundEnabled,
                        onCheckedChange = {
                            repository.setIsNearbyBackgroundEnabled(it)
                        },
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isBatterySaverEnabled) {
                            repository.setUseHighPowerDiscovery(!useHighPowerDiscovery)
                            if (repository.isNearbyFriendsActive.value) {
                                repository.setIsNearbyFriendsActive(false)
                                Toast.makeText(
                                    context,
                                    "Turned off nearby friends to apply changes",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .alpha(if (isBatterySaverEnabled) 0.5f else 1f),
                    ) {
                        Text(text = "High-power discovery")
                        Text(
                            text = "Faster discovery in crowded areas, but uses more battery.",
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                    Checkbox(
                        enabled = !isBatterySaverEnabled,
                        checked = useHighPowerDiscovery,
                        onCheckedChange = {
                            repository.setUseHighPowerDiscovery(it)
                        },
                    )
                }
            }

            Card {
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = "Internet usage",
                    style = MaterialTheme.typography.titleMedium,
                )
                var isAutoUpdateExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = isAutoUpdateExpanded,
                    onExpandedChange = { isAutoUpdateExpanded = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                            .fillMaxWidth(),
                        value = when (autoUpdateMode) {
                            DataRepository.AutoUpdateMode.NEVER -> "Never"
                            DataRepository.AutoUpdateMode.ONLY_UNMETERED -> "Only on unmetered"
                            DataRepository.AutoUpdateMode.ALWAYS -> "Always"
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Auto-update app data") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isAutoUpdateExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    )
                    ExposedDropdownMenu(
                        expanded = isAutoUpdateExpanded,
                        onDismissRequest = { isAutoUpdateExpanded = false },
                    ) {
                        DataRepository.AutoUpdateMode.entries.forEach { mode ->
                            DropdownMenuItem(
                                enabled = mode != DataRepository.AutoUpdateMode.ALWAYS || !isBatterySaverEnabled,
                                text = {
                                    Column {
                                        Text(
                                            text = when (mode) {
                                                DataRepository.AutoUpdateMode.NEVER -> "Never"
                                                DataRepository.AutoUpdateMode.ONLY_UNMETERED -> "Only on unmetered"
                                                DataRepository.AutoUpdateMode.ALWAYS -> "Always"
                                            }
                                        )
                                        Text(
                                            text = when (mode) {
                                                DataRepository.AutoUpdateMode.NEVER -> "Best for battery life"
                                                DataRepository.AutoUpdateMode.ONLY_UNMETERED -> "Recommended"
                                                DataRepository.AutoUpdateMode.ALWAYS -> if (isBatterySaverEnabled) "Disabled to save battery"
                                                else "May cause additional charges"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                        )
                                    }
                                },
                                onClick = {
                                    repository.setAutoUpdateMode(mode)
                                    isAutoUpdateExpanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            repository.setShowImagesOnMetered(!showImagesOnMetered)
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = "Always load images and videos",
                    )
                    Checkbox(
                        checked = showImagesOnMetered,
                        onCheckedChange = {
                            repository.setShowImagesOnMetered(it)
                        }
                    )
                }
            }

            Card {
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = "Data & Storage",
                    style = MaterialTheme.typography.titleMedium
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AnimatedContent(
                        modifier = Modifier.weight(1f),
                        targetState = repository.isDownloading,
                    ) {
                        if (it) {
                            LoadingIndicator()
                        } else {
                            Text(
                                modifier = Modifier.alpha(.75f),
                                text = "${artists.size} artists${if (incompleteArtistsCount > 0) " ($incompleteArtistsCount incomplete)" else ""}\n${events.size} events\n${venues.size} venues",
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                    OutlinedIconButton(
                        onClick = {
                            coroutineScope.launch(Dispatchers.IO) {
                                repository.tryDownloadData(force = true)
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_refresh),
                            contentDescription = null,
                        )
                    }
                }

                Text(
                    modifier = Modifier.padding(16.dp),
                    text = "Sharing is caring",
                    style = MaterialTheme.typography.titleSmall
                )

                ListItem(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        shareApp(context, true)
                    },
                    leadingContent = {
                        Icon(
                            painter = painterResource(R.drawable.ic_send),
                            contentDescription = null,
                        )
                    },
                    trailingContent = {
                        OutlinedIconButton(
                            onClick = {
                                shareApp(context)
                            },
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_share),
                                contentDescription = null,
                            )
                        }
                    },
                    supportingContent = {
                        Text(
                            text = "Sent apk may not work on the target device. Download UZ App from the Play Store if possible.",
                            style = MaterialTheme.typography.labelSmall,
                        )
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = Color.Transparent,
                    ),
                ) {
                    Text("Send app")
                }

                ListItem(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        backStack.add(Destination.DataSync)
                    },
                    leadingContent = {
                        Icon(
                            painter = painterResource(R.drawable.ic_connect_without_contact),
                            contentDescription = null,
                        )
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = Color.Transparent,
                    )
                ) {
                    Text("Send data to another device")
                }

                ListItem(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        backStack.add(Destination.FavoritesSync)
                    },
                    leadingContent = {
                        Icon(
                            painter = painterResource(R.drawable.ic_star),
                            contentDescription = null,
                        )
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = Color.Transparent,
                    )
                ) {
                    Text("Import/Export favorites")
                }

                Text(
                    modifier = Modifier.padding(16.dp),
                    text = "Free up space",
                    style = MaterialTheme.typography.titleSmall
                )

                ListItem(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        repository.clearMessages()
                    },
                    leadingContent = {
                        Icon(
                            painter = painterResource(R.drawable.ic_delete_forever),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                        )
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = Color.Transparent,
                        headlineColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text("Remove all messages from device")
                }

                ListItem(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !repository.isDownloading && (artists.isNotEmpty() || events.isNotEmpty()),
                    onClick = {
                        coroutineScope.launch(Dispatchers.IO) {
                            repository.deleteDatabase()
                        }
                        backStack.removeLastOrNull()
                    },
                    leadingContent = {
                        Icon(
                            painter = painterResource(R.drawable.ic_delete_forever),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                        )
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = Color.Transparent,
                        headlineColor = MaterialTheme.colorScheme.error,
                        disabledHeadlineColor = MaterialTheme.colorScheme.error.copy(alpha = 0.38f),
                        disabledLeadingIconColor = MaterialTheme.colorScheme.error.copy(alpha = 0.38f),
                    ),
                ) {
                    Text("Remove artist and event data from device")
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
