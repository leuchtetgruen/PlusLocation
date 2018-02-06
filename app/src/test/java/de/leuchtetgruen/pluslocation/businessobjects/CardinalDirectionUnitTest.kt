package de.leuchtetgruen.pluslocation.businessobjects

import org.junit.Assert.assertEquals
import org.junit.Test

class CardinalDirectionUnitTest {
    companion object {
        val testCardinalDirection = CardinalDirection.NORTH_EAST
    }

    @Test
    fun description_is_right() {
        assertEquals(testCardinalDirection.toString(), "NE")
    }

    @Test
    fun angle_is_right() {
        assertEquals(testCardinalDirection.angle(), 45)
    }

    @Test
    fun converts_angle_to_cd_right() {
        assertEquals(CardinalDirection.fromBearing(45.0), CardinalDirection.NORTH_EAST)
    }

}