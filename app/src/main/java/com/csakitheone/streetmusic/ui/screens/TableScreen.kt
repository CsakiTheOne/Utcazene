package com.csakitheone.streetmusic.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.csakitheone.streetmusic.R
import com.csakitheone.streetmusic.data.LocalRepository
import com.csakitheone.streetmusic.data.model.TableItem
import com.csakitheone.streetmusic.navigation.LocalNavBackStack
import com.csakitheone.streetmusic.ui.components.NearbyConnectionsDisplay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableScreen() {
    val repository = LocalRepository.current
    val backStack = LocalNavBackStack.current
    val haptic = LocalHapticFeedback.current
    val tableItems by repository.tableItems.collectAsState()

    var tableWidth by remember { mutableIntStateOf(0) }
    var tableHeight by remember { mutableIntStateOf(0) }

    var fabExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Table") },
                navigationIcon = {
                    IconButton(onClick = { backStack.removeAt(backStack.lastIndex) }) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = "Back")
                    }
                },
                actions = {
                    NearbyConnectionsDisplay()
                    IconButton(onClick = { repository.clearTable() }) {
                        Icon(
                            painterResource(R.drawable.ic_delete_forever),
                            contentDescription = "Clear Table"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButtonMenu(
                expanded = fabExpanded,
                button = {
                    ToggleFloatingActionButton(
                        checked = fabExpanded,
                        onCheckedChange = { fabExpanded = it }
                    ) {
                        val painter = if (fabExpanded) {
                            painterResource(R.drawable.ic_close)
                        } else {
                            painterResource(R.drawable.ic_add)
                        }
                        Icon(painter, contentDescription = "Add Item")
                    }
                }
            ) {
                FloatingActionButtonMenuItem(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val newItem = TableItem.Die(
                            id = System.currentTimeMillis().toString(),
                            x = 0.5f,
                            y = 0.5f
                        )
                        repository.updateTableItem(newItem)
                        fabExpanded = false
                    },
                    icon = { Icon(painterResource(R.drawable.dice_6), contentDescription = null) },
                    text = { Text("Die") }
                )
                FloatingActionButtonMenuItem(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val newItem = TableItem.CardStack(
                            id = System.currentTimeMillis().toString(),
                            x = 0.5f,
                            y = 0.5f,
                            cards = (0..51).shuffled()
                        )
                        repository.updateTableItem(newItem)
                        fabExpanded = false
                    },
                    icon = {
                        Icon(
                            painterResource(R.drawable.ic_music_circle),
                            contentDescription = null
                        )
                    },
                    text = { Text("Card Stack") }
                )
                FloatingActionButtonMenuItem(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val newItem = TableItem.Coin(
                            id = System.currentTimeMillis().toString(),
                            x = 0.5f,
                            y = 0.5f
                        )
                        repository.updateTableItem(newItem)
                        fabExpanded = false
                    },
                    icon = { Icon(painterResource(R.drawable.ic_vote), contentDescription = null) },
                    text = { Text("Coin") }
                )
                FloatingActionButtonMenuItem(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val newItem = TableItem.Counter(
                            id = System.currentTimeMillis().toString(),
                            x = 0.5f,
                            y = 0.5f,
                            label = "HP"
                        )
                        repository.updateTableItem(newItem)
                        fabExpanded = false
                    },
                    icon = { Icon(painterResource(R.drawable.ic_info), contentDescription = null) },
                    text = { Text("Counter") }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFF2E7D32)) // Green table surface
                .onSizeChanged {
                    tableWidth = it.width
                    tableHeight = it.height
                }
        ) {
            tableItems.forEach { item ->
                TableItemView(
                    item = item,
                    tableWidth = tableWidth,
                    tableHeight = tableHeight,
                    onUpdate = { repository.updateTableItem(it) },
                    onRemove = { repository.removeTableItem(it) },
                    onStackMerge = { other ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (item is TableItem.CardStack) {
                            val merged = item.copy(
                                cards = item.cards + other.cards,
                                lastUpdated = System.currentTimeMillis()
                            )
                            repository.updateTableItem(merged)
                            repository.removeTableItem(other)
                        }
                    },
                    otherStacks = tableItems.filterIsInstance<TableItem.CardStack>()
                        .filter { it.id != item.id }
                )
            }
        }
    }
}

