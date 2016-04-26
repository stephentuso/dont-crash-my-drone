package com.dontcrashmydrone.dontcrashmydrone.ui;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dontcrashmydrone.dontcrashmydrone.DroneConnectionStateListener;
import com.dontcrashmydrone.dontcrashmydrone.DroneHelper;
import com.dontcrashmydrone.dontcrashmydrone.LocationCheckingService;
import com.dontcrashmydrone.dontcrashmydrone.NotificationReceiver;
import com.dontcrashmydrone.dontcrashmydrone.NotificationService;
import com.dontcrashmydrone.dontcrashmydrone.R;
import com.dontcrashmydrone.dontcrashmydrone.util.LocationHelper;
import com.dontcrashmydrone.dontcrashmydrone.util.WeatherHelper;
import com.dontcrashmydrone.dontcrashmydrone.weather.WeatherConditions;
import com.o3dr.services.android.lib.drone.connection.ConnectionResult;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Launcher activity
 */
public class StartFlightActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();

    private static final String KEY_LOGIN_ACTIVITY_STARTED = "login_started";

    private boolean loginStarted = false;

    private LocationHelper locationHelper;
    private WeatherHelper weatherHelper;

    public Location currentLocation;

    private NotificationReceiver receiver;
    DroneHelper droneHelper;

    EditText udpPortField;

    @Bind(R.id.timeLabel) TextView weatherTimeLabel;
    @Bind(R.id.temperatureLabel) TextView weatherTempLabel;
    @Bind(R.id.humidityValue) TextView weatherHumidityLabel;
    @Bind(R.id.precipValue) TextView weatherPrecipLabel;
    @Bind(R.id.summaryLabel) TextView mSummaryLabel;
    @Bind(R.id.refreshImageView) ImageView weatherRefreshButton;
    @Bind(R.id.progressBar) ProgressBar weatherProgressBar;
    @Bind(R.id.locationLabel) TextView weatherLocationLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        ButterKnife.bind(this);

        locationHelper = new LocationHelper(this);
        weatherHelper = new WeatherHelper(this);

        weatherProgressBar.setVisibility(View.INVISIBLE);

        refreshWeather();

        //final double latitude = 32.8267;
        //final double longitude = -122.423;

        weatherRefreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshWeather();
            }
        });

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

        if (!locationHelper.permissionsGranted())
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1234);

        //Start button
        findViewById(R.id.button_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onStartButtonClick();
            }
        });


        //TODO: Figure out the right way to use services and receivers for this
        IntentFilter filter = new IntentFilter(NotificationReceiver.RECEIVER_ACTION);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new NotificationReceiver();
        registerReceiver(receiver, filter);

        Intent notificationIntent = new Intent(this, NotificationService.class);
        startService(notificationIntent);

        Intent locationIntent = new Intent(this, LocationCheckingService.class);
        startService(locationIntent);
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

    private void refreshWeather() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setLoading(true);
            }
        });
        locationHelper.getLocation(new LocationHelper.LocationCallback() {
            @Override
            public void onSuccess(final Location location) {
                currentLocation = location;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        weatherHelper.getForecast(location.getLatitude(), location.getLongitude(), new WeatherHelper.WeatherCallback() {
                            @Override
                            public void onSuccess(final WeatherConditions conditions) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        setLoading(false);
                                        displayWeatherConditions(conditions);
                                    }
                                });
                            }

                            @Override
                            public void onError(final Error error) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        setLoading(false);
                                        displayWeatherError(error);
                                    }
                                });
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(final Error error) {
                Log.w(TAG, error);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setLoading(false);
                        displayWeatherError(error);
                    }
                });
            }
        }, 10000);
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

    private void setLoading(boolean loading) {
        if (loading) {
            weatherProgressBar.setVisibility(View.VISIBLE);
            weatherRefreshButton.setVisibility(View.INVISIBLE);
        } else {
            weatherProgressBar.setVisibility(View.INVISIBLE);
            weatherRefreshButton.setVisibility(View.VISIBLE);
        }
    }

    private void displayWeatherConditions(WeatherConditions conditions) {
        if (locationHelper.getAddress(currentLocation) != null) {
            //TODO: Make this async, it takes a long time and causes frame skips
            Address address = locationHelper.getAddress(currentLocation);
            weatherLocationLabel.setText(address.getLocality() + ", " + address.getAdminArea());
        }

        weatherTempLabel.setText(conditions.getTemperature() + "");
        weatherTimeLabel.setText("Last updated at " + conditions.getFormattedTime());
        weatherHumidityLabel.setText(conditions.getHumidity() + "");
        weatherPrecipLabel.setText(conditions.getPrecipChance() + "%");
        mSummaryLabel.setText(conditions.getSummary());
    }

    private void displayWeatherError(Error error) {
        //TODO
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        refreshWeather();
    }

}
