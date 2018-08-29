package de.leuchtetgruen.pluslocation.businessobjects

import de.leuchtetgruen.pluslocation.businessobjects.openlocationcode.CodeArea
import kotlinx.coroutines.experimental.async
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

data class POINetworkFile(val area: CodeArea, val url: String) {

    fun buildReader(callback : (reader : BufferedReader) -> Unit) {
        async {
            val urlObject = URL(url)
            val reader = BufferedReader(InputStreamReader(urlObject.openStream()))
            callback(reader)
        }
    }

}