package com.csakitheone.streetmusic.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.zIndex
import com.csakitheone.streetmusic.R
import com.csakitheone.streetmusic.data.TeremEventProvider
import com.csakitheone.streetmusic.ui.components.DaySelectorRow
import com.csakitheone.streetmusic.ui.components.MusicianCard
import com.csakitheone.streetmusic.ui.components.UzCard
import com.csakitheone.streetmusic.ui.theme.UtcazeneTheme
import com.csakitheone.streetmusic.util.CustomTabsManager
import com.csakitheone.streetmusic.util.TranslatorManager
import java.time.LocalDate

class TeremActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalendarScreen()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Preview
    @Composable
    fun CalendarScreen() {
        UtcazeneTheme {
            val scroll = rememberLazyListState()

            var selectedDay by remember {
                mutableStateOf(
                    if ((19..22).contains(LocalDate.now().dayOfMonth)) LocalDate.now().dayOfMonth
                    else 19
                )
            }
            var englishDescription: String? by remember { mutableStateOf(null) }

            val musiciansToday by remember(selectedDay) {
                mutableStateOf(
                    TeremEventProvider.musicians[selectedDay] ?: listOf()
                )
            }

            if (!englishDescription.isNullOrBlank()) {
                AlertDialog(
                    title = { Text(text = stringResource(id = R.string.musician_description)) },
                    text = { Text(text = englishDescription ?: "") },
                    onDismissRequest = { englishDescription = null },
                    confirmButton = {
                        TextButton(onClick = { englishDescription = null }) {
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
                    Surface(
                        modifier = Modifier.zIndex(2f),
                        shadowElevation = if (scroll.canScrollBackward) 16.dp else 0.dp,
                    ) {
                        TopAppBar(
                            title = { Text(text = "TEREM: UNLOCK FEST Vol.4") },
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
                                    onClick = {
                                        CustomTabsManager.open(
                                            this@TeremActivity,
                                            "https://fb.me/e/1k0ftn6MA"
                                        )
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_facebook),
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
                    }
                    LazyColumn(
                        modifier = Modifier
                            .padding(horizontal = 8.dp),
                        state = scroll,
                    ) {
                        item {
                            DaySelectorRow(
                                selectedDay = selectedDay,
                                onChange = { selectedDay = it },
                            )
                            UzCard(
                                modifier = Modifier.padding(8.dp),
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            modifier = Modifier.padding(8.dp),
                                            text = stringResource(id = R.string.musician_description),
                                            style = MaterialTheme.typography.titleMedium,
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        IconButton(
                                            onClick = {
                                                TranslatorManager
                                                    .translateDescription(TeremEventProvider.description) {
                                                        englishDescription = it
                                                    }
                                            },
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Translate,
                                                contentDescription = null,
                                            )
                                        }
                                    }
                                    Text(
                                        modifier = Modifier.padding(8.dp),
                                        text = TeremEventProvider.description,
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                            }
                        }
                        items(
                            items = musiciansToday,
                            key = { it.name }) { musician ->
                            MusicianCard(
                                modifier = Modifier.padding(8.dp),
                                musician = musician,
                            )
                        }
                    }
                }
            }
        }
    }
}
