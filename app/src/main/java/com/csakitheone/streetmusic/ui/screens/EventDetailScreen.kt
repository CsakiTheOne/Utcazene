package com.csakitheone.streetmusic.ui.screens

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import com.csakitheone.streetmusic.R
import com.csakitheone.streetmusic.data.LocalRepository
import com.csakitheone.streetmusic.navigation.Destination
import com.csakitheone.streetmusic.navigation.LocalNavBackStack
import com.csakitheone.streetmusic.ui.components.EventCard
import com.csakitheone.streetmusic.ui.components.FavoritesIndicator
import com.csakitheone.streetmusic.ui.components.YouTubeEmbed
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(eventId: Int) {
    val repository = LocalRepository.current
    val backStack = LocalNavBackStack.current
    val event by repository.getEvent(eventId).collectAsState(initial = null)
    val venue by (event?.let { repository.getVenueByName(it.place) }
        ?: flowOf(null)).collectAsState(initial = null)
    val artist by (event?.let { repository.getArtist(it.artistSlug) }
        ?: flowOf(null)).collectAsState(initial = null)
    val allEvents by repository.events.collectAsState(initial = emptyList())

    val isToday by remember(event) {
        derivedStateOf {
            val today = LocalDate.now()
            val eventDate = event?.startTime?.let { LocalDateTime.parse(it).toLocalDate() }
            today == eventDate
        }
    }

    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(isToday) {
        while (isToday) {
            now = LocalDateTime.now()
            delay(1.minutes)
        }
    }

    val progress by remember(event, isToday, now) {
        derivedStateOf {
            val e = event ?: return@derivedStateOf 0f
            if (!isToday || e.startTime.isEmpty() || e.endTime.isEmpty()) return@derivedStateOf 0f
            val startTime = LocalTime.parse(e.startTime.substring(11)).toSecondOfDay()
            var endTime = LocalTime.parse(e.endTime.substring(11)).toSecondOfDay()
            if (endTime < startTime) endTime += 24 * 60 * 60
            if (startTime == endTime) return@derivedStateOf 0f
            val progress =
                (now.toLocalTime().toSecondOfDay() - startTime).toFloat() / (endTime - startTime)
            progress.coerceIn(0f, 1f)
        }
    }

    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Box {
                if (repository.shouldShowImage() && !artist?.image.isNullOrBlank()) {
                    AsyncImage(
                        modifier = Modifier
                            .matchParentSize()
                            .alpha(1f - scrollBehavior.state.collapsedFraction),
                        model = artist?.image,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                    )
                }
                LargeTopAppBar(
                    title = {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = .75f),
                            ),
                        ) {
                            SelectionContainer {
                                Text(
                                    modifier = Modifier.padding(4.dp),
                                    text = artist?.name ?: event?.artistName ?: "Event Details",
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        FilledIconButton(
                            onClick = { backStack.removeLastOrNull() },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                            )
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_arrow_back),
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        event?.let {
                            val favSlug = "${it.artistSlug} at ${it.startTime}"
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                ),
                            ) {
                                FavoritesIndicator(slug = favSlug)
                            }
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = .5f),
                    ),
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .padding(
                        top = padding.calculateTopPadding() + 16.dp,
                        bottom = padding.calculateBottomPadding() + 16.dp,
                        start = 16.dp,
                        end = 16.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Event Details
                Card {
                    SelectionContainer {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(32.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    modifier = Modifier.padding(end = 16.dp),
                                    painter = painterResource(R.drawable.shortcut_places),
                                    contentDescription = null,
                                )
                                Column {
                                    Text(
                                        text = event?.place ?: "",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    if (!venue?.address.isNullOrBlank()) {
                                        Text(
                                            text = venue?.address ?: "",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                FilledIconButton(
                                    modifier = Modifier.padding(start = 16.dp),
                                    onClick = { backStack.add(Destination.Map(event?.place)) }
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_map),
                                        contentDescription = null,
                                    )
                                }
                            }

                            event?.let { e ->
                                val startTime = LocalDateTime.parse(e.startTime)
                                val endTime = LocalDateTime.parse(e.endTime)
                                val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        modifier = Modifier.padding(end = 16.dp),
                                        painter = painterResource(R.drawable.shortcut_events),
                                        contentDescription = null,
                                    )
                                    val dateFormatter = DateTimeFormatter.ofPattern("MMMM d, EEEE")
                                    Text(
                                        text = startTime.format(dateFormatter),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text(
                                        modifier = Modifier.fillMaxWidth(),
                                        text = "${startTime.format(timeFormatter)} - ${
                                            endTime.format(
                                                timeFormatter
                                            )
                                        }",
                                        style = MaterialTheme.typography.headlineLarge,
                                        textAlign = TextAlign.Center,
                                    )

                                    if (progress > 0) {
                                        LinearWavyProgressIndicator(
                                            modifier = Modifier.fillMaxWidth(),
                                            progress = { progress },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Button to Artist Detail
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        event?.artistSlug?.let {
                            backStack.add(Destination.ArtistDetail(it))
                        }
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_info),
                        contentDescription = null,
                        modifier = Modifier.padding(end = ButtonDefaults.IconSpacing)
                    )
                    Text("View artist profile")
                }

                // Other events
                val otherEvents by remember(event, allEvents) {
                    derivedStateOf { allEvents.filter { it.artistSlug == event?.artistSlug && it.id != eventId } }
                }
                if (otherEvents.isNotEmpty()) {
                    Text(
                        text = "${event?.artistName} also plays at",
                        style = MaterialTheme.typography.titleLarge
                    )
                    otherEvents.forEach { otherEvent ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = otherEvent.startTime.substring(8, 10),
                                style = MaterialTheme.typography.labelLarge,
                            )
                            EventCard(
                                modifier = Modifier.weight(1f),
                                event = otherEvent,
                            )
                        }
                    }
                }

                val nextHereEvents by remember(event, allEvents) {
                    derivedStateOf {
                        allEvents.filter {
                            try {
                                val samePlace = it.place == event?.place
                                val isSameDay =
                                    it.startTime.substring(0, 10) == event?.startTime?.substring(
                                        0,
                                        10
                                    )
                                val isAfter =
                                    LocalDateTime.parse(it.startTime) > LocalDateTime.parse(event?.startTime)
                                samePlace && isSameDay && isAfter
                            } catch (e: Exception) {
                                false
                            }
                        }.sortedBy { LocalDateTime.parse(it.startTime) }
                    }
                }
                if (nextHereEvents.isNotEmpty()) {
                    Text(
                        text = "After this at ${event?.place}",
                        style = MaterialTheme.typography.titleLarge
                    )
                    nextHereEvents.forEach { otherEvent ->
                        EventCard(event = otherEvent)
                    }
                }

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "ID: $eventId",
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
