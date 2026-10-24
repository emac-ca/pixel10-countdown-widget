package com.example.pixelcountdownwidget.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.ZoneId

@Entity(tableName = "timers")
data class TimerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val label: String,
    val mode: String,
    val startInstantEpochMillis: Long,
    val targetInstantEpochMillis: Long?,
    val zoneId: String,
    val selectedForWidget: Boolean = false,
    val createdAtEpochMillis: Long = System.currentTimeMillis()
) {
    fun zone(): ZoneId = ZoneId.of(zoneId)
    fun startInstant(): Instant = Instant.ofEpochMilli(startInstantEpochMillis)
    fun targetInstant(): Instant? = targetInstantEpochMillis?.let { Instant.ofEpochMilli(it) }
}
