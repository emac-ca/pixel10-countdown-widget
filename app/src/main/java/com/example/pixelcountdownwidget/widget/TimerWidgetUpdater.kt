package com.example.pixelcountdownwidget.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent

object TimerWidgetUpdater {
    fun updateWidgetNow() {
        // no-op placeholder until a Context is available
    }

    fun updateWidgetNow(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val component = ComponentName(context, CountdownWidgetProvider::class.java)
        val ids = manager.getAppWidgetIds(component)

        val intent = Intent(context, CountdownWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        }

        context.sendBroadcast(intent)
        scheduleRefresh(context)
    }

    fun scheduleRefresh(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, CountdownWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nextMinute = (System.currentTimeMillis() / 60_000L + 1L) * 60_000L
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            nextMinute,
            60_000L,
            pendingIntent
        )
    }
}
