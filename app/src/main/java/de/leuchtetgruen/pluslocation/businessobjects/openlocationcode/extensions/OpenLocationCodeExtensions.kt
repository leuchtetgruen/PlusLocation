package de.leuchtetgruen.pluslocation.businessobjects.openlocationcode.extensions

import de.leuchtetgruen.pluslocation.businessobjects.openlocationcode.OpenLocationCode

fun OpenLocationCode.regionCode() : String = this.code.substring(0, 4)

fun OpenLocationCode.zoneCode() : String = this.code.substring(0, 6)