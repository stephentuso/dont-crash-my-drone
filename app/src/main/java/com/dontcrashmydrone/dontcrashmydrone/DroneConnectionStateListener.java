package com.dontcrashmydrone.dontcrashmydrone;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.o3dr.android.client.interfaces.DroneListener;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.connection.ConnectionResult;

/**
 * Created by stephentuso on 4/24/16.
 * Simplifies DroneListener down to just onConnected and onDisconnected
 * Other methods can also be overridden if other events are needed
 */
public abstract class DroneConnectionStateListener implements DroneListener {

    public abstract void onConnected();

    public abstract void onDisconnected();

    @Override
    public void onDroneConnectionFailed(ConnectionResult result) {

    }

    @Override
    public void onDroneEvent(String event, Bundle extras) {
        switch (event) {
            case AttributeEvent.STATE_CONNECTED:
                onConnected();
                break;
            case AttributeEvent.STATE_DISCONNECTED:
                onDisconnected();
                break;
        }
    }

    @Override
    public void onDroneServiceInterrupted(String errorMsg) {

    }
}
