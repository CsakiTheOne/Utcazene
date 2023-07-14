package com.csakitheone.streetmusic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.ViewDay
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.csakitheone.streetmusic.util.Helper.Companion.toLocalTime
import com.csakitheone.streetmusic.data.EventsProvider
import com.csakitheone.streetmusic.model.Event
import com.csakitheone.streetmusic.ui.components.DaySelectorDialog
import com.csakitheone.streetmusic.ui.components.EventCard
import com.csakitheone.streetmusic.ui.components.NowIndicator
import com.csakitheone.streetmusic.ui.components.util.ListPreferenceHolder
import com.csakitheone.streetmusic.ui.theme.UtcazeneTheme
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import java.time.LocalTime

class CalendarActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalendarScreen()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Preview
    @Composable
    fun CalendarScreen() {
        val scroll = rememberLazyListState()

        var selectedDay by remember {
            mutableStateOf(
                if ((19..22).contains(LocalDate.now().dayOfMonth)) LocalDate.now().dayOfMonth
                else 19
            )
        }
        var isDaySelectorDialogVisible by remember { mutableStateOf(false) }

        var isOnlyUpcoming by remember { mutableStateOf(true) }
        var eventsPinned by remember { mutableStateOf<List<Event>>(listOf()) }
        var isOnlyPinned by remember { mutableStateOf(false) }

        val events by remember { mutableStateOf(EventsProvider.getEvents(this)) }
        val eventsToday by remember(
            events,
            selectedDay,
            eventsPinned,
            isOnlyPinned,
            isOnlyUpcoming
        ) {
            mutableStateOf(
                events
                    .asSequence()
                    .filter {
                        !isOnlyUpcoming || (LocalTime.now().isBefore(it.time.toLocalTime()) || it.time.toLocalTime().hour < 5)
                    }
                    .filter { !isOnlyPinned || eventsPinned.contains(it) }
                    .filter { it.day == selectedDay }
                    .sortedBy { it.author.name }
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

        ListPreferenceHolder(
            id = "eventsPinned",
            value = eventsPinned,
            onValueChanged = { eventsPinned = it.toList() },
            type = object : TypeToken<Event>() {}.type,
        )

        UtcazeneTheme {
            if (isDaySelectorDialogVisible) {
                DaySelectorDialog(
                    selectedDay = selectedDay,
                    onChanged = { selectedDay = it },
                    onDismissRequest = { isDaySelectorDialogVisible = false },
                )
            }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Surface(
                        modifier = Modifier.zIndex(2f),
                        shadowElevation = if (scroll.canScrollBackward) 16.dp else 0.dp,
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
                            colors = TopAppBarDefaults.smallTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background,
                                titleContentColor = MaterialTheme.colorScheme.onBackground,
                                navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                                actionIconContentColor = MaterialTheme.colorScheme.onBackground,
                            ),
                        )
                    }
                    LazyColumn(
                        modifier = Modifier
                            .padding(horizontal = 8.dp),
                        state = scroll,
                    ) {
                        item {
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                ElevatedFilterChip(
                                    modifier = Modifier.padding(8.dp),
                                    selected = LocalDate.now().dayOfMonth == selectedDay,
                                    onClick = { isDaySelectorDialogVisible = true },
                                    label = { Text(text = "$selectedDay.") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.CalendarToday,
                                            contentDescription = null,
                                        )
                                    },
                                )
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
                        items(
                            items = eventsToday,
                            key = { "${it.author.name} ${it.day} ${it.time}" }) { event ->
                            if (event == nextEvent) {
                                NowIndicator(modifier = Modifier.padding(8.dp))
                            }
                            EventCard(
                                modifier = Modifier.padding(8.dp),
                                event = event,
                                isPinned = eventsPinned.contains(event),
                                onPinnedChangeRequest = {
                                    eventsPinned = if (it) eventsPinned + event
                                    else eventsPinned.filter { e -> e != event }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
