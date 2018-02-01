package de.leuchtetgruen.pluslocation.businessobjects

import org.gavaghan.geodesy.Ellipsoid
import org.gavaghan.geodesy.GeodeticCalculator
import org.gavaghan.geodesy.GlobalCoordinates

class WGS84Coordinates(latitude: Double, longitude: Double) : GlobalCoordinates(latitude, longitude) {
    companion object {

        private val WGS_84 = Ellipsoid.WGS84
        private val CALCULATOR = GeodeticCalculator()
    }


    enum class CardinalDirection {
        NORTH, NORTH_EAST, EAST, SOUTH_EAST, SOUTH, SOUTH_WEST, WEST, NORTH_WEST, UNKNOWN;

        override fun toString(): String {
            when (this) {
                NORTH -> return "N"
                NORTH_EAST -> return "NE"
                EAST -> return "E"
                SOUTH_EAST -> return "SE"
                SOUTH -> return "S"
                SOUTH_WEST -> return "SW"
                WEST -> return "W"
                NORTH_WEST -> return "NW"
            }
            return ""
        }

        companion object {

            private fun bearingInRange(min: Double, max: Double, bearing: Double): Boolean {
                return bearing >= min && bearing < max
            }

            fun fromBearing(bearing: Double): CardinalDirection {
                return if (bearing > 337.5 || bearing < 22.5) {
                    NORTH
                } else if (bearingInRange(22.5, 67.5, bearing)) {
                    NORTH_EAST
                } else if (bearingInRange(67.5, 112.5, bearing)) {
                    EAST
                } else if (bearingInRange(112.5, 157.5, bearing)) {
                    SOUTH_EAST
                } else if (bearingInRange(157.5, 202.5, bearing)) {
                    SOUTH
                } else if (bearingInRange(202.5, 247.5, bearing)) {
                    SOUTH_WEST
                } else if (bearingInRange(247.5, 292.5, bearing)) {
                    WEST
                } else if (bearingInRange(292.5, 337.5, bearing)) {
                    NORTH_WEST
                } else
                    UNKNOWN
            }
        }
    }

    fun distanceInMeters(otherCoordinate: WGS84Coordinates): Double {
        val curve = CALCULATOR.calculateGeodeticCurve(WGS_84, this, otherCoordinate)

        return curve.ellipsoidalDistance
    }

    fun bearingInRadians(otherCoordinate: WGS84Coordinates): Double {
        val srcLat = Math.toRadians(this.latitude)
        val dstLat = Math.toRadians(otherCoordinate.latitude)
        val dLng = Math.toRadians(otherCoordinate.longitude - this.longitude)

        return Math.atan2(Math.sin(dLng) * Math.cos(dstLat), Math.cos(srcLat) * Math.sin(dstLat) - Math.sin(srcLat) * Math.cos(dstLat) * Math.cos(dLng))
    }

    fun bearingInDegrees(otherCoordinate: WGS84Coordinates): Double {
        val rad = bearingInRadians(otherCoordinate)
        return (Math.toDegrees(rad) + 360) % 360
    }

    fun direction(otherCoordinate: WGS84Coordinates): CardinalDirection {
        return CardinalDirection.fromBearing(bearingInDegrees(otherCoordinate))
    }

    override fun equals(other: Any?): Boolean {
        if (other !is WGS84Coordinates) return false

        return ((other.latitude == this.latitude) &&
                (other.longitude == this.longitude))

    }


}