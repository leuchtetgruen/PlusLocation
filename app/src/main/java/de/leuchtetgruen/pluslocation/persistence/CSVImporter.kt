package de.leuchtetgruen.pluslocation.persistence

import android.app.Activity
import com.opencsv.CSVReader
import de.leuchtetgruen.pluslocation.businessobjects.POI
import kotlinx.coroutines.experimental.async
import java.io.Reader

class CSVImporter(reader: Reader, val activity: Activity) {

    private val csvReader = CSVReader(reader)
    private var running = false

    init {
        POIDatabase.build(activity)
    }

    fun import(progress : (count : Int) -> Unit, done : () -> Unit) {
        async {
            var i = 0
            running = true
            csvReader.iterator().forEach {
                val poi = csvLineToPOI(it)
                if (poi != null) {
                    i++
                    POIDatabase.dao().insertPoi(poi)
                }

                if ((i % 100) == 0) {
                    activity.runOnUiThread { progress(i) }
                }
            }
            activity.runOnUiThread(done)
            running = false
        }

    }

    fun isRunning() : Boolean = running

    private fun csvLineToPOI(it: Array<String>): POI? {
        return if (it.size < 2) {
            null
        }
        else if (it.size == 2) {
            POI(it[0], it[1])
        }
        else {
            POI(it[0], it[1], it[2].toInt() == 1)
        }
    }
}