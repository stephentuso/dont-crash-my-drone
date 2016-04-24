package com.dontcrashmydrone.dontcrashmydrone.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.dontcrashmydrone.dontcrashmydrone.DroneHelper;
import com.dontcrashmydrone.dontcrashmydrone.R;
import com.o3dr.services.android.lib.coordinate.LatLong;

import java.util.Timer;
import java.util.TimerTask;

public class InFlightActivity extends AppCompatActivity {

    DroneHelper droneHelper;

    TextView textLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_flight);
        droneHelper = new DroneHelper(this);

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
                finish();
            }
        });

        textLocation = (TextView) findViewById(R.id.text_location);

        //This will be removed, just for dev purposes
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                final LatLong location = droneHelper.getLocation();
                if (location != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textLocation.setText(location.getLatitude() + ", " + location.getLatitude());
                        }
                    });
                }

            }
        }, 0, 1000);

    }

    @Override
    public void onBackPressed() {
        droneHelper.disconnectFromDrone();
        super.onBackPressed();
    }
}
