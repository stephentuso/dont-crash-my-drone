package com.dontcrashmydrone.dontcrashmydrone.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

public class LocationHelper {
    Timer timer;

    LocationManager locationManager;
    LocationCallback callback;

    boolean gpsEnabled = false;
    boolean networkEnabled = false;

    private Context context;

    public LocationHelper(Context context) {
        this.context = context;
    }

    public boolean permissionsGranted() {
        return (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }

    public void getLocation(LocationCallback callback) {
        getLocation(callback, 20000);
    }

    public void getLocation(LocationCallback callback, long timeToWait) {

        if (!permissionsGranted()) {
            callback.onError(new Error("Location permission not granted"));
            return ;
        }

        if (locationManager == null)
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        //exceptions will be thrown if provider is not permitted.
        try {
            gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception exception) {}

        try {
            networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {}

        //don't start listeners if no provider is enabled
        if (!gpsEnabled && !networkEnabled) {
            callback.onError(new Error("Location providers not enabled"));
            return;
        }

        Log.i(getClass().getSimpleName(), "Getting location");

        final LocationChangeListener gpsListener, networkListener;
        final List<LocationChangeListener> listeners = new ArrayList<>();

        gpsListener = new LocationChangeListener(callback, listeners);

        networkListener = new LocationChangeListener(callback, listeners);

        listeners.add(gpsListener);
        listeners.add(networkListener);

        if (gpsEnabled)
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gpsListener);
        if(networkEnabled)
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, networkListener);

        timer=new Timer();

        // As a fallback, return the last known location
        timer.schedule(new GetLastLocationTask(callback, listeners), timeToWait);
    }

    public @Nullable Address getAddress(Location location) {
        return getAddress(context, location);
    }

    public static @Nullable Address getAddress(Context context, Location location) {
        if (location == null)
            return null;

        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            return geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1).get(0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    class GetLastLocationTask extends TimerTask {

        LocationCallback callback;
        List<LocationChangeListener> listeners;

        protected GetLastLocationTask(LocationCallback callback, List<LocationChangeListener> listeners) {
            this.callback = callback;
            this.listeners = listeners;
        }

        @Override
        public void run() {
            for (LocationChangeListener listener: listeners) {
                locationManager.removeUpdates(listener);
            }

            Location net_loc = null,
                    gps_loc = null;
            if(gpsEnabled)
                gps_loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(networkEnabled)
                net_loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            //if there are both values use most recent
            if (gps_loc != null && net_loc != null){
                if(gps_loc.getTime()>net_loc.getTime())
                    callback.onSuccess(gps_loc);
                else
                    callback.onSuccess(net_loc);
                return;
            }

            if (gps_loc != null) {
                callback.onSuccess(gps_loc);
            } else if (net_loc != null) {
                callback.onSuccess(net_loc);
            } else {
                callback.onError(new Error("Error getting location"));
            }


        }
    }

    class LocationChangeListener implements LocationListener {

        LocationCallback callback;
        List<LocationChangeListener> listeners;

        public LocationChangeListener(LocationCallback callback, List<LocationChangeListener> listeners) {
            this.callback = callback;
            this.listeners = listeners;
        }

        @Override
        public void onLocationChanged(Location location) {
            timer.cancel();
            callback.onSuccess(location); //TODO (stephentuso) Maybe add check for accuracy
            for (LocationChangeListener listener: listeners) {
                locationManager.removeUpdates(listener);
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {}

        @Override
        public void onProviderEnabled(String s) {}

        @Override
        public void onProviderDisabled(String s) {}
    }

    public interface LocationCallback {
        void onSuccess(Location location);
        void onError(Error error);
    }

}