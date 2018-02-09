package de.leuchtetgruen.pluslocation.ui.activities

import android.app.SearchManager
import android.arch.lifecycle.Observer
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.view.Menu
import android.widget.RelativeLayout
import android.widget.SearchView
import de.leuchtetgruen.pluslocation.R
import de.leuchtetgruen.pluslocation.persistence.POIDatabase
import de.leuchtetgruen.pluslocation.ui.CSVImporterDialog
import de.leuchtetgruen.pluslocation.ui.viewmodels.CompassViewModel
import kotlinx.android.synthetic.main.bottom_sheet_destination_info.*
import kotlinx.android.synthetic.main.content_compass.*


class CompassActivity : LocationActivity() {


    private lateinit var infoSheetBehaviour: BottomSheetBehavior<RelativeLayout>

    // Observers
    private var distanceObserver: Observer<String> = Observer { txtDistance.text = it }
    private var nearbyObserver: Observer<String> = Observer { txtNearby.text = it }
    private var destinationNameObeserver: Observer<String> = Observer { txtDestination.text = it }
    private var destinationCodeObserver: Observer<String> = Observer { txtDestinationCode.text = it }
    private var rotationObserver: Observer<Float> = Observer { imgCompass.rotation = it!! }
    private var needleRotationObserver: Observer<Float> = Observer { imgCompassNeedle.rotation = it!! }
    private var opacityObserver: Observer<Float> = Observer {
        imgCompass.alpha = it!!
        imgCompassNeedle.alpha = it
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compass)

        viewModel = CompassViewModel.create(this)

        POIDatabase.build(this)
        setupInfoSheet()

        if (intent.action == Intent.ACTION_VIEW) {
            CSVImporterDialog.startFromIntent(intent, this)
        }


    }

    private fun setupInfoSheet() {
        val vm = viewModel as CompassViewModel
        infoSheetBehaviour = BottomSheetBehavior.from(bottom_sheet_destination)

        btnShowSheet.setOnClickListener({
            infoSheetBehaviour.state = BottomSheetBehavior.STATE_EXPANDED
        })

        btnShowOnMap.setOnClickListener({
            vm.showCurrentDestinationOnMap()
        })
        btnChangeCode.setOnClickListener({
            vm.enterCode()
        })
        btnChoosePOI.setOnClickListener({
            vm.choosePOI()
        })
        btnSearch.setOnClickListener({
            onSearchRequested()
        })
        btnCamera.setOnClickListener({
            vm.openCamera()
        })
    }

    override fun onStart() {
        super.onStart()
        addObservers()
    }

    override fun onResume() {
        super.onResume()
        viewModel?.reloadSavedData()
    }

    override fun onStop() {
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


    private fun addObservers() {
        val vm = viewModel as CompassViewModel

        vm.distanceString.observe(this, distanceObserver)
        vm.nearbyString.observe(this, nearbyObserver)
        vm.compassRotation.observe(this, rotationObserver)
        vm.needleRotation.observe(this, needleRotationObserver)
        vm.compassAndNeedleOpacity.observe(this, opacityObserver)
        vm.targetName.observe(this, destinationNameObeserver)
        vm.targetCode.observe(this, destinationCodeObserver)

        lifecycle.addObserver(vm)
    }

    private fun removeObservers() {
        val vm = viewModel as CompassViewModel
        vm.distanceString.removeObserver(distanceObserver)
        vm.nearbyString.removeObserver(nearbyObserver)
        vm.compassRotation.removeObserver(rotationObserver)
        vm.needleRotation.removeObserver(needleRotationObserver)
        vm.compassAndNeedleOpacity.removeObserver(opacityObserver)
        vm.targetName.removeObserver(destinationNameObeserver)
        vm.targetCode.removeObserver(destinationCodeObserver)

        lifecycle.removeObserver(vm)
    }


}
