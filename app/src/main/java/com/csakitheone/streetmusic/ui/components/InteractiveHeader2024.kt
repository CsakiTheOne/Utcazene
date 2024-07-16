package com.csakitheone.streetmusic.ui.components

import android.media.MediaPlayer
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.csakitheone.streetmusic.R
import com.csakitheone.streetmusic.util.CustomTabsManager

@Composable
fun InteractiveHeader2024(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    var mediaPlayer: MediaPlayer? by remember { mutableStateOf(null) }

    fun playSound(resId: Int) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(context, resId)
        mediaPlayer?.start()
    }

    Box(modifier = modifier) {
        Image(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(192f / 66f),
            painter = painterResource(id = R.drawable.header),
            contentDescription = null,
        )
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f / 2.5f)
                    .clickable {
                        playSound(R.raw.sound_instrument_tambourine)
                    },
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f / 2.5f)
                    .clickable {
                        playSound(R.raw.sound_instrument_guitar)
                    },
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f / 2.5f)
                    .clickable {
                        playSound(R.raw.sound_instrument_drum)
                    },
            )
            Box(
                modifier = Modifier
                    .weight(2f)
                    .aspectRatio(1f / 1.2f)
                    .clickable {
                        CustomTabsManager.open(
                            context,
                            "https://musiclab.chromeexperiments.com/Spectrogram/",
                        )
                    },
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f / 2.5f)
                    .clickable {
                        playSound(R.raw.sound_instrument_guitar)
                    },
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f / 2.5f)
                    .clickable {
                        playSound(R.raw.sound_instrument_cello)
                    },
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f / 2.5f)
                    .clickable {
                        playSound(R.raw.sound_meme_snoring)
                    },
            )
        }
    }
}