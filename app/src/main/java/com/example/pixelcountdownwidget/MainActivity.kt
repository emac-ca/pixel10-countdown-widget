package com.example.pixelcountdownwidget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pixelcountdownwidget.ui.TimerApp
import com.example.pixelcountdownwidget.ui.TimerViewModel
import com.example.pixelcountdownwidget.ui.TimerViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as PixelCountdownApp
        val factory = TimerViewModelFactory(app.database.timerDao())

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: TimerViewModel = viewModel(factory = factory)
                    TimerApp(viewModel = viewModel)
                }
            }
        }
    }
}