@Composable
fun TableItemView(
    item: TableItem,
    tableWidth: Int,
    tableHeight: Int,
    onUpdate: (TableItem) -> Unit,
    onRemove: (TableItem) -> Unit,
    onStackMerge: (TableItem.CardStack) -> Unit,
    otherStacks: List<TableItem.CardStack>
) {
    val haptic = LocalHapticFeedback.current
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    var isPickedUp by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isPickedUp) 1.2f else 1f, label = "Item scale")

    LaunchedEffect(item.x, item.y, tableWidth, tableHeight) {
        offsetX = item.x * tableWidth
        offsetY = item.y * tableHeight
    }

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .zIndex(if (isPickedUp) 1f else 0f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .pointerInput(item) {
                detectDragGestures(
                    onDragStart = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        isPickedUp = true
                    },
                    onDragEnd = {
                        isPickedUp = false
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val newX = if (tableWidth > 0) offsetX / tableWidth else item.x
                        val newY = if (tableHeight > 0) offsetY / tableHeight else item.y

                        // Check for merge if CardStack
                        if (item is TableItem.CardStack) {
                            val target = otherStacks.find { other ->
                                val dx = (other.x * tableWidth) - offsetX
                                val dy = (other.y * tableHeight) - offsetY
                                (dx * dx + dy * dy) < (64.dp.toPx() * 64.dp.toPx())
                            }
                            if (target != null) {
                                onStackMerge(target)
                                return@detectDragGestures
                            }
                        }

                        val updated = when (item) {
                            is TableItem.Die -> item.copy(
                                x = newX,
                                y = newY,
                                lastUpdated = System.currentTimeMillis()
                            )

                            is TableItem.CardStack -> item.copy(
                                x = newX,
                                y = newY,
                                lastUpdated = System.currentTimeMillis()
                            )

                            is TableItem.Coin -> item.copy(
                                x = newX,
                                y = newY,
                                lastUpdated = System.currentTimeMillis()
                            )

                            is TableItem.Counter -> item.copy(
                                x = newX,
                                y = newY,
                                lastUpdated = System.currentTimeMillis()
                            )
                        }
                        onUpdate(updated)
                    },
                    onDragCancel = {
                        isPickedUp = false
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                )
            }
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            when (item) {
                is TableItem.Die -> DieView(item, onUpdate)
                is TableItem.CardStack -> CardStackView(item, onUpdate)
                is TableItem.Coin -> CoinView(item, onUpdate)
                is TableItem.Counter -> CounterView(item, onUpdate)
            }
            IconButton(
                modifier = Modifier.size(24.dp),
                onClick = { onRemove(item) }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = "Remove",
                    modifier = Modifier.size(16.dp),
                    tint = Color.White.copy(alpha = 0.75f)
                )
            }
        }
    }
}

@Composable
fun DieView(die: TableItem.Die, onUpdate: (TableItem) -> Unit) {
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    var rollingValue by remember { mutableIntStateOf(die.value) }

    LaunchedEffect(die.value) {
        rollingValue = die.value
    }

    val diceRes = when (rollingValue) {
        1 -> R.drawable.dice_1
        2 -> R.drawable.dice_2
        3 -> R.drawable.dice_3
        4 -> R.drawable.dice_4
        5 -> R.drawable.dice_5
        else -> R.drawable.dice_6
    }

    Image(
        painter = painterResource(diceRes),
        contentDescription = "Die ${rollingValue}",
        modifier = Modifier
            .size(64.dp)
            .clickable {
                scope.launch {
                    repeat(10) {
                        rollingValue = (1..6).random()
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        kotlinx.coroutines.delay(60)
                    }
                    val finalValue = (1..6).random()
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onUpdate(
                        die.copy(
                            value = finalValue,
                            lastUpdated = System.currentTimeMillis()
                        )
                    )
                }
            }
    )
}

