package de.leuchtetgruen.pluslocation.businessobjects.openlocationcode.extensions

import de.leuchtetgruen.pluslocation.businessobjects.CardinalDirection
import de.leuchtetgruen.pluslocation.businessobjects.WGS84Coordinates
import de.leuchtetgruen.pluslocation.businessobjects.openlocationcode.OpenLocationCode

fun OpenLocationCode.regionCode() : String = this.code.substring(0, 4)

fun OpenLocationCode.zoneCode() : String = this.code.substring(0, 6)

fun OpenLocationCode.neighbourhoodCode() : String = this.code.substring(0, 8)

/**
 * will return first 8 letters of codes in the neighbourhood by simply going 300m in all directions
 */
fun OpenLocationCode.neighbourHoodCodes(desiredDistanceInMeters : Int = 300) : List<String> {
    val coordinate = decode().center()

    return listOf(
            coordinate.coordinateInDirection(desiredDistanceInMeters, CardinalDirection.NORTH),
            coordinate.coordinateInDirection(desiredDistanceInMeters, CardinalDirection.NORTH_EAST),
            coordinate.coordinateInDirection(desiredDistanceInMeters, CardinalDirection.EAST),
            coordinate.coordinateInDirection(desiredDistanceInMeters, CardinalDirection.SOUTH_EAST),
            coordinate.coordinateInDirection(desiredDistanceInMeters, CardinalDirection.SOUTH),
            coordinate.coordinateInDirection(desiredDistanceInMeters, CardinalDirection.SOUTH_WEST),
            coordinate.coordinateInDirection(desiredDistanceInMeters, CardinalDirection.WEST),
            coordinate.coordinateInDirection(desiredDistanceInMeters, CardinalDirection.NORTH_WEST)
    ).map { OpenLocationCode(it.latitude, it.longitude).neighbourhoodCode() }
}