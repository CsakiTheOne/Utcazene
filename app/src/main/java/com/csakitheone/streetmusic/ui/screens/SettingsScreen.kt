package com.csakitheone.streetmusic.ui.screens

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    val repository = LocalRepository.current
    val coroutineScope = rememberCoroutineScope()
    val backStack = LocalNavBackStack.current

    val showImagesOnMetered by repository.showImagesOnMetered.collectAsState(initial = false)
    val nickname by repository.nickname.collectAsState()

    val artists by repository.artists.collectAsState(initial = emptyList())
    val events by repository.events.collectAsState(initial = emptyList())

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
                    Text(text = "Profile", style = MaterialTheme.typography.titleMedium)
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
                        text = "Show images on metered connections",
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
                        Text("Redownload data")
                    }

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !repository.isDownloading && repository.hasData.collectAsState(
                            initial = false
                        ).value,
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
                        Text("Remove data from device")
                    }
                }
            }
        }
    }
}
