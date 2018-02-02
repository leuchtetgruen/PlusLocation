package de.leuchtetgruen.pluslocation.ui.viewmodels

import android.app.Application
import android.arch.lifecycle.*
import de.leuchtetgruen.pluslocation.businessobjects.POI
import de.leuchtetgruen.pluslocation.businessobjects.openlocationcode.OpenLocationCode
import de.leuchtetgruen.pluslocation.businessobjects.openlocationcode.extensions.center
import de.leuchtetgruen.pluslocation.helpers.Constants
import de.leuchtetgruen.pluslocation.persistence.POIDatabase
import de.leuchtetgruen.pluslocation.ui.activities.PoiListActivity

class PoiListViewModel(app: Application?) : AndroidViewModel(app!!), LifecycleObserver {

    companion object {
        fun create(activity: PoiListActivity): PoiListViewModel {
            return ViewModelProviders.of(activity).get(PoiListViewModel::class.java)
        }

        val MIN_METERS_MOVED_FOR_REQUERYING = 20
    }

    private var currentLocationCode : OpenLocationCode = OpenLocationCode(Constants.TV_TOWER_BLN_CODE)
    private var query : String = ""


    val poiCountText : MutableLiveData<String> = MutableLiveData()
    val searchResults : MutableLiveData<List<POI>> = MutableLiveData()


    fun query(q : String) {
        this.query = q
        val currentLocation = currentLocationCode.decode().center()


        searchResults.value = if (query.isEmpty()) {
            POIDatabase.nearbyPois(currentLocationCode)
        }
        else {
            POIDatabase.dao().query("%$query%")
        }.sortedBy { currentLocation.distanceInMeters(it.coordinate()) }


        val count = searchResults.value?.size ?: 0
        poiCountText.value = String.format("%d POIs nearby", count)
    }

    fun setCurrentLocationCode(code : OpenLocationCode) {
        if (code.decode().center().distanceInMeters(currentLocationCode.decode().center()) > MIN_METERS_MOVED_FOR_REQUERYING) {
            this.currentLocationCode = code
            query(query)
        }
    }




    // Lifecycle events
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun onResume() {
        POIDatabase.build(getApplication())
    }


}