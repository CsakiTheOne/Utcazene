package com.csakitheone.streetmusic.ui.screens

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.csakitheone.streetmusic.R
import com.csakitheone.streetmusic.data.LocalRepository
import com.csakitheone.streetmusic.navigation.LocalNavBackStack
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataSyncScreen() {
    val repository = LocalRepository.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val backStack = LocalNavBackStack.current

    val artists by repository.getAllArtistEntities().collectAsState(initial = emptyList())
    val events by repository.getAllEventEntities().collectAsState(initial = emptyList())
    val discoveredEndpoints by repository.nearbyManager.dataSync.discoveredEndpoints.collectAsState()
    val connectedEndpoints by repository.nearbyManager.dataSync.connectedEndpoints.collectAsState()
    val incomingData by repository.nearbyManager.dataSync.incomingData.collectAsState()
    val nearbyError by repository.nearbyManager.error.collectAsState()

    var isDiscovering by remember { mutableStateOf(false) }
    var isAdvertising by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (!permissions.values.all { it }) {
            Toast.makeText(context, "Permissions required for data sync", Toast.LENGTH_SHORT).show()
        }
    }

    val nearbyPermissions = mutableListOf<String>().apply {
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        add(Manifest.permission.ACCESS_COARSE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(Manifest.permission.BLUETOOTH_ADVERTISE)
            add(Manifest.permission.BLUETOOTH_CONNECT)
            add(Manifest.permission.BLUETOOTH_SCAN)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }
    }

    LaunchedEffect(incomingData) {
        incomingData?.let { data ->
            repository.syncData(data.artists, data.events)
            Toast.makeText(context, "Data synced successfully!", Toast.LENGTH_LONG).show()
            repository.nearbyManager.dataSync.clearIncomingData()
            if (backStack.size > 1) {
                backStack.removeAt(backStack.lastIndex)
            }
        }
    }

    LaunchedEffect(nearbyError) {
        nearbyError?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            repository.nearbyManager.clearError()
            isDiscovering = false
            isAdvertising = false
        }
    }

    DisposableEffect(Unit) {
        repository.setNearbyFeatures(false)

        onDispose {
            repository.nearbyManager.dataSync.stop()
            repository.setNearbyFeatures(repository.nearbyFeatures.value)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Data Sync") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (backStack.size > 1) {
                            backStack.removeAt(backStack.lastIndex)
                        }
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Your Local Data", style = MaterialTheme.typography.titleMedium)
                    Text("Artists: ${artists.size}")
                    Text("Events: ${events.size}")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        permissionLauncher.launch(nearbyPermissions.toTypedArray())
                        isDiscovering = true
                        isAdvertising = false
                        repository.nearbyManager.dataSync.startDiscovery()
                    },
                    enabled = !isDiscovering
                ) {
                    Text("Receive Data")
                }
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        permissionLauncher.launch(nearbyPermissions.toTypedArray())
                        isAdvertising = true
                        isDiscovering = false
                        repository.nearbyManager.dataSync.startAdvertising(repository.nickname.value)
                    },
                    enabled = !isAdvertising
                ) {
                    Text("Send Data")
                }
            }

            if (isDiscovering) {
                Text("Searching for nearby devices...", style = MaterialTheme.typography.labelLarge)
                if (discoveredEndpoints.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(discoveredEndpoints.toList()) { (id, name) ->
                            ListItem(
                                modifier = Modifier.clickable {
                                    if (!connectedEndpoints.containsKey(id)) {
                                        repository.nearbyManager.dataSync.connectToEndpoint(id)
                                    }
                                },
                                leadingContent = {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_connect_without_contact),
                                        contentDescription = null
                                    )
                                },
                                trailingContent = null,
                                overlineContent = null,
                                supportingContent = { Text(if (connectedEndpoints.containsKey(id)) "Connected" else "Tap to connect") },
                                colors = ListItemDefaults.colors(),
                                elevation = ListItemDefaults.elevation(ListItemDefaults.Elevation),
                                content = { Text(name) },
                            )
                        }
                    }
                }
            }

            if (isAdvertising) {
                Text(
                    "Waiting for someone to connect...",
                    style = MaterialTheme.typography.labelLarge
                )
                if (connectedEndpoints.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(connectedEndpoints.toList()) { (id, payload) ->
                            ListItem(
                                modifier = Modifier.clickable {
                                    repository.nearbyManager.dataSync.sendData(id, artists, events)
                                    Toast.makeText(context, "Sending data...", Toast.LENGTH_SHORT)
                                        .show()
                                },
                                leadingContent = {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_connect_without_contact),
                                        contentDescription = null
                                    )
                                },
                                trailingContent = null,
                                overlineContent = null,
                                supportingContent = { Text("Tap to send data") },
                                colors = ListItemDefaults.colors(),
                                elevation = ListItemDefaults.elevation(ListItemDefaults.Elevation),
                                content = { Text(payload.nickname) },
                            )
                        }
                    }
                }
            }
        }
    }
}
