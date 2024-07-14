package com.csakitheone.streetmusic.util

import android.content.Context
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class Helper {
    companion object {

        fun String.toLocalTime(): LocalTime {
            if (!this.contains(":")) {
                return LocalTime.of(0, 0)
            }
            val h = this.substringBefore(":").toIntOrNull() ?: 23
            val m = this.substringAfter(":").toIntOrNull() ?: 59
            return LocalTime.of(h, m)
        }

        fun LocalTime.format(): String {
            return this.format(DateTimeFormatter.ofPattern("HH:mm"))
        }

        fun isUnmeteredNetworkAvailable(context: Context?): Boolean {
            if (context == null) return false
            val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) == true
            } else {
                val activeNetworkInfo = connectivityManager.activeNetworkInfo
                if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                    return true
                }
            }
            return false
        }

        fun imageUrlToBitmapUri(context: Context, imageUrl: String?, callback: (Uri?) -> Unit) {
            if (imageUrl == null) {
                callback(null)
                return
            }
            Thread {
                try {
                    val url = URL(imageUrl)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.doInput = true
                    connection.connect()
                    val input: InputStream = connection.inputStream
                    val imgBitmap = BitmapFactory.decodeStream(input)
                    val imgBitmapPath = MediaStore.Images.Media.insertImage(
                        context.contentResolver, imgBitmap,
                        "img", ""
                    )
                    callback(Uri.parse(imgBitmapPath))
                }
                catch (ex: Exception) {
                    callback(null)
                }
            }.start()
        }
    }
}