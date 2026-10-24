package com.example.pixelcountdownwidget.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TimerDao {
    @Query("SELECT * FROM timers ORDER BY createdAtEpochMillis DESC")
    fun getAll(): Flow<List<TimerEntity>>

    @Query("SELECT * FROM timers WHERE selectedForWidget = 1 ORDER BY createdAtEpochMillis DESC")
    fun getSelectedForWidget(): Flow<List<TimerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(timer: TimerEntity): Long

    @Update
    suspend fun update(timer: TimerEntity)

    @Delete
    suspend fun delete(timer: TimerEntity)
}
