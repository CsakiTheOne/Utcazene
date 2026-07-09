package com.csakitheone.streetmusic.ui.screens

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(eventId: Int) {
    val context = LocalContext.current
    val repository = LocalRepository.current
    val backStack = LocalNavBackStack.current
    val event by repository.getEvent(eventId).collectAsState(initial = null)
    val artist by (event?.let { repository.getArtist(it.artistSlug) }
        ?: flowOf(null)).collectAsState(initial = null)
    val otherEvents by (event?.let { repository.getEventsByArtist(it.artistSlug) } ?: flowOf(
        emptyList()
    )).collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(event?.artistName ?: "Event Details") },
                navigationIcon = {
                    IconButton(onClick = { backStack.removeLastOrNull() }) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = "Back")
                    }
                },
                actions = {
                    event?.let {
                        val favSlug = "${it.artistSlug} at ${it.startTime}"
                        FavoritesIndicator(slug = favSlug)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Artist Image
            if (repository.shouldShowImage() && !artist?.image.isNullOrBlank()) {
                Card(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(16.dp),
                        )
                        AsyncImage(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 256.dp)
                                .clickable {
                                    artist?.image?.let {
                                        context.startActivity(
                                            Intent(
                                                Intent.ACTION_VIEW,
                                                it.toUri()
                                            )
                                        )
                                    }
                                },
                            model = artist?.image,
                            contentDescription = null,
                            contentScale = ContentScale.FillWidth,
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Artist Name and Country
                Column {
                    Text(
                        text = artist?.name ?: event?.artistName ?: "",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    artist?.country?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                // Event Details Card
                Card {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                modifier = Modifier.size(20.dp),
                                painter = painterResource(R.drawable.shortcut_places),
                                contentDescription = null,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = event?.place ?: "",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        event?.let { e ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(R.drawable.shortcut_events),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                val startTime = LocalDateTime.parse(e.startTime)
                                val endTime = LocalDateTime.parse(e.endTime)
                                val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d.")
                                val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                                Text(
                                    text = "${startTime.format(dateFormatter)}\n${
                                        startTime.format(
                                            timeFormatter
                                        )
                                    } - ${endTime.format(timeFormatter)}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                // Button to Artist Detail
                OutlinedButton(
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
                val otherTimes = otherEvents.filter { it.id != eventId }
                if (otherTimes.isNotEmpty()) {
                    Text(
                        text = "Other performances",
                        style = MaterialTheme.typography.titleLarge
                    )
                    otherTimes.forEach { otherEvent ->
                        EventCard(event = otherEvent)
                    }
                }
            }
        }
    }
}
