package de.leuchtetgruen.pluslocation.network

import android.app.Activity
import android.arch.lifecycle.MutableLiveData
import de.leuchtetgruen.pluslocation.businessobjects.POINetworkFile
import de.leuchtetgruen.pluslocation.businessobjects.openlocationcode.CodeArea
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
//import kotlinx.coroutines.experimental.async
import org.json.JSONObject
import java.math.BigDecimal
import java.net.URL

object POINetworkDatasource {

        val BASE_URL = "http://leuchtetgruen.de/pluslocation/"
        val INDEX_URL = BASE_URL + "index.json"


    val networkFiles : MutableLiveData<List<POINetworkFile>> = MutableLiveData()


    fun queryLocations(activity: Activity) {
        CoroutineScope(Dispatchers.Default).async  {
            val jsonString = URL(INDEX_URL).readText()
            val jsonList = JSONObject(jsonString)
            val locations = mapJsonToNetworkPOIs(jsonList)
            activity.runOnUiThread {
                networkFiles.setValue(locations)
            }
        }
    }

    private fun mapJsonToNetworkPOIs(jsonList: JSONObject): List<POINetworkFile> {
        val list = ArrayList<POINetworkFile>()
        jsonList.keys().forEach {
            val jsoNObj = jsonList.getJSONObject(it)
            val area = mapJsonAreaToCodeArea(jsoNObj)
            val itemCount = jsoNObj.getInt("size")
            val networkFile = POINetworkFile(area, BASE_URL + it, itemCount)
            list.add(networkFile)
        }
        return list
    }

    private fun mapJsonAreaToCodeArea(jsonArea: JSONObject): CodeArea {
        val latMin = BigDecimal(jsonArea.getDouble("lat_min"))
        val lonMin = BigDecimal(jsonArea.getDouble("lon_min"))
        val latMax = BigDecimal(jsonArea.getDouble("lat_max"))
        val lonMax = BigDecimal(jsonArea.getDouble("lon_max"))
        //TODO dont use codearea but a more fitting model - bounding box
        return CodeArea(latMax, lonMax, latMin, lonMin)
    }
}