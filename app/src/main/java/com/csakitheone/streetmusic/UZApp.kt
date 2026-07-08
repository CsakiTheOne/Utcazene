package com.csakitheone.streetmusic

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.csakitheone.streetmusic.data.api.UtcazeneApi

class UZApp : Application(), SingletonImageLoader.Factory {
    private val unsafeClient by lazy { UtcazeneApi().unsafeOkHttpClient() }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(
                    OkHttpNetworkFetcherFactory(
                        callFactory = { unsafeClient }
                    )
                )
            }
            .build()
    }
}
