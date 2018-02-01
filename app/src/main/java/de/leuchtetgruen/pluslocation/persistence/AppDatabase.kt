package de.leuchtetgruen.pluslocation.persistence

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import de.leuchtetgruen.pluslocation.businessobjects.POI

@Database(entities = arrayOf(POI::class), version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun poiDao() : POIDao
}