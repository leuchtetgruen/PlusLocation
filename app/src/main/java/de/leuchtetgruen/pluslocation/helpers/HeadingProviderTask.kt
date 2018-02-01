package de.leuchtetgruen.pluslocation.helpers

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

class HeadingProviderTask(context : Context, private val headingListener: HeadingListener) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    interface HeadingListener {
        fun headingChanged(heading: Float)
        fun reliabilityChanged(reliability : Int) {Log.i("HeadingListener#reliabilityChanged", reliability.toString()) }
    }

    fun start() {
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION)
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(p0: Sensor?, accuracy: Int) {
        headingListener.reliabilityChanged(accuracy)
    }

    override fun onSensorChanged(sensorEvent: SensorEvent?) {
       if (sensorEvent != null) {
           headingListener.headingChanged(sensorEvent.values[0])
       }
    }

}