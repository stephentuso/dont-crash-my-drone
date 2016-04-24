package com.dontcrashmydrone.dontcrashmydrone.ui;

import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.dontcrashmydrone.dontcrashmydrone.DroneConnectionStateListener;
import com.dontcrashmydrone.dontcrashmydrone.DroneHelper;
import com.dontcrashmydrone.dontcrashmydrone.R;
import com.o3dr.android.client.interfaces.DroneListener;
import com.o3dr.services.android.lib.coordinate.LatLong;

import java.util.Timer;
import java.util.TimerTask;

public class InFlightActivity extends AppCompatActivity {

    DroneHelper droneHelper;

    TextView textLocation;

    //In case drone disconnects while in flight
    private DroneConnectionStateListener listener = new DroneConnectionStateListener() {
        @Override
        public void onConnected() {}

        @Override
        public void onDisconnected() {
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_flight);
        droneHelper = new DroneHelper(this);
        droneHelper.registerListener(listener);

        /*
        //Future: Exit if not connected
        if (!droneHelper.isConnected()) {
            Toast.makeText(this, "Drone not connected", Toast.LENGTH_LONG).show();
            finish();
        }
        */

        findViewById(R.id.button_disconnect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                droneHelper.disconnectFromDrone();
            }
        });

        textLocation = (TextView) findViewById(R.id.text_location);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        droneHelper.unregisterListener(listener);
    }

    @Override
    public void onBackPressed() {
        droneHelper.disconnectFromDrone();
        super.onBackPressed();
    }

}
