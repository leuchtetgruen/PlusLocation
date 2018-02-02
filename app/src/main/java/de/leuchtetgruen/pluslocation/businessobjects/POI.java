package de.leuchtetgruen.pluslocation.businessobjects;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import de.leuchtetgruen.pluslocation.businessobjects.openlocationcode.CodeArea;
import de.leuchtetgruen.pluslocation.businessobjects.openlocationcode.OpenLocationCode;

@Entity(tableName = "poi", indices = {@Index("poi_code"), @Index("poi_name")})
public class POI {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "poi_code")
    private String code;

    @ColumnInfo(name = "poi_name")
    private String name;
    @ColumnInfo(name = "nearby")
    private boolean usableForNearby;

    @Ignore
    private WGS84Coordinates calculatedCoordinate;

    @Ignore
    public POI(String code, String name, boolean usableForNearby) {
        this.code = code;
        this.name = name;
        this.usableForNearby = usableForNearby;
    }


    public POI(String code, String name) {
        this(code, name, false);
    }

    public WGS84Coordinates coordinate() {
        if (calculatedCoordinate == null) {
            CodeArea area = new OpenLocationCode(code).decode();
            calculatedCoordinate = new WGS84Coordinates(area.getCenterLatitude(), area.getCenterLongitude());
        }

        return calculatedCoordinate;
    }

    public int getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public boolean isUsableForNearby() {
        return usableForNearby;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUsableForNearby(boolean usableForNearby) {
        this.usableForNearby = usableForNearby;
    }

    public String describe(WGS84Coordinates currentCoordinate) {
        return name + "(" + String.format("%.2f km", currentCoordinate.distanceInMeters(coordinate()) / 1000) + " " + currentCoordinate.direction(coordinate()).toString() +")";
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof  POI)) return false;

        return (((POI) obj).getCode().equals(code) &&
                ((POI) obj).getName().equals(name)
        );
    }
}
