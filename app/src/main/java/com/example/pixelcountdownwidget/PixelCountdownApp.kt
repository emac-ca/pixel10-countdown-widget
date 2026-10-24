package com.example.pixelcountdownwidget

import android.app.Application
import com.example.pixelcountdownwidget.data.TimerDatabase

class PixelCountdownApp : Application() {
    val database by lazy { TimerDatabase.getInstance(this) }
}
