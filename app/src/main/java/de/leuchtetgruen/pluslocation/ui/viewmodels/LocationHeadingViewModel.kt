package de.leuchtetgruen.pluslocation.ui.viewmodels

import android.app.Application
import android.arch.lifecycle.*
import de.leuchtetgruen.pluslocation.businessobjects.WGS84Coordinates
import de.leuchtetgruen.pluslocation.helpers.HeadingProviderTask
import de.leuchtetgruen.pluslocation.persistence.SavedCode

abstract class LocationHeadingViewModel(private val app: Application?) : AndroidViewModel(app!!), LifecycleObserver, HeadingProviderTask.HeadingListener {


    protected var currentCoordinate: WGS84Coordinates? = null

    protected var targetCoordinate: WGS84Coordinates? = null

    protected var bearingToTarget: Double? = null

    protected var distanceToTargetInMeter : Double? = null

    private lateinit var headingProviderTask: HeadingProviderTask

    val targetName: MutableLiveData<String> = MutableLiveData()
    val targetCode: MutableLiveData<String> = MutableLiveData()

    open fun updateCurrentLocation(currentCoordinate: WGS84Coordinates) {
        this.currentCoordinate = currentCoordinate
        calculateHeadingToTarget()
        calculateDistanceToTarget()
    }


    private fun calculateHeadingToTarget() {
        if ((currentCoordinate == null) || (targetCoordinate == null)) return

        bearingToTarget = currentCoordinate!!.bearingInDegrees(targetCoordinate!!)
    }

    private fun calculateDistanceToTarget() {
        if ((currentCoordinate == null) || (targetCoordinate == null)) return

        distanceToTargetInMeter = currentCoordinate!!.distanceInMeters(targetCoordinate!!)
    }

    open fun reloadSavedData() {
        targetCoordinate = SavedCode.savedLocation(this.getApplication())

        targetCode.value = SavedCode.savedCode(this.getApplication())
        targetName.value = SavedCode.savedName(this.getApplication())
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