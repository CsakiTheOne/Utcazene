package com.csakitheone.streetmusic

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.csakitheone.streetmusic.util.Helper.Companion.toLocalTime
import com.csakitheone.streetmusic.util.BatteryManager
import com.csakitheone.streetmusic.model.Musician
import com.csakitheone.streetmusic.model.Event
import com.csakitheone.streetmusic.ui.components.MusicianCard
import com.csakitheone.streetmusic.ui.components.MenuCard
import com.csakitheone.streetmusic.ui.components.util.ListPreferenceHolder
import com.csakitheone.streetmusic.ui.theme.UtcazeneTheme
import com.csakitheone.streetmusic.util.Helper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.time.ZoneId

class EventActivity : ComponentActivity() {
    companion object {
        const val EXTRA_EVENT_JSON = "event_json"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EventScreen()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Preview
    @Composable
    fun EventScreen() {
        val context = LocalContext.current

        var event: Event? by remember { mutableStateOf(null) }
        var eventsPinned by remember { mutableStateOf<List<Event>>(listOf()) }
        val isPinned by remember(event, eventsPinned) {
            mutableStateOf(eventsPinned.contains(event))
        }

        LaunchedEffect(Unit) {
            event = Gson().fromJson(intent.getStringExtra(EXTRA_EVENT_JSON), Event::class.java)
        }

        ListPreferenceHolder(
            id = "eventsPinned",
            value = eventsPinned,
            onValueChanged = { eventsPinned = it.toList() },
            type = object : TypeToken<Event>() {}.type,
        )

        UtcazeneTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column {
                    TopAppBar(
                        title = { Text(text = "${stringResource(id = R.string.month_july)} ${event?.day}. ${event?.time ?: "00:00"}") },
                        navigationIcon = {
                            IconButton(onClick = { finish() }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = null,
                                )
                            }
                        },
                        actions = {
                            IconButton(
                                modifier = Modifier.padding(start = 8.dp),
                                onClick = {
                                    eventsPinned = if (!isPinned) eventsPinned + event!!
                                    else eventsPinned.filter { it != event }
                                },
                            ) {
                                Icon(
                                    imageVector = if (isPinned) Icons.Default.Star
                                    else Icons.Default.StarBorder,
                                    contentDescription = null,
                                )
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
                        modifier = Modifier
                            .padding(8.dp)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        if (
                            !BatteryManager.isBatterySaverEnabled &&
                            Helper.isUnmeteredNetworkAvailable(context) &&
                            !event?.musician?.imageUrl.isNullOrBlank()
                        ) {
                            OutlinedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                            ) {
                                AsyncImage(
                                    modifier = Modifier.fillMaxWidth(),
                                    model = if (event != null && !event!!.musician.imageUrl.isNullOrBlank()) event!!.musician.imageUrl
                                    else "https://http.cat/images/404.jpg",
                                    contentDescription = null,
                                )
                            }
                        }
                        MusicianCard(
                            modifier = Modifier.padding(8.dp),
                            musician = event?.musician ?: Musician.fromString("Előadó neve (Ország)"),
                        )
                        Row {
                            Row(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    modifier = Modifier.padding(8.dp),
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                )
                                Text(
                                    modifier = Modifier.padding(8.dp),
                                    text = "${stringResource(id = R.string.month_july)} ${event?.day ?: 19}.",
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    modifier = Modifier.padding(8.dp),
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                )
                                Text(
                                    modifier = Modifier.padding(8.dp),
                                    text = event?.time ?: "00:00",
                                )
                            }
                        }
                        if (event?.place?.geoLink == null) {
                            Row(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    modifier = Modifier.padding(8.dp),
                                    imageVector = Icons.Default.Place,
                                    contentDescription = null,
                                )
                                Text(
                                    modifier = Modifier.padding(8.dp),
                                    text = event?.place?.name ?: "...",
                                )
                            }
                        }
                        else {
                            MenuCard(
                                modifier = Modifier.padding(8.dp),
                                onClick = {
                                    startActivity(
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse(event?.place?.geoLink)
                                        )
                                    )
                                },
                                imageVector = Icons.Default.Place,
                                title = event?.place?.name,
                            )
                        }

                        /*
                        MenuCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            onClick = {
                                //TODO show place
                            },
                            icon = Icons.Default.Place,
                            title = event?.place ?: "Helyszín",
                        )
                        */
                        MenuCard(
                            modifier = Modifier.padding(8.dp),
                            onClick = {
                                val eventLocalTime = event?.time?.toLocalTime() ?: LocalTime.now()
                                val eventStart = LocalDateTime.now()
                                    .withMonth(Month.JULY.value)
                                    .withDayOfMonth(event?.day ?: 19)
                                    .withHour(eventLocalTime.hour)
                                    .withMinute(eventLocalTime.minute)
                                    .withSecond(0)
                                    .withNano(0)
                                    .atZone(ZoneId.systemDefault())
                                    .toInstant()
                                    .toEpochMilli()
                                startActivity(
                                    Intent(Intent.ACTION_EDIT)
                                        .setType("vnd.android.cursor.item/event")
                                        .putExtra(
                                            CalendarContract.Events.TITLE,
                                            event?.musician?.name
                                        )
                                        .putExtra(
                                            CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                                            eventStart
                                        )
                                        .putExtra(
                                            CalendarContract.Events.EVENT_LOCATION,
                                            event?.place?.name
                                        )
                                        .putExtra(
                                            CalendarContract.Events.DESCRIPTION,
                                            getString(R.string.exported_event_description)
                                        )
                                )
                            },
                            imageVector = Icons.Default.Event,
                            title = stringResource(id = R.string.export_to_calendar),
                        )
                    }
                }
            }
        }
    }
}
