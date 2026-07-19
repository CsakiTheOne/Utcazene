package com.csakitheone.streetmusic.ui.screens

import android.content.Intent
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TwoRowsTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.csakitheone.streetmusic.R
import com.csakitheone.streetmusic.data.LocalRepository
import com.csakitheone.streetmusic.data.model.Event
import com.csakitheone.streetmusic.navigation.Destination
import com.csakitheone.streetmusic.navigation.LocalNavBackStack
import com.csakitheone.streetmusic.ui.components.EventCard
import com.utsman.osmandcompose.CameraProperty
import com.utsman.osmandcompose.CameraState
import com.utsman.osmandcompose.DefaultMapProperties
import com.utsman.osmandcompose.Marker
import com.utsman.osmandcompose.OpenStreetMap
import com.utsman.osmandcompose.rememberMarkerState
import kotlinx.coroutines.delay
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import java.time.LocalDateTime
import kotlin.time.Duration.Companion.seconds

@Composable
fun MapScreen() {
    val context = LocalContext.current
    val backStack = LocalNavBackStack.current
    val repository = LocalRepository.current
    val venues by repository.venues.collectAsState(initial = emptyList())
    val events by repository.events.collectAsState(initial = emptyList())

    val googleMyMapsUrl =
        remember { "https://www.google.com/maps/d/embed?mid=12plW9qjTupsu26_lLGD-lnE4jqUczO4U&ehbc=2E312F" }

    val nameToGeoPointMap = remember {
        mapOf(
            "Cemix színpad" to GeoPoint(47.093254, 17.907600),
            "Fortuna színpad" to GeoPoint(47.093067, 17.908593),
            "Food Truck Show színpad" to GeoPoint(47.093275, 17.912175),
            "Man at Work színpad" to GeoPoint(47.093729, 17.910940),
            "Pannon Egyetem színpad" to GeoPoint(47.093118, 17.910564),
            "Meló-Diák színpad" to GeoPoint(47.093613, 17.909783),
            "Kossuth utca" to GeoPoint(47.092774, 17.908938),
            "Gizella udvar" to GeoPoint(47.0934288, 17.9085051),
            // External locations
            "Sarolta udvar" to GeoPoint(47.093656, 17.909333),
            "Íródeák Művészeti Udvar" to GeoPoint(47.0906418, 17.9066187),
            "Gyárkert" to GeoPoint(47.0949355, 17.9160484),
            "TEREM" to GeoPoint(47.0926645, 17.9074231),
        )
    }

    var selectedMapIndex by rememberSaveable { mutableIntStateOf(0) }

    var now by remember { mutableStateOf(LocalDateTime.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(30.seconds)
            now = LocalDateTime.now()
        }
    }

    val nowPlayingEvents by remember(events, now) {
        derivedStateOf {
            events.filter {
                LocalDateTime.parse(it.startTime).isBefore(now) &&
                        LocalDateTime.parse(it.endTime).isAfter(now)
            }
        }
    }

    // Workaround for https://github.com/utsman/osm-android-compose/issues/4
    var cameraState by remember {
        mutableStateOf(
            CameraState(
                CameraProperty(
                    geoPoint = GeoPoint(47.0937412, 17.9104748),
                    zoom = 17.0
                )
            )
        )
    }

    LaunchedEffect(cameraState.zoom, cameraState.geoPoint) {
        cameraState = CameraState(
            CameraProperty(
                geoPoint = cameraState.geoPoint,
                zoom = cameraState.zoom
            )
        )
    }

    val mapProperties by remember {
        mutableStateOf(
            DefaultMapProperties.copy(
                minZoomLevel = 16.0,
                maxZoomLevel = 21.0,
            )
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2D2F2F)),
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Map") },
                    navigationIcon = {
                        IconButton(
                            onClick = { backStack.removeLastOrNull() },
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_arrow_back),
                                contentDescription = null,
                            )
                        }
                    },
                    actions = {
                        OutlinedButton(
                            onClick = {
                                context.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        "https://www.google.com/android/find/people".toUri()
                                    )
                                )
                            },
                        ) {
                            Icon(
                                modifier = Modifier
                                    .padding(end = ButtonDefaults.IconSpacing)
                                    .size(24.dp)
                                    .clip(CircleShape),
                                painter = painterResource(R.drawable.find_hub_icon),
                                contentDescription = null,
                                tint = Color.Unspecified,
                            )
                            Text("Find friends")
                        }
                    },
                )
                PrimaryTabRow(
                    selectedTabIndex = selectedMapIndex,
                ) {
                    Tab(
                        selected = selectedMapIndex == 0,
                        onClick = { selectedMapIndex = 0 },
                        text = { Text("by Csáki") },
                    )
                    Tab(
                        selected = selectedMapIndex == 1,
                        onClick = { selectedMapIndex = 1 },
                        text = { Text("by Utcazene") },
                    )
                }
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            when (selectedMapIndex) {
                0 -> {
                    OpenStreetMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraState = cameraState,
                        properties = mapProperties,
                    ) {
                        nameToGeoPointMap.keys.forEach { name ->
                            val geoPoint = remember(name) { nameToGeoPointMap[name]!! }
                            val venue = remember(name, venues) { venues.find { it.name == name } }
                            val nowPlaying = remember(name, nowPlayingEvents) {
                                nowPlayingEvents.filter { it.place == name }
                            }

                            Marker(
                                state = rememberMarkerState(key = name, geoPoint = geoPoint),
                                title = name,
                                icon = ContextCompat.getDrawable(
                                    context,
                                    R.drawable.ic_music_circle
                                ).apply { this!!.setTint(0xFFff5669.toInt()) },
                            ) {
                                ElevatedCard {
                                    Column(
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .widthIn(min = 150.dp, max = 300.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        Text(
                                            text = name,
                                            style = MaterialTheme.typography.titleMedium,
                                        )
                                        if (venue != null) {
                                            Text(
                                                text = venue.address,
                                                style = MaterialTheme.typography.labelSmall,
                                            )
                                        }
                                        if (nowPlaying.isNotEmpty()) {
                                            Text(
                                                modifier = Modifier.padding(top = 16.dp),
                                                text = "Now playing:"
                                            )
                                            nowPlaying.forEach { event ->
                                                EventCard(event = event)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { context ->
                            WebView(context).apply {
                                settings.domStorageEnabled = true
                                settings.javaScriptEnabled = true
                                webViewClient = WebViewClient()
                                loadUrl(googleMyMapsUrl)
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                )
                            }
                        },
                    )
                }
            }
        }
    }
}
