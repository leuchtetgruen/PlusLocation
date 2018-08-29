package de.leuchtetgruen.pluslocation.network

import de.leuchtetgruen.pluslocation.businessobjects.POINetworkFile
import de.leuchtetgruen.pluslocation.businessobjects.WGS84Coordinates
import de.leuchtetgruen.pluslocation.businessobjects.openlocationcode.CodeArea
import de.leuchtetgruen.pluslocation.businessobjects.openlocationcode.extensions.contains
import kotlinx.coroutines.experimental.async
import org.json.JSONObject
import java.math.BigDecimal
import java.net.URL

class POINetworkDatasource {
    companion object {
        val BASE_URL = "http://leuchtetgruen.de/pluslocation/"
        val INDEX_URL = BASE_URL + "index.json"
    }

    fun findSourcesForLocation(location : WGS84Coordinates, callback: (files : List<POINetworkFile>) -> Unit) {
        allLocations {
            val filteredList = it.filter {
                it.area.contains(location)
            }
            callback(filteredList)
        }
    }

    fun allLocations(callback: (files : List<POINetworkFile>) -> Unit) {
        async {
            val jsonString = URL(INDEX_URL).readText()
            val jsonList = JSONObject(jsonString)
            val locations = mapJsonToNetworkPOIs(jsonList)
            callback(locations)
        }
    }

    private fun mapJsonToNetworkPOIs(jsonList: JSONObject): List<POINetworkFile> {
        val list = ArrayList<POINetworkFile>()
        jsonList.keys().forEach {
            val jsonArea = jsonList.getJSONObject(it)
            val area = mapJsonAreaToCodeArea(jsonArea)
            val networkFile = POINetworkFile(area, BASE_URL + it)
            list.add(networkFile)
        }
        return list
    }

    private fun mapJsonAreaToCodeArea(jsonArea: JSONObject): CodeArea {
        val latMin = BigDecimal(jsonArea.getDouble("lat_min"))
        val lonMin = BigDecimal(jsonArea.getDouble("lon_min"))
        val latMax = BigDecimal(jsonArea.getDouble("lat_max"))
        val lonMax = BigDecimal(jsonArea.getDouble("lon_max"))
        return CodeArea(latMax, lonMax, latMin, lonMin)
    }
}