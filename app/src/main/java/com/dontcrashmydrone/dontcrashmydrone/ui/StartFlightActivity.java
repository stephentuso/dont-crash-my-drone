package com.dontcrashmydrone.dontcrashmydrone.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
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
import com.dontcrashmydrone.dontcrashmydrone.NotificationReceiver;
import com.dontcrashmydrone.dontcrashmydrone.NotificationService;
import com.dontcrashmydrone.dontcrashmydrone.R;
import com.dontcrashmydrone.dontcrashmydrone.util.LocationHelper;
import com.dontcrashmydrone.dontcrashmydrone.weather.Current;
import com.dontcrashmydrone.dontcrashmydrone.weather.Forecast;
import com.o3dr.services.android.lib.drone.connection.ConnectionResult;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Launcher activity
 */
public class StartFlightActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();

    private static final String KEY_LOGIN_ACTIVITY_STARTED = "login_started";

    private boolean loginStarted = false;

    private LocationHelper mLocationHelper;

    private NotificationReceiver receiver;
    DroneHelper droneHelper;

    EditText udpPortField;

    private Forecast mForecast;

    @Bind(R.id.timeLabel)
    TextView mTimeLabel;
    @Bind(R.id.temperatureLabel) TextView mTemperatureLabel;
    @Bind(R.id.humidityValue) TextView mHumidityValue;
    @Bind(R.id.precipValue) TextView mPrecipValue;
    @Bind(R.id.summaryLabel) TextView mSummaryLabel;
    @Bind(R.id.refreshImageView) ImageView mRefreshImageView;
    @Bind(R.id.progressBar) ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        ButterKnife.bind(this);

        mLocationHelper = new LocationHelper(this);

        mProgressBar.setVisibility(View.INVISIBLE);

        mLocationHelper.getLocation(new LocationHelper.LocationCallback() {
            @Override
            public void success(Location location) {
                getForecast(location.getLatitude(), location.getLongitude());
            }

            @Override
            public void error() {

            }
        });

        //final double latitude = 32.8267;
        //final double longitude = -122.423;

        mRefreshImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLocationHelper.getLocation(new LocationHelper.LocationCallback() {
                    @Override
                    public void success(Location location) {
                        getForecast(location.getLatitude(), location.getLongitude());
                    }

                    @Override
                    public void error() {

                    }
                });
            }
        });

        Log.d(TAG, "Main UI code is running");

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

    private void getForecast(double latitude, double longitude) {
        String apiKey = "e7220de7fb16ee318ac979fd820daf74";
        String forecastUrl = "https://api.forecast.io/forecast/" + apiKey + "/" + latitude + "," + longitude;


        if (isNetworkAvailable()) {
            toggleRefresh();

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(forecastUrl).build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });
                    alertUserAboutError();
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });
                    try {
                        String jsonData = response.body().string();
                        Log.v(TAG, jsonData);
                        if (response.isSuccessful()) {
                            mForecast = parseForecastDetails(jsonData);
                            // It is mandatory to run this on the UI thread
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateDisplay();
                                }
                            });
                        } else {
                            alertUserAboutError();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Exception caught: ", e);
                    } catch (JSONException e) {
                        Log.e(TAG, "Exception caught: ", e);
                    }
                }
            });
        } else {
            alertUserAboutNoNetworkError();
            // Toast.makeText(this, R.string.network_unavailable_message, Toast.LENGTH_LONG).show();
        }
    }

    private void toggleRefresh() {
        if (mProgressBar.getVisibility() == View.INVISIBLE) {
            mProgressBar.setVisibility(View.VISIBLE);
            mRefreshImageView.setVisibility(View.INVISIBLE);
        } else {
            mProgressBar.setVisibility(View.INVISIBLE);
            mRefreshImageView.setVisibility(View.VISIBLE);
        }
    }

    private void updateDisplay() {
        mTemperatureLabel.setText(mForecast.getCurrent().getTemperature() + "");
        mTimeLabel.setText("At " + mForecast.getCurrent().getFormattedTime() + " it will be");
        mHumidityValue.setText(mForecast.getCurrent().getHumidity() + "");
        mPrecipValue.setText(mForecast.getCurrent().getPrecipChance() + "%");
        mSummaryLabel.setText(mForecast.getCurrent().getSummary());
    }
    private Forecast parseForecastDetails(String jsonData) throws JSONException{
        Forecast forecast = new Forecast();
        forecast.setCurrent(getCurrentDetails(jsonData));
        return forecast;
    }

    private Current getCurrentDetails(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        JSONObject currently = forecast.getJSONObject("currently");
        Log.i(TAG, "From JSON: " + timezone);

        Current current = new Current();

        current.setHumidity(currently.getDouble("humidity"));
        current.setTime(currently.getLong("time"));
        current.setPrecipChance(currently.getDouble("precipProbability"));
        current.setSummary(currently.getString("summary"));
        current.setTemperature(currently.getDouble("temperature"));
        current.setTimeZone(timezone);

        Log.d(TAG, current.getFormattedTime());
        return current;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()){
            isAvailable = true;
        }
        return isAvailable;
    }

    private void alertUserAboutError() {

    }

    private void alertUserAboutNoNetworkError() {

    }
}
