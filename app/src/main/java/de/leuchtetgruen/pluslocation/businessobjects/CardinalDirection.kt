package de.leuchtetgruen.pluslocation.businessobjects

enum class CardinalDirection {
    NORTH, NORTH_EAST, EAST, SOUTH_EAST, SOUTH, SOUTH_WEST, WEST, NORTH_WEST, UNKNOWN;

    override fun toString(): String {
        return when (this) {
            NORTH -> "N"
            NORTH_EAST -> "NE"
            EAST -> "E"
            SOUTH_EAST -> "SE"
            SOUTH -> "S"
            SOUTH_WEST -> "SW"
            WEST -> "W"
            NORTH_WEST -> "NW"
            UNKNOWN -> ""
        }
    }

    fun angle() : Int {
        return when (this) {
            NORTH -> 0
            NORTH_EAST -> 45
            EAST -> 90
            SOUTH_EAST -> 135
            SOUTH -> 180
            SOUTH_WEST -> 225
            WEST -> 270
            NORTH_WEST -> 315
            UNKNOWN -> 0
        }
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