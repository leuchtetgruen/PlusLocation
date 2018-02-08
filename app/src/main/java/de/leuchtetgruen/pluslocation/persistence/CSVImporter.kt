package de.leuchtetgruen.pluslocation.persistence

import android.app.Activity
import com.opencsv.CSVReader
import de.leuchtetgruen.pluslocation.businessobjects.POI
import de.leuchtetgruen.pluslocation.businessobjects.openlocationcode.OpenLocationCode
import kotlinx.coroutines.experimental.async
import java.io.Reader

class CSVImporter(reader: Reader, private val activity: Activity) {

    private val csvReader = CSVReader(reader)
    private var running = false
    private var errorCounter = 0

    init {
        POIDatabase.build(activity)
    }

    fun import(progress : (count : Int) -> Unit, done : () -> Unit) {
        try {
            async {
                var i = 0
                running = true
                csvReader.iterator().forEach {
                    val poi = csvLineToPOI(it)
                    if (poi != null) {
                        i++
                        POIDatabase.dao().insertPoi(poi)
                    }
                    else {
                        errorCounter++
                    }

                    if ((i % 100) == 0) {
                        activity.runOnUiThread { progress(i) }
                    }
                }
                activity.runOnUiThread(done)
                running = false
            }
        } catch (e: Exception) {
            errorCounter++
            activity.runOnUiThread(done)
            running = false
        }


    }

    fun isRunning() : Boolean = running

    fun getErrorCount() : Int = errorCounter

    private fun csvLineToPOI(values: Array<String>): POI? {
        if (values.size < 2) {
            return null
        }

        val code = values[0]

        if (!OpenLocationCode.isValidCode(code)) {
            return null
        }

        val name = values[1]
        val isUseableForNearby = if (values.size > 2) {
            try {
                values[2].toInt() == 1
            } catch (e : NumberFormatException) {
                false
            }

        }
        else {
            false
        }

        return POI(code, name, isUseableForNearby)
    }
}