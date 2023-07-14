package com.csakitheone.streetmusic

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lightbulb
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.csakitheone.streetmusic.ui.components.MenuCard
import com.csakitheone.streetmusic.ui.theme.UtcazeneTheme

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
                            title = { Text(text = "Extrák") },
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
                        modifier = Modifier.padding(8.dp),
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
                                        "Ez az oldal még készülőben van és nincs lefordítva!"
                            )
                        }
                        Text(
                            modifier = Modifier.padding(8.dp),
                            text = "Ivós játék kellékek",
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
                                Toast.makeText(
                                    this@ExtrasActivity,
                                    "wip",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            painter = painterResource(id = R.drawable.ic_cards_playing),
                            title = "Francia kártya pakli (wip)",
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
                            painter = painterResource(id = R.drawable.ic_dice_5),
                            title = "Dobókocka (wip)",
                        )
                        Text(
                            modifier = Modifier.padding(8.dp),
                            text = "Játékok",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        MenuCard(
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth(),
                            onClick = {
                                Toast.makeText(
                                    this@ExtrasActivity,
                                    "Ötleteket kérek!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            imageVector = Icons.Default.Check,
                            title = "Utcazene bingó (wip)",
                        )
                    }
                }
            }
        }
    }
}
