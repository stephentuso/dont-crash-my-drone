package com.dontcrashmydrone.dontcrashmydrone.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dontcrashmydrone.dontcrashmydrone.R;

import butterknife.Bind;

public class WeatherActivity extends AppCompatActivity {

    public static final String TAG = WeatherActivity.class.getSimpleName();
    public static final String HOURLY_FORECAST = "HOURLY_FORECAST";
    //private Forecast mForecast;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
    }

    private void getForecast(double latitude, double longitude) {
        String apiKey = "e7220de7fb16ee318ac979fd820daf74";
        String forecastUrl = "https://api.forecast.io/forecast/" + apiKey + "/" + latitude + "," + longitude;
    }
}
