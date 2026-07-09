package com.csakitheone.streetmusic.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.csakitheone.streetmusic.R
import com.csakitheone.streetmusic.data.LocalRepository
import com.csakitheone.streetmusic.navigation.LocalNavBackStack
import com.csakitheone.streetmusic.ui.components.EventCard
import com.csakitheone.streetmusic.ui.components.NearbyConnectionsDisplay
import com.csakitheone.streetmusic.ui.components.NowIndicator
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.time.Duration.Companion.seconds

@Composable
fun CalendarScreen() {
    val repository = LocalRepository.current
    val backStack = LocalNavBackStack.current

    val events by repository.events.collectAsState(initial = emptyList())
    val allStarredSlugs by repository.allFavorites.collectAsState(initial = emptySet())
    val dates by repository.eventDates.collectAsState(initial = emptyList())
    var selectedDate by remember { mutableStateOf<String?>(null) }
    var showOnlyStarred by remember { mutableStateOf(false) }
    var showOnlyUpcoming by remember { mutableStateOf(false) }

    var currentTime by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30.seconds)
            currentTime = LocalDateTime.now()
        }
    }

    val eventsAtDate by remember(
        events,
        selectedDate,
        showOnlyStarred,
        showOnlyUpcoming,
        allStarredSlugs,
        currentTime
    ) {
        derivedStateOf {
            events
                .filter { it.startTime.startsWith(selectedDate ?: "") }
                .filter {
                    if (showOnlyStarred) {
                        allStarredSlugs.contains("${it.artistSlug} at ${it.startTime}")
                    } else true
                }
                .filter {
                    if (showOnlyUpcoming) {
                        try {
                            LocalDateTime.parse(it.endTime).isAfter(currentTime)
                        } catch (_: Exception) {
                            true
                        }
                    } else true
                }
                .sortedBy { it.startTime }
        }
    }

    val today = remember { LocalDate.now().toString() }

    val indicatorIndex by remember(eventsAtDate, selectedDate, currentTime) {
        derivedStateOf {
            if (selectedDate != today) -1
            else {
                val nowTime = currentTime.toLocalTime()
                eventsAtDate.indexOfFirst {
                    try {
                        LocalTime.parse(it.startTime.substringAfter("T")).isAfter(nowTime)
                    } catch (_: Exception) {
                        false
                    }
                }
            }
        }
    }

    LaunchedEffect(dates) {
        if (!selectedDate.isNullOrBlank()) return@LaunchedEffect

        selectedDate = if (dates.contains(today)) {
            today
        } else {
            dates.firstOrNull()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendar") },
                navigationIcon = {
                    IconButton(onClick = { backStack.removeLastOrNull() }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    NearbyConnectionsDisplay()
                },
            )
        },
        bottomBar = {
            NavigationBar {
                dates.forEach {
                    NavigationBarItem(
                        selected = selectedDate == it,
                        onClick = { selectedDate = it },
                        icon = {
                            Text(it.takeLast(2))
                        },
                        label = {
                            Text(LocalDate.parse(it).dayOfWeek.toString())
                        }
                    )
                }
            }
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (selectedDate == today) {
                        FilterChip(
                            selected = showOnlyUpcoming,
                            onClick = { showOnlyUpcoming = !showOnlyUpcoming },
                            label = { Text("Upcoming") }
                        )
                    } else if (showOnlyUpcoming) {
                        SideEffect { showOnlyUpcoming = false }
                    }
                    FilterChip(
                        selected = showOnlyStarred,
                        onClick = { showOnlyStarred = !showOnlyStarred },
                        label = { Text("Starred") },
                        leadingIcon = if (showOnlyStarred) {
                            {
                                Icon(
                                    painter = painterResource(R.drawable.ic_star),
                                    contentDescription = null
                                )
                            }
                        } else null
                    )
                }
            }
            itemsIndexed(eventsAtDate, key = { _, event -> event.id }) { index, event ->
                if (index == indicatorIndex) {
                    NowIndicator(
                        time = currentTime.toLocalTime(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                }
                EventCard(event = event)
            }
            if (indicatorIndex == -1 && selectedDate == today && eventsAtDate.isNotEmpty()) {
                val lastEventEndsAfterNow = try {
                    LocalDateTime.parse(eventsAtDate.last().endTime).isAfter(currentTime)
                } catch (_: Exception) {
                    false
                }
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
        }
    }
}
