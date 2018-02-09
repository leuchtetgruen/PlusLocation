package de.leuchtetgruen.pluslocation.ui.viewmodels

import android.app.Application
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.OnLifecycleEvent
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.hardware.SensorManager
import android.net.Uri
import de.leuchtetgruen.pluslocation.R
import de.leuchtetgruen.pluslocation.businessobjects.POI
import de.leuchtetgruen.pluslocation.businessobjects.WGS84Coordinates
import de.leuchtetgruen.pluslocation.businessobjects.openlocationcode.OpenLocationCode
import de.leuchtetgruen.pluslocation.persistence.POIDatabase
import de.leuchtetgruen.pluslocation.ui.activities.CameraActivity
import de.leuchtetgruen.pluslocation.ui.activities.CompassActivity
import de.leuchtetgruen.pluslocation.ui.activities.EnterCodeActivity
import de.leuchtetgruen.pluslocation.ui.activities.PoiListActivity

class CompassViewModel(private val app: Application?) : LocationHeadingViewModel(app) {

    companion object {
        fun create(activity: CompassActivity): CompassViewModel {
            return ViewModelProviders.of(activity).get(CompassViewModel::class.java)
        }
    }

    val distanceString: MutableLiveData<String> = MutableLiveData()
    val nearbyString: MutableLiveData<String> = MutableLiveData()
    val compassRotation: MutableLiveData<Float> = MutableLiveData()
    val needleRotation: MutableLiveData<Float> = MutableLiveData()
    val compassAndNeedleOpacity: MutableLiveData<Float> = MutableLiveData()




    override fun updateCurrentLocation(currentCoordinate: WGS84Coordinates) {
        super.updateCurrentLocation(currentCoordinate)
        updateDistance()
    }

    override fun headingChanged(heading: Double) {
        compassRotation.value = 360 - heading.toFloat()

        if ((targetCoordinate == null) || (currentCoordinate == null)) return

        val bearingToTarget = currentCoordinate!!.bearingInDegrees(targetCoordinate!!)
        needleRotation.value = (bearingToTarget - heading).toFloat()
    }

    override fun reliabilityChanged(reliability: Int) {
        compassAndNeedleOpacity.value = when (reliability) {
            SensorManager.SENSOR_STATUS_UNRELIABLE -> 0.1f
            SensorManager.SENSOR_STATUS_ACCURACY_LOW -> 0.33f
            SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> 0.66f
            SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> 1.0f
            else -> 1.0f
        }
    }

    override fun reloadSavedData() {
        super.reloadSavedData()
        updateNearby()
    }

    private fun updateNearby() {
        if ((targetCode.value == null) || (targetCoordinate == null)) return

        val targetDescription = if (targetName.value != null) {
            targetName.value
        } else {
            targetCode.value
        }

        val closestPOIs = POIDatabase.closestPoisInAllDirections(OpenLocationCode(targetCode.value!!))

        nearbyString.value = when {
            closestPOIs.isEmpty() -> "No POIs close to $targetDescription found"
            closestPOIs.size == 1 -> "POI close to $targetDescription: " + describeWayToPoi(closestPOIs.first())
            else -> "POIs close to $targetDescription : " + closestPOIs.joinToString { describeWayToPoi(it) }
        }
    }


    private fun updateDistance() {
        if ((currentCoordinate == null) || (targetCoordinate == null)) return

        val distance = targetCoordinate!!.distanceInMeters(currentCoordinate!!)

        if (distance > 1000) {
            distanceString.value = String.format("%.1fkm", (distance / 1000))
        } else {
            distanceString.value = String.format("%dm", distance.toInt())
        }
    }

    private fun describeWayToPoi(poi: POI): String {
        return if (targetCoordinate == null) {
            ""
        } else {
            String.format(app!!.getString(R.string.poi_way_description), poi.name, targetCoordinate!!.distanceInMeters(poi.coordinate()) / 1000, targetCoordinate!!.direction(poi.coordinate()).toString())
        }
    }

    //Lifecycle methods
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun onResumeCompass() {
        reloadSavedData()
    }

    // Button handler
    fun showCurrentDestinationOnMap() {
        if (targetCoordinate == null) {
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

    fun openCamera() {
        app?.startActivity(CameraActivity.intentTo(app))
    }

}