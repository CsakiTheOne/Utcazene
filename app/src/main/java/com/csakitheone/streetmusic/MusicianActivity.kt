package com.csakitheone.streetmusic

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.csakitheone.streetmusic.data.EventsProvider
import com.csakitheone.streetmusic.data.UzApi
import com.csakitheone.streetmusic.model.Event
import com.csakitheone.streetmusic.model.Musician
import com.csakitheone.streetmusic.ui.components.EventCard
import com.csakitheone.streetmusic.ui.components.MenuCard
import com.csakitheone.streetmusic.ui.components.UzCard
import com.csakitheone.streetmusic.ui.components.util.ListPreferenceHolder
import com.csakitheone.streetmusic.ui.theme.UtcazeneTheme
import com.csakitheone.streetmusic.util.BatterySaverManager
import com.csakitheone.streetmusic.util.CustomTabsManager
import com.csakitheone.streetmusic.util.Helper
import com.csakitheone.streetmusic.util.TranslatorManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MusicianActivity : ComponentActivity() {
    companion object {
        const val EXTRA_MUSICIAN_JSON = "musician_json"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MusicianScreen()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
    @Preview
    @Composable
    fun MusicianScreen() {
        UtcazeneTheme {
            val context = LocalContext.current

            var musician: Musician? by remember { mutableStateOf(null) }

            val imageLoader by remember {
                mutableStateOf(
                    ImageLoader.Builder(context)
                        .okHttpClient(UzApi.client)
                        .build()
                )
            }

            var isDescriptionExpanded by remember { mutableStateOf(false) }
            var englishDescription: String? by remember { mutableStateOf(null) }

            var performances by remember { mutableStateOf(mapOf<Int, List<Event>>()) }
            var musiciansPinned by remember { mutableStateOf<List<Musician>>(listOf()) }
            val isPinned by remember(musician, musiciansPinned) {
                mutableStateOf(musiciansPinned.contains(musician))
            }
            var eventsPinned by remember { mutableStateOf<List<Event>>(listOf()) }

            LaunchedEffect(Unit) {
                musician = Gson().fromJson(
                    intent.getStringExtra(EXTRA_MUSICIAN_JSON),
                    Musician::class.java
                )
                EventsProvider.getEvents(this@MusicianActivity) { events ->
                    performances = events
                        .filter { it.musician == musician }
                        .sortedBy { it.time }
                        .groupBy { it.day }
                        .toSortedMap()
                }
            }

            ListPreferenceHolder(
                id = "authorsPinned",
                value = musiciansPinned,
                onValueChanged = { musiciansPinned = it.toList() },
                type = object : TypeToken<Musician>() {}.type,
            )

            ListPreferenceHolder(
                id = "eventsPinned",
                value = eventsPinned,
                onValueChanged = { eventsPinned = it.toList() },
                type = object : TypeToken<Event>() {}.type,
            )

            if (!englishDescription.isNullOrBlank()) {
                AlertDialog(
                    title = { Text(text = stringResource(id = R.string.musician_description)) },
                    text = { Text(text = englishDescription ?: "") },
                    onDismissRequest = { englishDescription = null },
                    confirmButton = {
                        TextButton(onClick = { englishDescription = null }) {
                            Text(text = stringResource(id = R.string.close))
                        }
                    },
                )
            }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column {
                    TopAppBar(
                        title = {
                            Column {
                                Text(text = musician?.name ?: "Zenész neve")
                                if (musician?.country != null) {
                                    Text(
                                        text = "${musician!!.getFlag()} ${musician!!.country}",
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
                                    musiciansPinned = if (!isPinned) musiciansPinned + musician!!
                                    else musiciansPinned.filter { it != musician }
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
                        if (
                            !BatterySaverManager.isBatterySaverEnabled &&
                            !musician?.imageUrl.isNullOrBlank()
                        ) {
                            OutlinedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                onClick = {
                                    CustomTabsManager.open(context, musician!!.imageUrl!!)
                                },
                            ) {
                                AsyncImage(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 64.dp, max = 400.dp),
                                    imageLoader = imageLoader,
                                    model = musician!!.imageUrl,
                                    contentDescription = null,
                                )
                            }
                        }
                        if (!musician?.description.isNullOrBlank()) {
                            UzCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                onClick = {
                                    isDescriptionExpanded = !isDescriptionExpanded
                                }
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            modifier = Modifier.padding(8.dp),
                                            text = stringResource(id = R.string.musician_description),
                                            style = MaterialTheme.typography.titleMedium,
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        IconButton(
                                            onClick = {
                                                TranslatorManager
                                                    .translateDescription(musician!!.description!!) {
                                                        englishDescription = it
                                                    }
                                            },
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Translate,
                                                contentDescription = null,
                                            )
                                        }
                                    }
                                    AnimatedContent(
                                        targetState = isDescriptionExpanded,
                                        label = "DescriptionExpand",
                                    ) {
                                        SelectionContainer {
                                            Text(
                                                modifier = Modifier.padding(8.dp),
                                                text = if (it || musician!!.description!!.length < 150) musician!!.description!!
                                                else musician!!.description!!.take(120) + "...",
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        if (!musician?.youtubeUrl.isNullOrBlank()) {
                            MenuCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                onClick = {
                                    startActivity(
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse(musician!!.youtubeUrl!!)
                                        )
                                    )
                                },
                                painter = painterResource(id = R.drawable.ic_youtube),
                                title = stringResource(id = R.string.watch_on_youtube),
                            )
                        }
                        Row {
                            musician?.tags?.map { tag ->
                                FilterChip(
                                    modifier = Modifier.padding(8.dp),
                                    selected = false,
                                    onClick = {},
                                    label = { Text(text = stringResource(id = tag)) },
                                )
                            }
                        }
                        if (performances.isNotEmpty()) {
                            Text(
                                modifier = Modifier.padding(8.dp),
                                text = stringResource(id = R.string.musician_performances),
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                        performances.map { day ->
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
