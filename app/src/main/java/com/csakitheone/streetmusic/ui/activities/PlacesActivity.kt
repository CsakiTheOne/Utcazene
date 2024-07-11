package com.csakitheone.streetmusic.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.ViewDay
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
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
import com.csakitheone.streetmusic.R
import com.csakitheone.streetmusic.data.EventsProvider
import com.csakitheone.streetmusic.model.Event
import com.csakitheone.streetmusic.model.Place
import com.csakitheone.streetmusic.ui.components.DaySelectorRow
import com.csakitheone.streetmusic.ui.components.EventCard
import com.csakitheone.streetmusic.ui.components.MenuCard
import com.csakitheone.streetmusic.ui.components.util.ListPreferenceHolder
import com.csakitheone.streetmusic.ui.theme.UtcazeneTheme
import com.csakitheone.streetmusic.util.CustomTabsManager
import com.csakitheone.streetmusic.util.Helper.Companion.toLocalTime
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import java.time.LocalTime

class PlacesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PlacesScreen()
        }
        enableEdgeToEdge()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Preview
    @Composable
    fun PlacesScreen() {
        UtcazeneTheme {
            val scroll = rememberLazyListState()

            var selectedDay by remember {
                mutableStateOf(
                    if ((24..27).contains(LocalDate.now().dayOfMonth)) LocalDate.now().dayOfMonth
                    else 24
                )
            }

            var eventsPinned by remember { mutableStateOf<List<Event>>(listOf()) }
            var isOnlyPinned by remember { mutableStateOf(false) }
            var isOnlyUpcoming by remember { mutableStateOf(true) }

            var eventsGrouped by remember { mutableStateOf(listOf<Map.Entry<Place, List<Event>>>()) }

            LaunchedEffect(selectedDay, eventsPinned, isOnlyPinned, isOnlyUpcoming) {
                EventsProvider.getEventsThisYear(this@PlacesActivity) { events ->
                    eventsGrouped = events
                        .filter { it.day == selectedDay }
                        .filter { !isOnlyPinned || eventsPinned.contains(it) }
                        .filter { !isOnlyUpcoming || LocalTime.now().isBefore(it.time.toLocalTime()) }
                        .sortedBy { it.time }
                        .groupBy { it.place }
                        .entries
                        .toList()
                }
            }

            ListPreferenceHolder(
                id = "eventsPinned",
                value = eventsPinned,
                onValueChanged = { eventsPinned = it.toList() },
                type = object : TypeToken<Event>() {}.type,
            )

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
                            title = { Text(text = stringResource(id = R.string.places)) },
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
                                        CustomTabsManager.open(
                                            this@PlacesActivity,
                                            "https://www.google.com/maps/d/u/0/embed?mid=12plW9qjTupsu26_lLGD-lnE4jqUczO4U&ehbc=2E312F&ll=47.09391673012697%2C17.90851453104826&z=15"
                                        )
                                    }
                                ) {
                                    Icon(imageVector = Icons.Default.Map, contentDescription = null)
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
                            .weight(1f)
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
                        items(items = eventsGrouped, key = { it.key.name }) { entry ->
                            Column {
                                if (entry.key.geoLink != null) {
                                    MenuCard(
                                        modifier = Modifier.padding(8.dp),
                                        onClick = {
                                            startActivity(
                                                Intent(
                                                    Intent.ACTION_VIEW,
                                                    Uri.parse(entry.key.geoLink)
                                                )
                                            )
                                        },
                                        imageVector = Icons.Default.Place,
                                        title = entry.key.name,
                                    )
                                } else {
                                    Text(
                                        modifier = Modifier.padding(8.dp),
                                        text = entry.key.name,
                                        style = MaterialTheme.typography.titleMedium,
                                    )
                                }
                                Row(
                                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                                ) {
                                    entry.value.map { event ->
                                        EventCard(
                                            modifier = Modifier
                                                .padding(8.dp)
                                                .width(300.dp),
                                            event = event,
                                            isPinned = eventsPinned.contains(event),
                                            onPinnedChangeRequest = {
                                                eventsPinned = if (it) eventsPinned + event
                                                else eventsPinned.filter { e -> e != event }
                                            },
                                            showPlace = false,
                                        )
                                    }
                                }
                            }
                        }
                    }
                    NavigationBar {
                        DaySelectorRow(
                            selectedDay = selectedDay,
                            onChange = { selectedDay = it },
                        )
                    }
                }
            }
        }
    }
}
