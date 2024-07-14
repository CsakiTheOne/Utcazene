package com.csakitheone.streetmusic.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.csakitheone.streetmusic.R
import com.csakitheone.streetmusic.ui.components.MenuCard
import com.csakitheone.streetmusic.ui.components.UzCard
import com.csakitheone.streetmusic.ui.theme.UtcazeneTheme
import com.csakitheone.streetmusic.util.CustomTabsManager

class ExtrasActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ExtrasScreen()
        }
        enableEdgeToEdge()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Preview
    @Composable
    fun ExtrasScreen() {
        UtcazeneTheme {
            var isIdeasDialogVisible by remember { mutableStateOf(false) }
            var ideaIndex by remember(isIdeasDialogVisible) { mutableStateOf(0) }
            val idea = stringArrayResource(id = R.array.drinking_game_ideas)[ideaIndex]

            if (isIdeasDialogVisible) {
                AlertDialog(
                    properties = DialogProperties(usePlatformDefaultWidth = false),
                    modifier = Modifier
                        .padding(28.dp)
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    title = {
                        Text(text = idea.substringBefore(':').trim())
                    },
                    text = {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            Text(text = idea.substringAfter(':').trim())
                        }
                    },
                    onDismissRequest = { isIdeasDialogVisible = false },
                    dismissButton = {
                        TextButton(onClick = { isIdeasDialogVisible = false }) {
                            Text(text = stringResource(id = R.string.close))
                        }
                    },
                    confirmButton = {
                        Row {
                            IconButton(
                                onClick = { if (ideaIndex > 0) ideaIndex-- },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipPrevious,
                                    contentDescription = null,
                                )
                            }
                            IconButton(
                                onClick = {
                                    val lastIndex = resources.getStringArray(R.array.drinking_game_ideas).lastIndex
                                    if (ideaIndex <  lastIndex) ideaIndex++
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipNext,
                                    contentDescription = null,
                                )
                            }
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
                        //shadowElevation = if (scroll.canScrollBackward) 16.dp else 0.dp,
                    ) {
                        TopAppBar(
                            title = { Text(text = stringResource(id = R.string.extras)) },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
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
                    Column(
                        modifier = Modifier
                            .padding(8.dp)
                            .verticalScroll(rememberScrollState())
                            .navigationBarsPadding(),
                    ) {
                        Text(
                            modifier = Modifier.padding(8.dp),
                            text = stringResource(id = R.string.drinking_game_props),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        BadgedBox(badge = {
                            Badge(modifier = Modifier.offset(x = -(32).dp, y = 8.dp)) {
                                Text(text = stringResource(id = R.string.beta_badge))
                            }
                        }) {
                            MenuCard(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth(),
                                onClick = {
                                    isIdeasDialogVisible = true
                                },
                                imageVector = Icons.Default.Lightbulb,
                                title = stringResource(id = R.string.ideas),
                            )
                        }
                        BadgedBox(badge = {
                            Badge(modifier = Modifier.offset(x = -(32).dp, y = 8.dp)) {
                                Text(text = stringResource(id = R.string.beta_badge))
                            }
                        }) {
                            MenuCard(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth(),
                                onClick = {
                                    startActivity(
                                        Intent(
                                            this@ExtrasActivity,
                                            PlayingCardsActivity::class.java
                                        )
                                    )
                                },
                                painter = painterResource(id = R.drawable.ic_cards_playing),
                                title = stringResource(id = R.string.playing_cards),
                            )
                        }
                        MenuCard(
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth(),
                            onClick = {
                                startActivity(
                                    Intent(
                                        this@ExtrasActivity,
                                        DiceActivity::class.java
                                    )
                                )
                            },
                            painter = painterResource(id = R.drawable.ic_dice_5),
                            title = stringResource(id = R.string.dice),
                        )
                        Text(
                            modifier = Modifier.padding(8.dp),
                            text = stringResource(id = R.string.time_travel),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        UzCard(
                            modifier = Modifier.padding(8.dp),
                        ) {
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                            ) {
                                mapOf(
                                    "2023" to "20230713112625",
                                    "2022" to "20220814121825",
                                    "2021" to "20210825145109",
                                    "2019" to "20190805195352",
                                    "2018" to "20180819182241",
                                    "2017" to "20170815150613",
                                    "2016" to "20160822113311",
                                    "2015" to "20150812065329",
                                    "2014" to "20140814051205",
                                    "2013" to "20130817085830",
                                    "2012" to "20120828002612",
                                    "2011" to "20110722065923",
                                    "2010" to "20100824180853",
                                    //TODO insert missing
                                    "2004" to "20040929123114",
                                ).map {
                                    Button(
                                        modifier = Modifier
                                            .padding(8.dp),
                                        onClick = {
                                            CustomTabsManager.open(
                                                this@ExtrasActivity,
                                                "https://web.archive.org/web/${it.value}/http://www.utcazene.hu/",
                                            )
                                        },
                                    ) {
                                        Text(text = it.key)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
