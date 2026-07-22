package com.csakitheone.streetmusic.ui.widgets

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.csakitheone.streetmusic.MainActivity
import com.csakitheone.streetmusic.UZApp
import com.csakitheone.streetmusic.data.model.Event
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalDateTime

class TodayPlanWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = (context.applicationContext as UZApp).repository
        val events = repository.events.first()
        
        provideContent {
            val now = LocalDateTime.now()
            val today = LocalDate.now().toString()

            val todayStarred = events.filter { 
                it.isStarred && it.startTime.startsWith(today) && LocalDateTime.parse(it.startTime).isAfter(now)
            }.sortedBy { it.startTime }

            GlanceTheme {
                WidgetContent(context, todayStarred)
            }
        }
    }

    @Composable
    private fun WidgetContent(context: Context, events: List<Event>) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.widgetBackground)
                .appWidgetBackground()
                .cornerRadius(16.dp)
                .padding(8.dp)
                .clickable(actionRunCallback<UpdateWidgetAction>()),
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(GlanceModifier.defaultWeight())
                Text(
                    text = "Today's Plan",
                    modifier = GlanceModifier.clickable(actionStartActivity(Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })),
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        color = GlanceTheme.colors.onSurface
                    )
                )
                Spacer(GlanceModifier.defaultWeight())
                Box(
                    modifier = GlanceModifier
                        .background(GlanceTheme.colors.secondaryContainer)
                        .cornerRadius(8.dp)
                        .padding(horizontal = 16.dp, vertical = 2.dp)
                        .clickable(actionStartActivity(Intent(context, MainActivity::class.java).apply {
                            action = "com.csakitheone.streetmusic.ACTION_CALENDAR"
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        })),
                    contentAlignment = Alignment.Center
                ) {
                    Text("+", style = TextStyle(color = GlanceTheme.colors.onSecondaryContainer, fontWeight = FontWeight.Bold))
                }
            }
            Spacer(GlanceModifier.height(8.dp))
            if (events.isEmpty()) {
                Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "No upcoming starred events", style = TextStyle(color = GlanceTheme.colors.onSurface))
                }
            } else {
                LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                    items(events) { event ->
                        EventItem(context, event)
                    }
                }
            }
        }
    }

    @Composable
    private fun EventItem(context: Context, event: Event) {
        val time = event.startTime.substring(11, 16)
        val intent = Intent(context, MainActivity::class.java).apply {
            action = "com.csakitheone.streetmusic.ACTION_EVENT_DETAIL"
            putExtra("eventId", event.id)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        Column(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .background(GlanceTheme.colors.surface)
                .cornerRadius(8.dp)
                .padding(8.dp)
                .clickable(actionStartActivity(intent))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = time,
                    style = TextStyle(fontWeight = FontWeight.Bold, color = GlanceTheme.colors.primary)
                )
                Spacer(GlanceModifier.width(8.dp))
                Text(
                    text = event.artistName,
                    style = TextStyle(color = GlanceTheme.colors.onSurface),
                    maxLines = 1
                )
            }
            Text(
                text = event.place,
                style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant),
                maxLines = 1
            )
        }
    }
}
