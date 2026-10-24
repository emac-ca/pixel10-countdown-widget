package com.example.pixelcountdownwidget.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pixelcountdownwidget.APP_VERSION_NAME
import com.example.pixelcountdownwidget.data.TimerEntity
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerApp(viewModel: TimerViewModel) {
    val timers by viewModel.timers.collectAsState(initial = emptyList())
    val label by viewModel.newLabel.collectAsState()
    val zoneText by viewModel.zoneInput.collectAsState()
    val dateText by viewModel.dateText.collectAsState()
    val timeText by viewModel.timeText.collectAsState()

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf(LocalTime.of(9, 0)) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var zoneExpanded by remember { mutableStateOf(false) }

    val zoneOptions = remember(zoneText) {
        ZoneId.getAvailableZoneIds()
            .filter { zoneText.isBlank() || it.contains(zoneText, ignoreCase = true) }
            .sorted()
            .take(40)
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault().rules.getOffset(Instant.now())).toInstant().toEpochMilli()
    )
    val timePickerState = rememberTimePickerState(
        initialHour = selectedTime.hour,
        initialMinute = selectedTime.minute,
        is24Hour = true
    )

    fun applyPickedDate() {
        val millis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
        selectedDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
        viewModel.setDate(selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
    }

    fun applyPickedTime() {
        selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
        viewModel.setTime(selectedTime.format(DateTimeFormatter.ofPattern("HH:mm")))
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Countdown Timers") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Version $APP_VERSION_NAME",
                style = MaterialTheme.typography.labelMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = label,
                onValueChange = { viewModel.setLabel(it) },
                label = { Text("Timer label") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Mode: automatic (future = count down, past = count up)",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = zoneExpanded,
                onExpandedChange = { zoneExpanded = !zoneExpanded }
            ) {
                OutlinedTextField(
                    value = zoneText,
                    onValueChange = { value ->
                        viewModel.setZoneInput(value)
                        zoneExpanded = true
                    },
                    label = { Text("Time zone") },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (zoneText.isNotBlank()) {
                                TextButton(onClick = {
                                    viewModel.setZoneInput("")
                                    zoneExpanded = true
                                }) {
                                    Text("Clear")
                                }
                            }
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = zoneExpanded)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = zoneExpanded,
                    onDismissRequest = { zoneExpanded = false }
                ) {
                    zoneOptions.forEach { option ->
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                viewModel.setZone(ZoneId.of(option))
                                zoneExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = if (dateText.isBlank()) "Pick date" else dateText)
                }

                Button(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = if (timeText.isBlank()) "Pick time" else timeText)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.saveTimer() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save timer")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Existing timers",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(timers) { timer ->
                    TimerRow(
                        timer = timer,
                        onToggleWidget = { viewModel.toggleWidgetSelection(timer) },
                        onDelete = { viewModel.deleteTimer(timer) }
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        applyPickedDate()
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select time") },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        applyPickedTime()
                        showTimePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TimerRow(timer: TimerEntity, onToggleWidget: () -> Unit, onDelete: () -> Unit) {
    val state = computeState(timer, Instant.now())

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(timer.label, fontWeight = FontWeight.Bold)
                Text(
                    text = when (timer.mode) {
                        "UP" -> "Up from ${timer.startInstant().atZone(timer.zone()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm z"))}"
                        else -> "Target ${timer.targetInstant()?.atZone(timer.zone())?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm z")) ?: ""}"
                    }
                )
                Text(state.displayText)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Widget")
                Checkbox(
                    checked = timer.selectedForWidget,
                    onCheckedChange = { onToggleWidget() }
                )
                TextButton(onClick = onDelete) {
                    Text("Delete")
                }
            }
        }
    }
}

data class TimerDisplayState(
    val displayText: String,
    val isFinished: Boolean = false
)

fun computeState(timer: TimerEntity, now: Instant): TimerDisplayState {
    return when (timer.mode) {
        "UP" -> {
            val diff = Duration.between(timer.startInstant(), now)
            val secs = if (diff.isNegative) 0 else diff.seconds
            TimerDisplayState(displayText = formatSmartDuration(secs))
        }
        else -> {
            val target = timer.targetInstant() ?: now
            val diff = Duration.between(now, target)
            val secs = if (diff.isNegative) 0 else diff.seconds
            TimerDisplayState(displayText = formatSmartDuration(secs), isFinished = diff.isNegative)
        }
    }
}

fun formatSmartDuration(totalSeconds: Long): String {
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
