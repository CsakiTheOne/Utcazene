package com.csakitheone.streetmusic.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.csakitheone.streetmusic.R
import com.csakitheone.streetmusic.data.DataStore
import com.csakitheone.streetmusic.data.EventsProvider
import com.csakitheone.streetmusic.model.Musician
import com.csakitheone.streetmusic.ui.components.BigMusicianCard
import com.csakitheone.streetmusic.ui.theme.UtcazeneTheme
import kotlinx.coroutines.launch

class MusiciansActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MusiciansScreen()
        }
        enableEdgeToEdge()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Preview
    @Composable
    fun MusiciansScreen() {
        UtcazeneTheme {
            val coroutineScope = rememberCoroutineScope()
            val scroll = rememberLazyListState()

            val favoriteMusicians by DataStore.getState(
                key = DataStore.favoriteMusiciansKey,
                defaultValue = setOf()
            )

            val shareSheetState = rememberModalBottomSheetState()
            var isShareSheetOpen by remember { mutableStateOf(false) }

            val searchFieldFocusRequester = remember { FocusRequester() }
            var isSearchVisible by remember { mutableStateOf(false) }
            var searchQuery by remember(isSearchVisible) { mutableStateOf("") }

            var isOnlyPinned by remember { mutableStateOf(false) }
            var filterTags by remember { mutableStateOf(listOf<String>()) }

            var musicians by remember { mutableStateOf(listOf<Musician>()) }
            val visibleMusicians by remember(
                musicians,
                favoriteMusicians,
                isOnlyPinned,
                filterTags,
                isSearchVisible,
                searchQuery,
            ) {
                if (isSearchVisible) derivedStateOf {
                    musicians
                        .filter { it.name.contains(searchQuery, ignoreCase = true) }
                        .sortedBy { it.name }
                } else derivedStateOf {
                    musicians
                        .filter { filterTags.isEmpty() || filterTags == it.tags }
                        .filter { !isOnlyPinned || (isOnlyPinned && favoriteMusicians.contains(it.name)) }
                        .sortedBy { it.name }
                }
            }

            LaunchedEffect(Unit) {
                EventsProvider.getEventsThisYear(this@MusiciansActivity) { events ->
                    musicians = events.groupBy { it.musician }.keys.distinct()
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
                            Text(text = "Share favorites as list")
                        },
                        onClick = {
                            val text = favoriteMusicians
                                .sorted()
                                .joinToString("\n") { "- $it" }
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
                    Surface(
                        modifier = Modifier.zIndex(2f),
                        shadowElevation = if (scroll.canScrollBackward) 16.dp else 0.dp,
                    ) {
                        AnimatedContent(targetState = isSearchVisible) {
                            if (it) {
                                SideEffect {
                                    searchFieldFocusRequester.requestFocus()
                                }
                                TextField(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .focusRequester(searchFieldFocusRequester)
                                        .fillMaxWidth()
                                        .statusBarsPadding(),
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = null,
                                        )
                                    },
                                    trailingIcon = {
                                        IconButton(
                                            onClick = {
                                                if (searchQuery.isNotEmpty()) searchQuery = ""
                                                else isSearchVisible = false
                                            },
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = null,
                                            )
                                        }
                                    },
                                )
                            }
                            else {
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
                                    actions = {
                                        IconButton(
                                            onClick = { isShareSheetOpen = true },
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Share,
                                                contentDescription = null,
                                            )
                                        }
                                        IconButton(
                                            onClick = { isSearchVisible = !isSearchVisible },
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Search,
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
                            }
                        }
                    }
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
                        musicians.flatMap { it.tags ?: listOf() }
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
                                    label = { Text(text = stringResource(id = Musician.tagStrings[tag]!!)) },
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
                    LazyColumn(
                        modifier = Modifier
                            .padding(horizontal = 8.dp),
                        state = scroll,
                    ) {
                        items(items = visibleMusicians, key = { it.id }) { musician ->
                            BigMusicianCard(
                                modifier = Modifier.padding(8.dp),
                                musician = musician,
                                isPinned = favoriteMusicians.contains(musician.name),
                                onPinnedChangeRequest = {
                                    DataStore.setValue(
                                        this@MusiciansActivity,
                                        DataStore.favoriteMusiciansKey,
                                        if (favoriteMusicians.contains(musician.name))
                                            favoriteMusicians.filter { it != musician.name }.toSet()
                                        else favoriteMusicians + musician.name
                                    )
                                },
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.navigationBarsPadding())
                        }
                    }
                }
            }
        }
    }
}
