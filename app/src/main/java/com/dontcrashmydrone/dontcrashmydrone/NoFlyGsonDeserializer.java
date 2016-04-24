package com.dontcrashmydrone.dontcrashmydrone;

import android.graphics.Point;
import android.location.Location;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by amohnacs on 4/23/16.
 */
public class NoFlyGsonDeserializer implements JsonDeserializer<Geometry> {

    final String TAG = getClass().getSimpleName();

    @Override
    public Geometry deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {


        Geometry mGeometry= new Geometry();
        ArrayList<Location> coordinatesArray = new ArrayList<>();

        JsonObject body = json.getAsJsonObject();

        //JsonObject properties = body.getAsJsonObject("properties");
        JsonArray features = body.getAsJsonArray("features");
        //Log.e(TAG, features.toString());

        for(JsonElement jElement : features) {

            String nameGetter = jElement.getAsJsonObject().get("properties").getAsJsonObject().get("name").getAsString();
            if (nameGetter.equals("Boeing Field King County International Airport")) {
                //Log.e(TAG, nameGetter.toString());

                JsonObject geometry = jElement.getAsJsonObject().get("geometry").getAsJsonObject();
                JsonArray coordinatesJsonArray = geometry.get("coordinates").getAsJsonArray();
                JsonArray oneInCoordinatesArray = coordinatesJsonArray.get(0).getAsJsonArray();

                for (JsonElement jelElement : oneInCoordinatesArray) {
                    //Log.e(TAG, jelElement.getAsJsonArray().toString());

                    Location location = new Location("");
                    location.setLatitude(jelElement.getAsJsonArray().get(0).getAsDouble());
                    location.setLongitude(jelElement.getAsJsonArray().get(1).getAsDouble());

                    coordinatesArray.add(location);

                    mGeometry.setCoordinates(coordinatesArray);
                }

            }

        }
        return mGeometry;
    }
}
