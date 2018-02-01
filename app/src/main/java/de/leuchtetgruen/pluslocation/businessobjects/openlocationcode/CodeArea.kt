package de.leuchtetgruen.pluslocation.businessobjects.openlocationcode

import java.math.BigDecimal

/**
 * Coordinates of a decoded Open Location Code.
 *
 *
 * The coordinates include the latitude and longitude of the lower left and upper right corners
 * and the center of the bounding box for the area the code represents.
 */
open class CodeArea(
        private val southLatitude: BigDecimal,
        private val westLongitude: BigDecimal,
        private val northLatitude: BigDecimal,
        private val eastLongitude: BigDecimal) {

    val latitudeHeight: Double
        get() = northLatitude.subtract(southLatitude).toDouble()

    val longitudeWidth: Double
        get() = eastLongitude.subtract(westLongitude).toDouble()

    val centerLatitude: Double
        get() = southLatitude.add(northLatitude).toDouble() / 2

    val centerLongitude: Double
        get() = westLongitude.add(eastLongitude).toDouble() / 2

    fun getSouthLatitude(): Double {
        return southLatitude.toDouble()
    }

    fun getWestLongitude(): Double {
        return westLongitude.toDouble()
    }

    fun getNorthLatitude(): Double {
        return northLatitude.toDouble()
    }

    fun getEastLongitude(): Double {
        return eastLongitude.toDouble()
    }
}