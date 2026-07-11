package com.csakitheone.streetmusic.ui.widgets

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager

object WidgetUpdateHelper {
    suspend fun updateAllWidgets(context: Context) {
        val manager = GlanceAppWidgetManager(context)
        
        manager.getGlanceIds(TodayPlanWidget::class.java).forEach {
            TodayPlanWidget().update(context, it)
        }
        
        manager.getGlanceIds(NowPlayingWidget::class.java).forEach {
            NowPlayingWidget().update(context, it)
        }
    }
}
