package com.csakitheone.streetmusic.ui.screens

import android.content.Intent
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.fromColorLong
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import com.csakitheone.streetmusic.R
import com.csakitheone.streetmusic.navigation.LocalNavBackStack
import com.csakitheone.streetmusic.navigation.LocalSharedTransitionContext

@Composable
fun MapScreen() {
    val context = LocalContext.current
    val sharedTransitionContext = LocalSharedTransitionContext.current
    val backStack = LocalNavBackStack.current

    val mapUrl =
        remember { "https://www.google.com/maps/d/embed?mid=12plW9qjTupsu26_lLGD-lnE4jqUczO4U&ehbc=2E312F" }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (sharedTransitionContext != null) with(sharedTransitionContext.sharedTransitionScope) {
                    Modifier.sharedBounds(
                        sharedTransitionContext.sharedTransitionScope.rememberSharedContentState("MapScreen"),
                        sharedTransitionContext.animatedVisibilityScope
                    )
                }
                else Modifier
            ),
        // #2d2f2f
        color = Color(0xFF2d2f2f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    WebView(context).apply {
                        settings.domStorageEnabled = true
                        settings.javaScriptEnabled = true
                        webViewClient = WebViewClient()
                        loadUrl(mapUrl)
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                        )
                    }
                },
            )

            HorizontalFloatingToolbar(
                expanded = true,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
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
                TextButton(
                    onClick = {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                "https://www.google.com/android/find/people".toUri()
                            )
                        )
                    },
                ) {
                    Icon(
                        modifier = Modifier
                            .padding(end = ButtonDefaults.IconSpacing)
                            .size(24.dp)
                            .clip(CircleShape),
                        painter = painterResource(R.drawable.find_hub_icon),
                        contentDescription = null,
                        tint = Color.Unspecified,
                    )
                    Text("Find friends")
                }
            }
        }
    }
}
