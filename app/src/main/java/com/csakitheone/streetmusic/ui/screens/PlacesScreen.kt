package com.csakitheone.streetmusic.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.carousel.CarouselDefaults
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.csakitheone.streetmusic.R
import com.csakitheone.streetmusic.data.LocalRepository
import com.csakitheone.streetmusic.navigation.Destination
import com.csakitheone.streetmusic.navigation.LocalNavBackStack
import com.csakitheone.streetmusic.ui.components.EventCard
import com.csakitheone.streetmusic.ui.components.NearbyConnectionsDisplay
import java.time.LocalDate
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacesScreen(
    onRequestSearch: () -> Unit,
) {
    val repository = LocalRepository.current
    val backStack = LocalNavBackStack.current

    val events by repository.events.collectAsState(initial = emptyList())
    val allStarredSlugs by repository.allFavorites.collectAsState(initial = emptySet())
    val dates by repository.eventDates.collectAsState(initial = emptyList())
    var selectedDate by rememberSaveable { mutableStateOf<String?>(null) }
    var showOnlyStarred by rememberSaveable { mutableStateOf(false) }
    var showOnlyUpcoming by rememberSaveable { mutableStateOf(false) }

    val today = remember { LocalDate.now().toString() }

    val eventsByPlace by remember(
        events,
        selectedDate,
        showOnlyStarred,
        showOnlyUpcoming,
        allStarredSlugs
    ) {
        derivedStateOf {
            val now = LocalDateTime.now()
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
                            LocalDateTime.parse(it.endTime).isAfter(now)
                        } catch (_: Exception) {
                            true
                        }
                    } else true
                }
                .groupBy { it.place }
                .mapValues { entry ->
                    entry.value.sortedBy { it.startTime }
                }
                .toSortedMap()
        }
    }

    LaunchedEffect(dates) {
        val today = LocalDate.now().toString()

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
                title = { Text("Places") },
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
                    IconButton(
                        onClick = { onRequestSearch() }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_search),
                            contentDescription = "Search"
                        )
                    }
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
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { backStack.add(Destination.Map) },
                text = { Text("Map") },
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_map),
                        contentDescription = null
                    )
                },
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
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
            items(eventsByPlace.keys.toList()) { placeName ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        text = placeName,
                        style = MaterialTheme.typography.titleMedium
                    )
                    HorizontalMultiBrowseCarousel(
                        state = rememberCarouselState(initialItem = 0) {
                            eventsByPlace[placeName]?.size ?: 0
                        },
                        preferredItemWidth = 256.dp,
                        flingBehavior = CarouselDefaults.noSnapFlingBehavior(),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        itemSpacing = 16.dp,
                    ) { itemIndex ->
                        val event = eventsByPlace[placeName]?.get(itemIndex)
                        if (event != null) {
                            EventCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .widthIn(min = 240.dp),
                                event = event,
                                hidePlace = true,
                            )
                        }
                    }
                }
            }
        }
    }
}
