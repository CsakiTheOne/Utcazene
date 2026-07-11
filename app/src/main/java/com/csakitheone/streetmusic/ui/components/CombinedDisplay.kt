package com.csakitheone.streetmusic.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Card
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.csakitheone.streetmusic.data.model.ExternalEvent
import com.csakitheone.streetmusic.data.model.Event

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
        is Event -> EventCard(modifier = modifier, event = data)
        
        is ExternalEvent -> Card(
            modifier = modifier,
            onClick = onClick ?: {},
        ) {
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
        }

        else -> Text(
            modifier = modifier,
            text = "Unknown data type"
        )
    }
}
