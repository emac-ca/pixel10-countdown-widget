package com.example.pixelcountdownwidget.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent

object TimerWidgetUpdater {
    fun updateWidgetNow() {
        // Placeholder for now; widget refresh is triggered during app updates.
    }

    fun updateWidget(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val component = ComponentName(context, CountdownWidgetProvider::class.java)
        val appWidgetIds = manager.getAppWidgetIds(component)
        val intent = Intent(context, CountdownWidgetProvider::class.java).apply {
            action = "android.appwidget.action.APPWIDGET_UPDATE"
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
        }
        context.sendBroadcast(intent)
    }
}
