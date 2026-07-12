package com.csakitheone.streetmusic.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.csakitheone.streetmusic.UZApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class EventNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val repository = (context.applicationContext as UZApp).repository
        val scope = CoroutineScope(Dispatchers.IO)

        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                Log.d("EventNotificationReceiver", "Boot completed, rescheduling alarms")
                scope.launch {
                    AlarmScheduler.rescheduleAll(context, repository)
                }
            }
            "com.csakitheone.streetmusic.ACTION_NOTIFY_EVENT" -> {
                val eventId = intent.getIntExtra("eventId", -1)
                Log.d("EventNotificationReceiver", "Alarm triggered for event $eventId")
                if (eventId != -1) {
                    scope.launch {
                        val event = repository.getEvent(eventId).first()
                        if (event != null && event.isStarred) {
                            NotificationHelper.showEventNotification(context, event)
                        } else {
                            Log.d("EventNotificationReceiver", "Event $eventId not found or not starred, skipping notification")
                        }
                    }
                }
            }
        }
    }
}
