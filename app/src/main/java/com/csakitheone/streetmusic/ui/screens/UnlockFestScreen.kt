package com.csakitheone.streetmusic.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.csakitheone.streetmusic.R
import com.csakitheone.streetmusic.data.UnlockFestRepository
import com.csakitheone.streetmusic.navigation.LocalNavBackStack
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnlockFestScreen() {
    val backStack = LocalNavBackStack.current
    val uriHandler = LocalUriHandler.current

    val days = UnlockFestRepository.eventDays
    var selectedDay by remember { mutableIntStateOf(days.firstOrNull() ?: 0) }

    val events = remember(selectedDay) {
        UnlockFestRepository.events.filter { it.day == selectedDay }.sortedBy { it.order }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Unlock Fest Vol. VII") },
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
                            uriHandler.openUri(UnlockFestRepository.ticketsUrl)
                        },
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_ticket),
                            contentDescription = "Tickets"
                        )
                    }
                    IconButton(
                        onClick = {
                            uriHandler.openUri(UnlockFestRepository.facebookEventUrl)
                        },
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_facebook),
                            contentDescription = "Facebook"
                        )
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                days.forEach { day ->
                    NavigationBarItem(
                        selected = selectedDay == day,
                        onClick = { selectedDay = day },
                        icon = {
                            Text("$day")
                        },
                        label = {
                            Text(LocalDate.parse("2026-07-$day").dayOfWeek.name)
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            val ticketPriceForSelectedDay by remember(selectedDay) {
                derivedStateOf { UnlockFestRepository.ticketPrices[selectedDay] ?: 0 }
            }
            ExtendedFloatingActionButton(
                text = { Text("Daily ticket: $ticketPriceForSelectedDay HUF") },
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_ticket),
                        contentDescription = "Tickets"
                    )
                },
                onClick = {
                    uriHandler.openUri(UnlockFestRepository.ticketsUrl)
                }
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(events) { event ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        event.url?.let { uriHandler.openUri(it) }
                    },
                ) {
                    Text(
                        modifier = Modifier.padding(16.dp),
                        text = event.name,
                        style = if (event.url != null) MaterialTheme.typography.titleMedium
                        else MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}
