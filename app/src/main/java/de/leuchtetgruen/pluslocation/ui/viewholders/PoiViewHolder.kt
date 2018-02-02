package de.leuchtetgruen.pluslocation.ui.viewholders

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import de.leuchtetgruen.pluslocation.businessobjects.POI
import de.leuchtetgruen.pluslocation.businessobjects.WGS84Coordinates
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.poi_listitem.*

class PoiViewHolder(override val containerView : View?, clickHandler : (POI?) -> Unit) : RecyclerView.ViewHolder(containerView), LayoutContainer {
    val context : Context = containerView!!.context
    var curPoi : POI? = null

    init {
        containerView?.setOnClickListener({
            clickHandler(curPoi)
        })
    }

    fun update(poi: POI, curCoordinates : WGS84Coordinates) {
        curPoi = poi
        txtPOICode.text = poi.code
        txtPOIName.text = poi.name

        val distanceInKm = (curCoordinates.distanceInMeters(poi.coordinate()) / 1000).toFloat()
        txtDistanceSmall.text = String.format("%.1f km", distanceInKm)
    }
}