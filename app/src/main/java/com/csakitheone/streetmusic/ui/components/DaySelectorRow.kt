package com.csakitheone.streetmusic.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.csakitheone.streetmusic.R

@Preview
@Composable
fun DaySelectorRow(
    modifier: Modifier = Modifier,
    selectedDay: Int = 24,
    onChange: (Int) -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        mapOf(
            17 to stringResource(id = R.string.day_thursday),
            18 to stringResource(id = R.string.day_friday),
            19 to stringResource(id = R.string.day_saturday),
        ).map {
            NavigationBarItem(
                selected = selectedDay == it.key,
                onClick = {
                    onChange(it.key)
                },
                icon = {
                    Text(
                        text = it.key.toString(),
                        color = if (selectedDay == it.key) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onBackground,
                    )
                },
                label = { Text(text = it.value) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.onBackground,
                    unselectedTextColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        }
    }
}