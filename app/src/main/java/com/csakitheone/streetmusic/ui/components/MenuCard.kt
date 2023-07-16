package com.csakitheone.streetmusic.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    imageVector: ImageVector? = null,
    painter: Painter? = null,
    title: String? = "",
    contentOrientationHorizontal: Boolean = false,
    content: @Composable () -> Unit = {},
) {
    UzCard(
        modifier = modifier,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            if (imageVector != null) {
                Icon(
                    modifier = Modifier
                        .width(48.dp)
                        .aspectRatio(1f),
                    imageVector = imageVector,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                )
            }
            else if (painter != null) {
                Icon(
                    modifier = Modifier
                        .width(48.dp)
                        .aspectRatio(1f),
                    painter = painter,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                )
            }
            Text(
                modifier = Modifier.weight(1f).padding(4.dp),
                text = title ?: "",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
            content()
        }
    }
}
