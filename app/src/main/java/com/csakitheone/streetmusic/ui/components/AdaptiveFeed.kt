package com.csakitheone.streetmusic.ui.components

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.carousel.CarouselDefaults
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.csakitheone.streetmusic.R
import com.csakitheone.streetmusic.data.EventsProvider
import com.csakitheone.streetmusic.model.Event
import com.csakitheone.streetmusic.model.Musician
import com.csakitheone.streetmusic.ui.activities.CalendarActivity
import com.csakitheone.streetmusic.ui.activities.MusiciansActivity
import com.csakitheone.streetmusic.util.Helper.Companion.toLocalTime
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AdaptiveFeed(
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
) {
    val context = LocalContext.current

    var isInfoDialogOpen by remember { mutableStateOf(false) }

    var dateTime by remember { mutableStateOf(LocalDateTime.now()) }

    var events by remember { mutableStateOf(emptyList<Event>()) }
    val eventsToday by remember(events) {
        derivedStateOf {
            events.filter { it.day == LocalDate.now().dayOfMonth }
        }
    }
    val eventsNowPlaying by remember(eventsToday, dateTime) {
        derivedStateOf {
            eventsToday
                .groupBy { it.place }
                .mapNotNull { (_, eventsHere) ->
                    val indexOfNext =
                        eventsHere.indexOfFirst { it.time.toLocalTime() > LocalTime.now() }
                    if (indexOfNext == -1) return@mapNotNull eventsHere.lastOrNull()
                    eventsHere.getOrNull(indexOfNext - 1)
                }
        }
    }
    var headlinerMusicians by remember { mutableStateOf(emptyList<Musician>()) }

    LaunchedEffect(Unit) {
        EventsProvider.getEventsThisYear(context) { newEvents ->
            events = newEvents
                .sortedBy { it.time.toLocalTime() }
            headlinerMusicians = newEvents
                .map { it.musician }
                .distinct()
                .filter {
                    it.tags?.contains(Musician.TAG_FOREIGN) == true && !it.isIncomplete()
                }
                .sortedBy { it.name }
        }
    }

    fun isTodayUtcazeneDay() =
        dateTime.year == events.firstOrNull()?.year && dateTime.month == Month.JULY && events.any { it.day == dateTime.dayOfMonth }

    fun isUtcazeneStartedToday() = isTodayUtcazeneDay() && eventsNowPlaying.isNotEmpty()

    if (isInfoDialogOpen) {
        AlertDialog(
            onDismissRequest = { isInfoDialogOpen = false },
            title = {
                Text(text = stringResource(id = R.string.adaptive_feed))
            },
            text = {
                Text(text = stringResource(id = R.string.adaptive_feed_description))
            },
            confirmButton = {
                TextButton(
                    onClick = { isInfoDialogOpen = false },
                ) {
                    Text(text = stringResource(id = R.string.close))
                }
            },
        )
    }

    LazyColumn(
        modifier = modifier,
        state = lazyListState,
    ) {
        if (isUtcazeneStartedToday()) {
            // Now playing
            item {
                Row(
                    modifier = Modifier.clickable { isInfoDialogOpen = true },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        modifier = Modifier.padding(8.dp),
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                    )
                    Text(
                        modifier = Modifier
                            .padding(8.dp)
                            .weight(1f),
                        text = stringResource(id = R.string.now_playing),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    IconButton(
                        modifier = Modifier.padding(8.dp),
                        onClick = {
                            dateTime = LocalDateTime.now()
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                        )
                    }
                }
            }
            items(eventsNowPlaying, { it.toString() }) { event ->
                EventCard(
                    modifier = Modifier.padding(8.dp),
                    event = event,
                )
            }
        } else if (isTodayUtcazeneDay()) {
            // Tonight on Utcazene
            item {
                Row(
                    modifier = Modifier.clickable { isInfoDialogOpen = true },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        modifier = Modifier.padding(8.dp),
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                    )
                    Text(
                        modifier = Modifier.padding(8.dp),
                        text = stringResource(id = R.string.tonight_on_utcazene),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
            items(eventsToday.take(20), { it.toString() }) { event ->
                EventCard(
                    modifier = Modifier.padding(8.dp),
                    event = event,
                )
            }
            item {
                MenuCard(
                    modifier = Modifier.padding(8.dp),
                    onClick = {
                        context.startActivity(
                            Intent(context, CalendarActivity::class.java)
                        )
                    },
                    title = stringResource(id = R.string.more),
                )
            }
        } else if (headlinerMusicians.isNotEmpty()) {
            // This year on Utcazene
            item {
                Row(
                    modifier = Modifier.clickable { isInfoDialogOpen = true },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        modifier = Modifier.padding(8.dp),
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                    )
                    Text(
                        modifier = Modifier.padding(8.dp),
                        text = stringResource(id = R.string.this_year_on_utcazene),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                HorizontalMultiBrowseCarousel(
                    modifier = Modifier
                        .padding(8.dp)
                        .clip(MaterialTheme.shapes.extraLarge),
                    state = rememberCarouselState { headlinerMusicians.size },
                    preferredItemWidth = 400.dp,
                    flingBehavior = CarouselDefaults.noSnapFlingBehavior(),
                ) { index ->
                    BigMusicianCard(
                        musician = headlinerMusicians[index],
                    )
                }
                MenuCard(
                    modifier = Modifier.padding(8.dp),
                    onClick = {
                        context.startActivity(
                            Intent(context, MusiciansActivity::class.java)
                        )
                    },
                    title = stringResource(id = R.string.more),
                )
            }
        } else {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    LoadingIndicator(
                        modifier = Modifier.padding(24.dp),
                    )
                }
            }
        }
    }
}