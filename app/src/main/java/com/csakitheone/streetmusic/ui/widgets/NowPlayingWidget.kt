package com.csakitheone.streetmusic.ui.widgets

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import java.time.LocalDateTime

import java.time.format.DateTimeFormatter

class NowPlayingWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = (context.applicationContext as UZApp).repository
        val events = repository.events.first()

        provideContent {
            val now = LocalDateTime.now()
            val lastUpdated = now.format(DateTimeFormatter.ofPattern("HH:mm"))
            val nowPlaying = events.filter {
                LocalDateTime.parse(it.startTime).isBefore(now) &&
                        LocalDateTime.parse(it.endTime).isAfter(now)
            }

            GlanceTheme {
                WidgetContent(context, nowPlaying, lastUpdated)
            }
        }
    }

    @Composable
    private fun WidgetContent(context: Context, events: List<Event>, lastUpdated: String) {
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
            Text(
                text = "Now Playing",
                modifier = GlanceModifier.clickable(actionStartActivity(Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })),
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.onSurface
                )
            )
            Text(
                text = "Updated: $lastUpdated",
                style = TextStyle(
                    fontSize = 10.sp,
                    color = GlanceTheme.colors.onSurfaceVariant
                )
            )
            Spacer(GlanceModifier.height(8.dp))
            if (events.isEmpty()) {
                Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Nothing playing right now", style = TextStyle(color = GlanceTheme.colors.onSurface))
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
        val intent = Intent(context, MainActivity::class.java).apply {
            action = "com.csakitheone.streetmusic.ACTION_EVENT_DETAIL"
            putExtra("eventId", event.id)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK // Required when starting from outside an Activity
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
            Text(
                text = event.artistName,
                style = TextStyle(fontWeight = FontWeight.Bold, color = GlanceTheme.colors.onSurface),
                maxLines = 1
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = event.place,
                    style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant),
                    maxLines = 1,
                    modifier = GlanceModifier.defaultWeight()
                )
                Spacer(GlanceModifier.width(8.dp))
                Text(
                    text = "until ${event.endTime.substring(11, 16)}",
                    style = TextStyle(color = GlanceTheme.colors.primary),
                )
            }
        }
    }
}
