package com.csakitheone.streetmusic

import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.net.ConnectivityManager
import androidx.activity.ComponentActivity
import androidx.room.Room
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.csakitheone.streetmusic.data.DataRepository
import com.csakitheone.streetmusic.data.api.UtcazeneApi
import com.csakitheone.streetmusic.data.local.AppDatabase
import com.csakitheone.streetmusic.data.nearby.NearbyManager
import kotlinx.coroutines.MainScope

class UZApp : Application(), SingletonImageLoader.Factory {
    private val unsafeClient by lazy { UtcazeneApi().unsafeOkHttpClient() }

    val repository: DataRepository by lazy {
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "utcazene.db"
        ).fallbackToDestructiveMigration(true).build()
        val connectivityManager =
            getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val prefs = getSharedPreferences("favorites", Context.MODE_PRIVATE)
        val nearbyManager = NearbyManager(applicationContext, MainScope())
        DataRepository(applicationContext, UtcazeneApi(), db, connectivityManager, prefs, nearbyManager)
    }

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

internal fun Context.findActivity(): ComponentActivity {
    var context = this
    while (context is ContextWrapper) {
        if (context is ComponentActivity) return context
        context = context.baseContext
    }
    throw IllegalStateException("Picture in picture should be called in the context of an Activity")
}
