package com.example.pixelcountdownwidget.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.util.TypedValue
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
        updateWidgets(context, appWidgetManager, appWidgetIds)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: android.os.Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        updateWidgets(context, appWidgetManager, intArrayOf(appWidgetId))
    }

    private fun updateWidgets(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val app = context.applicationContext as? PixelCountdownApp ?: return
        val dao = app.database.timerDao()

        val selectedTimers = runBlocking {
            dao.getSelectedForWidget().first()
        }

        appWidgetIds.forEach { appWidgetId ->
            val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
            val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 72)
            val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 72)

            val textSize = when {
                minWidth < 90 || minHeight < 90 -> 9f
                minWidth < 140 || minHeight < 110 -> 11f
                minWidth < 200 || minHeight < 150 -> 13f
                else -> 15f
            }

            val views = RemoteViews(context.packageName, R.layout.widget_countdown)
            views.setTextViewTextSize(R.id.widget_label, TypedValue.COMPLEX_UNIT_SP, textSize)
            views.setTextViewTextSize(R.id.widget_time, TypedValue.COMPLEX_UNIT_SP, textSize)

            if (selectedTimers.isEmpty()) {
                views.setTextViewText(R.id.widget_label, "No timers")
                views.setTextViewText(R.id.widget_time, "--")
            } else {
                val visibleTimers = when {
                    minWidth < 110 || minHeight < 100 -> selectedTimers.take(1)
                    minWidth < 180 || minHeight < 130 -> selectedTimers.take(2)
                    else -> selectedTimers.take(3)
                }

                val summary = visibleTimers.joinToString("\n") { timer ->
                    "${timer.label}: ${formatTimerValue(timer)}"
                }

                views.setTextViewText(R.id.widget_label, "")
                views.setTextViewText(R.id.widget_time, summary)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

private fun formatTimerValue(timer: TimerEntity): String {
    val now = Instant.now()
    val totalSeconds = when (timer.mode) {
        "UP" -> {
            val diff = Duration.between(timer.startInstant(), now)
            if (diff.isNegative) 0L else diff.seconds
        }
        else -> {
            val target = timer.targetInstant() ?: now
            val diff = Duration.between(now, target)
            if (diff.isNegative) 0L else diff.seconds
        }
    }

    return formatSmartDuration(totalSeconds)
}

private fun formatSmartDuration(totalSeconds: Long): String {
    if (totalSeconds <= 0L) return "0s"

    val minutes = totalSeconds / 60
    val hours = minutes / 60
    val days = hours / 24
    val weeks = days / 7

    return when {
        weeks > 0 -> {
            val remainingDays = days % 7
            if (remainingDays > 0) "${weeks}w ${remainingDays}d" else "${weeks}w"
        }
        days > 0 -> {
            val remainingHours = hours % 24
            if (remainingHours > 0) "${days}d ${remainingHours}h" else "${days}d"
        }
        hours > 0 -> {
            val remainingMinutes = minutes % 60
            if (remainingMinutes > 0) "${hours}h ${remainingMinutes}m" else "${hours}h"
        }
        minutes > 0 -> {
            val remainingSeconds = totalSeconds % 60
            if (remainingSeconds > 0) "${minutes}m ${remainingSeconds}s" else "${minutes}m"
        }
        else -> "${totalSeconds}s"
    }
}
