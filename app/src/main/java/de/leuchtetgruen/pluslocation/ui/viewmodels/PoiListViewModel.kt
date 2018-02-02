package de.leuchtetgruen.pluslocation.ui.viewmodels

import android.app.Application
import android.arch.lifecycle.*
import de.leuchtetgruen.pluslocation.businessobjects.POI
import de.leuchtetgruen.pluslocation.businessobjects.WGS84Coordinates
import de.leuchtetgruen.pluslocation.helpers.Constants
import de.leuchtetgruen.pluslocation.persistence.POIDatabase
import de.leuchtetgruen.pluslocation.ui.activities.PoiListActivity

class PoiListViewModel(app: Application?) : AndroidViewModel(app!!), LifecycleObserver {

    companion object {
        fun create(activity: PoiListActivity): PoiListViewModel {
            return ViewModelProviders.of(activity).get(PoiListViewModel::class.java)
        }
    }

    private var searchResults : List<POI> = ArrayList()
    private var query : String = ""
    private var currentPosition : WGS84Coordinates = WGS84Coordinates(Constants.TV_TOWER_BLN_LAT, Constants.TV_TOWER_BLN_LON)

    val poiCountText : MutableLiveData<String> = MutableLiveData()

    fun setCurrentPosition(coordinate : WGS84Coordinates) {
        currentPosition = coordinate
    }


    private fun updateResultCount() {
        poiCountText.value = if (query.isEmpty()) {
            String.format("%d POIs in total", POIDatabase.dao().count())
        }
        else {
            String.format("%d POIs found", searchResults.size)
        }
    }


    // Lifecycle events
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun onResume() {
        POIDatabase.build(getApplication())
        updateResultCount()
    }


}