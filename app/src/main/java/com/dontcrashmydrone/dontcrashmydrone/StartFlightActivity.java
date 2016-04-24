package com.dontcrashmydrone.dontcrashmydrone;

import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import com.dontcrashmydrone.dontcrashmydrone.Polygons.Point;
import com.dontcrashmydrone.dontcrashmydrone.Polygons.Polygon;

import java.text.DecimalFormat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class StartFlightActivity extends AppCompatActivity {
    private final String TAG = getClass().getSimpleName();

    private NotificationReceiver receiver;
    Retrofit retrofit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        IntentFilter filter = new IntentFilter(NotificationReceiver.RECEIVER_ACTION);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new NotificationReceiver();
        registerReceiver(receiver, filter);


        retrofit = new NoFlyRetrofitHelper().getRetrofit();

        NoFlyEndpointInterface newsService = retrofit.create(NoFlyEndpointInterface.class);
        final Call<Geometry> call = newsService.groupList();

        call.enqueue(new Callback<Geometry>() {
            @Override
            public void onResponse(Call<Geometry> call, Response<Geometry> response) {
                if (response.isSuccessful()) {
                    Geometry resp = response.body();
                    DecimalFormat decimalFormat = new DecimalFormat("#");

                    //Log.e(TAG, resp.getCoordinates().toString());

                    for(int i = 0; i < resp.getCoordinates().size(); i++) {
                        Log.e(TAG, resp.getCoordinates().get(i).getLatitude() + ", " + resp.getCoordinates().get(i).getLatitude());

                        Polygon polygon = Polygon.Builder()
                                .addVertex(
                                        new Point((float)resp.getCoordinates().get(i).getLatitude(),
                                                (float)resp.getCoordinates().get(i).getLongitude())).build();

                        Log.e(TAG, polygon.toString());

                    }

                } else {
                    Log.e(TAG, "response fail");
                }
            }

            @Override
            public void onFailure(Call<Geometry> call, Throwable t) {

            }
        });



        startService();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
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
}
