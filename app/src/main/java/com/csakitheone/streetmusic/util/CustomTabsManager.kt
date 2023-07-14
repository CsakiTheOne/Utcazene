package com.csakitheone.streetmusic.util

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.ui.graphics.toArgb
import com.csakitheone.streetmusic.ui.theme.Blue

class CustomTabsManager {
    companion object {

        private val colorSchemeParams = CustomTabColorSchemeParams.Builder()
            .setToolbarColor(Blue.toArgb())
            .build()

        fun open(context: Context, url: String) {
            CustomTabsIntent.Builder()
                .setDefaultColorSchemeParams(colorSchemeParams)
                .build()
                .launchUrl(context, Uri.parse(url))
        }

    }
}