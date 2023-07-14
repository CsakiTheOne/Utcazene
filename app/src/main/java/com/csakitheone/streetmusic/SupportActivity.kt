package com.csakitheone.streetmusic

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.csakitheone.streetmusic.ui.components.UzCard
import com.csakitheone.streetmusic.ui.theme.UtcazeneTheme
import com.csakitheone.streetmusic.util.CustomTabsManager

class SupportActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SupportScreen()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Preview
    @Composable
    fun SupportScreen() {
        var isAccountNumberDialogVisible by remember { mutableStateOf(false) }
        var isAccountNumberDialogAltText by remember { mutableStateOf(false) }

        UtcazeneTheme {
            if (isAccountNumberDialogVisible) {
                AlertDialog(
                    title = { Text(text = stringResource(id = R.string.account_number)) },
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                modifier = Modifier.weight(1f),
                                text = if (isAccountNumberDialogAltText) "11773487 - 01636022"
                                else stringResource(id = R.string.easter_egg_account_number)
                            )
                            if (isAccountNumberDialogAltText) {
                                IconButton(
                                    onClick = {
                                        val clipboardManager =
                                            getSystemService(ClipboardManager::class.java)
                                        clipboardManager.setPrimaryClip(
                                            ClipData.newPlainText(
                                                "account number",
                                                "1177348701636022"
                                            )
                                        )
                                    },
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = null,
                                    )
                                }
                            }
                        }
                    },
                    onDismissRequest = {
                        if (isAccountNumberDialogAltText) {
                            isAccountNumberDialogVisible = false
                            isAccountNumberDialogAltText = false
                        } else {
                            isAccountNumberDialogAltText = true
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (isAccountNumberDialogAltText) {
                                    isAccountNumberDialogVisible = false
                                    isAccountNumberDialogAltText = false
                                } else {
                                    isAccountNumberDialogAltText = true
                                }
                            }
                        ) {
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
                        //shadowElevation = if (scroll.canScrollBackward) 16.dp else 0.dp,
                    ) {
                        TopAppBar(
                            title = { Text(text = stringResource(id = R.string.support_the_dev)) },
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
                        UzCard(
                            modifier = Modifier.padding(8.dp),
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                            ) {
                                Text(
                                    modifier = Modifier.padding(8.dp),
                                    text = stringResource(id = R.string.other_apps_by_csaki),
                                )
                                TextButton(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .fillMaxWidth(),
                                    onClick = {
                                        startActivity(
                                            Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse("https://play.google.com/store/apps/details?id=com.csakitheone.froccs")
                                            )
                                        )
                                    },
                                ) {
                                    Text(text = "Fröccs")
                                }
                                TextButton(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .fillMaxWidth(),
                                    onClick = {
                                        startActivity(
                                            Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse("https://play.google.com/store/apps/details?id=com.csakitheone.distanthug")
                                            )
                                        )
                                    },
                                ) {
                                    Text(text = "Distant Hug")
                                }
                                TextButton(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .fillMaxWidth(),
                                    onClick = {
                                        startActivity(
                                            Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse("https://play.google.com/store/apps/dev?id=5554124272482096869")
                                            )
                                        )
                                    },
                                ) {
                                    Text(text = stringResource(id = R.string.more_apps))
                                }
                            }
                        }
                        UzCard(
                            modifier = Modifier.padding(8.dp),
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                            ) {
                                Text(
                                    modifier = Modifier.padding(8.dp),
                                    text = stringResource(id = R.string.support_methods),
                                )
                                TextButton(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .fillMaxWidth(),
                                    onClick = {
                                        isAccountNumberDialogVisible = true
                                    },
                                ) {
                                    Text(text = stringResource(id = R.string.show_account_number))
                                }
                                TextButton(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .fillMaxWidth(),
                                    onClick = {
                                        CustomTabsManager.open(
                                            this@SupportActivity,
                                            "https://imgv2-2-f.scribdassets.com/img/document/8367982/original/0225210b85/1684555739?v=1"
                                        )
                                    },
                                ) {
                                    Text(text = stringResource(id = R.string.depressed_altgirl))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}