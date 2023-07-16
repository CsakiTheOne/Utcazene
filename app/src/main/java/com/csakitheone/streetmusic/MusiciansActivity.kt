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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.Label
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.csakitheone.streetmusic.data.EventsProvider
import com.csakitheone.streetmusic.model.Musician
import com.csakitheone.streetmusic.ui.components.MusicianCard
import com.csakitheone.streetmusic.ui.components.util.ListPreferenceHolder
import com.csakitheone.streetmusic.ui.theme.UtcazeneTheme
import com.google.gson.reflect.TypeToken

class MusiciansActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MusiciansScreen()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Preview
    @Composable
    fun MusiciansScreen() {
        val scroll = rememberLazyListState()

        var posterId: Int? by remember { mutableStateOf(null) }

        var musiciansPinned by remember { mutableStateOf<List<Musician>>(listOf()) }
        var isOnlyPinned by remember { mutableStateOf(false) }
        var filterTags by remember { mutableStateOf(listOf<Int>()) }

        var musicians by remember { mutableStateOf(listOf<Musician>()) }
        val visibleMusicians by remember(musicians, musiciansPinned, isOnlyPinned, filterTags) {
            mutableStateOf(
                musicians
                    .filter { filterTags.isEmpty() || filterTags == it.tags }
                    .filter { !isOnlyPinned || (isOnlyPinned && musiciansPinned.contains(it)) }
                    .sortedBy { it.name }
            )
        }

        LaunchedEffect(Unit) {
            EventsProvider.getEvents(this@MusiciansActivity) { events ->
                musicians = events.groupBy { it.musician }.keys.toList()
            }
        }

        ListPreferenceHolder(
            id = "authorsPinned",
            value = musiciansPinned,
            onValueChanged = { musiciansPinned = it.toList() },
            type = object : TypeToken<Musician>() {}.type,
        )

        UtcazeneTheme {
            if (posterId != null) {
                Dialog(
                    properties = DialogProperties(usePlatformDefaultWidth = false),
                    onDismissRequest = { posterId = null },
                ) {
                    if (posterId != null) {
                        Image(
                            painter = painterResource(id = posterId!!),
                            contentDescription = null,
                        )
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
                    Surface(
                        modifier = Modifier.zIndex(2f),
                        shadowElevation = if (scroll.canScrollBackward) 16.dp else 0.dp,
                    ) {
                        TopAppBar(
                            title = { Text(text = stringResource(id = R.string.musicians)) },
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
                                musicians
                                    .flatMap { it.tags ?: listOf() }
                                    .distinct()
                                    .map { tag ->
                                        ElevatedFilterChip(
                                            modifier = Modifier.padding(8.dp),
                                            selected = filterTags.contains(tag),
                                            onClick = {
                                                filterTags =
                                                    if (filterTags.contains(tag)) filterTags.filter { it != tag }
                                                    else filterTags + tag
                                            },
                                            label = { Text(text = stringResource(id = tag)) },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = if (filterTags.contains(tag)) Icons.Filled.Label
                                                    else Icons.Outlined.Label,
                                                    contentDescription = null,
                                                )
                                            }
                                        )
                                    }
                            }
                        }
                        items(items = visibleMusicians, key = { it.name }) { musician ->
                            MusicianCard(
                                modifier = Modifier.padding(8.dp),
                                musician = musician,
                                isPinned = musiciansPinned.contains(musician),
                                onPinnedChangeRequest = {
                                    musiciansPinned = if (it) musiciansPinned + musician
                                    else musiciansPinned.filter { a -> a != musician }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
