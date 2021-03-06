package de.leuchtetgruen.pluslocation.persistence

import android.arch.persistence.room.Room
import android.content.Context
import de.leuchtetgruen.pluslocation.businessobjects.POI
import de.leuchtetgruen.pluslocation.businessobjects.openlocationcode.OpenLocationCode
import de.leuchtetgruen.pluslocation.businessobjects.openlocationcode.extensions.center
import de.leuchtetgruen.pluslocation.businessobjects.openlocationcode.extensions.neighbourHoodCodes
import de.leuchtetgruen.pluslocation.businessobjects.openlocationcode.extensions.zoneCode


object POIDatabase {

    private lateinit var appDatabase: AppDatabase

    fun build(ctx: Context) {
        appDatabase = Room.databaseBuilder(ctx, AppDatabase::class.java, "pois.db").allowMainThreadQueries().build()
    }

    fun dao(): POIDao = appDatabase.poiDao()

    /**
     * This is used to show nearby POIs for a given code to give the user a better understanding of where
     * a certain place is. It only shows pois marked to be used for this (so a cafe is not returned, an important
     * landmark or a station however is)
     */
    fun closestPoisInAllDirections(plusCode: OpenLocationCode, desiredSize : Int = 3): List<POI> {
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

        return distinctDirectionEntries.take(desiredSize)
    }

    fun nearbyPois(plusCode : OpenLocationCode) : List<POI> {
                val neighbouringCellCodes = plusCode.neighbourHoodCodes().distinct()
        val listOfPoisInNeighbouringCells = neighbouringCellCodes.map {
            dao().anyNearby(it + "%")
        }
        return listOfPoisInNeighbouringCells.flatten().distinct()

    }


}