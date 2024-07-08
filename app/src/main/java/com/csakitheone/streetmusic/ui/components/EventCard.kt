package com.csakitheone.streetmusic.ui.components

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.csakitheone.streetmusic.model.Event
import com.csakitheone.streetmusic.model.Musician
import com.csakitheone.streetmusic.model.Place
import com.csakitheone.streetmusic.ui.activities.EventActivity
import com.google.gson.Gson

@Preview
@Composable
fun EventCard(
    modifier: Modifier = Modifier,
    event: Event = Event(
        musician = Musician(name = "JAN TERLOUW JUNIOR & THE NIGHTCLUB"),
        year = 2024,
        day = 24,
        time = "19:30",
        place = Place("Nagyon Balaton nagyszínpad (a Hangvilla előtt)"),
    ),
    isPinned: Boolean? = null,
    onPinnedChangeRequest: (Boolean) -> Unit = {},
    showPlace: Boolean = true,
) {
    val context = LocalContext.current

    UzCard(
        modifier = modifier,
        onClick = {
            context.startActivity(
                Intent(context, EventActivity::class.java)
                    .putExtra(EventActivity.EXTRA_EVENT_JSON, Gson().toJson(event))
            )
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
                    text = event.musician.name,
                    style = MaterialTheme.typography.titleSmall,
                )
                if (isPinned != null) {
                    IconButton(
                        modifier = Modifier.padding(start = 8.dp),
                        onClick = {
                            onPinnedChangeRequest(!isPinned)
                        },
                    ) {
                        Icon(
                            imageVector = if (isPinned) Icons.Default.Favorite
                            else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (showPlace) {
                    Text(
                        modifier = Modifier
                            .padding(8.dp)
                            .weight(1f),
                        text = event.place.name,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = event.time,
                )
            }
        }
    }
}