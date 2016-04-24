package com.dontcrashmydrone.dontcrashmydrone;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.dontcrashmydrone.dontcrashmydrone.Polygons.Point;
import com.dontcrashmydrone.dontcrashmydrone.Polygons.Polygon;
import com.o3dr.services.android.lib.coordinate.LatLong;

import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by stephentuso on 4/24/16.
 */
public class LocationCheckingService extends IntentService {

    public static final String DRONE_LOCATION_UPDATED = "drone_location_updated";
    public static final String KEY_LOCATION = "drone_location";

    private DroneHelper droneHelper;
    private LocalBroadcastManager broadcastManager;

    public LocationCheckingService() {
        super(LocationCheckingService.class.getSimpleName());
    }

    Polygon polygon = null;

    @Override
    public void onCreate() {
        super.onCreate();
        droneHelper = new DroneHelper(this);
        broadcastManager = LocalBroadcastManager.getInstance(this.getApplicationContext());

        Retrofit retrofit = new NoFlyRetrofitHelper().getRetrofit();

        NoFlyEndpointInterface newsService = retrofit.create(NoFlyEndpointInterface.class);
        final Call<Geometry> call = newsService.groupList();

        call.enqueue(new Callback<Geometry>() {
            @Override
            public void onResponse(Call<Geometry> call, Response<Geometry> response) {
                if (response.isSuccessful()) {
                    Geometry resp = response.body();

                    //Log.e(TAG, resp.getCoordinates().toString());

                    Polygon.Builder builder = Polygon.Builder();

                    for(int i = 0; i < resp.getCoordinates().size(); i++) {
                        Log.e("TAg", resp.getCoordinates().get(i).getLatitude() + ", " + resp.getCoordinates().get(i).getLatitude());

                        builder.addVertex(
                                        new Point((float)resp.getCoordinates().get(i).getLatitude(),
                                                (float)resp.getCoordinates().get(i).getLongitude()));

                    }

                    polygon = builder.build();

                } else {
                    Log.e("TAG", "response fail");
                }
            }

            @Override
            public void onFailure(Call<Geometry> call, Throwable t) {

            }
        });

    }


    @Override
    protected void onHandleIntent(Intent intent) {
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                final LatLong location = droneHelper.getLocation();

                if (location != null && polygon != null) {
                    final Point point = new Point((float)location.getLatitude(), (float)location.getLongitude());

                    String title = "Drone is safe";
                    if (polygon.contains(point)) {
                        title = "WARNING: IN NO FLY ZONE";
                    }

                    Log.i("CHECKING", location.getLatitude() + ", " + location.getLatitude());
                    Intent intent = new Intent(DRONE_LOCATION_UPDATED);
                    intent.putExtra(KEY_LOCATION, location.getLatitude() + "," + location.getLatitude());
                    broadcastManager.sendBroadcast(intent);
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(LocationCheckingService.this)
                            .setContentTitle(title)
                            .setSmallIcon(R.mipmap.ic_launcher);

                    NotificationManager mNotificationManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.notify(1234, builder.build());

                }

            }
        }, 0, 10000);
    }
}
