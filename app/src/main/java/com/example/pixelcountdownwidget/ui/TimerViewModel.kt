package com.example.pixelcountdownwidget.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pixelcountdownwidget.data.TimerDao
import com.example.pixelcountdownwidget.data.TimerEntity
import com.example.pixelcountdownwidget.model.TimerMode
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

    private val _selectedZone = MutableStateFlow(ZoneId.systemDefault())
    val selectedZone: StateFlow<ZoneId> = _selectedZone

    private val _zoneInput = MutableStateFlow(ZoneId.systemDefault().id)
    val zoneInput: StateFlow<String> = _zoneInput

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

    fun setZone(zone: ZoneId) {
        _selectedZone.value = zone
        _zoneInput.value = zone.id
    }

    fun setZoneInput(value: String) {
        _zoneInput.value = value
        runCatching { ZoneId.of(value) }.onSuccess { _selectedZone.value = it }
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

        val zone = _selectedZone.value
        val localDateTime = LocalDateTime.parse(
            "$date $time",
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        )

        val targetInstant = ZonedDateTime.of(localDateTime, zone).toInstant()
        val mode = if (targetInstant.isBefore(Instant.now())) TimerMode.UP else TimerMode.DOWN

        val entity = TimerEntity(
            label = label,
            mode = mode.name,
            startInstantEpochMillis = if (mode == TimerMode.UP) targetInstant.toEpochMilli() else System.currentTimeMillis(),
            targetInstantEpochMillis = if (mode == TimerMode.DOWN) targetInstant.toEpochMilli() else null,
            zoneId = zone.id,
            selectedForWidget = false
        )

        viewModelScope.launch {
            dao.insert(entity)
            clearFields()
        }
    }

    fun deleteTimer(entity: TimerEntity) {
        viewModelScope.launch {
            dao.delete(entity)
        }
    }

    fun toggleWidgetSelection(entity: TimerEntity) {
        viewModelScope.launch {
            dao.update(entity.copy(selectedForWidget = !entity.selectedForWidget))
        }
    }

    private fun clearFields() {
        _newLabel.value = ""
        _dateText.value = ""
        _timeText.value = "00:00"
        _zoneInput.value = ZoneId.systemDefault().id
        _selectedZone.value = ZoneId.systemDefault()
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
