package com.dontcrashmydrone.dontcrashmydrone.util;

import android.util.Log;

import com.cocoahero.android.geojson.Feature;
import com.cocoahero.android.geojson.MultiPolygon;
import com.cocoahero.android.geojson.Polygon;
import com.cocoahero.android.geojson.Position;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by stephentuso on 4/27/16.
 */
public class GeoJsonUtils {

    /**
     * Gets features that are within about 25 miles to the provided latitude and longitude
     * This needs to use an InputStream to avoid an out of memory exception (we are dealing with huge JSON files)
     * @param latitude
     * @param longitude
     * @param input An input stream for the json file to read
     * @throws IOException
     */
    public static List<Feature> getNearbyFeaturesInCollection(double latitude, double longitude, InputStream input) throws IOException, JSONException {
        List<Feature> features = new ArrayList<>();
        JsonReader reader = new JsonReader(new InputStreamReader(input, "UTF-8"));
        reader.beginObject();
        while(reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("features")) {
                features = getNearbyFeatures(latitude, longitude, reader);
            } else {
                reader.skipValue();
            }
        }
        return features;
    }

    //TODO: Make this more efficient - maybe create Feature directly rather than creating JSONObject first
    // (this takes about 12 seconds for an 8 mb file on my phone)
    public static List<Feature> getNearbyFeatures(double latitude, double longitude, JsonReader reader) throws IOException, JSONException {
        List<Feature> features = new ArrayList<>();
        reader.beginArray();
        while (reader.hasNext()) {
            JSONObject json = getObjectFromReader(reader);
            Feature feature = new Feature(json);
            Position position = null;
            if (feature.getGeometry() instanceof Polygon) {
                Polygon polygon = (Polygon) feature.getGeometry();
                position = polygon.getRings().get(0).getPositions().get(0);
            } else if (feature.getGeometry() instanceof MultiPolygon) {
                MultiPolygon multiPolygon = (MultiPolygon) feature.getGeometry();
                position = multiPolygon.getPolygons().get(0).getRings().get(0).getPositions().get(0);
            }

            if (position != null) {
                double latDif = Math.abs(latitude - position.getLatitude());
                double lngDif = Math.abs(longitude - position.getLongitude());
                if (latDif < 0.5 && lngDif < 0.5) {
                    Log.i("GeoJsonUtils", "Close: " + position.getLatitude() + ", " + position.getLongitude());
                    features.add(feature);
                }
            }

        }
        reader.endArray();
        return features;
    }

    public static JSONObject getObjectFromReader(JsonReader reader) throws IOException, JSONException {
        JSONObject object = new JSONObject();
        reader.beginObject();
        while (reader.peek() == JsonToken.NAME) {
            addNextToObject(object, reader);
        }
        reader.endObject();
        return object;
    }

    public static JSONArray getArrayFromReader(JsonReader reader) throws IOException, JSONException {
        JSONArray array = new JSONArray();
        reader.beginArray();
        while (reader.hasNext()) {
            addNextToArray(array, reader);
        }
        reader.endArray();
        return array;
    }

    public static void addNextToObject(JSONObject object, JsonReader reader) throws IOException, JSONException {
        if (reader.peek() == JsonToken.NULL)
            return;
        String name = reader.nextName();
        switch (reader.peek()) {
            case BEGIN_ARRAY:
                object.put(name, getArrayFromReader(reader));
                break;
            case BEGIN_OBJECT:
                object.put(name, getObjectFromReader(reader));
                break;
            case STRING:
                object.put(name, reader.nextString());
                break;
            case NUMBER:
                object.put(name, reader.nextDouble());
                /*String number = reader.nextString();
                try {
                    object.put(name, Integer.parseInt(number));
                    break;
                } catch (Exception e) {}
                try {
                    object.put(name, Long.parseLong(number));
                    break;
                } catch (Exception e) {}
                try {
                    object.put(name, Double.parseDouble(number));
                    break;
                } catch (Exception e) {}*/
                break;
            case BOOLEAN:
                object.put(name, reader.nextBoolean());
                break;
            case NULL:
                reader.nextNull();
                object.put(name, null);
                break;
        }
    }

    public static void addNextToArray(JSONArray array, JsonReader reader) throws IOException, JSONException {
        switch (reader.peek()) {
            case BEGIN_ARRAY:
                array.put(getArrayFromReader(reader));
                break;
            case BEGIN_OBJECT:
                array.put(getObjectFromReader(reader));
                break;
            case STRING:
                array.put(reader.nextString());
                break;
            case NUMBER:
                array.put(reader.nextDouble());
                break;
            case BOOLEAN:
                array.put(reader.nextBoolean());
                break;
            case NULL:
                reader.nextNull();
                array.put(null);
                break;
        }
    }

}
