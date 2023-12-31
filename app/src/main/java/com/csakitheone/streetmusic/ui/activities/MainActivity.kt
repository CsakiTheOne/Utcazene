package com.csakitheone.streetmusic.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Feed
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.window.DialogProperties
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.csakitheone.streetmusic.R
import com.csakitheone.streetmusic.data.EventsProvider
import com.csakitheone.streetmusic.data.MotdProvider
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
    companion object {

        const val EXTRA_IGNORE_EVENT_ENDED = "ignore_event_ended"

    }

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

        if (!intent.getBooleanExtra(EXTRA_IGNORE_EVENT_ENDED, false)) {
            startActivity(Intent(this, HubActivity::class.java))
            finish()
        }
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

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
    @Preview
    @Composable
    fun MainScreen() {
        UtcazeneTheme {
            val context = LocalContext.current

            var isDataStateVisible by remember(EventsProvider.state) {
                mutableStateOf(
                    EventsProvider.state != EventsProvider.STATE_DOWNLOADED &&
                            EventsProvider.state != EventsProvider.STATE_PREFERENCES
                )
            }
            var motd by remember { mutableStateOf(MotdProvider.getRandomMotd(context)) }
            var isWebsitesMenuVisible by remember { mutableStateOf(false) }

            var isNowPlayingDialogVisible by remember { mutableStateOf(false) }
            val eventsNowPlaying by remember(events, isNowPlayingDialogVisible) {
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

            LaunchedEffect(Unit) {
                EventsProvider.getEvents(this@MainActivity) {
                    events = it
                    if (intent.data.toString() == "now_playing") {
                        isNowPlayingDialogVisible = true
                    }
                }
            }

            if (isNowPlayingDialogVisible) {
                AlertDialog(
                    properties = DialogProperties(usePlatformDefaultWidth = false),
                    title = { Text(text = stringResource(id = R.string.now_playing)) },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                        ) {
                            eventsNowPlaying.map { event ->
                                EventCard(
                                    modifier = Modifier.padding(8.dp),
                                    event = event,
                                )
                            }
                        }
                    },
                    onDismissRequest = { isNowPlayingDialogVisible = false },
                    confirmButton = {
                        TextButton(
                            onClick = { isNowPlayingDialogVisible = false }
                        ) {
                            Text(text = stringResource(id = R.string.close))
                        }
                    },
                )
            }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    TopAppBar(
                        title = { Text(text = stringResource(id = R.string.app_name)) },
                        actions = {
                            AnimatedVisibility(
                                visible = EventsProvider.state != EventsProvider.STATE_UNKNOWN &&
                                        !isDataStateVisible
                            ) {
                                IconButton(
                                    onClick = {
                                        isDataStateVisible = true
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Storage,
                                        contentDescription = null
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.smallTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            titleContentColor = MaterialTheme.colorScheme.onBackground,
                            navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                            actionIconContentColor = MaterialTheme.colorScheme.onBackground,
                        ),
                    )
                    Column(
                        modifier = Modifier.padding(8.dp),
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
                                            EventsProvider.getEvents(
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
                        Column {
                            AnimatedContent(
                                targetState = motd,
                                label = "MotdChange",
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 80.dp, max = 120.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        modifier = Modifier.padding(8.dp),
                                        text = it,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onBackground,
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Button(
                                    modifier = Modifier.padding(8.dp),
                                    onClick = {
                                        isNowPlayingDialogVisible = true
                                    },
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = null,
                                    )
                                    Text(
                                        modifier = Modifier.padding(start = 8.dp),
                                        text = stringResource(id = R.string.now_playing),
                                    )
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                SmallFloatingActionButton(
                                    modifier = Modifier.padding(8.dp),
                                    onClick = {
                                        motd = MotdProvider.getRandomMotd(this@MainActivity, motd)
                                    },
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NavigateNext,
                                        contentDescription = null,
                                    )
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
                            )
                        }
                        MenuCard(
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
                        )
                        MenuCard(
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth(),
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
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        MenuCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            onClick = { isWebsitesMenuVisible = true },
                            imageVector = Icons.Default.Language,
                            title = stringResource(id = R.string.websites),
                        ) {
                            DropdownMenu(
                                expanded = isWebsitesMenuVisible,
                                onDismissRequest = { isWebsitesMenuVisible = false }
                            ) {
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Feed,
                                            contentDescription = null,
                                        )
                                    },
                                    text = { Text(text = stringResource(id = R.string.open_website)) },
                                    onClick = {
                                        CustomTabsManager.open(
                                            this@MainActivity,
                                            "https://utcazene.hu/"
                                        )
                                        isWebsitesMenuVisible = false
                                    },
                                )
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_facebook),
                                            contentDescription = null,
                                        )
                                    },
                                    text = { Text(text = stringResource(id = R.string.open_facebook)) },
                                    onClick = {
                                        CustomTabsManager.open(
                                            this@MainActivity,
                                            "https://facebook.com/utcazene"
                                        )
                                        isWebsitesMenuVisible = false
                                    },
                                )
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_instagram),
                                            contentDescription = null,
                                        )
                                    },
                                    text = { Text(text = stringResource(id = R.string.open_instagram)) },
                                    onClick = {
                                        CustomTabsManager.open(
                                            this@MainActivity,
                                            "https://instagram.com/utcazene"
                                        )
                                        isWebsitesMenuVisible = false
                                    },
                                )
                            }
                        }
                        MenuCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            onClick = {
                                startActivity(
                                    Intent(
                                        this@MainActivity,
                                        SupportActivity::class.java
                                    )
                                )
                            },
                            imageVector = Icons.Default.Code,
                            title = stringResource(id = R.string.made_by_csaki),
                        )
                    }
                }
            }
        }
    }
}
