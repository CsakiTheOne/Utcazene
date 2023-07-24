package com.csakitheone.streetmusic

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Feed
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.csakitheone.streetmusic.ui.components.MenuCard
import com.csakitheone.streetmusic.ui.theme.UtcazeneTheme
import com.csakitheone.streetmusic.util.CustomTabsManager

class HubActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HubScreen()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Preview
    @Composable
    fun HubScreen() {
        UtcazeneTheme {
            var isWebsitesMenuVisible by remember { mutableStateOf(false) }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    TopAppBar(
                        title = { Text(text = stringResource(id = R.string.app_name)) },
                        colors = TopAppBarDefaults.smallTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            titleContentColor = MaterialTheme.colorScheme.onBackground,
                            navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                            actionIconContentColor = MaterialTheme.colorScheme.onBackground,
                        ),
                    )
                    Text(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        text = "Thank you for being with us!",
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.displayMedium,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        text = "Keep the app and stay tuned for content all year round. " +
                                "You'll be able to check out all musicians who previously " +
                                "performed at Utcazene.",
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                    )
                    MenuCard(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        onClick = {
                            startActivity(
                                Intent(
                                    this@HubActivity,
                                    ExtrasActivity::class.java
                                )
                            )
                        },
                        imageVector = Icons.Default.VideogameAsset,
                        title = stringResource(id = R.string.extras),
                    )
                    MenuCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        onClick = { isWebsitesMenuVisible = true },
                        imageVector = Icons.Default.Language,
                        title = stringResource(id = R.string.websites),
                    ) {
                        DropdownMenu(
                            expanded = isWebsitesMenuVisible,
                            onDismissRequest = { isWebsitesMenuVisible = false }
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
                                        this@HubActivity,
                                        "https://utcazene.hu/"
                                    )
                                    isWebsitesMenuVisible = false
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
                                        this@HubActivity,
                                        "https://facebook.com/utcazene"
                                    )
                                    isWebsitesMenuVisible = false
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
                                        this@HubActivity,
                                        "https://instagram.com/utcazene"
                                    )
                                    isWebsitesMenuVisible = false
                                },
                            )
                        }
                    }
                    MenuCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        onClick = {
                            startActivity(
                                Intent(
                                    this@HubActivity,
                                    SupportActivity::class.java
                                )
                            )
                        },
                        imageVector = Icons.Default.Code,
                        title = stringResource(id = R.string.made_by_csaki),
                    )
                    TextButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        onClick = {
                            startActivity(
                                Intent(this@HubActivity, MainActivity::class.java)
                                    .putExtra(MainActivity.EXTRA_IGNORE_EVENT_ENDED, true)
                            )
                        },
                    ) {
                        Text(text = "Go back to the home screen")
                    }
                }
            }
        }
    }
}