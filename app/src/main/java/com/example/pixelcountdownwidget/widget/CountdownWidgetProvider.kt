package com.example.pixelcountdownwidget.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.example.pixelcountdownwidget.PixelCountdownApp
import com.example.pixelcountdownwidget.R
import com.example.pixelcountdownwidget.data.TimerEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.Duration
import java.time.Instant

class CountdownWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        val app = context.applicationContext as? PixelCountdownApp ?: return
        val dao = app.database.timerDao()

        val timerList = runBlocking {
            dao.getAll().first()
        }

        val timer = timerList.firstOrNull()
        val views = RemoteViews(context.packageName, R.layout.widget_countdown)

        if (timer == null) {
            views.setTextViewText(R.id.widget_label, "No timers")
            views.setTextViewText(R.id.widget_time, "--")
        } else {
            val state = when (timer.mode) {
                "UP" -> {
                    val diff = Duration.between(timer.startInstant(), Instant.now())
                    val totalSeconds = if (diff.isNegative) 0 else diff.seconds
                    formatSeconds(totalSeconds)
                }
                else -> {
                    val target = timer.targetInstant() ?: Instant.now()
                    val diff = Duration.between(Instant.now(), target)
                    val totalSeconds = if (diff.isNegative) 0 else diff.seconds
                    formatSeconds(totalSeconds)
                }
            }

            views.setTextViewText(R.id.widget_label, timer.label)
            views.setTextViewText(R.id.widget_time, state)
        }

        appWidgetManager.updateAppWidget(appWidgetIds, views)
    }
}

private fun formatSeconds(totalSeconds: Long): String {
    val days = totalSeconds / 86400
    val hours = (totalSeconds % 86400) / 3600
    val mins = (totalSeconds % 3600) / 60
    val secs = totalSeconds % 60

    return when {
        days > 0 -> "${days}d ${hours}h"
        hours > 0 -> "${hours}h ${mins}m"
        mins > 0 -> "${mins}m ${secs}s"
        else -> "${secs}s"
    }
}
