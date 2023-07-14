package com.csakitheone.streetmusic.ui.components

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.csakitheone.streetmusic.AuthorActivity
import com.csakitheone.streetmusic.R
import com.csakitheone.streetmusic.model.Author
import com.csakitheone.streetmusic.model.Event
import com.csakitheone.streetmusic.ui.components.util.ListPreferenceHolder
import com.csakitheone.streetmusic.ui.components.util.PreferenceHolder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun AuthorCard(
    modifier: Modifier = Modifier,
    author: Author = Author(
        name = "Előadó neve",
        country = "Ország",
        tags = listOf(R.string.author_tag_friend),
    ),
    isPinned: Boolean? = null,
    onPinnedChangeRequest: (Boolean) -> Unit = {},
) {
    val context = LocalContext.current

    UzCard(
        modifier = modifier,
        onClick = {
            context.startActivity(
                Intent(context, AuthorActivity::class.java)
                    .putExtra(AuthorActivity.EXTRA_AUTHOR_JSON, Gson().toJson(author))
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
                    text = author.name,
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
                            imageVector = if (isPinned) Icons.Default.Star
                            else Icons.Default.StarBorder,
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
                if (!author.country.isNullOrBlank()) {
                    Text(
                        modifier = Modifier.padding(8.dp),
                        text = author.country,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = author.tags?.joinToString { context.getString(it) } ?: "",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}