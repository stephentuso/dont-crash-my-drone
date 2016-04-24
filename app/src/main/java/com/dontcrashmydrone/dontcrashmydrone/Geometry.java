package com.dontcrashmydrone.dontcrashmydrone;

import android.location.Location;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class Geometry {

    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("coordinates")
    @Expose
    private List<Location> coordinates = new ArrayList<Location>();

    /**
     *
     * @return
     * The type
     */
    public String getType() {
        return type;
    }

    /**
     *
     * @param type
     * The type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     *
     * @return
     * The coordinates
     */
    public List<Location> getCoordinates() {
        return coordinates;
    }

    /**
     *
     * @param coordinates
     * The coordinates
     */
    public void setCoordinates(List<Location> coordinates) {
        this.coordinates = coordinates;
    }

}
