package com.csakitheone.streetmusic.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.csakitheone.streetmusic.R

@Preview
@Composable
fun DaySelectorDialog(
    selectedDay: Int = 19,
    onChanged: (Int) -> Unit = {},
    onDismissRequest: () -> Unit = {},
) {
    AlertDialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
        icon = { Icon(imageVector = Icons.Default.EditCalendar, contentDescription = null) },
        title = { Text(text = stringResource(id = R.string.choose_day)) },
        text = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 80.dp),
            ) {
                mapOf(
                    19 to stringResource(id = R.string.day_wednesday),
                    20 to stringResource(id = R.string.day_thursday),
                    21 to stringResource(id = R.string.day_friday),
                    22 to stringResource(id = R.string.day_saturday),
                ).map {
                    NavigationBarItem(
                        selected = selectedDay == it.key,
                        onClick = {
                            onChanged(it.key)
                            onDismissRequest()
                        },
                        icon = {
                            Text(text = it.key.toString())
                        },
                        label = { Text(text = it.value) },
                    )
                }
            }
            /*Column {
                listOf(
                    "19 Szerda",
                    "20 Csütörtök",
                    "21 Péntek",
                    "22 Szombat",
                ).map {
                    val dayId = it.substringBefore(" ").toInt()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onChanged(dayId)
                                onDismissRequest()
                            },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = selectedDay == dayId,
                            onClick = {
                                onChanged(dayId)
                                onDismissRequest()
                            },
                        )
                        Text(text = it)
                    }
                }
            }*/
        },
        onDismissRequest = { onDismissRequest() },
        confirmButton = {
            TextButton(onClick = { onDismissRequest() }) {
                Text(text = stringResource(id = R.string.close))
            }
        },
    )
}