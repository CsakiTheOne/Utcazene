package com.csakitheone.streetmusic

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.csakitheone.streetmusic.ui.components.MenuCard
import com.csakitheone.streetmusic.ui.theme.UtcazeneTheme
import com.csakitheone.streetmusic.util.CustomTabsManager

class ExtrasActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ExtrasScreen()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Preview
    @Composable
    fun ExtrasScreen() {
        UtcazeneTheme {
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
                            .verticalScroll(rememberScrollState()),
                    ) {
                        Card(
                            modifier = Modifier.padding(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            ),
                        ) {
                            Text(
                                modifier = Modifier.padding(16.dp),
                                text = "This page is work in progress and not yet translated! /\n" +
                                        "Ez az oldal készülőben van és még nincs lefordítva!"
                            )
                        }
                        Text(
                            modifier = Modifier.padding(8.dp),
                            text = "Drinking game props / Ivós játék kellékek",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        MenuCard(
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth(),
                            onClick = {
                                Toast.makeText(
                                    this@ExtrasActivity,
                                    "wip",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            imageVector = Icons.Default.Lightbulb,
                            title = "Ötletek (wip)",
                        )
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
                            title = "Francia kártya",
                        )
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
                            title = "Dice / Dobókocka",
                        )
                        Text(
                            modifier = Modifier.padding(8.dp),
                            text = "Games / Játékok",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        MenuCard(
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth(),
                            onClick = {
                                Toast.makeText(
                                    this@ExtrasActivity,
                                    "I need ideas! / Ötleteket kérek!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            imageVector = Icons.Default.Check,
                            title = "Utcazene bingó (wip)",
                        )
                        Text(
                            modifier = Modifier.padding(8.dp),
                            text = "Time travel / Időutazás",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        mapOf(
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
                            "2004" to "20040929123114",
                        ).map {
                            Button(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth(),
                                onClick = {
                                    CustomTabsManager.open(
                                        this@ExtrasActivity,
                                        "https://web.archive.org/web/${it.value}/http://www.utcazene.hu/",
                                    )
                                },
                            ) {
                                Text(text = "Utcazene ${it.key}")
                            }
                        }
                    }
                }
            }
        }
    }
}
