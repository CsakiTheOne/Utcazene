package com.csakitheone.streetmusic.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview
@Composable
fun MenuCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    imageVector: ImageVector = Icons.Default.Star,
    title: String? = "",
    contentOrientationHorizontal: Boolean = false,
    content: @Composable () -> Unit = {},
) {
    UzCard(
        modifier = modifier,
        onClick = onClick,
    ) {
        Box(
            modifier = Modifier.padding(8.dp),
            contentAlignment = Alignment.BottomStart,
        ) {
            Icon(
                modifier = Modifier
                    .width(48.dp)
                    .aspectRatio(1f)
                    .alpha(.4f),
                imageVector = imageVector,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
            )
            if (contentOrientationHorizontal) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = title ?: "",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    content()
                }
            }
            else {
                Column(
                    modifier = Modifier.padding(8.dp),
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = title ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                    )
                    content()
                }
            }
        }
    }
}

@Composable
fun MenuCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    painter: Painter,
    title: String? = "",
    contentOrientationHorizontal: Boolean = false,
    content: @Composable () -> Unit = {},
) {
    UzCard(
        modifier = modifier,
        onClick = onClick,
    ) {
        Box(
            modifier = Modifier.padding(8.dp),
            contentAlignment = Alignment.BottomStart,
        ) {
            Icon(
                modifier = Modifier
                    .width(48.dp)
                    .aspectRatio(1f)
                    .alpha(.4f),
                painter = painter,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
            )
            if (contentOrientationHorizontal) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = title ?: "",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    content()
                }
            }
            else {
                Column(
                    modifier = Modifier.padding(8.dp),
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = title ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                    )
                    content()
                }
            }
        }
    }
}