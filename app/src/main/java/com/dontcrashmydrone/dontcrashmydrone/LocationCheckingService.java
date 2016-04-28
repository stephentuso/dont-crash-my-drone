package com.dontcrashmydrone.dontcrashmydrone;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.cocoahero.android.geojson.Feature;
import com.cocoahero.android.geojson.Position;
import com.dontcrashmydrone.dontcrashmydrone.Polygons.Point;
import com.dontcrashmydrone.dontcrashmydrone.Polygons.Polygon;
import com.dontcrashmydrone.dontcrashmydrone.ui.InFlightActivity;
import com.dontcrashmydrone.dontcrashmydrone.util.FileUtils;
import com.dontcrashmydrone.dontcrashmydrone.util.GeoJsonUtils;
import com.o3dr.services.android.lib.coordinate.LatLong;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by stephentuso on 4/24/16.
 * This
 */
public class LocationCheckingService extends IntentService {
//TODO: Do this a better way: This doesn't need to be an IntentService.
// Fix broadcast receiver and send notifications from that, also update InFlightActivity with status
// Or, maybe rather than having this check for the location every 4 seconds, have it listen for when the location is updated?

    public static final String DRONE_LOCATION_UPDATED = "drone_location_updated";
    public static final String KEY_LOCATION = "drone_location";

    private static final int NOTIFICATION_ID = 1;

    public static final int STATUS_SAFE = 0;
    public static final int STATUS_NEAR = 1;
    public static final int STATUS_IN_RESTRICTED = 2;

    private DroneHelper droneHelper;
    private LocalBroadcastManager broadcastManager;

    List<Polygon> noFlyPolygons = new ArrayList<>();

    private boolean loadedPolygons = false;

    private NotificationManager notificationManager;

    private Timer timer;
    private TimerTask checkLocationTask;

    public LocationCheckingService() {
        super(LocationCheckingService.class.getSimpleName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        droneHelper = new DroneHelper(this);
        broadcastManager = LocalBroadcastManager.getInstance(this.getApplicationContext());
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        timer = new Timer();
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (!loadedPolygons) {
            loadPolygons();
        }

        new Timer().scheduleAtFixedRate(new TimerTask() { //TODO: Make this a var and cancel it in onDestroy
            @Override
            public void run() {
                final LatLong location = droneHelper.getLocation();
                if (location != null && noFlyPolygons.size() != 0) {

                    final float latitude = (float) location.getLatitude();
                    final float longitude = (float) location.getLongitude();

                    final Point currentPoint = new Point(latitude, longitude);

                    int status = STATUS_SAFE;

                    //Check if inside no fly zone first
                    if (pointIsInNoFly(currentPoint)) {
                        status = STATUS_IN_RESTRICTED;
                    }
                    //Check if near no fly zone
                    else if (pointIsCloseToNoFly(currentPoint)) {
                        status = STATUS_NEAR;
                    }

                    String title = "Safe fly zone";
                    int drawable = R.drawable.ic_check_white_24dp;
                    int priority = 0;
                    long[] vibrate = {200, 0, 200, 0};

                    switch (status) {
                        case STATUS_NEAR:
                            title = "Warning: Close to no fly zone";
                            drawable = R.drawable.ic_warning_white_24dp;
                            vibrate[1] = 500;
                            vibrate[3] = 500;
                            priority = 1;
                            break;
                        case STATUS_IN_RESTRICTED:
                            title = "WARNING: IN NO FLY ZONE";
                            drawable = R.drawable.ic_error_red_24dp;
                            vibrate[1] = 2000;
                            vibrate[3] = 2000;
                            priority = 1;
                    }
                    //vibrator.vibrate(vibrate, -1);

                    Log.i("CHECKING", location.getLatitude() + ", " + location.getLongitude());
                    Intent intent = new Intent(DRONE_LOCATION_UPDATED);
                    intent.putExtra(KEY_LOCATION, location.getLatitude() + "," + location.getLatitude());
                    broadcastManager.sendBroadcast(intent); // This doesn't seem to work, so is not implemented anymore

                    Intent notificationIntent = new Intent(getApplicationContext(), InFlightActivity.class);
                    notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(LocationCheckingService.this)
                            .setContentTitle(title)
                            .setSmallIcon(drawable)
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true)
                            .setVibrate(vibrate) //This isn't working
                            .setPriority(priority);

                    notificationManager.notify(NOTIFICATION_ID, builder.build());
                }

            }
        }, 0, 4000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        notificationManager.cancelAll();
    }

    private boolean pointIsInNoFly(Point point) {
        for (Polygon polygon : noFlyPolygons) {
            if (polygon.contains(point)) {
                return true;
            }
        }
        return false;
    }

    private boolean pointIsCloseToNoFly(Point point) {
        final float closeDif = 0.003f; // About 1000 feet (varies for latitude)
        final float latitude = point.x;
        final float longitude = point.y;
        List<Point> closePoints = new ArrayList<Point>(); //TODO: Use a loop to iterate through rather than hardcoding, also make more thorough
        closePoints.add(new Point(latitude - closeDif, longitude));
        closePoints.add(new Point(latitude, longitude - closeDif));
        closePoints.add(new Point(latitude + closeDif, longitude));
        closePoints.add(new Point(latitude, longitude + closeDif));
        return pointInListIsInNoFly(closePoints);
    }

    private boolean pointInListIsInNoFly(List<Point> points) {
        for (Point point : points) {
            if (pointIsInNoFly(point)) {
                return true;
            }
        }
        return false;
    }

    private void loadPolygons() {
        final String[] files = {"5_mile_airport.geojson", "us_military.geojson"};
        while(droneHelper.getLocation() == null) {}
        LatLong droneLocation = droneHelper.getLocation();
        for (int i = 0; i < files.length; i++) {
            File jsonFile = new FileUtils(this).getFile("json", files[i]);
            try {

                List<Feature> features =  GeoJsonUtils.getNearbyFeaturesInCollection(droneLocation.getLatitude(), droneLocation.getLongitude(), new FileInputStream(jsonFile));

                for (Feature feature : features) {
                    if (feature.getGeometry() instanceof com.cocoahero.android.geojson.Polygon) {
                        com.cocoahero.android.geojson.Polygon polygon = (com.cocoahero.android.geojson.Polygon) feature.getGeometry();
                        List<Position> positions = polygon.getRings().get(0).getPositions();
                        Polygon.Builder builder = Polygon.Builder();
                        for (Position position : positions) {
                            builder.addVertex(new Point((float) position.getLatitude(), (float) position.getLongitude()));
                        }
                        noFlyPolygons.add(builder.build());
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        loadedPolygons = true;
    }

}
