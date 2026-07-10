package com.csakitheone.streetmusic.ui.screens

import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.fromColorLong
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.csakitheone.streetmusic.R
import com.csakitheone.streetmusic.navigation.LocalNavBackStack
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@Composable
fun MapScreen() {
    val activity = LocalActivity.current
    val scope = rememberCoroutineScope()
    val backStack = LocalNavBackStack.current

    val mapUrl =
        remember { "https://www.google.com/maps/d/embed?mid=12plW9qjTupsu26_lLGD-lnE4jqUczO4U&ehbc=2E312F" }

    var webView by remember { mutableStateOf<WebView?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF2d2f2f),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    WebView(context).apply {
                        settings.domStorageEnabled = true
                        settings.javaScriptEnabled = true
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                scope.launch {
                                    delay(1.seconds)
                                    activity?.runOnUiThread {
                                        view?.evaluateJavascript(
                                            "document.getElementsByClassName(\"i4ewOd-pzNkMb-haAclf\")[0].style.display = \"none\";",
                                            null
                                        )
                                    }
                                }
                            }
                        }
                        loadUrl(mapUrl)
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                        )
                        webView = this
                    }
                },
            )

            HorizontalFloatingToolbar(
                expanded = true,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 8.dp, bottom = 16.dp),
            ) {
                TextButton(
                    onClick = { backStack.removeLastOrNull() },
                ) {
                    Icon(
                        modifier = Modifier.padding(end = ButtonDefaults.IconSpacing),
                        painter = painterResource(R.drawable.ic_arrow_back),
                        contentDescription = null,
                    )
                    Text("Exit map")
                }
                OutlinedButton(
                    onClick = {
                        val js =
                            "document.getElementsByClassName(\"i4ewOd-pzNkMb-ornU0b-b0t70b-Bz112c\")[0].click();"
                        webView?.evaluateJavascript(js, null)
                    },
                ) {
                    Text("Legend")
                }
            }
        }
    }
}
