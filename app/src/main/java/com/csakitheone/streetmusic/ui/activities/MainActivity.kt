package com.csakitheone.streetmusic.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Feed
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.csakitheone.streetmusic.R
import com.csakitheone.streetmusic.data.EventsProvider
import com.csakitheone.streetmusic.model.Event
import com.csakitheone.streetmusic.ui.components.AdaptiveFeed
import com.csakitheone.streetmusic.ui.components.MenuCard
import com.csakitheone.streetmusic.ui.components.UzCard
import com.csakitheone.streetmusic.ui.theme.UtcazeneTheme
import com.csakitheone.streetmusic.util.CustomTabsManager
import com.csakitheone.streetmusic.util.InAppUpdater

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
            var isMenuOpen by remember { mutableStateOf(false) }

            var isDataStateVisible by remember(EventsProvider.state) {
                mutableStateOf(EventsProvider.state != EventsProvider.STATE_DOWNLOADED)
            }

            val adaptiveFeedState = rememberLazyListState()

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
                    AnimatedContent(
                        targetState = adaptiveFeedState.canScrollBackward,
                    ) { canScrollUp ->
                        if (canScrollUp) {
                            TopAppBar(
                                title = { Text(text = stringResource(id = R.string.app_name)) },
                                actions = {
                                    IconButton(
                                        modifier = Modifier.padding(8.dp),
                                        onClick = { isDataStateVisible = !isDataStateVisible },
                                    ) {
                                        Icon(imageVector = Icons.Default.SdStorage, contentDescription = null)
                                    }
                                    IconButton(
                                        modifier = Modifier,
                                        onClick = { isMenuOpen = true },
                                    ) {
                                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = null)
                                        MainMenu(
                                            isOpen = isMenuOpen,
                                            onDismissRequest = { isMenuOpen = false },
                                        )
                                    }
                                }
                            )
                        }
                        else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .statusBarsPadding(),
                            ) {
                                Image(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            CustomTabsManager.open(
                                                this@MainActivity,
                                                "https://musiclab.chromeexperiments.com/Spectrogram/",
                                            )
                                        },
                                    painter = painterResource(id = R.drawable.header),
                                    contentDescription = null,
                                )
                                Row(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .align(Alignment.TopEnd),
                                ) {
                                    IconButton(
                                        onClick = { isDataStateVisible = !isDataStateVisible },
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SdStorage,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                    IconButton(
                                        onClick = { isMenuOpen = true },
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                        )
                                        MainMenu(
                                            isOpen = isMenuOpen,
                                            onDismissRequest = { isMenuOpen = false },
                                        )
                                    }
                                }
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
                                    IconButton(
                                        enabled = EventsProvider.state != EventsProvider.STATE_DOWNLOADING,
                                        onClick = {
                                            EventsProvider.getEventsThisYear(
                                                this@MainActivity,
                                                forceDownload = true
                                            )
                                        },
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = null,
                                        )
                                    }
                                    IconButton(
                                        onClick = { isDataStateVisible = false },
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = null,
                                        )
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
                                isCompressed = adaptiveFeedState.canScrollBackward,
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
                                title = "${stringResource(id = R.string.musicians)} (${events.groupBy { it.musician }.size})",
                                isCompressed = adaptiveFeedState.canScrollBackward,
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
                                isCompressed = adaptiveFeedState.canScrollBackward,
                            ) {
                                FilledTonalIconButton(
                                    modifier = Modifier,
                                    onClick = {
                                        CustomTabsManager.open(
                                            this@MainActivity,
                                            "https://www.google.com/maps/d/u/0/embed?mid=12plW9qjTupsu26_lLGD-lnE4jqUczO4U&ehbc=2E312F&ll=47.09391673012697%2C17.90851453104826&z=15"
                                        )
                                    },
                                ) {
                                    Icon(imageVector = Icons.Default.Map, contentDescription = null)
                                }
                            }
                            MenuCard(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .weight(1f),
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
                                isCompressed = adaptiveFeedState.canScrollBackward,
                            )
                        }
                    }
                    AdaptiveFeed(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .weight(1f)
                            .navigationBarsPadding(),
                        lazyListState = adaptiveFeedState,
                    )
                }
            }
        }
    }
    
    @Composable
    fun MainMenu(
        isOpen: Boolean,
        onDismissRequest: () -> Unit,
    ) {
        DropdownMenu(
            expanded = isOpen,
            onDismissRequest = onDismissRequest,
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
                    onDismissRequest()
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
                    onDismissRequest()
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
                    onDismissRequest()
                },
            )
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
                    onDismissRequest()
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
                    onDismissRequest()
                },
            )
        }
    }
}
