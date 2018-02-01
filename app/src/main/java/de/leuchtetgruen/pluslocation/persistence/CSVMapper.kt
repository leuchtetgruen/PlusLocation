package de.leuchtetgruen.pluslocation.persistence

import com.opencsv.CSVReader
import de.leuchtetgruen.pluslocation.businessobjects.POI
import kotlinx.coroutines.experimental.async
import java.io.Reader

class CSVMapper(private val reader: Reader) {
    var allEntries : List<POI?> = ArrayList()

    var loadingThread = Thread()

    init {
        loadingThread = Thread {
            val csvReader = CSVReader(reader)

            val allEntriesStrArr = csvReader.readAll()
            allEntries = allEntriesStrArr.map { csvLineToPOI(it) }
        }

        loadingThread.start()
    }

    fun access(callback: (list : List<POI>) -> Unit) {
        async {
            if ((allEntries.size == 0) && (loadingThread.isAlive)) {
                loadingThread.join()
            }

            callback(allEntries.filterNotNull())
        }
    }

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