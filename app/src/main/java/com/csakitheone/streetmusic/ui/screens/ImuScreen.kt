package com.csakitheone.streetmusic.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.csakitheone.streetmusic.R
import com.csakitheone.streetmusic.data.ImuRepository
import com.csakitheone.streetmusic.navigation.LocalNavBackStack
import com.csakitheone.streetmusic.ui.components.FavoritesIndicator
import com.csakitheone.streetmusic.ui.components.NowIndicator
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.collections.sortedBy
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImuScreen() {
    val backStack = LocalNavBackStack.current
    val uriHandler = LocalUriHandler.current

    val days = ImuRepository.eventDays
    var selectedDay by rememberSaveable { mutableIntStateOf(days.firstOrNull() ?: 0) }

    var currentTime by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30.seconds)
            currentTime = LocalDateTime.now()
        }
    }

    val events = remember(selectedDay) {
        ImuRepository.events.filter { it.day == selectedDay }
            .sortedBy { if (it.time.hour >= 12) it.time.toSecondOfDay() else it.time.toSecondOfDay() + 86400 }
    }

    val todayDay = remember { currentTime.dayOfMonth }

    val indicatorIndex by remember(events, selectedDay, currentTime) {
        derivedStateOf {
            if (selectedDay != todayDay) -1
            else {
                val nowTime = currentTime.toLocalTime()
                events.indexOfFirst { it.time.isAfter(nowTime) }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Íródeák Művészeti Udvar") },
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
                            uriHandler.openUri(ImuRepository.imuFacebookUrl)
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
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(events) { index, event ->
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = event.time.toString(),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        SelectionContainer(modifier = Modifier.weight(1f)) {
                            Text(
                                text = event.name,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                        FavoritesIndicator(slug = "imu_${event.name}")
                    }
                }
            }
            if (indicatorIndex == -1 && selectedDay == todayDay && events.isNotEmpty()) {
                val lastEventTime = events.last().time
                if (currentTime.toLocalTime().isAfter(lastEventTime)) {
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
