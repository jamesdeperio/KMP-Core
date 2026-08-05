package io.github.jamesdeperio.devtool

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

actual object ApiMonitor {
    private var shakeDetector: ShakeDetector? = null
    private var activity: ComponentActivity? = null

    fun init(activity: ComponentActivity) {
        this.activity = activity
    }

    actual fun listen() {
        val activity = this.activity ?: return
        shakeDetector =
            ShakeDetector(activity.applicationContext) {
                openApiLoggingScreen()
            }.also { it.start() }
    }

    actual fun openApiLoggingScreen() {
        val ctx = activity ?: return
        val intent = Intent(ctx, DebugActivity::class.java)
        ctx.startActivity(intent)
    }

    actual fun stop() {
        shakeDetector?.stop()
        shakeDetector = null
        activity = null // ⚠️ CRITICAL: Release the activity reference!
    }
}

@Composable
actual fun ApiMonitorPlatformEffect() {
    val context = LocalContext.current
    DisposableEffect(context) {
        val activity = context as? ComponentActivity
        if (activity != null) {
            ApiMonitor.init(activity)
            ApiMonitor.listen()
            println(" ApiMonitor is listening...")
        } else {
            println(" Failed to established ApiMonitor...")
        }
        onDispose {
            ApiMonitor.stop()
        }
    }
}
