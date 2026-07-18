package com.csakitheone.streetmusic.ui.components

import android.app.PictureInPictureParams
import android.os.Build
import android.util.Log
import android.util.Rational
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toAndroidRectF
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.PictureInPictureModeChangedInfo
import androidx.core.graphics.toRect
import androidx.core.util.Consumer
import com.csakitheone.streetmusic.findActivity
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

@Composable
fun YouTubeEmbed(
    modifier: Modifier = Modifier,
    videoId: String,
    onPipChanged: (Boolean) -> Unit = {},
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val pipModifier = Modifier.onGloballyPositioned { layoutCoordinates ->
        val builder = PictureInPictureParams.Builder()
        val sourceRect = layoutCoordinates.boundsInWindow().toAndroidRectF().toRect()
        builder.setSourceRectHint(sourceRect)
        builder.setAspectRatio(
            Rational(16, 9)
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setAutoEnterEnabled(true)
        }
        context.findActivity().setPictureInPictureParams(builder.build())
    }

    DisposableEffect(context) {
        val activity = context.findActivity()
        val onUserLeaveBehavior = Runnable {
            activity.enterPictureInPictureMode(PictureInPictureParams.Builder().build())
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            activity.addOnUserLeaveHintListener(onUserLeaveBehavior)
        }

        onDispose {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                activity.removeOnUserLeaveHintListener(onUserLeaveBehavior)
            } else {
                activity.setPictureInPictureParams(
                    PictureInPictureParams.Builder()
                        .setAutoEnterEnabled(false)
                        .build()
                )
            }
        }
    }

    DisposableEffect(context) {
        val listener = Consumer<PictureInPictureModeChangedInfo> { info ->
            onPipChanged(info.isInPictureInPictureMode)
        }
        context.findActivity().addOnPictureInPictureModeChangedListener(listener)
        onDispose {
            context.findActivity().removeOnPictureInPictureModeChangedListener(listener)
        }
    }

    Card(
        modifier = modifier.then(pipModifier)
    ) {
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