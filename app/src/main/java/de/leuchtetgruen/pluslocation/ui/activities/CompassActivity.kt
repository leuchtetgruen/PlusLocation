package de.leuchtetgruen.pluslocation.ui.activities

import android.Manifest
import android.arch.lifecycle.Observer
import android.location.Location
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.widget.RelativeLayout
import com.google.android.gms.location.LocationListener
import de.leuchtetgruen.pluslocation.R
import de.leuchtetgruen.pluslocation.businessobjects.WGS84Coordinates
import de.leuchtetgruen.pluslocation.helpers.InitialImporter
import de.leuchtetgruen.pluslocation.helpers.LocationProviderTask
import de.leuchtetgruen.pluslocation.helpers.ui.PermissionActivity
import de.leuchtetgruen.pluslocation.persistence.POIDatabase
import de.leuchtetgruen.pluslocation.ui.viewmodels.CompassViewModel
import kotlinx.android.synthetic.main.bottom_sheet_destination_info.*
import kotlinx.android.synthetic.main.content_compass.*


class CompassActivity : PermissionActivity(), PermissionActivity.PermissionListener, LocationListener {

    private val viewModel by lazy { CompassViewModel.create(this)}

    private lateinit var locationProviderTask: LocationProviderTask
    private lateinit var infoSheetBehaviour: BottomSheetBehavior<RelativeLayout>

    // Observers
    private var distanceObserver: Observer<String> = Observer { txtDistance.text = it }
    private var nearbyObserver : Observer<String> = Observer { txtNearby.text = it }
    private var destinationNameObeserver : Observer<String> = Observer { txtDestination.text = it }
    private var destinationCodeObserver : Observer<String> = Observer { txtDestinationCode.text = it }
    private var rotationObserver: Observer<Float> = Observer { imgCompassNESW.rotation = it!! }
    private var needleRotationObserver : Observer<Float> = Observer { imgCompass.rotation = it!! }
    private var opacityObserver : Observer<Float> = Observer {
        imgCompassNESW.alpha = it!!
        imgCompass.alpha = it
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compass)

        POIDatabase.build(this)
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
        btnChangeCode.setOnClickListener({
            viewModel.enterCode()
        })
        btnChoosePOI.setOnClickListener({
            viewModel.choosePOI()
        })
    }

    override fun onStart() {
        super.onStart()
        addObservers()
        startQueryingLocation()

        InitialImporter.import(this)
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
        viewModel.nearbyString.observe(this, nearbyObserver)
        viewModel.compassRotation.observe(this, rotationObserver)
        viewModel.needleRotation.observe(this, needleRotationObserver)
        viewModel.compassAndNeedleOpacity.observe(this, opacityObserver)
        viewModel.targetName.observe(this, destinationNameObeserver)
        viewModel.targetCode.observe(this, destinationCodeObserver)

        lifecycle.addObserver(viewModel)
    }

    private fun removeObservers() {
        viewModel.distanceString.removeObserver(distanceObserver)
        viewModel.nearbyString.removeObserver(nearbyObserver)
        viewModel.compassRotation.removeObserver(rotationObserver)
        viewModel.needleRotation.removeObserver(needleRotationObserver)
        viewModel.compassAndNeedleOpacity.removeObserver(opacityObserver)
        viewModel.targetName.removeObserver(destinationNameObeserver)
        viewModel.targetCode.removeObserver(destinationCodeObserver)

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
