package de.leuchtetgruen.pluslocation.ui.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.leuchtetgruen.pluslocation.R
import de.leuchtetgruen.pluslocation.businessobjects.POI
import de.leuchtetgruen.pluslocation.businessobjects.WGS84Coordinates
import de.leuchtetgruen.pluslocation.helpers.Constants
import de.leuchtetgruen.pluslocation.ui.viewholders.PoiViewHolder

class PoiListAdapter(private val clickHandler : (POI?) -> Unit) : RecyclerView.Adapter<PoiViewHolder>() {

    private var poiList : List<POI> = ArrayList()
    private var currentPosition : WGS84Coordinates = WGS84Coordinates(Constants.TV_TOWER_BLN_LAT, Constants.TV_TOWER_BLN_LON)

    fun setCurrentPosition(coordinate : WGS84Coordinates) {
        currentPosition = coordinate
    }

    fun setSearchResults(results: List<POI>?) {
        if (results != null) {
            poiList = results
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): PoiViewHolder {
        val view: View = LayoutInflater.from(parent?.context).inflate(R.layout.poi_listitem, parent, false)
        return PoiViewHolder(view, clickHandler)
    }

    override fun getItemCount(): Int = poiList.size

    override fun onBindViewHolder(holder: PoiViewHolder?, position: Int) {
        holder?.update(poiList[position], currentPosition)
    }


}