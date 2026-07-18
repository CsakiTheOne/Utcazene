package com.csakitheone.streetmusic.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.csakitheone.streetmusic.R
import com.csakitheone.streetmusic.data.LocalRepository
import com.csakitheone.streetmusic.data.nearby.ThreadNode
import com.csakitheone.streetmusic.navigation.LocalNavBackStack
import com.csakitheone.streetmusic.ui.components.NearbyConnectionsDisplay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    initialRootNodeId: String = "main",
    isHeadless: Boolean = false,
    headlessModifier: Modifier = Modifier,
) {
    val repository = LocalRepository.current
    val backStack = LocalNavBackStack.current
    val allNodes by repository.nearbyManager.friends.allThreadNodes.collectAsState()

    val artist by repository.getArtist(initialRootNodeId).collectAsState(null)

    var focusNodeId by rememberSaveable(initialRootNodeId) { mutableStateOf(initialRootNodeId) }

    // Resolve the "effective" root node by following the single-child chain from the focus node.
    // This is now done in a LaunchedEffect as requested, so it only auto-advances when allNodes changes.
    var effectiveRootNodeId by rememberSaveable(initialRootNodeId) { mutableStateOf(focusNodeId) }

    LaunchedEffect(allNodes) {
        var current = allNodes.find { it.id == effectiveRootNodeId } ?: return@LaunchedEffect
        while (true) {
            val children = allNodes.filter { it.parentId == current.id }
            if (children.size == 1) {
                current = children.first()
            } else {
                break
            }
        }
        if (effectiveRootNodeId != current.id) {
            effectiveRootNodeId = current.id
        }
    }

    val effectiveRootNode = allNodes.find { it.id == effectiveRootNodeId } ?: ThreadNode(
        id = effectiveRootNodeId,
        parentId = "main",
        senderName = artist?.name ?: "UZ App",
        content = if (artist != null) "Welcome to my thread!" else "Welcome to the \"$initialRootNodeId\" thread!",
    )

    val parents = remember(effectiveRootNode, allNodes) {
        val list = mutableListOf<ThreadNode>()
        var current = allNodes.find { it.id == effectiveRootNode.parentId }
        while (current != null) {
            list.add(0, current)
            current = allNodes.find { it.id == current.parentId }
        }
        list
    }

    val replies = remember(effectiveRootNode, allNodes) {
        allNodes.filter { it.parentId == effectiveRootNode.id }
    }

    val content = @Composable {
        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                reverseLayout = true,
            ) {
                if (replies.isNotEmpty()) {
                    items(replies.reversed()) { node ->
                        ChatBubble(
                            node = node,
                            replyCount = allNodes.count { it.parentId == node.id },
                            onClick = {
                                focusNodeId = node.id
                                effectiveRootNodeId = node.id
                            }
                        )
                    }
                    item {
                        HorizontalDivider()
                        Text(
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                            text = "Replies",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                item {
                    ChatBubble(
                        node = effectiveRootNode,
                        isRoot = true,
                        replyCount = allNodes.count { it.parentId == effectiveRootNode.id },
                        onClick = {
                            focusNodeId = effectiveRootNode.id
                            effectiveRootNodeId = effectiveRootNode.id
                        }
                    )
                }

                items(parents.reversed()) { node ->
                    ChatBubble(
                        node = node,
                        isParent = true,
                        replyCount = allNodes.count { it.parentId == node.id },
                        onClick = {
                            focusNodeId = node.id
                            effectiveRootNodeId = node.id
                        }
                    )
                }

                item {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "This is a highly unstable, experimental feature",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            ChatInput(
                replyingTo = effectiveRootNode.content.lines().firstOrNull() ?: "",
                onSend = { content ->
                    repository.nearbyManager.friends.sendMessage(effectiveRootNode.id, content)
                }
            )
        }
    }

    if (isHeadless) {
        Box(modifier = headlessModifier) {
            content()
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Chat") },
                    navigationIcon = {
                        IconButton(onClick = { backStack.removeLastOrNull() }) {
                            Icon(
                                painterResource(R.drawable.ic_arrow_back),
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        NearbyConnectionsDisplay()
                    },
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .padding(
                        top = padding.calculateTopPadding(),
                        start = padding.calculateStartPadding(LayoutDirection.Ltr),
                        end = padding.calculateEndPadding(LayoutDirection.Ltr),
                    )
                    .navigationBarsPadding()
                    .imePadding(),
            ) {
                content()
            }
        }
    }
}

@Composable
fun ChatBubble(
    node: ThreadNode,
    isParent: Boolean = false,
    isRoot: Boolean = false,
    replyCount: Int = 0,
    onClick: () -> Unit,
) {
    val backgroundColor = when {
        isParent -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        isRoot -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.secondaryContainer
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            onClick = onClick,
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            shape = RoundedCornerShape(12.dp),
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = node.senderName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (replyCount > 1) {
                        Text(
                            text = "$replyCount replies",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                Text(
                    text = node.content,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun ChatInput(
    replyingTo: String,
    onSend: (String) -> Unit,
) {
    var text by remember { mutableStateOf("") }

    Surface(
        tonalElevation = 4.dp,
    ) {
        Column {
            Text(
                text = "Replying to \"$replyingTo\"",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
            TextField(
                value = text,
                onValueChange = { if (it.length <= 280) text = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Write a reply...") },
                maxLines = 4,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                )
            )
            IconButton(
                onClick = {
                    if (text.isNotBlank()) {
                        onSend(text)
                        text = ""
                    }
                },
                enabled = text.isNotBlank()
            ) {
                Icon(painterResource(R.drawable.ic_send), contentDescription = "Send")
            }
        }
    }
}
}
