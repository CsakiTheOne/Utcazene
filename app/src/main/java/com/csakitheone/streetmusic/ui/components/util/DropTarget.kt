package com.csakitheone.streetmusic.ui.components.util

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned

@Composable
fun DropTarget(
    modifier: Modifier = Modifier,
    dragPosition: Offset,
    onDragEnter: () -> Unit,
    onDragExit: () -> Unit = {},
    content: @Composable BoxScope.() -> Unit,
) {
    var isCurrentTarget by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .onGloballyPositioned { coords ->
                if (coords.boundsInWindow().contains(dragPosition) && !isCurrentTarget) {
                    onDragEnter()
                    isCurrentTarget = true
                }
                else if (!coords.boundsInWindow().contains(dragPosition) && isCurrentTarget) {
                    onDragExit()
                    isCurrentTarget = false
                }
            },
        content = content,
    )
}