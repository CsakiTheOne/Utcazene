package com.csakitheone.streetmusic.data.nearby

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.csakitheone.streetmusic.MainActivity
import com.csakitheone.streetmusic.R
import com.csakitheone.streetmusic.UZApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class NearbyBackgroundService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val notificationId = 1
    private val channelId = "nearby_background_service"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val repository = (applicationContext as UZApp).repository

        if (intent?.action == ACTION_STOP) {
            repository.setIsNearbyFriendsActive(false)
            stopSelf()
            return START_NOT_STICKY
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            channelId,
            "Nearby Friends Background Service",
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)

        startForeground(notificationId, buildNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE)

        repository.nearbyManager.setNearbyFriendsActive(true)

        serviceScope.launch {
            repository.nearbyManager.friends.connectedFriends.collect { connected ->
                notificationManager.notify(notificationId, buildNotification(connected.size))
            }
        }

        return START_STICKY
    }

    private fun buildNotification(connectedCount: Int = 0): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val settingsIntent = Intent(this, MainActivity::class.java).apply {
            action = ACTION_SETTINGS
        }
        val settingsPendingIntent = PendingIntent.getActivity(
            this, 1, settingsIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, NearbyBackgroundService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 2, stopIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = if (connectedCount == 0) {
            "Searching for nearby friends..."
        } else {
            "Connected to $connectedCount friend${if (connectedCount > 1) "s" else ""}"
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("StreetPass mode active")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_connect_without_contact)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .addAction(R.drawable.ic_settings, "Settings", settingsPendingIntent)
            .addAction(R.drawable.ic_close, "Stop", stopPendingIntent)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        val repository = (applicationContext as UZApp).repository
        repository.nearbyManager.setNearbyFriendsActive(false)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_STOP = "com.csakitheone.streetmusic.ACTION_STOP_NEARBY"
        const val ACTION_SETTINGS = "com.csakitheone.streetmusic.ACTION_SETTINGS"
    }
}
