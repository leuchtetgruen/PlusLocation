package de.leuchtetgruen.pluslocation

import de.leuchtetgruen.pluslocation.businessobjects.WGS84Coordinates
import de.leuchtetgruen.pluslocation.businessobjects.openlocationcode.OpenLocationCode
import de.leuchtetgruen.pluslocation.businessobjects.openlocationcode.extensions.center
import de.leuchtetgruen.pluslocation.businessobjects.openlocationcode.extensions.contains
import de.leuchtetgruen.pluslocation.helpers.Constants
import org.junit.Test

import org.junit.Assert.*

class OpenLocationCodeUnitTest {
    companion object {
        val COORD_TV_TOWER = WGS84Coordinates(Constants.TV_TOWER_BLN_LAT, Constants.TV_TOWER_BLN_LON)
        val OLC_TV_TOWER = OpenLocationCode(Constants.TV_TOWER_BLN_CODE)
    }

    @Test
    fun convertingLatLng_toOLC_isCorrect() = assertEquals(OLC_TV_TOWER.code, Constants.TV_TOWER_BLN_CODE)

    @Test
    fun convertOLC_toOLC_isCorrect() = assertEquals(OLC_TV_TOWER.decode().center(), COORD_TV_TOWER)

}
