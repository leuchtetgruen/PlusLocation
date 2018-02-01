package de.leuchtetgruen.pluslocation.businessobjects

import de.leuchtetgruen.pluslocation.businessobjects.openlocationcode.OpenLocationCode
import de.leuchtetgruen.pluslocation.businessobjects.openlocationcode.extensions.center

data class POI(val plusCode : OpenLocationCode, val name : String) {

    var calcCoordinate : WGS84Coordinates? = null

    fun coordinate() : WGS84Coordinates {
        if (calcCoordinate == null) {
            calcCoordinate = plusCode.decode().center()
        }
        return calcCoordinate!!
    }

    fun relativePositionDescription(currentCoordinate: WGS84Coordinates): String {
        return name + "(" + String.format("%.2f km", currentCoordinate.distanceInMeters(coordinate()) / 1000) + " " + currentCoordinate.direction(coordinate()).toString() +")"
    }


}