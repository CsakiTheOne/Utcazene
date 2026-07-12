package com.csakitheone.streetmusic.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.csakitheone.streetmusic.MainActivity
import com.csakitheone.streetmusic.R
import com.csakitheone.streetmusic.data.model.Event

object NotificationHelper {
    private const val TAG = "NotificationHelper"
    private const val CHANNEL_ID = "event_reminders"
    private const val CHANNEL_NAME = "Event Reminders"
    private const val CHANNEL_DESCRIPTION = "Notifications for starred events starting soon"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showEventNotification(context: Context, event: Event) {
        Log.d(TAG, "Showing notification for event ${event.id}")
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            action = "com.csakitheone.streetmusic.ACTION_EVENT_DETAIL"
            putExtra("eventId", event.id)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            event.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_star)
            .setContentTitle("Event Starting: ${event.artistName}")
            .setContentText("At ${event.place} - ${event.startTime.substring(11, 16)}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setUsesChronometer(true)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(event.id, builder.build())
    }
}
