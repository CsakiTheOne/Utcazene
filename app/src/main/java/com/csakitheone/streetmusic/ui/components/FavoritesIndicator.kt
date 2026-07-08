package com.csakitheone.streetmusic.ui.components

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.csakitheone.streetmusic.R
import com.csakitheone.streetmusic.data.LocalRepository

@Composable
fun FavoritesIndicator(
    slug: String,
    isStarred: Boolean,
    onToggle: () -> Unit
) {
    val repository = LocalRepository.current
    val context = LocalContext.current
    val connectedFriends by repository.nearbyManager.friends.connectedFriends.collectAsState()

    val favoritedBy = connectedFriends.values.filter { it.favoriteSlugs.contains(slug) }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        favoritedBy.forEach { payload ->
            Icon(
                modifier = Modifier
                    .size(16.dp)
                    .clickable {
                        Toast.makeText(context, payload.nickname, Toast.LENGTH_SHORT).show()
                    },
                painter = painterResource(R.drawable.ic_star),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
        }

        IconButton(onClick = onToggle) {
            Icon(
                painter = painterResource(if (isStarred) R.drawable.ic_star else R.drawable.ic_star_outline),
                contentDescription = if (isStarred) "Unstar" else "Star",
                tint = if (isStarred) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
        }
    }
}
