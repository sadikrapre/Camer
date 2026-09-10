package com.example.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlin.math.abs

data class LevelState(
    val rollDegrees: Float = 0f,
    val pitchDegrees: Float = 0f,
    val isLevel: Boolean = false
)

class SpiritLevelTracker(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        ?: sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    var onLevelChanged: ((LevelState) -> Unit)? = null

    private var smoothedRoll = 0f
    private var smoothedPitch = 0f
    private val alpha = 0.25f // Smoothing factor

    fun start() {
        rotationSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        var roll = 0f
        var pitch = 0f

        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)

            // roll is orientation[2], pitch is orientation[1]
            roll = Math.toDegrees(orientation[2].toDouble()).toFloat()
            pitch = Math.toDegrees(orientation[1].toDouble()).toFloat()
        } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val ax = event.values[0]
            val ay = event.values[1]
            val az = event.values[2]

            roll = Math.toDegrees(kotlin.math.atan2(ax.toDouble(), ay.toDouble())).toFloat()
            pitch = Math.toDegrees(kotlin.math.atan2(-az.toDouble(), kotlin.math.sqrt((ax * ax + ay * ay).toDouble()))).toFloat()
        }

        // Apply low pass filter
        smoothedRoll = smoothedRoll + alpha * (roll - smoothedRoll)
        smoothedPitch = smoothedPitch + alpha * (pitch - smoothedPitch)

        val isLevel = abs(smoothedRoll) < 1.0f && abs(smoothedPitch) < 2.0f

        onLevelChanged?.invoke(
            LevelState(
                rollDegrees = smoothedRoll,
                pitchDegrees = smoothedPitch,
                isLevel = isLevel
            )
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

@Composable
fun rememberSpiritLevelState(): LevelState {
    val context = LocalContext.current
    var roll by remember { mutableFloatStateOf(0f) }
    var pitch by remember { mutableFloatStateOf(0f) }

    DisposableEffect(context) {
        val tracker = SpiritLevelTracker(context)
        tracker.onLevelChanged = { state ->
            roll = state.rollDegrees
            pitch = state.pitchDegrees
        }
        tracker.start()
        onDispose {
            tracker.stop()
        }
    }

    val isLevel = abs(roll) < 1.0f && abs(pitch) < 2.0f
    return LevelState(rollDegrees = roll, pitchDegrees = pitch, isLevel = isLevel)
}
