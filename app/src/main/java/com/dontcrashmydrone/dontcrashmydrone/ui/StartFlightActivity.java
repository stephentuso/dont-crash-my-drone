package com.dontcrashmydrone.dontcrashmydrone.ui;

import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

import com.dontcrashmydrone.dontcrashmydrone.DroneHelper;
import com.dontcrashmydrone.dontcrashmydrone.NotificationReceiver;
import com.dontcrashmydrone.dontcrashmydrone.NotificationService;
import com.dontcrashmydrone.dontcrashmydrone.R;

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
        try {
            int port = Integer.parseInt(input);
            droneHelper.connectToDrone(port);
        } catch (Exception e) {
            droneHelper.connectToDrone();
        }
        startInFlightActivity();
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
