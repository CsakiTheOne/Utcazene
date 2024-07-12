package com.csakitheone.streetmusic.ui.components

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.csakitheone.streetmusic.R
import com.csakitheone.streetmusic.data.EventsProvider
import com.csakitheone.streetmusic.model.Event
import com.csakitheone.streetmusic.model.Musician
import com.csakitheone.streetmusic.util.Helper.Companion.toLocalTime
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month

@Composable
fun AdaptiveFeed(
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
) {
    val context = LocalContext.current

    val dateTime by remember { mutableStateOf(LocalDateTime.now()) }

    var events by remember { mutableStateOf(emptyList<Event>()) }
    val eventsToday by remember(events) {
        derivedStateOf {
            events.filter { it.day == LocalDate.now().dayOfMonth }
        }
    }
    val eventsNowPlaying by remember(eventsToday) {
        derivedStateOf {
            eventsToday.filter { event ->
                val nextTime = eventsToday.firstOrNull {
                    it.time.toLocalTime() > event.time.toLocalTime().plusMinutes(20)
                }?.time?.toLocalTime()
                event.time.toLocalTime() < LocalTime.now().plusMinutes(5) &&
                        (nextTime == null || nextTime > LocalTime.now().minusMinutes(5))
            }
        }
    }
    var musicians by remember { mutableStateOf(emptyList<Musician>()) }

    LaunchedEffect(Unit) {
        EventsProvider.getEventsThisYear(context) { newEvents ->
            events = newEvents
                .sortedBy { it.time.toLocalTime() }
            musicians = newEvents
                .map { it.musician }
                .distinct()
                .sortedBy { it.name }
        }
    }

    fun isTodayUtcazeneDay() =
        dateTime.year == events.firstOrNull()?.year && dateTime.month == Month.JULY && events.any { it.day == dateTime.dayOfMonth }

    fun isUtcazeneStartedToday() = isTodayUtcazeneDay() && eventsNowPlaying.isNotEmpty()

    LazyColumn(
        modifier = modifier,
        state = lazyListState,
    ) {
        if (isUtcazeneStartedToday()) {
            // Now playing
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        modifier = Modifier.padding(8.dp),
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                    )
                    Text(
                        modifier = Modifier.padding(8.dp),
                        text = stringResource(id = R.string.now_playing),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
            items(eventsNowPlaying, { it.toString() }) { event ->
                EventCard(
                    modifier = Modifier.padding(8.dp),
                    event = event,
                )
            }
        }
        else if (isTodayUtcazeneDay()) {
            // Tonight on Utcazene
            item {
                Row(
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
            items(eventsToday, { it.toString() }) { event ->
                EventCard(
                    modifier = Modifier.padding(8.dp),
                    event = event,
                )
            }
        }
        else if (musicians.isNotEmpty()) {
            // This year on Utcazene
            item {
                Row(
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
            }
            items(musicians, { it.name }) { musician ->
                BigMusicianCard(
                    modifier = Modifier.padding(8.dp),
                    musician = musician,
                )
            }
        }
        else {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(24.dp),
                    )
                }
            }
        }
    }
}