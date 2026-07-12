package com.csakitheone.streetmusic.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.csakitheone.streetmusic.data.model.Artist
import com.csakitheone.streetmusic.data.model.ExternalEvent
import com.csakitheone.streetmusic.data.model.Event
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import kotlin.time.Duration.Companion.minutes

/**
 * A composable that can display multiple data types.
 * @param onClick Callback for card click. Utcazene events use their default onClick handler.
 */
@Composable
fun CombinedDisplay(
    modifier: Modifier = Modifier,
    data: Any,
    onClick: (() -> Unit)? = null,
) {
    when (data) {
        is Artist -> ArtistCard(modifier = modifier, artist = data)

        is Event -> EventCard(modifier = modifier, event = data)

        is ExternalEvent -> Card(
            modifier = modifier,
            onClick = onClick ?: {},
        ) {
            var now by remember { mutableStateOf(LocalDateTime.now()) }

            LaunchedEffect(Unit) {
                while (data.endTime != null && data.day == now.dayOfMonth) {
                    now = LocalDateTime.now()
                    delay(1.minutes)
                }
            }

            val progress by remember(now) {
                derivedStateOf {
                    if (data.endTime == null) return@derivedStateOf 0f
                    val startTime = data.startTime.toSecondOfDay()
                    var endTime = data.endTime.toSecondOfDay()
                    if (endTime < startTime) endTime += 24 * 60 * 60
                    if (startTime == endTime) return@derivedStateOf 0f
                    val progress = (now.toLocalTime()
                        .toSecondOfDay() - startTime) / (endTime - startTime.toFloat())
                    progress.coerceIn(0f, 1f)
                }
            }

            ListItem(
                content = {
                    SelectionContainer {
                        Text(text = data.name)
                    }
                },
                supportingContent = {
                    val timeText = if (data.endTime != null) {
                        "${data.startTime} - ${data.endTime}"
                    } else {
                        "${data.startTime}"
                    }
                    Column {
                        Text(text = timeText)
                        data.description?.let {
                            Text(
                                modifier = Modifier.padding(top = 8.dp),
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                },
                trailingContent = {
                    FavoritesIndicator(slug = data.slug)
                },
                colors = ListItemDefaults.colors(
                    containerColor = Color.Transparent,
                ),
            )
            if (data.endTime != null && data.day == now.dayOfMonth && progress > 0f) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    progress = { progress },
                )
            }
        }

        else -> Card(modifier = modifier) {
            Text(
                modifier = Modifier.padding(16.dp),
                text = "Unknown data type"
            )
        }
    }
}
