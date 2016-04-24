package com.dontcrashmydrone.dontcrashmydrone;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by amohnacs on 4/23/16.
 */
public interface NoFlyEndpointInterface {

    @GET("mapbox/drone-feedback/master/sources/geojson/5_mile_airport.geojson")
    Call<Geometry> groupList();
}
