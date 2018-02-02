package de.leuchtetgruen.pluslocation.persistence

import android.arch.persistence.room.Room
import android.content.Context
import android.util.Log
import de.leuchtetgruen.pluslocation.businessobjects.POI
import de.leuchtetgruen.pluslocation.businessobjects.openlocationcode.OpenLocationCode
import de.leuchtetgruen.pluslocation.businessobjects.openlocationcode.extensions.center
import de.leuchtetgruen.pluslocation.businessobjects.openlocationcode.extensions.zoneCode
import java.io.Reader


object POIDatabase {

    private lateinit var appDatabase: AppDatabase

    fun build(ctx: Context) {
        appDatabase = Room.databaseBuilder(ctx, AppDatabase::class.java, "pois.db").allowMainThreadQueries().build()
    }

    fun dao(): POIDao = appDatabase.poiDao()

    fun poisNear(plusCode: OpenLocationCode): List<POI> {
        val coordinate = plusCode.decode().center()

        val zoneCode = plusCode.zoneCode()

        val entriesInZone = dao().nearby(zoneCode + "%")
        val distanceEntriesInZone = entriesInZone.sortedBy { it.coordinate().distanceInMeters(coordinate) }


        val distinctDirectionEntries = ArrayList<POI>()


        distanceEntriesInZone.forEach {
            val direction = it.coordinate().direction(coordinate)
            if (!distinctDirectionEntries.any { it.coordinate().direction(coordinate) == direction }) {
                distinctDirectionEntries.add(it)
            }
        }

        return distinctDirectionEntries.take(3)
    }

    fun importFromCSV(reader : Reader) {
        var csvMapper = CSVMapper(reader)
        var dao = dao()
        Log.i("POIDatabase#import", "start")
        csvMapper.access {
            Log.i("POIDatabase#import", "hasData")
            it.forEach({
                dao.insertPoi(it)
            })
            Log.i("POIDatabase#import", "done")
        }
    }
}