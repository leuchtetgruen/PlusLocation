package de.leuchtetgruen.pluslocation

import de.leuchtetgruen.pluslocation.businessobjects.openlocationcode.OpenLocationCode
import de.leuchtetgruen.pluslocation.businessobjects.openlocationcode.extensions.neighbourHoodCodes
import de.leuchtetgruen.pluslocation.businessobjects.openlocationcode.extensions.neighbourhoodCode
import de.leuchtetgruen.pluslocation.businessobjects.openlocationcode.extensions.regionCode
import de.leuchtetgruen.pluslocation.businessobjects.openlocationcode.extensions.zoneCode
import de.leuchtetgruen.pluslocation.helpers.Constants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OpenLocationCodeExtensionUnitTest {

    companion object {
        val olcTvTower = OpenLocationCode(Constants.TV_TOWER_BLN_CODE)
    }

    @Test
    fun region_code_is_right() {
        assertEquals(olcTvTower.regionCode(), "9F4M")
    }

    @Test
    fun zone_code_is_right() {
        assertEquals(olcTvTower.zoneCode(), "9F4MGC")
    }

    @Test
    fun neighbourhood_code_is_right() {
        assertEquals(olcTvTower.neighbourhoodCode(), "9F4MGCC5")
    }

    @Test
    fun neighbourhood_codes_is_complete() {
        val neighbourhoodCodes = olcTvTower.neighbourHoodCodes()
        assertTrue(listOf("9F4MGCC4", "9F4MGCC6",
                "9F4MGC95", "9F4MGCF5",
                "9F4MGC94", "9F4MGC96",
                "9F4MGCF4", "9F4MGCF6",
                "9F4MGCC5").all { neighbourhoodCodes.contains(it) })
    }

}