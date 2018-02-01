package de.leuchtetgruen.pluslocation.ui.viewmodels

import android.app.Application
import android.arch.lifecycle.*
import android.hardware.SensorManager
import de.leuchtetgruen.pluslocation.businessobjects.WGS84Coordinates
import de.leuchtetgruen.pluslocation.helpers.HeadingProviderTask
import de.leuchtetgruen.pluslocation.helpers.SavedCode
import de.leuchtetgruen.pluslocation.ui.activities.CompassActivity

class CompassViewModel(app: Application?) : AndroidViewModel(app!!), LifecycleObserver, HeadingProviderTask.HeadingListener {

    companion object {
        fun create(activity: CompassActivity) : CompassViewModel {
            return ViewModelProviders.of(activity).get(CompassViewModel::class.java)
        }
    }

    private var currentCoordinate: WGS84Coordinates? = null
    private var targetCoordinate : WGS84Coordinates? = null

    private lateinit var headingProviderTask : HeadingProviderTask

    val distanceString : MutableLiveData<String> = MutableLiveData()
    val compassRotation : MutableLiveData<Float> = MutableLiveData()
    val needleRotation: MutableLiveData<Float> = MutableLiveData()
    val compassAndNeedleOpacity : MutableLiveData<Float> = MutableLiveData()


    fun updateCurrentLocation(currentCoordinate : WGS84Coordinates) {
        this.currentCoordinate = currentCoordinate

        updateValues()
    }

    override fun headingChanged(heading: Float) {
        compassRotation.value = 360 - heading

        if ((targetCoordinate == null) || (currentCoordinate == null)) return

        val bearingToTarget = currentCoordinate!!.bearingInDegrees(targetCoordinate!!)
        needleRotation.value = (bearingToTarget - heading).toFloat()
    }

    override fun reliabilityChanged(reliability : Int) {
        compassAndNeedleOpacity.value = when (reliability) {
            SensorManager.SENSOR_STATUS_UNRELIABLE -> 0.1f
            SensorManager.SENSOR_STATUS_ACCURACY_LOW -> 0.33f
            SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> 0.66f
            SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> 1.0f
            else -> 1.0f
        }
    }




    fun reloadSavedData() {
        targetCoordinate  = SavedCode.savedLocation(this.getApplication())
    }

    private fun updateValues() {
        updateDistance()
    }

    private fun updateDistance() {
        if ((currentCoordinate == null) || (targetCoordinate == null)) return

        val distance = targetCoordinate!!.distanceInMeters(currentCoordinate!!)

        if (distance > 1000) {
            distanceString.value = String.format("%.1fkm", (distance / 1000))
        }
        else {
            distanceString.value = String.format("%dm", distance)
        }
    }

    // Lifecycle events
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun onResume() {
        reloadSavedData()
        headingProviderTask = HeadingProviderTask(getApplication(), this)
        headingProviderTask.start()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun onStop() {
        headingProviderTask.stop()
    }


}