package com.csakitheone.streetmusic.ui.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
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
        colors = CardDefaults.cardColors(
            contentColor = Color.White,
        ),
    ) {
        Box(
            modifier = Modifier
                .heightIn(min = 64.dp, max = 256.dp),
        ) {
            AsyncImage(
                modifier = Modifier
                    .fillMaxSize(),
                imageLoader = imageLoader,
                model = musician.imageUrl,
                contentScale = ContentScale.Crop,
                contentDescription = null,
            )
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Black.copy(alpha = .8f),
                                    Color.Transparent,
                                ),
                            )
                        ),
                ) {
                    Row(
                        modifier = Modifier
                            .padding(8.dp)
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
                                text = musician.years.joinToString { "'${it.toString().takeLast(2)}" },
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = .8f),
                                ),
                            )
                        ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(8.dp)
                                .weight(1f),
                            text = musician.name,
                            style = MaterialTheme.typography.titleLarge,
                        )
                        if (isPinned != null) {
                            FilledIconButton(
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
                }
            }
        }
    }
}