package com.csakitheone.streetmusic

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.csakitheone.streetmusic.util.BatteryManager
import com.csakitheone.streetmusic.data.MotdProvider
import com.csakitheone.streetmusic.ui.components.MenuCard
import com.csakitheone.streetmusic.ui.components.UzCard
import com.csakitheone.streetmusic.ui.components.WebCard
import com.csakitheone.streetmusic.ui.components.util.PreferenceHolder
import com.csakitheone.streetmusic.ui.theme.UtcazeneTheme

class MainActivity : ComponentActivity() {
    private var isBatterySaverPreferenceLoaded by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().apply {
            setKeepOnScreenCondition { isBatterySaverPreferenceLoaded }
        }
        setContent {
            MainScreen()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
    @Preview
    @Composable
    fun MainScreen() {
        var isDevWarningVisible by remember { mutableStateOf(true) }
        var isBatterySaverDialogVisible by remember { mutableStateOf(false) }
        var motd by remember { mutableStateOf(MotdProvider.getRandomMotd(this)) }

        PreferenceHolder(
            id = "batterySaver",
            value = BatteryManager.isBatterySaverEnabled,
            isValueChanged = {
                BatteryManager.isBatterySaverEnabled = it
                isBatterySaverPreferenceLoaded = true
            },
            defaultValue = false,
        )

        UtcazeneTheme {
            if (isBatterySaverDialogVisible) {
                AlertDialog(
                    title = { Text(text = stringResource(id = R.string.battery_saver)) },
                    text = { Text(text = stringResource(id = R.string.battery_saver_description)) },
                    onDismissRequest = { isBatterySaverDialogVisible = false },
                    dismissButton = {
                        TextButton(
                            onClick = { isBatterySaverDialogVisible = false }
                        ) {
                            Text(text = stringResource(id = R.string.close))
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                BatteryManager.isBatterySaverEnabled = !BatteryManager.isBatterySaverEnabled
                                isBatterySaverDialogVisible = false
                            }
                        ) {
                            Text(text = stringResource(id = if (BatteryManager.isBatterySaverEnabled) R.string.turn_off else R.string.turn_on))
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
                            if (!isDevWarningVisible) {
                                IconButton(
                                    onClick = {
                                        isDevWarningVisible = true
                                    }
                                ) {
                                    Icon(imageVector = Icons.Default.Warning, contentDescription = null)
                                }
                            }
                            IconButton(
                                onClick = {
                                    isBatterySaverDialogVisible = true
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BatterySaver,
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
                        modifier = Modifier.padding(8.dp),
                    ) {
                        if (isDevWarningVisible) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                                ),
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.End,
                                ) {
                                    Text(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        text = stringResource(id = R.string.developer_warning),
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                    TextButton(
                                        onClick = { isDevWarningVisible = false },
                                    ) {
                                        Text(text = stringResource(id = R.string.hide))
                                    }
                                }
                            }
                        }
                        Row {
                            AnimatedContent(
                                modifier = Modifier.weight(1f),
                                targetState = motd,
                                label = "MotdChange",
                            ) {
                                UzCard(
                                    modifier = Modifier.padding(8.dp),
                                ) {
                                    Text(
                                        modifier = Modifier.padding(16.dp),
                                        text = it,
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                            }
                            SmallFloatingActionButton(
                                modifier = Modifier.padding(8.dp),
                                onClick = { motd = MotdProvider.getRandomMotd(this@MainActivity, motd) },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NavigateNext,
                                    contentDescription = null,
                                )
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
                                            AuthorsActivity::class.java
                                        )
                                    )
                                },
                                imageVector = Icons.Default.Mic,
                                title = stringResource(id = R.string.authors),
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
                        }
                        if (BatteryManager.isBatterySaverEnabled) {
                            MenuCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                onClick = { isBatterySaverDialogVisible = true },
                                imageVector = Icons.Default.BatterySaver,
                                title = stringResource(id = R.string.battery_saver)
                            )
                            Spacer(modifier = Modifier.weight(1f))
                        }
                        else {
                            WebCard(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .weight(1f),
                            )
                        }
                        MenuCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            onClick = {
                                startActivity(Intent(this@MainActivity, SupportActivity::class.java))
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
