package com.csakitheone.streetmusic.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.csakitheone.streetmusic.R
import com.csakitheone.streetmusic.data.LocalRepository
import com.csakitheone.streetmusic.navigation.Destination
import com.csakitheone.streetmusic.navigation.LocalNavBackStack

@Composable
fun NearbyConnectionsDisplay(
    modifier: Modifier = Modifier,
) {
    val backStack = LocalNavBackStack.current
    val repository = LocalRepository.current
    val nearbyFeatures by repository.isNearbyFriendsActive.collectAsState()

    var showDialog by remember { mutableStateOf(false) }

    AnimatedVisibility(!nearbyFeatures) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SuggestionChip(
                modifier = Modifier.padding(horizontal = 8.dp),
                onClick = { repository.setIsNearbyFriendsActive(true) },
                label = { Text("Nearby off") },
            )
        }
    }
    AnimatedVisibility(nearbyFeatures) {
        val connectedFriends by repository.nearbyManager.friends.connectedFriends.collectAsState()

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                dismissButton = {
                    Button(
                        onClick = {
                            repository.setIsNearbyFriendsActive(false)
                            showDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError,
                        ),
                    ) {
                        Text("Turn off")
                    }
                },
                confirmButton = {
                    Button(onClick = { showDialog = false }) {
                        Text("Close")
                    }
                },
                title = { Text("Nearby Friends") },
                text = {
                    LazyColumn {
                        if (connectedFriends.isEmpty()) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(
                                        16.dp,
                                        Alignment.CenterHorizontally
                                    ),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    CircularWavyProgressIndicator()
                                    Text("Looking for friends...")
                                }
                            }
                        }
                        items(connectedFriends.values.toList()) { friend ->
                            ListItem(
                                leadingContent = {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = friend.nickname.firstOrNull()?.toString() ?: "?",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                        )
                                    }
                                },
                                supportingContent = { Text(friend.screen) },
                                colors = ListItemDefaults.colors(
                                    containerColor = Color.Transparent,
                                ),
                            ) {
                                Text(friend.nickname)
                            }
                        }
                        item {
                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    backStack.add(Destination.Chat())
                                    showDialog = false
                                },
                            ) {
                                Icon(
                                    modifier = Modifier.padding(end = ButtonDefaults.IconSpacing),
                                    painter = painterResource(R.drawable.ic_chat_bubble),
                                    contentDescription = null
                                )
                                Text("Chat with friends")
                            }
                        }
                    }
                }
            )
        }

        Row(
            modifier = modifier.clickable { showDialog = true },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (connectedFriends.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(16.dp)
                        .size(24.dp)
                        .alpha(.75f),
                )
            } else {
                connectedFriends.values.forEach { payload ->
                    val name = payload.nickname
                    Box(
                        modifier = Modifier
                            .padding(16.dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = name.firstOrNull()?.toString() ?: "?",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }
            }
        }
    }
}
