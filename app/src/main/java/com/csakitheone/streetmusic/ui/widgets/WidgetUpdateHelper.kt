package com.csakitheone.streetmusic.ui.widgets

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.ActionCallback

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

class UpdateWidgetAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        WidgetUpdateHelper.updateAllWidgets(context)
    }
}
