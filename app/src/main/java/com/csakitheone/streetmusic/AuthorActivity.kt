package com.csakitheone.streetmusic

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.csakitheone.streetmusic.util.BatteryManager
import com.csakitheone.streetmusic.data.EventsProvider
import com.csakitheone.streetmusic.model.Author
import com.csakitheone.streetmusic.model.Event
import com.csakitheone.streetmusic.ui.components.EventCard
import com.csakitheone.streetmusic.ui.components.MenuCard
import com.csakitheone.streetmusic.ui.components.UzCard
import com.csakitheone.streetmusic.ui.components.util.ListPreferenceHolder
import com.csakitheone.streetmusic.ui.theme.UtcazeneTheme
import com.csakitheone.streetmusic.util.Helper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class AuthorActivity : ComponentActivity() {
    companion object {
        const val EXTRA_AUTHOR_JSON = "author_json"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AuthorScreen()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Preview
    @Composable
    fun AuthorScreen() {
        val context = LocalContext.current

        var author: Author? by remember { mutableStateOf(null) }
        val events by remember(author) {
            mutableStateOf(
                EventsProvider.getEvents(this)
                    .filter { it.author == author }
                    .sortedBy { it.time }
                    .groupBy { it.day }
            )
        }
        var authorsPinned by remember { mutableStateOf<List<Author>>(listOf()) }
        val isPinned by remember(author, authorsPinned) {
            mutableStateOf(authorsPinned.contains(author))
        }
        var eventsPinned by remember { mutableStateOf<List<Event>>(listOf()) }

        LaunchedEffect(Unit) {
            author = Gson().fromJson(intent.getStringExtra(EXTRA_AUTHOR_JSON), Author::class.java)
        }

        ListPreferenceHolder(
            id = "authorsPinned",
            value = authorsPinned,
            onValueChanged = { authorsPinned = it.toList() },
            type = object : TypeToken<Author>() {}.type,
        )

        ListPreferenceHolder(
            id = "eventsPinned",
            value = eventsPinned,
            onValueChanged = { eventsPinned = it.toList() },
            type = object : TypeToken<Event>() {}.type,
        )

        UtcazeneTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column {
                    TopAppBar(
                        title = {
                            Column {
                                Text(text = author?.name ?: "Előadó neve")
                                if (author?.country != null) {
                                    Text(
                                        text = author?.country ?: "Ország",
                                        style = MaterialTheme.typography.labelSmall,
                                    )
                                }
                            }
                        },
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
                                modifier = Modifier.padding(start = 8.dp),
                                onClick = {
                                    authorsPinned = if (!isPinned) authorsPinned + author!!
                                    else authorsPinned.filter { it != author }
                                },
                            ) {
                                Icon(
                                    imageVector = if (isPinned) Icons.Default.Star
                                    else Icons.Default.StarBorder,
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
                    Column(
                        modifier = Modifier
                            .padding(8.dp)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        if (!BatteryManager.isBatterySaverEnabled && Helper.isUnmeteredNetworkAvailable(
                                context
                            )
                        ) {
                            OutlinedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                            ) {
                                AsyncImage(
                                    modifier = Modifier.fillMaxWidth(),
                                    model = if (author != null && !author?.imageUrl.isNullOrBlank()) author!!.imageUrl
                                    else "https://http.cat/images/404.jpg",
                                    contentDescription = null,
                                )
                            }
                        }
                        if (author?.description != null) {
                            UzCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        modifier = Modifier.padding(8.dp),
                                        text = author!!.description!!
                                    )
                                }
                            }
                        }
                        if (author?.youtubeUrl != null) {
                            MenuCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                onClick = {
                                    startActivity(
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse(author!!.youtubeUrl!!)
                                        )
                                    )
                                },
                                painter = painterResource(id = R.drawable.ic_youtube),
                                title = stringResource(id = R.string.watch_on_youtube),
                            )
                        }
                        Row {
                            author?.tags?.map { tag ->
                                FilterChip(
                                    modifier = Modifier.padding(8.dp),
                                    selected = false,
                                    onClick = {},
                                    label = { Text(text = stringResource(id = tag)) },
                                )
                            }
                        }
                        Text(
                            modifier = Modifier.padding(8.dp),
                            text = stringResource(id = R.string.author_performances),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        events.map { day ->
                            Text(
                                modifier = Modifier.padding(8.dp),
                                text = "${stringResource(id = R.string.month_july)} ${day.key}.",
                            )
                            day.value.map { event ->
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
}
