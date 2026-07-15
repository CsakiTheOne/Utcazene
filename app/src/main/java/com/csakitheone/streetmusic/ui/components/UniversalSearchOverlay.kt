package com.csakitheone.streetmusic.ui.components

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.csakitheone.streetmusic.R
import com.csakitheone.streetmusic.data.CombinedRepository
import com.csakitheone.streetmusic.data.LocalRepository
import com.csakitheone.streetmusic.navigation.LocalNavBackStack
import com.csakitheone.streetmusic.navigation.label

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniversalSearchOverlay(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit = {},
) {
    val context = LocalContext.current
    val backStack = LocalNavBackStack.current
    val repository = LocalRepository.current

    BackHandler {
        onDismissRequest()
    }

    var isInitialized by remember { mutableStateOf(false) }
    LaunchedEffect(backStack.size) {
        if (isInitialized) {
            onDismissRequest()
        }
        isInitialized = true
    }

    val focusRequester = remember { FocusRequester() }

    val artists by repository.artists.collectAsState(initial = emptyList())
    val events by repository.events.collectAsState(initial = emptyList())
    val db by remember(artists, events) {
        derivedStateOf { CombinedRepository.getEverything(artists, events) }
    }

    var isExpanded by remember { mutableStateOf(true) }
    var query by remember { mutableStateOf("") }

    val results by remember(query, db) {
        derivedStateOf {
            if (query.isBlank() || artists.isEmpty() || events.isEmpty()) {
                return@derivedStateOf emptyList()
            }
            CombinedRepository.filterEverything(db, query)
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    SearchBar(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium),
        inputField = {
            SearchBarDefaults.InputField(
                modifier = Modifier.focusRequester(focusRequester),
                placeholder = { Text("Search anything...") },
                query = query,
                onQueryChange = { query = it },
                onSearch = {},
                expanded = isExpanded,
                onExpandedChange = { isExpanded = it },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (query.isBlank()) {
                                onDismissRequest()
                            } else {
                                query = ""
                            }
                        },
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_close),
                            contentDescription = null,
                        )
                    }
                },
            )
        },
        expanded = isExpanded,
        onExpandedChange = { isExpanded = it },
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp),
        ) {
            if (results.isEmpty()) {
                item {
                    Text(
                        text = "You can search for artists, Utcazene and external events here!",
                        textAlign = TextAlign.Center,
                    )
                }
            }
            items(items = results) { any ->
                CombinedDisplay(
                    data = any,
                    onClick = CombinedRepository.getClickActionForAny(context, backStack, any),
                )
            }
        }
    }
}