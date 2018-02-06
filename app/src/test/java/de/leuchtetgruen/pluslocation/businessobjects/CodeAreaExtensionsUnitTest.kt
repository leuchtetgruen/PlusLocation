package de.leuchtetgruen.pluslocation.businessobjects

import de.leuchtetgruen.pluslocation.businessobjects.openlocationcode.OpenLocationCode
import de.leuchtetgruen.pluslocation.businessobjects.openlocationcode.extensions.accuracyInMeters
import de.leuchtetgruen.pluslocation.businessobjects.openlocationcode.extensions.center
import de.leuchtetgruen.pluslocation.businessobjects.openlocationcode.extensions.contains
import de.leuchtetgruen.pluslocation.helpers.Constants
import org.junit.Assert.assertEquals
import org.junit.Test

class CodeAreaExtensionsUnitTest {
    companion object {
        // this also depends on that OpenLocationCode#decode() works correctly -
        // maybe build it directly?
        val codeAreaTvTower = OpenLocationCode(Constants.TV_TOWER_BLN_CODE).decode()
    }

    @Test
    fun accuracy_works() {
        assertEquals(codeAreaTvTower.accuracyInMeters().toInt(), 8)
    }

    @Test
    fun contains_works() {
        assertEquals(codeAreaTvTower.contains(codeAreaTvTower.center()), true)
    }
}