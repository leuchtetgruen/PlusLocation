package de.leuchtetgruen.pluslocation.ui.viewmodels

import android.app.Application
import android.arch.lifecycle.*
import android.content.Intent
import android.hardware.SensorManager
import android.net.Uri
import de.leuchtetgruen.pluslocation.businessobjects.WGS84Coordinates
import de.leuchtetgruen.pluslocation.businessobjects.openlocationcode.OpenLocationCode
import de.leuchtetgruen.pluslocation.helpers.HeadingProviderTask
import de.leuchtetgruen.pluslocation.persistence.POIDatabase
import de.leuchtetgruen.pluslocation.persistence.SavedCode
import de.leuchtetgruen.pluslocation.ui.activities.CompassActivity
import de.leuchtetgruen.pluslocation.ui.activities.EnterCodeActivity
import de.leuchtetgruen.pluslocation.ui.activities.PoiListActivity

class CompassViewModel(private val app: Application?) : AndroidViewModel(app!!), LifecycleObserver, HeadingProviderTask.HeadingListener {

    companion object {
        fun create(activity: CompassActivity) : CompassViewModel {
            return ViewModelProviders.of(activity).get(CompassViewModel::class.java)
        }
    }

    private var currentCoordinate: WGS84Coordinates? = null
    private var targetCoordinate : WGS84Coordinates? = null

    private lateinit var headingProviderTask : HeadingProviderTask

    val distanceString : MutableLiveData<String> = MutableLiveData()
    val nearbyString : MutableLiveData<String> = MutableLiveData()
    val compassRotation : MutableLiveData<Float> = MutableLiveData()
    val needleRotation: MutableLiveData<Float> = MutableLiveData()
    val compassAndNeedleOpacity : MutableLiveData<Float> = MutableLiveData()

    val targetName : MutableLiveData<String> = MutableLiveData()
    val targetCode : MutableLiveData<String> = MutableLiveData()



    fun updateCurrentLocation(currentCoordinate : WGS84Coordinates) {
        this.currentCoordinate = currentCoordinate

        updateDistance()
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

        targetCode.value = SavedCode.savedCode(this.getApplication())
        targetName.value = SavedCode.savedName(this.getApplication())

        updateNearby()
    }

    private fun updateNearby() {
        if (targetCode.value == null) return

        val closestPOIs = POIDatabase.closestPoisInAllDirections(OpenLocationCode(targetCode.value!!) )

        nearbyString.value = if (closestPOIs.isEmpty()) {
            "No POIs nearby found"
        }
        else if (closestPOIs.size == 1) {
            "POI nearby: " + closestPOIs.first().describe(targetCoordinate)
        }
        else {
            "Nearby POIs : " + closestPOIs.joinToString { it.describe(targetCoordinate) }
        }
    }


    private fun updateDistance() {
        if ((currentCoordinate == null) || (targetCoordinate == null)) return

        val distance = targetCoordinate!!.distanceInMeters(currentCoordinate!!)

        if (distance > 1000) {
            distanceString.value = String.format("%.1fkm", (distance / 1000))
        }
        else {
            distanceString.value = String.format("%dm", distance.toInt())
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

    // Button handler
    fun showCurrentDestinationOnMap() {
        if (targetCoordinate== null) {
            return
        }

        val gmmIntentUri = Uri.parse("geo:" + targetCoordinate!!.latitude + "," + targetCoordinate!!.longitude)
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.`package` = "com.google.android.apps.maps"

        app?.startActivity(mapIntent)
    }


    fun enterCode() {
        app?.startActivity(EnterCodeActivity.intentTo(app))
    }

    fun choosePOI() {
        app?.startActivity(PoiListActivity.intentTo(app))
    }


}