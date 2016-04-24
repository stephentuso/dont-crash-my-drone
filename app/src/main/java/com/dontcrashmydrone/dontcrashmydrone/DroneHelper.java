package com.dontcrashmydrone.dontcrashmydrone;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.o3dr.android.client.ControlTower;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.interfaces.DroneListener;
import com.o3dr.android.client.interfaces.TowerListener;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.connection.ConnectionResult;
import com.o3dr.services.android.lib.drone.connection.ConnectionType;
import com.o3dr.services.android.lib.drone.property.Gps;
import com.o3dr.services.android.lib.drone.property.Type;

import java.util.Set;

/**
 * Created by stephentuso on 4/23/16.
 */
public class DroneHelper {

    private DroneApplication droneApp;

    public DroneHelper(Context context) {
        try {
            droneApp = (DroneApplication) context.getApplicationContext();
        } catch (Exception e) {
            throw new IllegalStateException("Error getting instance of DroneApplication");
        }
    }

    public DroneHelper(Service service) {
        Application app  = service.getApplication();
        if (app instanceof DroneApplication) {
            droneApp = (DroneApplication) app;
        } else {
            throw new IllegalStateException("Application not instance of DroneApplication");
        }
    }

    public DroneHelper(Activity activity) {
        Application app  = activity.getApplication();
        if (app instanceof DroneApplication) {
            droneApp = (DroneApplication) app;
        } else {
            throw new IllegalStateException("Application not instance of DroneApplication");
        }
    }

    public @Nullable LatLong getLocation() {
        Gps gps = droneApp.getDrone().getAttribute(AttributeType.GPS);
        return gps.isValid() ? gps.getPosition() : null;
    }

}
