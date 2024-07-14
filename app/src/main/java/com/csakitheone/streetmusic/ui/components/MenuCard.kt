package com.csakitheone.streetmusic.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalAnimationApi::class)
@Preview
@Composable
fun MenuCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    imageVector: ImageVector? = null,
    painter: Painter? = null,
    title: String? = null,
    isCompressed: Boolean = false,
    content: @Composable () -> Unit = {},
) {
    AnimatedContent(
        modifier = modifier,
        targetState = isCompressed,
        transitionSpec = {
            if (targetState) {
                fadeIn() togetherWith fadeOut()
            } else {
                fadeIn() togetherWith fadeOut()
            }
        },
    ) {
        if (it) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onClick,
            ) {
                if (imageVector != null) {
                    Icon(
                        modifier = Modifier
                            .size(24.dp),
                        imageVector = imageVector,
                        contentDescription = null,
                    )
                } else if (painter != null) {
                    Icon(
                        modifier = Modifier
                            .size(24.dp),
                        painter = painter,
                        contentDescription = null,
                    )
                }
                Text(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    text = title ?: "",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        } else {
            UzCard(
                modifier = Modifier,
                onClick = onClick,
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    if (imageVector != null) {
                        Icon(
                            modifier = Modifier
                                .size(48.dp),
                            imageVector = imageVector,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondaryContainer,
                        )
                    } else if (painter != null) {
                        Icon(
                            modifier = Modifier
                                .size(48.dp),
                            painter = painter,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondaryContainer,
                        )
                    }
                    Text(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 4.dp),
                        text = title ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    content()
                }
            }
        }
    }
}