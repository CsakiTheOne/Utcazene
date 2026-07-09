package com.csakitheone.streetmusic.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.csakitheone.streetmusic.data.LocalRepository
import com.csakitheone.streetmusic.data.model.Event
import com.csakitheone.streetmusic.navigation.Destination
import com.csakitheone.streetmusic.navigation.LocalNavBackStack

@Composable
fun EventCard(
    modifier: Modifier = Modifier,
    event: Event,
    hidePlace: Boolean = false,
) {
    val repository = LocalRepository.current
    val backStack = LocalNavBackStack.current

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
    }
}
