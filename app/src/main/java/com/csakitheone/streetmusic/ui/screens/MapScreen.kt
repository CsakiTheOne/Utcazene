package com.csakitheone.streetmusic.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.material3.rememberStandardBottomSheetState
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.csakitheone.streetmusic.R
import com.csakitheone.streetmusic.data.LocalRepository
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
import java.time.LocalDateTime
import kotlin.time.Duration.Companion.seconds
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toDrawable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(initialPlaceName: String? = null) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val backStack = LocalNavBackStack.current
    val repository = LocalRepository.current
    val venues by repository.venues.collectAsState(initial = emptyList())
    val events by repository.events.collectAsState(initial = emptyList())

    val googleMapsBackgroundColor = remember { Color(0xFF4d6a79) }
    val googleMyMapsUrl =
        remember { "https://www.google.com/maps/d/u/0/embed?mid=12plW9qjTupsu26_lLGD-lnE4jqUczO4U&ll=47.09344004259072%2C17.90931178892166&z=17" }

    val nameToGeoPointMap = remember {
        mapOf(
            "Cemix színpad" to GeoPoint(47.093254, 17.907600),
            "Fortuna színpad" to GeoPoint(47.093067, 17.908593),
            "Food Truck Show színpad" to GeoPoint(47.093418, 17.912079),
            "Man at Work színpad" to GeoPoint(47.093729, 17.910940),
            "Pannon Egyetem színpad" to GeoPoint(47.093118, 17.910564),
            "Meló-Diák színpad" to GeoPoint(47.093613, 17.909783),
            "Kossuth utca" to GeoPoint(47.092774, 17.908938),
            "Gizella udvar" to GeoPoint(47.093458, 17.908763),
            // External locations
            "Sarolta udvar" to GeoPoint(47.093656, 17.909333),
            "Íródeák" to GeoPoint(47.0906418, 17.9066187),
            "Gyárkert" to GeoPoint(47.0949355, 17.9160484),
            "TEREM" to GeoPoint(47.0926645, 17.9074231),
        )
    }

    var selectedPlaceName by rememberSaveable { mutableStateOf(initialPlaceName) }

    var selectedMapIndex by rememberSaveable { mutableIntStateOf(0) }
    val colorScheme = MaterialTheme.colorScheme
    val uiContainerColor by remember(colorScheme, selectedMapIndex) {
        derivedStateOf {
            when (selectedMapIndex) {
                0 -> colorScheme.surfaceContainer.copy(alpha = .75f)
                1 -> googleMapsBackgroundColor
                else -> colorScheme.surfaceContainer
            }
        }
    }
    val uiContentColor by remember(colorScheme, selectedMapIndex) {
        derivedStateOf {
            when (selectedMapIndex) {
                0 -> colorScheme.onSurface
                1 -> Color.White
                else -> colorScheme.onSurface
            }
        }
    }

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
        val initialGeoPoint = initialPlaceName?.let { nameToGeoPointMap[it] }
            ?: GeoPoint(47.0937412, 17.9104748)
        mutableStateOf(
            CameraState(
                CameraProperty(
                    geoPoint = initialGeoPoint,
                    zoom = if (initialPlaceName != null) 19.0 else 17.0
                )
            )
        )
    }

    LaunchedEffect(selectedPlaceName) {
        selectedPlaceName?.let { name ->
            nameToGeoPointMap[name]?.let { geoPoint ->
                cameraState = CameraState(
                    CameraProperty(
                        geoPoint = geoPoint,
                        zoom = 19.0
                    )
                )
            }
        }
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

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(
            initialValue = if (initialPlaceName != null) SheetValue.PartiallyExpanded else SheetValue.Hidden,
        )
    )

    LaunchedEffect(selectedPlaceName) {
        if (selectedPlaceName != null) {
            scaffoldState.bottomSheetState.partialExpand()
        } else {
            scaffoldState.bottomSheetState.hide()
        }
    }

    LaunchedEffect(scaffoldState.bottomSheetState.currentValue) {
        if (scaffoldState.bottomSheetState.currentValue == SheetValue.Hidden) {
            selectedPlaceName = null
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 250.dp,
        sheetContent = {
            if (selectedPlaceName != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    val name = selectedPlaceName!!
                    val venue = remember(name, venues) { venues.find { it.name == name } }
                    val nowPlaying = remember(name, nowPlayingEvents) {
                        nowPlayingEvents.filter { it.place == name }
                    }
                    val upcoming = remember(name, events, now) {
                        val today = now.toLocalDate().toString()
                        events.filter {
                            it.place == name &&
                                    it.startTime.startsWith(today) &&
                                    LocalDateTime.parse(it.startTime).isAfter(now)
                        }.sortedBy { it.startTime }
                    }
                    val tomorrow = remember(name, events, now) {
                        val tomorrow = now.plusDays(1).toLocalDate().toString()
                        events.filter {
                            it.place == name &&
                                    it.startTime.startsWith(tomorrow)
                        }.sortedBy { it.startTime }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SelectionContainer(modifier = Modifier.weight(1f)) {
                            Column {
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.titleLarge,
                                )
                                if (venue != null) {
                                    Text(
                                        text = venue.address,
                                        style = MaterialTheme.typography.labelSmall,
                                    )
                                }
                            }
                        }
                        IconButton(
                            onClick = { selectedPlaceName = null }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_close),
                                contentDescription = null,
                            )
                        }
                    }

                    SelectionContainer {
                        Text(
                            modifier = Modifier.alpha(.5f),
                            text = nameToGeoPointMap[name].toString().dropLast(4),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }

                    when (name) {
                        "Íródeák" -> {
                            Button(
                                onClick = { backStack.add(Destination.Imu) }
                            ) {
                                Text("See what's up in Íródeák Művészeti Udvar")
                            }
                        }

                        "Gyárkert" -> {
                            Button(
                                onClick = { backStack.add(Destination.Gyarkert) }
                            ) {
                                Text("See events in Gyárkert")
                            }
                        }

                        "TEREM" -> {
                            Button(
                                onClick = { backStack.add(Destination.UnlockFest) }
                            ) {
                                Text("See more about Unlock Fest")
                            }
                        }
                    }

                    if (nowPlaying.isNotEmpty()) {
                        Text("Now playing:")
                        nowPlaying.forEach { event ->
                            EventCard(event = event)
                        }
                    }
                    if (upcoming.isNotEmpty()) {
                        Text("Upcoming today:")
                        upcoming.forEach { event ->
                            EventCard(event = event)
                        }
                    }
                    if (tomorrow.isNotEmpty()) {
                        Text("Tomorrow's lineup:")
                        tomorrow.forEach { event ->
                            EventCard(event = event)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = max(
                        paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                        paddingValues.calculateEndPadding(LayoutDirection.Ltr)
                    )
                )
                .background(googleMapsBackgroundColor),
        ) {
            if (selectedMapIndex == 0) {
                OpenStreetMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraState = cameraState,
                    properties = mapProperties,
                    onMapClick = { selectedPlaceName = null }
                ) {
                    nameToGeoPointMap.keys.forEach { name ->
                        val geoPoint = remember(name) { nameToGeoPointMap[name]!! }
                        val markerIcon = remember(name, selectedPlaceName == name, colorScheme.primary) {
                            val isSelected = selectedPlaceName == name
                            val color = if (isSelected) 0xFFff5669.toInt()
                            else colorScheme.primary.copy(
                                red = colorScheme.primary.red * 0.75f,
                                green = colorScheme.primary.green * 0.75f,
                                blue = colorScheme.primary.blue * 0.75f,
                            ).toArgb()
                            val icon = ContextCompat.getDrawable(context, R.drawable.marker_music_circle)!!.apply {
                                setTint(color)
                            }

                            val density = resources.displayMetrics.density
                            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                                this.color = Color.Black.toArgb()
                                textSize = 12 * density
                                textAlign = Paint.Align.CENTER
                                typeface = Typeface.DEFAULT_BOLD
                            }

                            val textBounds = Rect()
                            textPaint.getTextBounds(name, 0, name.length, textBounds)

                            val iconSize = (24 * density).toInt()
                            val padding = (4 * density).toInt()
                            val width = maxOf(iconSize, textBounds.width() + padding * 2)
                            val height = iconSize + textBounds.height() + padding * 2

                            val bitmap = createBitmap(width, height)
                            val canvas = Canvas(bitmap)

                            icon.setBounds(
                                (width - iconSize) / 2,
                                padding,
                                (width + iconSize) / 2,
                                padding + iconSize
                            )
                            icon.draw(canvas)

                            canvas.drawText(
                                name,
                                width / 2f,
                                textBounds.height().toFloat() + padding + iconSize,
                                textPaint
                            )

                            bitmap.toDrawable(resources)
                        }

                        Marker(
                            state = rememberMarkerState(key = name, geoPoint = geoPoint),
                            title = name,
                            snippet = name,
                            icon = markerIcon,
                            onClick = {
                                selectedPlaceName = name
                                true
                            }
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
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
                        Button(
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
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = uiContainerColor,
                        titleContentColor = uiContentColor,
                        navigationIconContentColor = uiContentColor,
                        actionIconContentColor = uiContentColor,
                    ),
                )
                PrimaryTabRow(
                    selectedTabIndex = selectedMapIndex,
                    divider = {},
                    containerColor = uiContainerColor,
                    contentColor = uiContentColor,
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
                if (selectedMapIndex == 1) {
                    AndroidView(
                        modifier = Modifier.weight(1f),
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
