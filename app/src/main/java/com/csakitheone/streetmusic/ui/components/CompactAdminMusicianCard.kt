package com.csakitheone.streetmusic.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.csakitheone.streetmusic.model.Musician

@Composable
fun CompactAdminMusicianCard(
    modifier: Modifier = Modifier,
    musician: Musician,
    onClick: () -> Unit,
) {
    UzCard(
        modifier = modifier,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier
                    .padding(8.dp)
                    .weight(1f),
                text = musician.name,
                style = MaterialTheme.typography.bodySmall,
            )
            if (musician.description.isNullOrBlank()) {
                Icon(
                    modifier = Modifier.padding(8.dp),
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                )
            }
            if (musician.country.isNullOrBlank()) {
                Icon(
                    modifier = Modifier.padding(8.dp),
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                )
            }
            if (musician.imageUrl.isNullOrBlank()) {
                Icon(
                    modifier = Modifier.padding(8.dp),
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                )
            }
            if (musician.youtubeUrl.isNullOrBlank()) {
                Icon(
                    modifier = Modifier.padding(8.dp),
                    imageVector = Icons.Default.OndemandVideo,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                )
            }
            if (musician.tags.isNullOrEmpty()) {
                Icon(
                    modifier = Modifier.padding(8.dp),
                    imageVector = Icons.Default.Label,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                )
            }
            if (musician.years.isNullOrEmpty()) {
                Icon(
                    modifier = Modifier.padding(8.dp),
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }

    }
}