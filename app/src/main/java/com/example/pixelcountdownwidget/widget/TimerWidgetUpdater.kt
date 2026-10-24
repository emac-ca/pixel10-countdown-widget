package com.example.pixelcountdownwidget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.example.pixelcountdownwidget.widget.CountdownWidgetProvider

object TimerWidgetUpdater {
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

        val nextMinute = (System.currentTimeMillis() / 60000L + 1L) * 60000L
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            nextMinute,
            AlarmManager.INTERVAL_MINUTE,
            pendingIntent
        )
    }
}
