package com.csakitheone.streetmusic.ui.components

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

@Composable
fun YouTubeEmbed(
    modifier: Modifier = Modifier,
    videoId: String,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    Card(modifier = modifier) {
        key(videoId) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
                factory = { context ->
                    YouTubePlayerView(context).apply {
                        lifecycleOwner.lifecycle.addObserver(this)
                        addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                            override fun onReady(youTubePlayer: YouTubePlayer) {
                                youTubePlayer.cueVideo(videoId, 0f)
                            }
                        })
                    }
                },
                onRelease = { view ->
                    lifecycleOwner.lifecycle.removeObserver(view)
                    view.release()
                }
            )
        }
    }
}