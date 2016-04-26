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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.dontcrashmydrone.dontcrashmydrone.weather.FlyingConditions;
import com.dontcrashmydrone.dontcrashmydrone.weather.WeatherConditions;
import com.o3dr.services.android.lib.drone.connection.ConnectionResult;

import java.util.List;

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

    private String udpPort = "14550";

    @Bind(R.id.layout_weather) ViewGroup weatherLayout;
    @Bind(R.id.label_flying_conditions) TextView flyingConditionsLabel;
    @Bind(R.id.image_flying_conditions) ImageView flyingConditionsImage;
    @Bind(R.id.layout_warnings) LinearLayout warningsLayout;
    @Bind(R.id.timeLabel) TextView weatherTimeLabel;
    @Bind(R.id.temperatureLabel) TextView weatherTempLabel;
    //@Bind(R.id.humidityValue) TextView weatherHumidityLabel;
    @Bind(R.id.precipValue) TextView weatherPrecipLabel;
    @Bind(R.id.summaryLabel) TextView mSummaryLabel;
    @Bind(R.id.refreshImageView) ImageView weatherRefreshButton;
    @Bind(R.id.progressBar) ProgressBar weatherProgressBar;
    @Bind(R.id.locationLabel) TextView weatherLocationLabel;
    @Bind(R.id.label_wind) TextView weatherWindLabel;
    @Bind(R.id.button_options) Button optionsButton;

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

        optionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOptionsDialog();
            }
        });

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
        String input = "14550";

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

    private void showOptionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Connection Options");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        TextView textView  = new TextView(this);
        textView.setText("UDP Port");
        layout.addView(textView);

        final EditText input = new EditText(this);
        input.setText(udpPort);
        layout.addView(input);

        builder.setView(layout);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                udpPort = input.getText().toString();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void setLoading(boolean loading) {
        if (loading) {
            weatherProgressBar.setVisibility(View.VISIBLE);
            weatherLayout.setVisibility(View.INVISIBLE);
        } else {
            weatherProgressBar.setVisibility(View.INVISIBLE);
            weatherLayout.setVisibility(View.VISIBLE);
        }
    }

    private void displayWeatherConditions(WeatherConditions conditions) {
        Address address = locationHelper.getAddress(currentLocation);
        if (address != null) {
            //TODO: Make this async, it takes a long time and causes frame skips
            weatherLocationLabel.setText(address.getLocality() + ", " + address.getAdminArea());
        }

        FlyingConditions flyingConditions = conditions.getFlyingConditions();
        String flyingString = "";
        int drawableId = 0;

        switch (flyingConditions.getConditionCode()) {
            case FlyingConditions.CONDITION_GOOD:
                flyingString = "Good flying conditions";
                drawableId = R.drawable.ic_check_black_24dp;
                break;
            case FlyingConditions.CONDITION_MEDIUM:
                flyingString = "Mediocre flying conditions";
                drawableId = R.drawable.ic_warning_amber_24dp;
                break;
            case FlyingConditions.CONDITION_POOR:
                flyingString = "Poor flying conditions";
                drawableId = R.drawable.ic_error_red_24dp;
                break;
        }

        flyingConditionsLabel.setText(flyingString);
        flyingConditionsImage.setImageDrawable(getResources().getDrawable(drawableId));
        displayFlyingWarnings(flyingConditions.getWarnings());

        weatherTempLabel.setText(conditions.getTemperature() + "\u00b0");
        weatherTimeLabel.setText("Updated " + conditions.getFormattedTime());
        weatherPrecipLabel.setText(conditions.getPrecipChance() + "%");
        mSummaryLabel.setText(conditions.getSummary());
        weatherWindLabel.setText(conditions.getWindSpeed() + " mph");
    }

    private void displayFlyingWarnings(List<String> warnings) {
        warningsLayout.removeAllViews();
        for (String warning : warnings) {
            TextView textView = new TextView(this, null, R.style.CardText);
            textView.setText(warning);
            warningsLayout.addView(textView);
        }
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