@Composable
fun CardStackView(stack: TableItem.CardStack, onUpdate: (TableItem) -> Unit) {
    val haptic = LocalHapticFeedback.current
    var showMenu by remember { mutableStateOf(false) }

    Box {
        Card(
            modifier = Modifier
                .size(width = 64.dp, height = 96.dp)
                .shadow(4.dp, RoundedCornerShape(4.dp))
                .clickable { showMenu = true },
            colors = CardDefaults.cardColors(
                containerColor = if (stack.isFlipped) Color.White else Color(0xFFB71C1C)
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (stack.isFlipped && stack.cards.isNotEmpty()) {
                    CardFace(stack.cards.last())
                } else {
                    Text("${stack.cards.size}", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
            DropdownMenuItem(
                text = { Text("Shuffle") },
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onUpdate(
                        stack.copy(
                            cards = stack.cards.shuffled(),
                            lastUpdated = System.currentTimeMillis()
                        )
                    )
                    showMenu = false
                }
            )
            DropdownMenuItem(
                text = { Text("Flip") },
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onUpdate(
                        stack.copy(
                            isFlipped = !stack.isFlipped,
                            lastUpdated = System.currentTimeMillis()
                        )
                    )
                    showMenu = false
                }
            )
            DropdownMenuItem(
                text = { Text("Pick top") },
                onClick = {
                    if (stack.cards.isNotEmpty()) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val top = stack.cards.last()
                        val newStack = stack.copy(
                            cards = stack.cards.dropLast(1),
                            lastUpdated = System.currentTimeMillis()
                        )
                        val singleCard = TableItem.CardStack(
                            id = System.currentTimeMillis().toString(),
                            x = stack.x + 0.05f,
                            y = stack.y + 0.05f,
                            cards = listOf(top),
                            isFlipped = true
                        )
                        onUpdate(newStack)
                        onUpdate(singleCard)
                    }
                    showMenu = false
                }
            )
            DropdownMenuItem(
                text = { Text("Split") },
                onClick = {
                    if (stack.cards.size > 1) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val mid = stack.cards.size / 2
                        val bottomHalf = stack.cards.take(stack.cards.size - mid)
                        val topHalf = stack.cards.takeLast(mid)

                        val newBottomStack =
                            stack.copy(cards = bottomHalf, lastUpdated = System.currentTimeMillis())
                        val newTopStack = TableItem.CardStack(
                            id = System.currentTimeMillis().toString(),
                            x = stack.x + 0.05f,
                            y = stack.y + 0.05f,
                            cards = topHalf,
                            isFlipped = stack.isFlipped
                        )
                        onUpdate(newBottomStack)
                        onUpdate(newTopStack)
                    }
                    showMenu = false
                }
            )
        }
    }
}

@Composable
fun CardFace(cardIndex: Int) {
    val suit = when (cardIndex / 13) {
        0 -> "♠"
        1 -> "♥"
        2 -> "♦"
        else -> "♣"
    }
    val rank = when (val r = cardIndex % 13) {
        0 -> "A"
        in 1..9 -> (r + 1).toString()
        10 -> "J"
        11 -> "Q"
        else -> "K"
    }
    val color = if (cardIndex / 13 == 1 || cardIndex / 13 == 2) Color.Red else Color.Black

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(rank, color = color, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(suit, color = color, fontSize = 24.sp)
    }
}

@Composable
fun CoinView(coin: TableItem.Coin, onUpdate: (TableItem) -> Unit) {
    val haptic = LocalHapticFeedback.current
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color(0xFFFFD700))
            .shadow(2.dp, CircleShape)
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onUpdate(
                    coin.copy(
                        isHeads = listOf(true, false).random(),
                        lastUpdated = System.currentTimeMillis()
                    )
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            if (coin.isHeads) "H" else "T",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Color.Black
        )
    }
}

@Composable
fun CounterView(counter: TableItem.Counter, onUpdate: (TableItem) -> Unit) {
    val haptic = LocalHapticFeedback.current
    var showEditLabelDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.width(100.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                counter.label.ifBlank { "Label" },
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.clickable { showEditLabelDialog = true }
            )
            Text("${counter.count}", style = MaterialTheme.typography.headlineMedium)
            Row {
                IconButton(onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onUpdate(
                        counter.copy(
                            count = counter.count - 1,
                            lastUpdated = System.currentTimeMillis()
                        )
                    )
                }) {
                    Text("-")
                }
                IconButton(onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onUpdate(
                        counter.copy(
                            count = counter.count + 1,
                            lastUpdated = System.currentTimeMillis()
                        )
                    )
                }) {
                    Text("+")
                }
            }
        }
    }

    if (showEditLabelDialog) {
        var labelText by remember { mutableStateOf(counter.label) }
        AlertDialog(
            onDismissRequest = { showEditLabelDialog = false },
            title = { Text("Edit Label") },
            text = {
                TextField(
                    value = labelText,
                    onValueChange = { labelText = it },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onUpdate(
                        counter.copy(
                            label = labelText,
                            lastUpdated = System.currentTimeMillis()
                        )
                    )
                    showEditLabelDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditLabelDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

