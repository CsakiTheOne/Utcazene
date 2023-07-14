package com.csakitheone.streetmusic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.csakitheone.streetmusic.data.EventsProvider
import com.csakitheone.streetmusic.model.Author
import com.csakitheone.streetmusic.model.Event
import com.csakitheone.streetmusic.ui.components.AuthorCard
import com.csakitheone.streetmusic.ui.components.EventCard
import com.csakitheone.streetmusic.ui.components.util.ListPreferenceHolder
import com.csakitheone.streetmusic.ui.theme.UtcazeneTheme
import com.google.gson.reflect.TypeToken

class PlacesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PlacesScreen()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Preview
    @Composable
    fun PlacesScreen() {
        val scroll = rememberLazyListState()

        var eventsPinned by remember { mutableStateOf<List<Event>>(listOf()) }
        var isOnlyPinned by remember { mutableStateOf(false) }

        val eventsGrouped by remember { mutableStateOf(
            EventsProvider.getEvents(this)
                .groupBy { it.place }
                .entries
                .toList()
        ) }

        ListPreferenceHolder(
            id = "eventsPinned",
            value = eventsPinned,
            onValueChanged = { eventsPinned = it.toList() },
            type = object : TypeToken<Event>() {}.type,
        )

        UtcazeneTheme {
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
                            colors = TopAppBarDefaults.smallTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background,
                                titleContentColor = MaterialTheme.colorScheme.onBackground,
                                navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                                actionIconContentColor = MaterialTheme.colorScheme.onBackground,
                            ),
                        )
                    }
                    LazyColumn(
                        modifier = Modifier.padding(horizontal = 8.dp),
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
                            }
                        }
                        items(items = eventsGrouped, key = { it.key.name }) { entry ->
                            Column {
                                Text(
                                    modifier = Modifier.padding(8.dp),
                                    text = entry.key.name,
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Row(
                                    Modifier.horizontalScroll(rememberScrollState()),
                                ) {
                                    (19..22).map { day ->
                                        Column {
                                            Text(
                                                modifier = Modifier.padding(8.dp),
                                                text = "$day.",
                                                style = MaterialTheme.typography.bodySmall,
                                            )
                                            entry.value
                                                .filter { it.day == day }
                                                .filter { !isOnlyPinned || eventsPinned.contains(it) }
                                                .map { event ->
                                                    EventCard(
                                                        modifier = Modifier
                                                            .padding(8.dp)
                                                            .width(200.dp),
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
                        }
                    }
                }
            }
        }
    }
}
