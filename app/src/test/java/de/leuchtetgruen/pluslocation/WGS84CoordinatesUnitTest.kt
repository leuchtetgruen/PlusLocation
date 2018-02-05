package de.leuchtetgruen.pluslocation

import de.leuchtetgruen.pluslocation.businessobjects.CardinalDirection
import de.leuchtetgruen.pluslocation.businessobjects.WGS84Coordinates
import de.leuchtetgruen.pluslocation.helpers.Constants
import org.junit.Assert.assertEquals
import org.junit.Test

class WGS84CoordinatesUnitTest {
    companion object {
        val COORDS_TV_TOWER = WGS84Coordinates(Constants.TV_TOWER_BLN_LAT, Constants.TV_TOWER_BLN_LON)
        val COORDS_TEUFELSBERG = WGS84Coordinates( 52.497222, 13.241111)
    }

    @Test
    fun measuring_distances_works() {
        assertEquals(COORDS_TV_TOWER.distanceInMeters(COORDS_TEUFELSBERG).toInt(), 11726)
    }

    @Test
    fun calculating_bearing_works() {
        assertEquals(COORDS_TV_TOWER.bearingInDegrees(COORDS_TEUFELSBERG).toInt(), 257)
    }

    @Test
    fun calculating_direction_works() {
        assertEquals(COORDS_TV_TOWER.direction(COORDS_TEUFELSBERG), CardinalDirection.WEST)
    }
}