package de.leuchtetgruen.pluslocation.ui.activities

import android.Manifest
import android.app.SearchManager
import android.arch.lifecycle.Observer
import android.content.ComponentName
import android.content.Context
import android.location.Location
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.view.Menu
import android.widget.RelativeLayout
import android.widget.SearchView
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
        setupSearch()
    }

    private fun setupInfoSheet() {
        infoSheetBehaviour = BottomSheetBehavior.from(bottom_sheet_destination);

        btnShowSheet.setOnClickListener({
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

    fun setupSearch() {
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView.setSearchableInfo(searchManager.getSearchableInfo(PoiListActivity.componentName(this)))
        searchView.setIconifiedByDefault(true)
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
        searchView.isIconified = true
    }

    override fun onStop() {
        locationProviderTask.stop()
        removeObservers()
        super.onStop()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the options menu from XML
        val inflater = menuInflater
        inflater.inflate(R.menu.compass_activity_menu, menu)

        // Get the SearchView and set the searchable configuration
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menu?.findItem(R.id.menu_search)?.actionView as SearchView
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(ComponentName(this, PoiListActivity::class.java)))
        searchView.setIconifiedByDefault(true)

        return true
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
