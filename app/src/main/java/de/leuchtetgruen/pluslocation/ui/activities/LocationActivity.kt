package de.leuchtetgruen.pluslocation.ui.activities

import android.Manifest
import android.location.Location
import com.google.android.gms.location.LocationListener
import de.leuchtetgruen.pluslocation.businessobjects.WGS84Coordinates
import de.leuchtetgruen.pluslocation.helpers.LocationProviderTask
import de.leuchtetgruen.pluslocation.helpers.ui.PermissionActivity
import de.leuchtetgruen.pluslocation.ui.viewmodels.LocationHeadingViewModel

open class LocationActivity : PermissionActivity(), PermissionActivity.PermissionListener, LocationListener {

    protected var viewModel : LocationHeadingViewModel? = null

    private lateinit var locationProviderTask: LocationProviderTask

    private fun startQueryingLocation() {
        locationProviderTask = LocationProviderTask(this, this)
        requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, this)
    }

    override fun onStart() {
        super.onStart()
        startQueryingLocation()
    }

    override fun onStop() {
        locationProviderTask.stop()
        super.onStop()
    }

    override fun permissionGranted(permission : String) {
        if (permission == Manifest.permission.ACCESS_FINE_LOCATION) {
            locationProviderTask.start()
        }
    }

    override fun permissionNotGranted() {
    }

    override fun onLocationChanged(location: Location?) {
        if ((location != null) && (viewModel != null)) {
            viewModel?.updateCurrentLocation(WGS84Coordinates(location.latitude, location.longitude))
        }
    }
}