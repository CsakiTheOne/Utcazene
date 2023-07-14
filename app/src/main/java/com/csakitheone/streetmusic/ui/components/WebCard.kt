package com.csakitheone.streetmusic.ui.components

import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import com.csakitheone.streetmusic.R
import com.csakitheone.streetmusic.ui.components.util.PreferenceHolder
import com.csakitheone.streetmusic.util.CustomTabsManager

@Preview
@Composable
fun WebCard(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var isMenuVisible by remember { mutableStateOf(false) }

    OutlinedCard(
        modifier = modifier,
    ) {
        Box(
            contentAlignment = Alignment.TopEnd,
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = {
                    WebView(it).apply {
                        webViewClient = WebViewClient()
                        loadUrl("https://utcazene.hu/")
                    }
                },
            )
            SmallFloatingActionButton(
                modifier = Modifier
                    .padding(8.dp)
                    .zIndex(1f)
                    .alpha(.9f),
                onClick = {
                    isMenuVisible = true
                },
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = null,
                )
                DropdownMenu(
                    expanded = isMenuVisible,
                    onDismissRequest = { isMenuVisible = false },
                ) {
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = null
                            )
                        },
                        text = { Text(text = stringResource(id = R.string.open_in_tab)) },
                        onClick = {
                            CustomTabsManager.open(context, "https://utcazene.hu/")
                            isMenuVisible = false
                        },
                    )
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_facebook),
                                contentDescription = null
                            )
                        },
                        text = { Text(text = stringResource(id = R.string.open_facebook)) },
                        onClick = {
                            CustomTabsManager.open(context, "https://www.facebook.com/utcazene")
                            isMenuVisible = false
                        },
                    )
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_instagram),
                                contentDescription = null
                            )
                        },
                        text = { Text(text = stringResource(id = R.string.open_instagram)) },
                        onClick = {
                            CustomTabsManager.open(context, "https://www.instagram.com/utcazene/")
                            isMenuVisible = false
                        },
                    )
                }
            }
        }
    }
}