package io.github.jamesdeperio.devtool

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreMotion.CMAcceleration
import platform.CoreMotion.CMMotionManager
import platform.Foundation.NSDate
import platform.Foundation.NSOperationQueue
import platform.Foundation.timeIntervalSince1970
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import kotlin.math.sqrt

actual object ApiMonitor {
    private var motionManager: CMMotionManager? = null
    private var shakeCallback: (() -> Unit)? = null

    private var lastShakeTime: Double = 0.0
    private var shakeCount = 0
    private val SHAKE_THRESHOLD = 2.5 // tweak this, iOS accelerometer is ~9.8 m/s² at rest
    private val MIN_SHAKES = 3
    private val SHAKE_WINDOW_S = 1.0

    @OptIn(ExperimentalForeignApi::class)
    actual fun listen() {
        println("working")
        val manager = CMMotionManager()
        motionManager = manager

        if (!manager.isAccelerometerAvailable()) return

        manager.accelerometerUpdateInterval = 0.1 // 100ms
        shakeCallback = {
            openApiLoggingScreen()
        }

        manager.startAccelerometerUpdatesToQueue(
            NSOperationQueue.mainQueue,
            { data, error ->
                data?.acceleration?.let { accel ->
                    detectShake(accel) // now type matches CMAcceleration
                }
            },
        )
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun detectShake(acceleration: CValue<CMAcceleration>) {
        acceleration.useContents {
            val magnitude = sqrt(x * x + y * y + z * z)
            val now = NSDate().timeIntervalSince1970

            if (magnitude > SHAKE_THRESHOLD) {
                if (now - lastShakeTime < SHAKE_WINDOW_S) {
                    shakeCount++
                } else {
                    shakeCount = 1
                }
                lastShakeTime = now
                println("testing shake")

                if (shakeCount >= MIN_SHAKES) {
                    shakeCount = 0
                    shakeCallback?.invoke()
                }
            }
        }
    }

    private fun getRootViewController(): UIViewController? {
        // iOS 13+ using scenes
        val scenes =
            UIApplication.sharedApplication.connectedScenes
                .filterIsInstance<UIWindowScene>()

        for (scene in scenes) {
            val windows = scene.windows
            for (i in 0 until windows.count()) {
                val window = windows[i] as? UIWindow
                if (window != null && window.isKeyWindow()) {
                    return window.rootViewController
                }
            }
        }

        // fallback for older iOS
        return UIApplication.sharedApplication.keyWindow?.rootViewController
    }

    actual fun openApiLoggingScreen() {
        val rootVC = getRootViewController() ?: return
        val debugScreenVC = DebugScreenController()
        dispatch_async(dispatch_get_main_queue()) {
            rootVC.presentViewController(debugScreenVC, animated = true, completion = null)
        }
    }

    actual fun stop() {
        motionManager?.stopAccelerometerUpdates()
        motionManager = null
    }
}

@Composable
actual fun ApiMonitorPlatformEffect() {
    DisposableEffect(Unit) {
        ApiMonitor.listen()
        onDispose {
            ApiMonitor.stop()
        }
    }
}
