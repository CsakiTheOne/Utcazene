package com.csakitheone.streetmusic.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.ViewDay
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.csakitheone.streetmusic.R
import com.csakitheone.streetmusic.data.DataStore
import com.csakitheone.streetmusic.data.EventsProvider
import com.csakitheone.streetmusic.model.Event
import com.csakitheone.streetmusic.ui.components.DaySelectorRow
import com.csakitheone.streetmusic.ui.components.EventCard
import com.csakitheone.streetmusic.ui.components.NowIndicator
import com.csakitheone.streetmusic.ui.theme.UtcazeneTheme
import com.csakitheone.streetmusic.util.Helper.Companion.toLocalTime
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

class CalendarActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalendarScreen()
        }
        enableEdgeToEdge()
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    @Preview
    @Composable
    fun CalendarScreen() {
        UtcazeneTheme {
            val coroutineScope = rememberCoroutineScope()

            val firstDay = 24
            val eventDurationDays = 4
            val pagerState = rememberPagerState(
                initialPage = LocalDate.now().dayOfMonth - firstDay,
                pageCount = { eventDurationDays },
            )

            val shareSheetState = rememberModalBottomSheetState()
            var isShareSheetOpen by remember { mutableStateOf(false) }

            val favoriteEvents by DataStore.getState(
                key = DataStore.favoriteEventsKey,
                defaultValue = setOf(),
            )
            var isOnlyPinned by remember { mutableStateOf(false) }
            var isOnlyUpcoming by remember(pagerState.currentPage) {
                mutableStateOf(LocalDate.now().dayOfMonth == firstDay + pagerState.currentPage)
            }

            var events by remember { mutableStateOf(listOf<Event>()) }

            LaunchedEffect(Unit) {
                EventsProvider.getEventsThisYear(this@CalendarActivity) {
                    events = it
                }
            }

            if (isShareSheetOpen) {
                ModalBottomSheet(
                    onDismissRequest = {
                        coroutineScope.launch {
                            shareSheetState.hide()
                        }.invokeOnCompletion { isShareSheetOpen = false }
                    },
                ) {
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.List,
                                contentDescription = null,
                            )
                        },
                        text = {
                            //TODO: translate
                            Text(text = "Share selected day's favorites as list")
                        },
                        onClick = {
                            val allFavEvents = favoriteEvents
                                .mapNotNull { events.find { event -> event.toString() == it } }
                            val today = allFavEvents.filter { it.day == firstDay + pagerState.currentPage }
                            val text = today
                                .sortedBy { it.time }
                                .joinToString("\n") { "- ${it.toStringTimeAndName()}" }
                            startActivity(
                                Intent.createChooser(
                                    Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, text)
                                    },
                                    null
                                )
                            )
                            coroutineScope.launch {
                                shareSheetState.hide()
                            }.invokeOnCompletion { isShareSheetOpen = false }
                        },
                    )
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.List,
                                contentDescription = null,
                            )
                        },
                        text = {
                            //TODO: translate
                            Text(text = "Share all favorites as list")
                        },
                        onClick = {
                            val allFavEvents = favoriteEvents
                                .mapNotNull { events.find { event -> event.toString() == it } }
                            val text = allFavEvents
                                .sortedBy { it.time }
                                .sortedBy { it.day }
                                .joinToString("\n") { "- ${it.toStringDayTimeAndName()}" }
                            startActivity(
                                Intent.createChooser(
                                    Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, text)
                                    },
                                    null
                                )
                            )
                            coroutineScope.launch {
                                shareSheetState.hide()
                            }.invokeOnCompletion { isShareSheetOpen = false }
                        },
                    )
                    TextButton(
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(8.dp),
                        onClick = {
                            coroutineScope.launch {
                                shareSheetState.hide()
                            }.invokeOnCompletion { isShareSheetOpen = false }
                        },
                    ) {
                        Text(text = stringResource(id = R.string.close))
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    TopAppBar(
                        title = { Text(text = stringResource(id = R.string.events)) },
                        navigationIcon = {
                            IconButton(onClick = { finish() }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = null,
                                )
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = {
                                    isShareSheetOpen = true
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            titleContentColor = MaterialTheme.colorScheme.onBackground,
                            navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                            actionIconContentColor = MaterialTheme.colorScheme.onBackground,
                        ),
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ElevatedFilterChip(
                            modifier = Modifier.padding(8.dp),
                            selected = isOnlyPinned,
                            onClick = { isOnlyPinned = !isOnlyPinned },
                            label = { Text(text = stringResource(id = R.string.filter_pinned)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (isOnlyPinned) Icons.Default.Star
                                    else Icons.Default.StarBorder,
                                    contentDescription = null,
                                )
                            },
                        )
                        AnimatedVisibility(visible = LocalDate.now().dayOfMonth == firstDay + pagerState.currentPage) {
                            ElevatedFilterChip(
                                modifier = Modifier.padding(8.dp),
                                selected = isOnlyUpcoming,
                                onClick = { isOnlyUpcoming = !isOnlyUpcoming },
                                label = { Text(text = stringResource(id = R.string.filter_upcoming)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (isOnlyUpcoming) Icons.Default.SkipNext
                                        else Icons.Default.ViewDay,
                                        contentDescription = null,
                                    )
                                },
                            )
                        }
                    }
                    HorizontalPager(
                        modifier = Modifier.weight(1f),
                        state = pagerState,
                    ) { pageIndex ->
                        val scroll = rememberLazyListState()
                        val eventsToday by remember(
                            events,
                            pageIndex,
                            favoriteEvents,
                            isOnlyPinned,
                            isOnlyUpcoming
                        ) {
                            mutableStateOf(
                                events
                                    .asSequence()
                                    .filter {
                                        !isOnlyUpcoming || (LocalTime.now()
                                            .isBefore(it.time.toLocalTime()) || it.time.toLocalTime().hour < 5)
                                    }
                                    .filter { !isOnlyPinned || favoriteEvents.contains(it.toString()) }
                                    .filter { it.day == firstDay + pageIndex }
                                    .sortedBy { it.musician.name }
                                    .sortedBy { if (it.time.toLocalTime().hour < 5) "b" + it.time else it.time }
                                    .toList()
                            )
                        }
                        val nextEvent by remember(eventsToday) {
                            mutableStateOf(
                                eventsToday.firstOrNull {
                                    LocalTime.now().isBefore(it.time.toLocalTime())
                                }
                            )
                        }

                        LaunchedEffect(pagerState.currentPage) {
                            scroll.scrollToItem(0)
                        }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                            state = scroll,
                        ) {
                            items(
                                items = eventsToday,
                                key = { "${it.id} ${it.musician.name} ${it.day} ${it.time}" }) { event ->
                                if (event == nextEvent) {
                                    NowIndicator(modifier = Modifier.padding(8.dp))
                                }
                                EventCard(
                                    modifier = Modifier.padding(8.dp),
                                    event = event,
                                    isPinned = favoriteEvents.contains(event.toString()),
                                    onPinnedChangeRequest = {
                                        DataStore.setValue(
                                            this@CalendarActivity,
                                            DataStore.favoriteEventsKey,
                                            if (favoriteEvents.contains(event.toString()))
                                                favoriteEvents - event.toString()
                                            else favoriteEvents + event.toString()
                                        )
                                    },
                                )
                            }
                        }
                    }
                    NavigationBar {
                        DaySelectorRow(
                            selectedDay = firstDay + pagerState.currentPage,
                            onChange = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(it - firstDay)
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}
