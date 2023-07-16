package com.csakitheone.streetmusic.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.csakitheone.streetmusic.R

class MotdProvider {
    companion object {

        /**
         * A list of messages that can appear on the main screen of the app.
         */
        

        fun getRandomMotd(context: Context, currentMotd: String? = null): String {
            val motdList = context.applicationContext.resources.getStringArray(R.array.motds)
            return motdList.filter { it != currentMotd }.random()
        }

    }
}