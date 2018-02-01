package de.leuchtetgruen.pluslocation.businessobjects.openlocationcode.extensions

import de.leuchtetgruen.pluslocation.businessobjects.WGS84Coordinates
import de.leuchtetgruen.pluslocation.businessobjects.openlocationcode.CodeArea

fun CodeArea.center() : WGS84Coordinates = WGS84Coordinates(this.centerLatitude, this.centerLongitude)

fun CodeArea.accuracyInMeters() : Double {
    val northWest = WGS84Coordinates(getNorthLatitude(), getWestLongitude())
    return center().distanceInMeters(northWest)
}

fun CodeArea.contains(coordinate : WGS84Coordinates) : Boolean {
    val northWest = WGS84Coordinates(getNorthLatitude(), getWestLongitude())
    val southEast = WGS84Coordinates(getSouthLatitude(), getEastLongitude())

    return ((northWest.latitude <= coordinate.latitude) &&
            (southEast.latitude >= coordinate.latitude) &&
            (northWest.longitude <= coordinate.longitude) &&
            (southEast.longitude >= coordinate.longitude))
}