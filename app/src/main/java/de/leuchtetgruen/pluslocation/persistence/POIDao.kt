package de.leuchtetgruen.pluslocation.persistence

import android.arch.persistence.room.*
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import de.leuchtetgruen.pluslocation.businessobjects.POI



@Dao
interface POIDao {
    @Query("select * from poi")
    fun getAllPois() : List<POI>


    @Query("select count(*) from poi")
    fun count(): Int

    @Query("select * from poi where poi_code LIKE :arg0 and nearby = 1")
    fun nearby(codeBeginning : String) : List<POI>

    @Insert(onConflict = REPLACE)
    fun insertPoi(poi: POI)

    @Update(onConflict = REPLACE)
    fun updatePOI(poi: POI)

    @Delete
    fun deletePOI(poi: POI)
}