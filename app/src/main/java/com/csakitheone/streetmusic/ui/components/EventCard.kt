package com.csakitheone.streetmusic.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.csakitheone.streetmusic.data.LocalRepository
import com.csakitheone.streetmusic.data.model.Event
import com.csakitheone.streetmusic.navigation.Destination
import com.csakitheone.streetmusic.navigation.LocalNavBackStack
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Composable
fun EventCard(
    modifier: Modifier = Modifier,
    event: Event,
    hidePlace: Boolean = false,
) {
    val repository = LocalRepository.current
    val backStack = LocalNavBackStack.current

    val isToday by remember(event) {
        derivedStateOf {
            val today = LocalDate.now()
            val eventDate = LocalDateTime.parse(event.startTime).toLocalDate()
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
            if (!isToday || event.startTime.isEmpty() || event.endTime.isEmpty()) return@derivedStateOf 0f
            val startTime = LocalTime.parse(event.startTime.substring(11)).toSecondOfDay()
            var endTime = LocalTime.parse(event.endTime.substring(11)).toSecondOfDay()
            if (endTime < startTime) endTime += 24 * 60 * 60
            if (startTime == endTime) return@derivedStateOf 0f
            val progress = (now.toLocalTime().toSecondOfDay() - startTime).toFloat() / (endTime - startTime)
            progress.coerceIn(0f, 1f)
        }
    }

    Card(
        modifier = modifier,
        onClick = {
            backStack.add(Destination.EventDetail(event.id))
        },
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier
                        .padding(8.dp)
                        .weight(1f),
                    text = event.artistName,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                FavoritesIndicator(
                    slug = "${event.artistSlug} at ${event.startTime}",
                    onToggled = {
                        if (it) {
                            repository.setFavorite(event.artistSlug, true)
                        }
                    },
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (!hidePlace) {
                    Text(
                        modifier = Modifier
                            .padding(8.dp)
                            .weight(1f),
                        text = event.place,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = event.startTime.substringAfter("T").take(5),
                )
            }
        }
        if (progress > 0) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                progress = { progress },
            )
        }
    }
}
