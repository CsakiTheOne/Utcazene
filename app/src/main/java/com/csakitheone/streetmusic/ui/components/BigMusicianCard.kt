package com.csakitheone.streetmusic.ui.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import com.csakitheone.streetmusic.data.UzApi
import com.csakitheone.streetmusic.model.Musician
import com.csakitheone.streetmusic.ui.activities.MusicianActivity
import com.google.gson.Gson

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BigMusicianCard(
    modifier: Modifier = Modifier,
    musician: Musician,
    isPinned: Boolean? = null,
    onPinnedChangeRequest: (Boolean) -> Unit = {},
    showYears: Boolean = true,
) {
    if (musician.imageUrl.isNullOrBlank()) {
        MusicianCard(
            modifier = modifier,
            musician = musician,
            isPinned = isPinned,
            onPinnedChangeRequest = onPinnedChangeRequest,
            showYears = showYears,
        )
        return
    }

    val context = LocalContext.current

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .okHttpClient(UzApi.client)
            .build()
    }

    Card(
        modifier = modifier,
        onClick = {
            context.startActivity(
                Intent(context, MusicianActivity::class.java)
                    .putExtra(MusicianActivity.EXTRA_MUSICIAN_JSON, Gson().toJson(musician))
            )
        },
    ) {
        Box(
            contentAlignment = Alignment.BottomStart,
        ) {
            AsyncImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 64.dp, max = 256.dp),
                imageLoader = imageLoader,
                model = musician.imageUrl,
                contentScale = ContentScale.Crop,
                contentDescription = null,
            )
            Box(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.background.copy(alpha = .8f),
                            ),
                        )
                    ),
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(8.dp)
                                .weight(1f),
                            text = musician.name,
                            style = MaterialTheme.typography.titleMedium,
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
    }
}