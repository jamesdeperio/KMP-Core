package io.github.jamesdeperio.devtool

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

class ShakeDetector(
    context: Context,
    private val onShake: () -> Unit,
) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val alpha = 0.8f
    private var gravity = floatArrayOf(0f, 0f, 0f)

    private val shakeThreshold = 13f
    private val minShakes = 3
    private val shakeWindowMs = 1000

    private var shakeCount = 0
    private var lastShakeTime = 0L

    fun start() {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // Remove gravity using low-pass filter
        gravity[0] = alpha * gravity[0] + (1 - alpha) * x
        gravity[1] = alpha * gravity[1] + (1 - alpha) * y
        gravity[2] = alpha * gravity[2] + (1 - alpha) * z

        val linearX = x - gravity[0]
        val linearY = y - gravity[1]
        val linearZ = z - gravity[2]

        val magnitude = sqrt(linearX * linearX + linearY * linearY + linearZ * linearZ)

        val now = System.currentTimeMillis()

        if (magnitude > shakeThreshold) {
            if (now - lastShakeTime < shakeWindowMs) {
                shakeCount++
            } else {
                shakeCount = 1
            }

            lastShakeTime = now

            if (shakeCount >= minShakes) {
                shakeCount = 0
                onShake()
            }
        }
    }

    override fun onAccuracyChanged(
        sensor: Sensor?,
        accuracy: Int,
    ) {}
}
