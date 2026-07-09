package com.csakitheone.streetmusic.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.csakitheone.streetmusic.R
import com.csakitheone.streetmusic.data.GyarkertRepository
import com.csakitheone.streetmusic.navigation.LocalNavBackStack
import com.csakitheone.streetmusic.ui.components.FavoritesIndicator
import com.csakitheone.streetmusic.ui.components.NowIndicator
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GyarkertScreen() {
    val backStack = LocalNavBackStack.current
    val uriHandler = LocalUriHandler.current

    var currentTime by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30.seconds)
            currentTime = LocalDateTime.now()
        }
    }

    val today = remember { LocalDate.now() }
    val isJuly23 = today.monthValue == 7 && today.dayOfMonth == 23
    val isJuly24 = today.monthValue == 7 && today.dayOfMonth == 24
    val isJuly25 = today.monthValue == 7 && today.dayOfMonth == 25

    val indicatorIndex by remember(currentTime) {
        derivedStateOf {
            if (!isJuly23) -1
            else {
                val nowTime = currentTime.toLocalTime()
                GyarkertRepository.pontOttPartiEvents.indexOfFirst {
                    it.startTime.isAfter(nowTime)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gyárkert") },
                navigationIcon = {
                    IconButton(onClick = { backStack.removeLastOrNull() }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            uriHandler.openUri(GyarkertRepository.url)
                        },
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_web),
                            contentDescription = "Website"
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = paddingValues.calculateTopPadding() + 16.dp,
                bottom = paddingValues.calculateBottomPadding() + 16.dp,
                start = 16.dp,
                end = 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "July 23: Pont Ott Parti",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            itemsIndexed(GyarkertRepository.pontOttPartiEvents) { index, event ->
                if (index == indicatorIndex) {
                    NowIndicator(
                        time = currentTime.toLocalTime(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    ListItem(
                        content = { Text(text = event.name) },
                        supportingContent = {
                            val timeText = if (event.endTime != null) {
                                "${event.startTime} - ${event.endTime}"
                            } else {
                                "${event.startTime}"
                            }
                            Column {
                                Text(text = timeText)
                                event.description?.let {
                                    Text(
                                        modifier = Modifier.padding(top = 8.dp),
                                        text = it,
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                            }
                        },
                        trailingContent = {
                            FavoritesIndicator(slug = "gyarkert_pop_${event.name}")
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = Color.Transparent,
                        ),
                    )
                }
            }

            if (indicatorIndex == -1 && isJuly23) {
                val lastEvent = GyarkertRepository.pontOttPartiEvents.lastOrNull()
                val lastEventEndsAfterNow = lastEvent?.let {
                    it.endTime?.isAfter(currentTime.toLocalTime()) ?: false
                } ?: false

                if (!lastEventEndsAfterNow) {
                    item {
                        NowIndicator(
                            time = currentTime.toLocalTime(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        )
                    }
                }
            }

            item {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { uriHandler.openUri(GyarkertRepository.pontOttPartiUrl) }
                ) {
                    Text("More info about Pont Ott Parti")
                }
            }

            item {
                Text(
                    modifier = Modifier.padding(top = 16.dp),
                    text = "Later",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            item {
                if (isJuly24) {
                    NowIndicator(
                        time = currentTime.toLocalTime(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { uriHandler.openUri(GyarkertRepository.day24facebookUrl) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            modifier = Modifier.padding(16.dp),
                            text = "July 24: Thievery Corporation",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        FavoritesIndicator(slug = "gyarkert_day24")
                    }
                }
            }

            item {
                if (isJuly25) {
                    NowIndicator(
                        time = currentTime.toLocalTime(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { uriHandler.openUri(GyarkertRepository.day25facebookUrl) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            modifier = Modifier.padding(16.dp),
                            text = "July 25: DESH",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        FavoritesIndicator(slug = "gyarkert_day25")
                    }
                }
            }
        }
    }
}
