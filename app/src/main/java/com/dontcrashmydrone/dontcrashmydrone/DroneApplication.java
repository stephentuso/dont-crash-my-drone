package com.dontcrashmydrone.dontcrashmydrone;

import android.app.Application;
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

/**
 * Created by stephentuso on 4/23/16.
 */
public class DroneApplication extends Application implements DroneListener, TowerListener {

    public static final int DEFAULT_UDP_PORT = 14550; //TODO: Add a preference for this

    private ControlTower controlTower;

    private Drone drone;
    private int droneType = Type.TYPE_UNKNOWN;
    private final Handler handler = new Handler();

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize the service manager
        this.controlTower = new ControlTower(this);
        this.drone = new Drone();

        this.controlTower.connect(this);
    }

    public void connectToDrone() {
        Log.i("DRONE", "Connecting to drone");
        Bundle extraParams = new Bundle();
        //extraParams.putString(ConnectionType.EXTRA_TCP_SERVER_IP, "10.0.2.15");
        //extraParams.putInt(ConnectionType.EXTRA_TCP_SERVER_PORT, 5760);
        extraParams.putInt(ConnectionType.EXTRA_TCP_SERVER_IP, 14550); // Set default port to 14550

        ConnectionParameter connectionParams = new ConnectionParameter(ConnectionType.TYPE_UDP, extraParams, null);
        this.drone.connect(connectionParams);
    }

    public Drone getDrone() {
        return drone;
    }

    @Override
    public void onTowerConnected() {
        Log.i("DRONE", "Tower connected");
        this.controlTower.registerDrone(this.drone, this.handler);
        this.drone.registerDroneListener(this);
        connectToDrone();
    }

    @Override
    public void onTowerDisconnected() {
        Log.w("DRONE", "Tower disconnected");
    }

    @Override
    public void onDroneConnectionFailed(ConnectionResult result) {
        Log.w("DRONE", result.getErrorMessage());
    }

    @Override
    public void onDroneEvent(String event, Bundle extras) {

        switch (event) {
            case AttributeEvent.STATE_CONNECTED:
                Log.i("DRONE", "Drone connected");
                Toast.makeText(this, "Drone connected", Toast.LENGTH_SHORT).show();
                LatLong location = new DroneHelper(this).getLocation();
                Log.i("DRONE", String.valueOf(location.getLatitude()));
                break;

            case AttributeEvent.STATE_DISCONNECTED:
                Log.i("DRONE", "Drone connected");
                Toast.makeText(this, "Drone disconnected", Toast.LENGTH_SHORT).show();
                break;
            case AttributeEvent.STATE_UPDATED:
                Log.i("DRONE", "State updated");
            default:
                break;
        }
    }

    @Override
    public void onDroneServiceInterrupted(String errorMsg) {
        Log.w("DRONE", errorMsg);
    }
}
