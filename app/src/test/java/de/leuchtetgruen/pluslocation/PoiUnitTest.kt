package de.leuchtetgruen.pluslocation

import de.leuchtetgruen.pluslocation.businessobjects.POI
import de.leuchtetgruen.pluslocation.businessobjects.WGS84Coordinates
import de.leuchtetgruen.pluslocation.helpers.Constants
import org.junit.Assert.*
import org.junit.Test


class PoiUnitTest {

    companion object {
        val tvTowerPOI = POI(Constants.TV_TOWER_BLN_CODE, Constants.TV_TOWER_BLN_NAME, true)
    }

    @Test
    fun test_Comparison_detects_correct_match() {
        assertTrue(tvTowerPOI.equals(POI(Constants.TV_TOWER_BLN_CODE, Constants.TV_TOWER_BLN_NAME, false)))
    }

    @Test
    fun test_Comparison_detects_different_code() {
        assertFalse(tvTowerPOI.equals(POI("DIFCODE", Constants.TV_TOWER_BLN_NAME, false)))
    }

    @Test
    fun test_Comparison_detects_different_name() {
        assertFalse(tvTowerPOI.equals(POI(Constants.TV_TOWER_BLN_CODE, "Not the TV tower", false)))
    }

    @Test
    fun test_calculates_coordinate_correctly() {
        assertEquals(tvTowerPOI.coordinate(), WGS84Coordinates(Constants.TV_TOWER_BLN_LAT, Constants.TV_TOWER_BLN_LON))
    }
}