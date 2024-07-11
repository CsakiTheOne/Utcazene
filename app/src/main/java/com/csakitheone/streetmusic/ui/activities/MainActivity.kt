package com.csakitheone.streetmusic.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.csakitheone.streetmusic.R
import com.csakitheone.streetmusic.data.EventsProvider
import com.csakitheone.streetmusic.model.Event
import com.csakitheone.streetmusic.ui.components.EventCard
import com.csakitheone.streetmusic.ui.components.MenuCard
import com.csakitheone.streetmusic.ui.components.UzCard
import com.csakitheone.streetmusic.ui.theme.UtcazeneTheme
import com.csakitheone.streetmusic.util.CustomTabsManager
import com.csakitheone.streetmusic.util.Helper.Companion.toLocalTime
import com.csakitheone.streetmusic.util.InAppUpdater
import java.time.LocalDate
import java.time.LocalTime

class MainActivity : ComponentActivity() {
    private var events by mutableStateOf(listOf<Event>())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().apply {
            setKeepOnScreenCondition { events.isNotEmpty() }
        }
        InAppUpdater.init(this)
        setContent {
            MainScreen()
        }
        enableEdgeToEdge()
    }

    override fun onDestroy() {
        super.onDestroy()
        InAppUpdater.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        InAppUpdater.onResume(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == InAppUpdater.UPDATE_FLOW_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                return
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Preview
    @Composable
    fun MainScreen() {
        UtcazeneTheme {
            val context = LocalContext.current

            var isMenuOpen by remember { mutableStateOf(false) }

            var isDataStateVisible by remember(EventsProvider.state) {
                mutableStateOf(
                    EventsProvider.state != EventsProvider.STATE_DOWNLOADED &&
                            EventsProvider.state != EventsProvider.STATE_PREFERENCES
                )
            }

            val eventsNowPlaying by remember(events) {
                val sortedEvents = events
                    .filter { it.day == LocalDate.now().dayOfMonth }
                    .sortedBy { it.time.toLocalTime() }
                mutableStateOf(
                    sortedEvents.filter { event ->
                        val nextTime = sortedEvents.firstOrNull {
                            it.time.toLocalTime() > event.time.toLocalTime().plusMinutes(20)
                        }?.time?.toLocalTime()
                        event.time.toLocalTime() < LocalTime.now().plusMinutes(5) &&
                                (nextTime == null || nextTime > LocalTime.now().minusMinutes(5))
                    }
                )
            }

            val eventsNowPlayingScrollState = rememberScrollState()

            LaunchedEffect(Unit) {
                EventsProvider.getEventsThisYear(this@MainActivity) {
                    events = it
                }
            }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding(),
                    ) {
                        Image(
                            modifier = Modifier
                                .fillMaxWidth(),
                            painter = painterResource(id = R.drawable.header),
                            contentDescription = null,
                        )
                        FilledIconButton(
                            modifier = Modifier
                                .padding(8.dp)
                                .align(Alignment.TopEnd),
                            onClick = { isMenuOpen = true },
                        ) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = null)
                            DropdownMenu(
                                expanded = isMenuOpen,
                                onDismissRequest = { isMenuOpen = false },
                            ) {
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Language,
                                            contentDescription = null
                                        )
                                    },
                                    text = { Text(text = "Uz HUB") },
                                    onClick = {
                                        startActivity(
                                            Intent(
                                                this@MainActivity,
                                                HubActivity::class.java
                                            )
                                        )
                                        isMenuOpen = false
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text(text = stringResource(id = R.string.made_by_csaki)) },
                                    onClick = {
                                        startActivity(
                                            Intent(
                                                this@MainActivity,
                                                SupportActivity::class.java
                                            )
                                        )
                                        isMenuOpen = false
                                    },
                                )
                            }
                        }
                    }
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp),
                    ) {
                        AnimatedVisibility(
                            visible = EventsProvider.state != EventsProvider.STATE_UNKNOWN &&
                                    isDataStateVisible
                        ) {
                            UzCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .weight(1f),
                                        text = stringResource(id = EventsProvider.state),
                                    )
                                    TextButton(
                                        enabled = EventsProvider.state != EventsProvider.STATE_DOWNLOADING,
                                        onClick = {
                                            EventsProvider.getEventsThisYear(
                                                this@MainActivity,
                                                forceDownload = true
                                            )
                                        },
                                    ) {
                                        Text(text = stringResource(id = R.string.refresh_data))
                                    }
                                    TextButton(
                                        onClick = { isDataStateVisible = false },
                                    ) {
                                        Text(text = stringResource(id = R.string.hide))
                                    }
                                }
                            }
                        }
                        AnimatedVisibility(visible = InAppUpdater.isFlexibleUpdateReady) {
                            Card(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                                ),
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                ) {
                                    Text(
                                        modifier = Modifier.padding(8.dp),
                                        text = stringResource(id = R.string.update_available),
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                ) {
                                    TextButton(
                                        onClick = { InAppUpdater.completeFlexibleUpdate() },
                                    ) {
                                        Text(text = stringResource(id = R.string.install_update))
                                    }
                                }
                            }
                        }
                        Row {
                            MenuCard(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .weight(1f),
                                onClick = {
                                    startActivity(
                                        Intent(
                                            this@MainActivity,
                                            CalendarActivity::class.java
                                        )
                                    )
                                },
                                imageVector = Icons.Default.CalendarMonth,
                                title = stringResource(id = R.string.events),
                                isCompressed = eventsNowPlayingScrollState.canScrollBackward,
                            )
                            MenuCard(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .weight(1f),
                                onClick = {
                                    startActivity(
                                        Intent(
                                            this@MainActivity,
                                            MusiciansActivity::class.java
                                        )
                                    )
                                },
                                imageVector = Icons.Default.Mic,
                                title = stringResource(id = R.string.musicians),
                                isCompressed = eventsNowPlayingScrollState.canScrollBackward,
                            )
                        }
                        Row {
                            MenuCard(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .weight(1f),
                                onClick = {
                                    startActivity(
                                        Intent(
                                            this@MainActivity,
                                            PlacesActivity::class.java
                                        )
                                    )
                                },
                                imageVector = Icons.Default.Place,
                                title = stringResource(id = R.string.places),
                                isCompressed = eventsNowPlayingScrollState.canScrollBackward,
                            )
                            MenuCard(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .weight(1f),
                                onClick = {
                                    CustomTabsManager.open(
                                        this@MainActivity,
                                        "https://www.google.com/maps/d/u/0/embed?mid=12plW9qjTupsu26_lLGD-lnE4jqUczO4U&ehbc=2E312F&ll=47.09391673012697%2C17.90851453104826&z=15"
                                    )
                                },
                                imageVector = Icons.Default.Map,
                                title = stringResource(id = R.string.map),
                                isCompressed = eventsNowPlayingScrollState.canScrollBackward,
                            )
                        }
                        /*MenuCard(
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth(),
                            onClick = {
                                startActivity(
                                    Intent(
                                        this@MainActivity,
                                        TeremActivity::class.java
                                    )
                                )
                            },
                            painter = painterResource(id = R.drawable.terem_logo),
                            title = "TEREM: UNLOCK FEST Vol.4",
                        )*/
                        MenuCard(
                            modifier = Modifier.padding(8.dp),
                            onClick = {
                                startActivity(
                                    Intent(
                                        this@MainActivity,
                                        ExtrasActivity::class.java
                                    )
                                )
                            },
                            imageVector = Icons.Default.VideogameAsset,
                            title = stringResource(id = R.string.extras),
                            isCompressed = eventsNowPlayingScrollState.canScrollBackward,
                        )
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(eventsNowPlayingScrollState),
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            text = stringResource(id = R.string.now_playing),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        eventsNowPlaying.map { event ->
                            EventCard(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                event = event,
                            )
                        }
                        if (eventsNowPlaying.isEmpty()) {
                            Text(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth(),
                                text = "😴",
                                textAlign = TextAlign.Center,
                            )
                        }
                        Spacer(modifier = Modifier.navigationBarsPadding())
                    }
                }
            }
        }
    }
}
