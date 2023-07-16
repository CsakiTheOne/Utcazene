package com.csakitheone.streetmusic.playingcards.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.csakitheone.streetmusic.playingcards.PlayingCardData

@Preview
@Composable
fun PlayingCard(
    modifier: Modifier = Modifier,
    data: PlayingCardData? = PlayingCardData.random(),
    elevation: Dp = 8.dp,
) {
    Surface(
        modifier = modifier,
        color = if (isSystemInDarkTheme()) Color(0xFF282828)
        else Color.White,
        shape = RoundedCornerShape(8.dp),
        tonalElevation = elevation,
        shadowElevation = elevation,
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = data?.label ?: "",
                textAlign = TextAlign.Start,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = data?.suit?.symbol ?: "",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = data?.label ?: "",
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}