package de.leuchtetgruen.pluslocation.helpers

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import de.leuchtetgruen.pluslocation.helpers.ui.PermissionActivity

class LocationProviderTask(private val act: PermissionActivity, private val locationListener: LocationListener) : GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    internal var googleApiClient: GoogleApiClient? = null
    private var locationRequest: LocationRequest? = null

    init {

        buildApiClient()
        buildDefaultLocationRequest()
    }

    fun buildDefaultLocationRequest(interval: Int, priority: Int) {
        locationRequest = LocationRequest()
        locationRequest!!.interval = interval.toLong()
        locationRequest!!.priority = priority
    }

    fun start() {
        if (googleApiClient == null) {
            buildApiClient()
        }

        googleApiClient!!.connect()
    }

    fun stop() {
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, locationListener)

        if (googleApiClient != null) {
            googleApiClient!!.disconnect()
        }
    }

    // private methods
    private fun buildApiClient() {
        googleApiClient = GoogleApiClient.Builder(act)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()

    }

    private fun buildDefaultLocationRequest() {
        buildDefaultLocationRequest(500, LocationRequest.PRIORITY_HIGH_ACCURACY)
    }


    private fun tryToRequestLocationUpdates() {
        act.requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, object : PermissionActivity.PermissionListener {
            @SuppressLint("MissingPermission")
            override fun permissionGranted() {
                if (ActivityCompat.checkSelfPermission(act, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(act, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return
                }
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, locationListener)
            }

            override fun permissionNotGranted() {

            }
        })
    }

    private fun tryToRequestLastKnownLocation() {
        act.requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, object : PermissionActivity.PermissionListener {
            @SuppressLint("MissingPermission")
            override fun permissionGranted() {
                if (ActivityCompat.checkSelfPermission(act, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(act, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return
                }
                val lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient)
                locationListener.onLocationChanged(lastKnownLocation)
            }

            override fun permissionNotGranted() {

            }
        })
    }

    // google connection listeners

    override fun onConnected(bundle: Bundle?) {
        tryToRequestLastKnownLocation()
        tryToRequestLocationUpdates()

    }

    override fun onConnectionSuspended(i: Int) {

    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {

    }

    companion object {

        private val MY_PERMISSIONS_REQUEST_FINE_LOCATION = 3457899 // the power of falling asleep on the numpad
    }


}