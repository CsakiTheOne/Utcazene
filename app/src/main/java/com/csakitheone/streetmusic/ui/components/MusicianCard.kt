package com.csakitheone.streetmusic.ui.components

import android.content.Intent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.csakitheone.streetmusic.ui.activities.MusicianActivity
import com.csakitheone.streetmusic.model.Musician
import com.google.gson.Gson

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun MusicianCard(
    modifier: Modifier = Modifier,
    musician: Musician = Musician(
        name = "Előadó neve",
        country = "Ország",
        tags = listOf(Musician.TAG_FRIEND),
    ),
    isPinned: Boolean? = null,
    onPinnedChangeRequest: (Boolean) -> Unit = {},
    showYears: Boolean = false,
) {
    val context = LocalContext.current

    UzCard(
        modifier = modifier,
        onClick = {
            context.startActivity(
                Intent(context, MusicianActivity::class.java)
                    .putExtra(MusicianActivity.EXTRA_MUSICIAN_JSON, Gson().toJson(musician))
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
                    modifier = Modifier.padding(8.dp).weight(1f),
                    text = musician.name,
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
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (!musician.country.isNullOrBlank()) {
                    Text(
                        modifier = Modifier.padding(8.dp),
                        text = "${musician.getFlag()} ${musician.country}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = musician.tags?.joinToString {
                        if (Musician.tagStrings[it] == null) ""
                        else context.getString(Musician.tagStrings[it]!!)
                    } ?: "",
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(modifier = Modifier.weight(1f))
                if (showYears && musician.years != null) {
                    Text(
                        modifier = Modifier.padding(8.dp),
                        text = musician.years.joinToString(),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}