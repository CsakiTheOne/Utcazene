package com.csakitheone.streetmusic

import android.os.Bundle
import android.os.PersistableBundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.csakitheone.streetmusic.ui.theme.UtcazeneTheme

class DiceActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DiceScreen()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Preview
    @Composable
    fun DiceScreen() {
        UtcazeneTheme(
            isTintingNavbar = false,
        ) {
            val colorScheme = MaterialTheme.colorScheme

            var dieSize by remember { mutableStateOf(6) }
            var currentNumber by remember(dieSize) { mutableStateOf(dieSize) }
            var dieColor by remember { mutableStateOf(colorScheme.primary) }

            fun roll() {
                val vibrator = getSystemService(Vibrator::class.java)
                val rollLength = (24..28).random()
                Thread {
                    repeat(rollLength) {
                        dieColor = colorScheme.background
                        Thread.sleep(20L)
                        dieColor = colorScheme.primary
                        currentNumber = (1..dieSize).random()
                        vibrator.vibrate(
                            VibrationEffect.createOneShot(
                                14L,
                                VibrationEffect.DEFAULT_AMPLITUDE
                            )
                        )
                        Thread.sleep(it * 8L)
                    }
                }.start()
            }

            LaunchedEffect(Unit) {
                window.navigationBarColor = colorScheme.surface.toArgb()
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
                    ) {
                        TopAppBar(
                            title = { Text(text = stringResource(id = R.string.dice)) },
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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Surface(
                            modifier = Modifier
                                .width(128.dp)
                                .aspectRatio(1f)
                                .clickable { roll() },
                            shape = RoundedCornerShape(16.dp),
                            color = dieColor,
                            shadowElevation = 16.dp,
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "$currentNumber",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.displayLarge,
                                )
                            }
                        }
                    }
                    NavigationBar(
                        tonalElevation = 0.dp,
                    ) {
                        listOf(4, 6, 8, 10, 12, 20)
                            .map {
                                NavigationBarItem(
                                    selected = dieSize == it,
                                    onClick = { dieSize = it },
                                    icon = { Text(text = "D$it") },
                                )
                            }
                    }
                }
            }
        }
    }
}