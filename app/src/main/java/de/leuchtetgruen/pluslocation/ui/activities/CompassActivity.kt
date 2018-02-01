package de.leuchtetgruen.pluslocation.ui.activities

import android.Manifest
import android.arch.lifecycle.Observer
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.view.View
import android.widget.RelativeLayout
import com.google.android.gms.location.LocationListener
import de.leuchtetgruen.pluslocation.R
import de.leuchtetgruen.pluslocation.businessobjects.WGS84Coordinates
import de.leuchtetgruen.pluslocation.helpers.LocationProviderTask
import de.leuchtetgruen.pluslocation.helpers.ui.PermissionActivity
import de.leuchtetgruen.pluslocation.ui.viewmodels.CompassViewModel
import kotlinx.android.synthetic.main.bottom_sheet_destination_info.*
import kotlinx.android.synthetic.main.content_compass.*


class CompassActivity : PermissionActivity(), PermissionActivity.PermissionListener, LocationListener {

    private val viewModel by lazy { CompassViewModel.create(this)}

    private lateinit var locationProviderTask: LocationProviderTask
    private lateinit var infoSheetBehaviour: BottomSheetBehavior<RelativeLayout>

    // Observers
    private var distanceObserver: Observer<String> = Observer { txtDistance.text = it }
    private var rotationObserver: Observer<Float> = Observer { imgCompassNESW.rotation = it!! }
    private var needleRotationObserver : Observer<Float> = Observer { imgCompass.rotation = it!! }
    private var opacityObserver : Observer<Float> = Observer {
        imgCompassNESW.alpha = it!!
        imgCompass.alpha = it!!
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compass)

        setupInfoSheet()
    }

    private fun setupInfoSheet() {
        infoSheetBehaviour = BottomSheetBehavior.from(bottom_sheet_destination);

        txtDestination.setOnClickListener({
            infoSheetBehaviour.state = BottomSheetBehavior.STATE_EXPANDED
        })

        btnShowOnMap.setOnClickListener({
            viewModel.showCurrentDestinationOnMap()
        })
    }

    override fun onStart() {
        super.onStart()
        addObservers()
        startQueryingLocation()
    }

    override fun onResume() {
        super.onResume()
        viewModel.reloadSavedData()
    }

    override fun onStop() {
        locationProviderTask.stop()
        removeObservers()
        super.onStop()
    }

    private fun startQueryingLocation() {
        locationProviderTask = LocationProviderTask(this, this)
        requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, this)
    }

    private fun addObservers() {
        viewModel.distanceString.observe(this, distanceObserver)
        viewModel.compassRotation.observe(this, rotationObserver)
        viewModel.needleRotation.observe(this, needleRotationObserver)
        viewModel.compassAndNeedleOpacity.observe(this, opacityObserver)

        lifecycle.addObserver(viewModel)
    }

    private fun removeObservers() {
        viewModel.distanceString.removeObserver(distanceObserver)
        viewModel.compassRotation.removeObserver(rotationObserver)
        viewModel.needleRotation.removeObserver(needleRotationObserver)
        viewModel.compassAndNeedleOpacity.removeObserver(opacityObserver)

        lifecycle.removeObserver(viewModel)
    }

    override fun permissionGranted() {
        locationProviderTask.start()
    }

    override fun permissionNotGranted() {
    }

    override fun onLocationChanged(location: Location?) {
        if (location != null) {
            viewModel.updateCurrentLocation(WGS84Coordinates(location.latitude, location.longitude))
        }
    }
}
