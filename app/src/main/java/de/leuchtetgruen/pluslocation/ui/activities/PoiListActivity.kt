package de.leuchtetgruen.pluslocation.ui.activities

import android.Manifest
import android.app.Activity
import android.app.SearchManager
import android.arch.lifecycle.Observer
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.google.android.gms.location.LocationListener
import de.leuchtetgruen.pluslocation.R
import de.leuchtetgruen.pluslocation.businessobjects.POI
import de.leuchtetgruen.pluslocation.businessobjects.WGS84Coordinates
import de.leuchtetgruen.pluslocation.businessobjects.openlocationcode.OpenLocationCode
import de.leuchtetgruen.pluslocation.helpers.LocationProviderTask
import de.leuchtetgruen.pluslocation.helpers.ui.PermissionActivity
import de.leuchtetgruen.pluslocation.persistence.CSVImporter
import de.leuchtetgruen.pluslocation.persistence.SavedCode
import de.leuchtetgruen.pluslocation.ui.adapters.PoiListAdapter
import de.leuchtetgruen.pluslocation.ui.viewmodels.PoiListViewModel
import kotlinx.android.synthetic.main.activity_poi_list.*
import java.io.BufferedReader
import java.io.InputStreamReader


class PoiListActivity : PermissionActivity(), LocationListener, PermissionActivity.PermissionListener {

    companion object {
        fun intentTo(context : Context) : Intent {
            val intent = Intent(context, PoiListActivity::class.java)

            return intent
        }

        fun componentName(context: Context): ComponentName? = ComponentName(context, PoiListActivity::class.java)

        val REQUEST_CODE = 42
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_poi_list)
        addObservers()
        startQueryingLocation()

        rcvPOIs.layoutManager = LinearLayoutManager(this)
        rcvPOIs.adapter = adapter

        // in case of search
        val intent = intent
        if (Intent.ACTION_SEARCH == intent.action) {
            val query = intent.getStringExtra(SearchManager.QUERY)
            viewModel.query(query)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                val uri = resultData.getData()
                val inputStream = contentResolver.openInputStream(uri)
                val reader = BufferedReader(InputStreamReader(inputStream))
                btnImport.text = "Reading..."
                val importer = CSVImporter(reader, this)
                importer.import(
                        { btnImport.text = String.format("%d Entries...", it) },
                        {
                            btnImport.text = "Done"
                            viewModel.reload()
                        }
                )
            }
        }
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
        viewModel.searchResults.observe(this, searchResultObserver)
        lifecycle.addObserver(viewModel)
    }

    private fun removeObservers() {
        viewModel.poiCountText.removeObserver(poiCountObserver)
        viewModel.searchResults.removeObserver(searchResultObserver)
    }

    private fun poiSelected(poi: POI) {
        if (poi != null) {
            SavedCode.changedCode(poi.code, poi.name, this)
            finish()
        }
    }

    fun importCSV(v : View) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)

        intent.addCategory(Intent.CATEGORY_OPENABLE)

        intent.type = "text/csv"

        startActivityForResult(Intent.createChooser(intent, "Open CSV"), REQUEST_CODE)
    }

    // interface methods
    override fun onLocationChanged(currentLocation: Location?) {
        if (currentLocation != null) {
            adapter.setCurrentPosition(WGS84Coordinates(currentLocation.latitude, currentLocation.longitude))
            viewModel.setCurrentLocationCode(OpenLocationCode(currentLocation.latitude, currentLocation.longitude))
        }

    }

    override fun permissionGranted() {
        locationProviderTask.start()
    }

    override fun permissionNotGranted() {
    }



}
