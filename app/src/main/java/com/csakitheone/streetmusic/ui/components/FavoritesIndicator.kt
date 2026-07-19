package com.csakitheone.streetmusic.ui.components

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.csakitheone.streetmusic.R
import com.csakitheone.streetmusic.data.LocalRepository

@Composable
fun FavoritesIndicator(
    modifier: Modifier = Modifier,
    slug: String,
    onToggled: (Boolean) -> Unit = {},
) {
    val context = LocalContext.current
    val repository = LocalRepository.current
    val connectedFriends by repository.nearbyManager.friends.connectedFriends.collectAsState()

    val myFavorites = repository.userFavorites.collectAsState(emptySet())
    val favoritedBy = connectedFriends.values.filter { it.favoriteSlugs.contains(slug) }

    val isStarred by remember(myFavorites) {
        derivedStateOf { myFavorites.value.contains(slug) }
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        favoritedBy.forEach { payload ->
            Icon(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(16.dp)
                    .clickable {
                        Toast.makeText(context, payload.nickname, Toast.LENGTH_SHORT).show()
                    },
                painter = painterResource(R.drawable.ic_star),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
        }

        IconButton(onClick = {
            repository.toggleFavorite(slug)
            onToggled(!isStarred)
        }) {
            Icon(
                painter = painterResource(if (isStarred) R.drawable.ic_star else R.drawable.ic_star_outline),
                contentDescription = if (isStarred) "Unstar" else "Star",
                tint = if (isStarred) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
        }
    }
}
