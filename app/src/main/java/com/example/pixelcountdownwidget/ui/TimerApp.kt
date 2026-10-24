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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.content.Intent
import com.example.pixelcountdownwidget.data.TimerEntity
import com.example.pixelcountdownwidget.model.TimerMode
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerApp(viewModel: TimerViewModel) {
    val context = LocalContext.current
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
            Button(
                onClick = {
                    val intent = Intent(context, WidgetConfigActivity::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Widget configuration")
            }

            Spacer(modifier = Modifier.height(8.dp))

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
            TimerDisplayState(displayText = formatSeconds(secs))
        }
        else -> {
            val target = timer.targetInstant() ?: now
            val diff = Duration.between(now, target)
            val secs = if (diff.isNegative) 0 else diff.seconds
            TimerDisplayState(displayText = formatSeconds(secs), isFinished = diff.isNegative)
        }
    }
}

fun formatSeconds(totalSeconds: Long): String {
    val days = totalSeconds / 86400
    val hours = (totalSeconds % 86400) / 3600
    val mins = (totalSeconds % 3600) / 60
    val secs = totalSeconds % 60

    return when {
        days > 0 -> "${days}d ${hours}h ${mins}m"
        hours > 0 -> "${hours}h ${mins}m ${secs}s"
        mins > 0 -> "${mins}m ${secs}s"
        else -> "${secs}s"
    }
}
