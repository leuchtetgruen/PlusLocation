package de.leuchtetgruen.pluslocation.businessobjects.openlocationcode.extensions

import de.leuchtetgruen.pluslocation.businessobjects.WGS84Coordinates
import de.leuchtetgruen.pluslocation.businessobjects.openlocationcode.CodeArea

fun CodeArea.center() : WGS84Coordinates = WGS84Coordinates(this.centerLatitude, this.centerLongitude)

fun CodeArea.accuracyInMeters() : Double {
    val northWest = WGS84Coordinates(getNorthLatitude(), getWestLongitude())
    return center().distanceInMeters(northWest)
}


fun CodeArea.contains(coordinate : WGS84Coordinates) : Boolean {
    val minLat = Math.min(getSouthLatitude(), getNorthLatitude())
    val maxLat = Math.max(getSouthLatitude(), getNorthLatitude())
    val minLon = Math.min(getEastLongitude(), getWestLongitude())
    val maxLon = Math.max(getEastLongitude(), getWestLongitude())

    return ((minLat <= coordinate.latitude) &&
            (maxLat >= coordinate.latitude) &&
            (minLon <= coordinate.longitude) &&
            (maxLon >= coordinate.longitude))
}