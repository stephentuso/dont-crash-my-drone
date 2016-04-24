package com.dontcrashmydrone.dontcrashmydrone.ui;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.dontcrashmydrone.dontcrashmydrone.DroneConnectionStateListener;
import com.dontcrashmydrone.dontcrashmydrone.DroneHelper;
import com.dontcrashmydrone.dontcrashmydrone.NotificationReceiver;
import com.dontcrashmydrone.dontcrashmydrone.NotificationService;
import com.dontcrashmydrone.dontcrashmydrone.R;
import com.o3dr.android.client.interfaces.DroneListener;
import com.o3dr.services.android.lib.drone.connection.ConnectionResult;

/**
 * Launcher activity
 */
public class StartFlightActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();

    private static final String KEY_LOGIN_ACTIVITY_STARTED = "login_started";

    private boolean loginStarted = false;

    private NotificationReceiver receiver;
    DroneHelper droneHelper;

    EditText udpPortField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        droneHelper = new DroneHelper(this);
        udpPortField = (EditText) findViewById(R.id.edit_text_udp) ;

        if (savedInstanceState != null) {
            loginStarted = savedInstanceState.getBoolean(KEY_LOGIN_ACTIVITY_STARTED, false);
        }

        //Show in flight activity if already connected
        if (droneHelper.isConnected()) {
            startInFlightActivity();
        } else if (!loginStarted) { //Start the login activity TODO: Only start if user hasn't logged in - store/check bool in sharedPrefs
            loginStarted = true;
            Intent loginIntent = new Intent(this, MainActivity.class);
            startActivity(loginIntent);
        }

        //Weather button
        findViewById(R.id.button_show_weather).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent weatherIntent = new Intent(StartFlightActivity.this, WeatherActivity.class);
                startActivity(weatherIntent);
            }
        });

        //Start button
        findViewById(R.id.button_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onStartButtonClick();
            }
        });

        IntentFilter filter = new IntentFilter(NotificationReceiver.RECEIVER_ACTION);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new NotificationReceiver();
        registerReceiver(receiver, filter);

        startService();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(KEY_LOGIN_ACTIVITY_STARTED, loginStarted);
        super.onSaveInstanceState(outState);
    }

    // Method to start the service
    public void startService() {
        Intent msgIntent = new Intent(this, NotificationService.class);
        msgIntent.putExtra(NotificationService.SERVICE_IN_PARAM, "start activity");
        startService(msgIntent);
    }

    // Method to stop the service
    public void stopService(View view) {
        stopService(new Intent(getBaseContext(), NotificationService.class));
    }

    private void onStartButtonClick() {
        String input = udpPortField.getText().toString();

        //Ensure input is valid int
        int port = -1;
        try {
            port = Integer.parseInt(input);
        } catch (Exception e) {}

        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Connecting");
        dialog.setCanceledOnTouchOutside(false);

        final DroneConnectionStateListener listener = new DroneConnectionStateListener() {
            @Override
            public void onConnected() {
                startInFlightActivity();
                droneHelper.unregisterListener(this);
                dialog.dismiss();
            }

            @Override
            public void onDroneConnectionFailed(ConnectionResult result) {
                Toast.makeText(getApplicationContext(), "Error connecting to drone", Toast.LENGTH_SHORT).show();
                droneHelper.unregisterListener(this);
                dialog.dismiss();
            }

            @Override
            public void onDisconnected() {}
        };

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                droneHelper.unregisterListener(listener);
            }
        });
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                droneHelper.unregisterListener(listener);
            }
        });
        dialog.show();

        droneHelper.registerListener(listener);

        if (port == -1) {
            droneHelper.connectToDrone(); //Will use default port
        } else {
            droneHelper.connectToDrone(port);
        }

    }

    private void startInFlightActivity() {
        Intent intent = new Intent(this, InFlightActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }
}
