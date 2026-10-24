package com.example.pixelcountdownwidget.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pixelcountdownwidget.data.TimerDao
import com.example.pixelcountdownwidget.data.TimerEntity
import com.example.pixelcountdownwidget.model.TimerMode
import com.example.pixelcountdownwidget.widget.TimerWidgetUpdater
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class TimerViewModel(
    private val dao: TimerDao
) : ViewModel() {

    private val _newLabel = MutableStateFlow("")
    val newLabel: StateFlow<String> = _newLabel

    private val _newMode = MutableStateFlow(TimerMode.DOWN)
    val newMode: StateFlow<TimerMode> = _newMode

    private val _selectedZone = MutableStateFlow(ZoneId.systemDefault())
    val selectedZone: StateFlow<ZoneId> = _selectedZone

    private val _dateText = MutableStateFlow("")
    val dateText: StateFlow<String> = _dateText

    private val _timeText = MutableStateFlow("00:00")
    val timeText: StateFlow<String> = _timeText

    val timers = dao.getAll()
        .combine(MutableStateFlow(Instant.now())) { list, _ -> list }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setLabel(value: String) {
        _newLabel.value = value
    }

    fun setMode(value: TimerMode) {
        _newMode.value = value
    }

    fun setZone(zone: ZoneId) {
        _selectedZone.value = zone
    }

    fun setDate(value: String) {
        _dateText.value = value
    }

    fun setTime(value: String) {
        _timeText.value = value
    }

    fun saveTimer() {
        val label = _newLabel.value.trim()
        if (label.isEmpty()) return

        val date = _dateText.value
        val time = _timeText.value
        if (date.isBlank() || time.isBlank()) return

        val localDateTime = LocalDateTime.parse(
            "$date $time",
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        )

        val zoned = ZonedDateTime.of(localDateTime, _selectedZone.value)
        val instant = zoned.toInstant()

        val entity = TimerEntity(
            label = label,
            mode = _newMode.value.name,
            startInstantEpochMillis = if (_newMode.value == TimerMode.UP) instant.toEpochMilli() else System.currentTimeMillis(),
            targetInstantEpochMillis = if (_newMode.value == TimerMode.DOWN) instant.toEpochMilli() else null,
            zoneId = _selectedZone.value.id
        )

        viewModelScope.launch {
            dao.insert(entity)
            TimerWidgetUpdater.updateWidgetNow()
            clearFields()
        }
    }

    fun deleteTimer(entity: TimerEntity) {
        viewModelScope.launch {
            dao.delete(entity)
            TimerWidgetUpdater.updateWidgetNow()
        }
    }

    private fun clearFields() {
        _newLabel.value = ""
        _newMode.value = TimerMode.DOWN
        _dateText.value = ""
        _timeText.value = "00:00"
    }
}

class TimerViewModelFactory(
    private val dao: TimerDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TimerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TimerViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
