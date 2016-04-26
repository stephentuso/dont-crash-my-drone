package com.dontcrashmydrone.dontcrashmydrone.util;

import android.content.Context;
import android.location.Address;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.dontcrashmydrone.dontcrashmydrone.weather.WeatherConditions;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by stephentuso on 4/26/16.
 */
public class WeatherHelper {
    private final String TAG = getClass().getSimpleName();

    private Context context;

    public interface WeatherCallback {
        void onSuccess(WeatherConditions conditions);
        void onError(Error error);
    }

    public WeatherHelper(Context context) {
        this.context = context;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()){
            isAvailable = true;
        }
        return isAvailable;
    }

    public void getForecast(double latitude, double longitude, final WeatherCallback callback) {
        String apiKey = "e7220de7fb16ee318ac979fd820daf74";
        String forecastUrl = "https://api.forecast.io/forecast/" + apiKey + "/" + latitude + "," + longitude;

        if (isNetworkAvailable()) {

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(forecastUrl).build();

            final Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    callback.onError(new Error("Error getting weather."));
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    try {
                        String jsonString = response.body().string();
                        Log.v(TAG, jsonString);
                        if (response.isSuccessful()) {
                            try {
                                callback.onSuccess(parseJSON(jsonString));
                            } catch (Exception e) {
                                callback.onError(new Error("Bad network response"));
                            }
                        } else {
                            callback.onError(new Error("Error getting weather."));
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Exception caught: ", e);
                    }
                }
            });
        } else {
            callback.onError(new Error("Network unavailable."));
        }
    }

    /**
     *
     * @param json A json string from forecast.io
     * @return A WeatherConditions object initialized with the data from the json string
     * @throws JSONException
     */
    private WeatherConditions parseJSON(String json) throws JSONException {
        JSONObject forecastJsonObject = new JSONObject(json);
        String timezone = forecastJsonObject.getString("timezone");
        JSONObject conditionsJsonObject = forecastJsonObject.getJSONObject("currently");
        WeatherConditions conditions = new WeatherConditions();

        conditions.setHumidity(conditionsJsonObject.getDouble("humidity"));
        conditions.setTime(conditionsJsonObject.getLong("time"));
        conditions.setPrecipChance(conditionsJsonObject.getDouble("precipProbability"));
        conditions.setSummary(conditionsJsonObject.getString("summary"));
        conditions.setTemperature(conditionsJsonObject.getDouble("temperature"));
        conditions.setLocationAddress(forecastJsonObject.getDouble("latitude") + ", " + forecastJsonObject.getDouble("longitude"));
        conditions.setWindSpeed(conditionsJsonObject.getDouble("windSpeed"));
        conditions.setTimeZone(timezone);

        return conditions;
    }

}
