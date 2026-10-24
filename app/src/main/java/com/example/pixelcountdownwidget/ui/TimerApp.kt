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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pixelcountdownwidget.data.TimerEntity
import com.example.pixelcountdownwidget.model.TimerMode
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerApp(viewModel: TimerViewModel) {
    val timers by viewModel.timers.collectAsState(initial = emptyList())
    val label by viewModel.newLabel.collectAsState()
    val mode by viewModel.newMode.collectAsState()
    val zoneText by viewModel.zoneInput.collectAsState()
    val dateText by viewModel.dateText.collectAsState()
    val timeText by viewModel.timeText.collectAsState()

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
            OutlinedTextField(
                value = label,
                onValueChange = { viewModel.setLabel(it) },
                label = { Text("Timer label") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = zoneText,
                onValueChange = { viewModel.setZoneInput(it) },
                label = { Text("Time zone (e.g. America/New_York)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = mode == TimerMode.DOWN,
                    onClick = { viewModel.setMode(TimerMode.DOWN) },
                    label = { Text("Count Down") }
                )
                FilterChip(
                    selected = mode == TimerMode.UP,
                    onClick = { viewModel.setMode(TimerMode.UP) },
                    label = { Text("Count Up") }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = dateText,
                onValueChange = { viewModel.setDate(it) },
                label = { Text("Date (yyyy-MM-dd)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = timeText,
                onValueChange = { viewModel.setTime(it) },
                label = { Text("Time (HH:mm)") },
                modifier = Modifier.fillMaxWidth()
            )

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

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
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
