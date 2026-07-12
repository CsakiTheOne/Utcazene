package com.csakitheone.streetmusic.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.csakitheone.streetmusic.data.DataRepository
import com.csakitheone.streetmusic.data.model.Event
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object AlarmScheduler {
    private const val TAG = "AlarmScheduler"

    fun scheduleEventAlarm(context: Context, event: Event) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Log.w(TAG, "Cannot schedule exact alarms - permission not granted")
            return
        }

        val intent = Intent(context, EventNotificationReceiver::class.java).apply {
            action = "com.csakitheone.streetmusic.ACTION_NOTIFY_EVENT"
            putExtra("eventId", event.id)
            putExtra("artistSlug", event.artistSlug)
            putExtra("startTime", event.startTime)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            event.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = LocalDateTime.parse(event.startTime, DateTimeFormatter.ISO_DATE_TIME)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        if (triggerTime <= System.currentTimeMillis()) {
            Log.d(TAG, "Event ${event.id} already started, skipping alarm")
            return
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
        Log.d(TAG, "Scheduled alarm for event ${event.id} at ${event.startTime}")
    }

    fun cancelEventAlarm(context: Context, eventId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, EventNotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            eventId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "Canceled alarm for event $eventId")
        }
    }

    suspend fun rescheduleAll(context: Context, repository: DataRepository) {
        val favoriteEvents = repository.events.first().filter { it.isStarred }
        Log.d(TAG, "Rescheduling ${favoriteEvents.size} favorite events")
        favoriteEvents.forEach {
            scheduleEventAlarm(context, it)
        }
    }
}
