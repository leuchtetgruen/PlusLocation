package de.leuchtetgruen.pluslocation.businessobjects

import de.leuchtetgruen.pluslocation.businessobjects.openlocationcode.CodeArea

data class POINetworkFile(val area: CodeArea, val filename: String) {
}