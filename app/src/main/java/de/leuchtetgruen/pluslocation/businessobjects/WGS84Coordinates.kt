package de.leuchtetgruen.pluslocation.businessobjects

import org.gavaghan.geodesy.Ellipsoid
import org.gavaghan.geodesy.GeodeticCalculator
import org.gavaghan.geodesy.GlobalCoordinates

class WGS84Coordinates(latitude: Double, longitude: Double) : GlobalCoordinates(latitude, longitude) {
    companion object {
        private val WGS_84 = Ellipsoid.WGS84
        private val CALCULATOR = GeodeticCalculator()
    }


    fun distanceInMeters(otherCoordinate: WGS84Coordinates): Double {
        val curve = CALCULATOR.calculateGeodeticCurve(WGS_84, this, otherCoordinate)

        return curve.ellipsoidalDistance
    }

    fun bearingInDegrees(otherCoordinate: WGS84Coordinates): Double {
        val rad = bearingInRadians(otherCoordinate)
        return (Math.toDegrees(rad) + 360) % 360
    }

    fun direction(otherCoordinate: WGS84Coordinates): CardinalDirection {
        return CardinalDirection.fromBearing(bearingInDegrees(otherCoordinate))
    }

    fun coordinateInDirection(distanceInMeters : Double, angle : Double) : WGS84Coordinates {
        val angleInRad = Math.toRadians(angle)

        val dLatInMeters = Math.cos(angleInRad) * distanceInMeters
        val dLonInMeters = Math.sin(angleInRad) * distanceInMeters

        /*
         * Approximation:
         * Delta of 1° in Longitude = 111111 Meters
         * Delta of 1° in Latitude = 111111 Meters * cos(lat)
         */
        val dLatInDegrees = dLatInMeters / (111111 * Math.cos(latitude))
        val dLonInDegrees = dLonInMeters / 111111

        return WGS84Coordinates(latitude + dLatInDegrees , longitude + dLonInDegrees)
    }

    private fun bearingInRadians(otherCoordinate: WGS84Coordinates): Double {
        val srcLat = Math.toRadians(this.latitude)
        val dstLat = Math.toRadians(otherCoordinate.latitude)
        val dLng = Math.toRadians(otherCoordinate.longitude - this.longitude)

        return Math.atan2(Math.sin(dLng) * Math.cos(dstLat), Math.cos(srcLat) * Math.sin(dstLat) - Math.sin(srcLat) * Math.cos(dstLat) * Math.cos(dLng))
    }

    override fun equals(other: Any?): Boolean {
        if (other !is WGS84Coordinates) return false

        return ((other.latitude == this.latitude) &&
                (other.longitude == this.longitude))
    }

    override fun hashCode(): Int {
        return "$latitude$longitude".hashCode()
    }




}