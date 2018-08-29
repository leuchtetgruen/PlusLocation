package de.leuchtetgruen.pluslocation.ui.activities

import android.Manifest
import android.app.SearchManager
import android.arch.lifecycle.Observer
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.google.android.gms.location.LocationListener
import de.leuchtetgruen.pluslocation.R
import de.leuchtetgruen.pluslocation.businessobjects.POI
import de.leuchtetgruen.pluslocation.businessobjects.POINetworkFile
import de.leuchtetgruen.pluslocation.businessobjects.WGS84Coordinates
import de.leuchtetgruen.pluslocation.businessobjects.openlocationcode.OpenLocationCode
import de.leuchtetgruen.pluslocation.businessobjects.openlocationcode.extensions.center
import de.leuchtetgruen.pluslocation.businessobjects.openlocationcode.extensions.contains
import de.leuchtetgruen.pluslocation.helpers.LocationProviderTask
import de.leuchtetgruen.pluslocation.helpers.ui.PermissionActivity
import de.leuchtetgruen.pluslocation.network.POINetworkDatasource
import de.leuchtetgruen.pluslocation.persistence.CSVImporter
import de.leuchtetgruen.pluslocation.persistence.SavedCode
import de.leuchtetgruen.pluslocation.ui.adapters.PoiListAdapter
import de.leuchtetgruen.pluslocation.ui.viewmodels.PoiListViewModel
import kotlinx.android.synthetic.main.activity_poi_list.*


class PoiListActivity : PermissionActivity(), LocationListener, PermissionActivity.PermissionListener {

    companion object {
        fun intentTo(context : Context) : Intent {

            return Intent(context, PoiListActivity::class.java)
        }

        fun componentName(context: Context): ComponentName? = ComponentName(context, PoiListActivity::class.java)

        const val REQUEST_CODE = 42
    }

    private val viewModel by lazy { PoiListViewModel.create(this)}
    private val adapter = PoiListAdapter({
        if (it != null) {
            poiSelected(it)
        }

    })



    private lateinit var locationProviderTask: LocationProviderTask

    private var poiCountObserver: Observer<String> = Observer { txtSearchResultCount.text = it }
    private var searchResultObserver : Observer<List<POI>> = Observer {  adapter.setSearchResults(it) }
    private var availablePoiFilesObserver : Observer<List<POINetworkFile>> = Observer {
        updateButton(it)
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_poi_list)
        addObservers()
        startQuerying()

        rcvPOIs.layoutManager = LinearLayoutManager(this)
        rcvPOIs.adapter = adapter

        // in case of search
        val intent = intent
        if (Intent.ACTION_SEARCH == intent.action) {
            val query = intent.getStringExtra(SearchManager.QUERY)
            viewModel.query(query)
        }
    }




    private fun startQuerying() {
        locationProviderTask = LocationProviderTask(this, this)
        requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, this)
        POINetworkDatasource.queryLocations(this)
    }

    override fun onStop() {
        removeObservers()
        locationProviderTask.stop()
        super.onStop()
    }

    private fun addObservers() {
        viewModel.poiCountText.observe(this, poiCountObserver)
        viewModel.searchResults.observe(this, searchResultObserver)
        POINetworkDatasource.networkFiles.observe(this, availablePoiFilesObserver)
        lifecycle.addObserver(viewModel)
    }

    private fun removeObservers() {
        viewModel.poiCountText.removeObserver(poiCountObserver)
        viewModel.searchResults.removeObserver(searchResultObserver)
        POINetworkDatasource.networkFiles.removeObserver(availablePoiFilesObserver)
    }

    private fun poiSelected(poi: POI) {
        if (poi != null) {
            SavedCode.changedCode(poi.code, poi.name, this)
            finish()
        }
    }

    fun importCSV(v : View) {
        if (POINetworkDatasource.networkFiles.value == null) return

        val file = poiFileForCurrentLocation(POINetworkDatasource.networkFiles.value!!).first() ?: return
        val act = this
        file.buildReader {
            Looper.prepare()
            val csvImporter = CSVImporter(it, act)
            btnImport.isEnabled = false
            csvImporter.import({
                btnImport.text = String.format("%d / %d imported... (%d errors)", it, file.itemCount, csvImporter.getErrorCount())
            }, {
                btnImport.text = "Done importing"
                btnImport.isEnabled = true

                viewModel.query("")
            })

        }



    }

    // interface methods
    override fun onLocationChanged(currentLocation: Location?) {
        if (currentLocation != null) {
            adapter.setCurrentPosition(WGS84Coordinates(currentLocation.latitude, currentLocation.longitude))
            viewModel.setCurrentLocationCode(OpenLocationCode(currentLocation.latitude, currentLocation.longitude))
        }

    }

    override fun permissionGranted(permission : String) {
        if (permission != Manifest.permission.ACCESS_FINE_LOCATION) return

        locationProviderTask.start()
    }

    override fun permissionNotGranted() {
    }

    private fun updateButton(networkFiles: List<POINetworkFile>?) {
        if (networkFiles == null) return
        val poisForLocationExist = poiFileForCurrentLocation(networkFiles).isNotEmpty()
        btnImport.isEnabled = poisForLocationExist
    }

    private fun poiFileForCurrentLocation(networkFiles: List<POINetworkFile>) : List<POINetworkFile> {
        val currentLocation = viewModel.getCurrentLocationCode().decode().center()
        return networkFiles.filter{
            it.area.contains(currentLocation)
        }
    }

}
