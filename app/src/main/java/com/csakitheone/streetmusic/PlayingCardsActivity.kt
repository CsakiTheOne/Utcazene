package com.csakitheone.streetmusic

import android.os.Bundle
import android.util.Size
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.csakitheone.streetmusic.playingcards.PlayingCardData
import com.csakitheone.streetmusic.playingcards.ui.components.PlayingCard
import com.csakitheone.streetmusic.ui.components.UzCard
import com.csakitheone.streetmusic.ui.components.util.DropTarget
import com.csakitheone.streetmusic.ui.theme.UtcazeneTheme

class PlayingCardsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PlayingCardsScreen()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Preview
    @Composable
    fun PlayingCardsScreen() {
        UtcazeneTheme {
            val VIEW_DECK = "deck"
            val VIEW_TABLE = "table"

            var selectedView by remember { mutableStateOf(VIEW_DECK) }
            var isMenuVisible by remember { mutableStateOf(false) }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Surface(
                        modifier = Modifier.zIndex(2f),
                    ) {
                        TopAppBar(
                            title = { Text(text = "Francia kártya") },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = null,
                                    )
                                }
                            },
                            actions = {
                                IconButton(onClick = { isMenuVisible = true }) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = null,
                                    )
                                    DropdownMenu(
                                        expanded = isMenuVisible,
                                        onDismissRequest = { isMenuVisible = false },
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text(text = "Pakli") },
                                            onClick = {
                                                selectedView = VIEW_DECK
                                                isMenuVisible = false
                                            },
                                            leadingIcon = {
                                                if (selectedView == VIEW_DECK) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                    )
                                                }
                                            },
                                        )
                                        DropdownMenuItem(
                                            text = { Text(text = "Asztal") },
                                            onClick = {
                                                selectedView = VIEW_TABLE
                                                isMenuVisible = false
                                            },
                                            leadingIcon = {
                                                if (selectedView == VIEW_TABLE) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                    )
                                                }
                                            },
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.smallTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background,
                                titleContentColor = MaterialTheme.colorScheme.onBackground,
                                navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                                actionIconContentColor = MaterialTheme.colorScheme.onBackground,
                            ),
                        )
                    }
                    when (selectedView) {
                        VIEW_DECK -> viewDeck()
                        VIEW_TABLE -> viewTable()
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
    @Composable
    fun viewDeck() {
        var deck by remember { mutableStateOf(PlayingCardData.deck()) }
        var cardIndex by remember { mutableStateOf(-1) }
        val currentCardData: PlayingCardData? by remember(deck, cardIndex) {
            mutableStateOf(if ((0..deck.lastIndex).contains(cardIndex)) deck[cardIndex] else null)
        }
        var hands by remember { mutableStateOf(mapOf<String, List<PlayingCardData>>()) }

        var handEditDialogKey: String? by remember { mutableStateOf(null) }
        var handEditDialogText by remember(handEditDialogKey) {
            mutableStateOf(
                handEditDialogKey ?: ""
            )
        }

        if (handEditDialogKey != null) {
            AlertDialog(
                title = { Text(text = "Kéz átnevezése") },
                text = {
                    TextField(
                        value = handEditDialogText,
                        onValueChange = { handEditDialogText = it },
                    )
                },
                onDismissRequest = { handEditDialogKey = null },
                dismissButton = {
                    TextButton(onClick = { handEditDialogKey = null }) {
                        Text(text = stringResource(id = R.string.close))
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val temp =
                                Pair(handEditDialogText, hands[handEditDialogKey] ?: listOf())
                            hands = hands - handEditDialogKey!! + temp
                            handEditDialogKey = null
                        }
                    ) {
                        Text(text = "Ok")
                    }
                },
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .heightIn(max = 300.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                hands.toSortedMap().map { hand ->
                    UzCard(
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(modifier = Modifier.padding(8.dp), text = hand.key)
                                Spacer(modifier = Modifier.weight(1f))
                                IconButton(
                                    onClick = { handEditDialogKey = hand.key }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null
                                    )
                                }
                                IconButton(
                                    enabled = hand.value.isEmpty(),
                                    onClick = { hands = hands - hand.key },
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                hand.value.map { cardData ->
                                    PlayingCard(
                                        modifier = Modifier
                                            .width(64.dp)
                                            .aspectRatio(2.5f / 3.5f)
                                            .padding(8.dp)
                                            .clickable {
                                                val temp = Pair(hand.key, hand.value - cardData)
                                                deck = deck + cardData
                                                hands = hands - hand.key
                                                hands = hands + temp
                                            },
                                        data = cardData,
                                    )
                                }
                                IconButton(
                                    modifier = Modifier.padding(8.dp),
                                    enabled = cardIndex >= 0 && cardIndex < deck.size,
                                    onClick = {
                                        val card = deck[cardIndex]
                                        val temp = Pair(hand.key, hand.value + card)
                                        deck = deck - card
                                        hands = hands - hand.key
                                        hands = hands + temp
                                    },
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FloatingActionButton(
                    onClick = {
                        if (cardIndex >= 0) cardIndex--
                    },
                ) {
                    Icon(
                        imageVector = if (cardIndex >= 0) Icons.Default.SkipPrevious
                        else Icons.Default.Block,
                        contentDescription = null,
                    )
                }
                AnimatedContent(targetState = currentCardData, label = "CardChange") {
                    PlayingCard(
                        modifier = Modifier
                            .width(148.dp)
                            .aspectRatio(2.5f / 3.5f),
                        data = it,
                    )
                }
                FloatingActionButton(
                    onClick = {
                        if (cardIndex <= deck.lastIndex) cardIndex++
                    },
                ) {
                    Icon(
                        imageVector = if (cardIndex <= deck.lastIndex) Icons.Default.SkipNext
                        else Icons.Default.Block,
                        contentDescription = null,
                    )
                }
            }
            Text(text = "${cardIndex + 1}/${deck.size}")
            Spacer(modifier = Modifier.weight(1f))
            Row(
                Modifier
                    .padding(8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.Center,
            ) {
                ExtendedFloatingActionButton(
                    modifier = Modifier.padding(8.dp),
                    onClick = {
                        deck = deck.shuffled()
                        cardIndex = -1
                        Toast.makeText(this@PlayingCardsActivity, "Megkeverve", Toast.LENGTH_SHORT)
                            .show()
                    },
                    icon = {
                        Icon(imageVector = Icons.Default.Shuffle, contentDescription = null)
                    },
                    text = { Text(text = "Keverés") },
                )
                ExtendedFloatingActionButton(
                    modifier = Modifier.padding(8.dp),
                    onClick = {
                        hands = hands + Pair(
                            listOf(
                                "💀",
                                "👽",
                                "💩",
                                "🐵",
                                "🐶",
                                "🐱",
                                "🦝",
                                "🐷",
                                "🐼",
                                "🐸",
                                "🐔",
                            ).random(), listOf()
                        )
                    },
                    icon = { Icon(imageVector = Icons.Default.Add, contentDescription = null) },
                    text = { Text(text = "Új kéz") },
                )
            }
        }
    }

    @Composable
    fun viewTable() {
        var deck by remember { mutableStateOf(PlayingCardData.deck().shuffled()) }

        var dragData: PlayingCardData? by remember { mutableStateOf(null) }
        var dragPosition by remember { mutableStateOf(Offset(0f, 0f)) }
        var dragOffset by remember { mutableStateOf(Offset(0f, -400f)) }
        var currentDropTarget by remember { mutableStateOf(Size(-1, -1)) }

        var gridSize by remember { mutableStateOf(Size(5, 5)) }
        var gridData by remember { mutableStateOf(mapOf<Size, PlayingCardData>()) }

        Column(modifier = Modifier.fillMaxSize()) {
            repeat(gridSize.height) { y ->
                Row(modifier = Modifier.weight(1f)) {
                    repeat(gridSize.width) { x ->
                        DropTarget(
                            modifier = Modifier
                                .weight(1f)
                                .padding(8.dp),
                            dragPosition = dragPosition + dragOffset,
                            onDragEnter = { currentDropTarget = Size(x, y) },
                        ) {
                            PlayingCard(
                                modifier = Modifier.clickable {
                                    if (gridData[Size(x, y)] != null) deck += gridData[Size(x, y)]!!
                                    gridData -= Size(x, y)
                                },
                                data = if (currentDropTarget == Size(x, y)) dragData
                                else gridData[Size(x, y)]
                            )
                        }
                    }
                }
            }
            DropTarget(
                modifier = Modifier.weight(1f).padding(8.dp),
                dragPosition = dragPosition + dragOffset,
                onDragEnter = { currentDropTarget = Size(-1, -1) },
            ) {
                UzCard(
                    modifier = Modifier
                        .fillMaxSize()
                        .onGloballyPositioned { coords ->
                            dragOffset = coords.boundsInWindow().topLeft
                        }
                        .pointerInput(Unit) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { offset ->
                                    dragPosition = offset
                                    dragData = deck.first()
                                    deck = deck - deck.first()
                                },
                                onDrag = { change, offset ->
                                    change.consume()
                                    dragPosition += offset
                                },
                                onDragCancel = {
                                    if (dragData != null) deck = deck + dragData!!
                                    dragData = null
                                },
                                onDragEnd = {
                                    if (dragData == null) return@detectDragGesturesAfterLongPress

                                    if (currentDropTarget != Size(-1, -1)) {
                                        if (gridData[currentDropTarget] != null) {
                                            deck = deck + gridData[currentDropTarget]!!
                                        }
                                        gridData = gridData + Pair(
                                            currentDropTarget,
                                            dragData!!.copy()
                                        )
                                    } else {
                                        deck = deck + dragData!!
                                    }
                                    dragData = null
                                    currentDropTarget = Size(-1, -1)
                                },
                            )
                        },
                ) {
                    Text(
                        modifier = Modifier.padding(8.dp).fillMaxSize(),
                        text = if (dragData != null) "Visszavonás"
                        else "Húzz innen egy lapot (${deck.size})",
                        textAlign = TextAlign.Center,
                    )
                }
                if (dragData != null) {
                    Text(
                        modifier = Modifier
                            .zIndex(2f)
                            .graphicsLayer {
                                translationX = dragPosition.x// + dragOffset.x
                                translationY = dragPosition.y - 100.dp.toPx()// + dragOffset.y
                            },
                        text = dragData.toString(),
                    )
                }
            }
        }
    }
}