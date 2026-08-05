package io.github.jamesdeperio.devtool

import androidx.compose.runtime.Composable

expect object ApiMonitor {
    fun listen()

    fun openApiLoggingScreen()

    fun stop()
}

@Composable
fun ApiMonitorEffect(enabled: Boolean = true) {
    if (enabled) {
        ApiMonitorPlatformEffect()
    }
}

@Composable
expect fun ApiMonitorPlatformEffect()
