package de.leuchtetgruen.pluslocation.ui.activities

import android.Manifest
import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import com.google.android.gms.location.LocationListener
import de.leuchtetgruen.pluslocation.R
import de.leuchtetgruen.pluslocation.businessobjects.WGS84Coordinates
import de.leuchtetgruen.pluslocation.helpers.LocationProviderTask
import de.leuchtetgruen.pluslocation.helpers.ui.PermissionActivity
import de.leuchtetgruen.pluslocation.ui.viewmodels.PoiListViewModel
import kotlinx.android.synthetic.main.activity_poi_list.*

class PoiListActivity : PermissionActivity(), LocationListener, PermissionActivity.PermissionListener {

    companion object {
        fun intentTo(context : Context) : Intent {
            val intent = Intent(context, PoiListActivity::class.java)

            return intent
        }
    }

    private val viewModel by lazy { PoiListViewModel.create(this)}
    private lateinit var locationProviderTask: LocationProviderTask

    private var poiCountObserver: Observer<String> = Observer { txtSearchResultCount.text = it }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_poi_list)
        addObservers()
        startQueryingLocation()
    }



    private fun startQueryingLocation() {
        locationProviderTask = LocationProviderTask(this, this)
        requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, this)
    }

    override fun onStop() {
        removeObservers()
        locationProviderTask.stop()
        super.onStop()
    }

    private fun addObservers() {
        viewModel.poiCountText.observe(this, poiCountObserver)
        lifecycle.addObserver(viewModel)
    }

    private fun removeObservers() {
        viewModel.poiCountText.removeObserver(poiCountObserver)
    }

    // interface methods
    override fun onLocationChanged(currentLocation: Location?) {
        if (currentLocation != null) {
            viewModel.setCurrentPosition(WGS84Coordinates(currentLocation.latitude, currentLocation.longitude))
        }

    }

    override fun permissionGranted() {
        locationProviderTask.start()
    }

    override fun permissionNotGranted() {
    }



}
