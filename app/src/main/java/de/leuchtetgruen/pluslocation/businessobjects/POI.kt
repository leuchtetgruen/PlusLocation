package de.leuchtetgruen.pluslocation.businessobjects

import android.arch.persistence.room.*
import de.leuchtetgruen.pluslocation.businessobjects.openlocationcode.OpenLocationCode


/**
 * A POI is a Point of Interest. POIs serve two purposes. A search feature allows users to find a POI
 * that they are interested in. The app then allows them to navigate to that POI.
 *
 * POIs marked as "usable for nearby" are POIs that are commonly known (such as big train stations, landmarks etc.)
 * When a user chooses a destination the app will show nearby POIs to give the user a rough idea where
 * the chosen destination is. (E.g. think you have the PlusCode of a hotel and the app tells you it's close to the
 * main station and the zoo)
 */
@Entity(tableName = "poi", indices = arrayOf(Index("poi_code"), Index("poi_name")))
class POI @Ignore
constructor(@field:ColumnInfo(name = "poi_code")
            var code: String, @field:ColumnInfo(name = "poi_name")
            var name: String, @field:ColumnInfo(name = "nearby")
            var isUsableForNearby: Boolean) {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int = 0

    @Ignore
    private var calculatedCoordinate: WGS84Coordinates? = null


    constructor(code: String, name: String) : this(code, name, false)

    fun coordinate(): WGS84Coordinates {
        if (calculatedCoordinate == null) {
            val area = OpenLocationCode(code).decode()
            calculatedCoordinate = WGS84Coordinates(area.centerLatitude, area.centerLongitude)
        }

        return calculatedCoordinate!!
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is POI) false else other.code == code && other.name == name

    }

    override fun hashCode(): Int {
        return "$code$name".hashCode()
    }
}
