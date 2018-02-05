package de.leuchtetgruen.pluslocation.businessobjects.openlocationcode.extensions

import de.leuchtetgruen.pluslocation.businessobjects.CardinalDirection
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
            coordinate.coordinateInDirection(desiredDistanceInMeters, CardinalDirection.NORTH.angle().toDouble()),
            coordinate.coordinateInDirection(desiredDistanceInMeters, CardinalDirection.NORTH_EAST.angle().toDouble()),
            coordinate.coordinateInDirection(desiredDistanceInMeters, CardinalDirection.EAST.angle().toDouble()),
            coordinate.coordinateInDirection(desiredDistanceInMeters, CardinalDirection.SOUTH_EAST.angle().toDouble()),
            coordinate.coordinateInDirection(desiredDistanceInMeters, CardinalDirection.SOUTH.angle().toDouble()),
            coordinate.coordinateInDirection(desiredDistanceInMeters, CardinalDirection.SOUTH_WEST.angle().toDouble()),
            coordinate.coordinateInDirection(desiredDistanceInMeters, CardinalDirection.WEST.angle().toDouble()),
            coordinate.coordinateInDirection(desiredDistanceInMeters, CardinalDirection.NORTH_WEST.angle().toDouble())
    ).map { OpenLocationCode(it.latitude, it.longitude).neighbourhoodCode() }
}