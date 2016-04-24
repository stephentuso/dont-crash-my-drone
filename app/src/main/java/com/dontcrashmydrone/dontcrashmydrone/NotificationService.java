package com.dontcrashmydrone.dontcrashmydrone;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.Tag;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.o3dr.services.android.lib.coordinate.LatLong;

import java.io.Console;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by amohnacs on 4/23/16.
 */
public class NotificationService extends IntentService {
    private  final String TAG = getClass().getSimpleName();

    public static final String SERVICE_IN_PARAM = "startflightactivity.serviceinparam";
    public static final String SEND_NOTIFICATION = "startflightactivity.serviceoutparam";

    public boolean mDangerZone = false;

    private int mNotificationType = 0;
    public static final int SEND_FIVE_MILE = 1;
    public static final int SEND_ONE_MILE = 2;
    public static final int SEND_CONTACT = 3;
    public static final int SEND_STOP_CONTACT = 4;

    int mStartMode;/** indicates how to behave if the service is killed */
    IBinder mBinder;/** interface for clients that bind */
    boolean mAllowRebind;/** indicates whether onRebind should be used */

    DroneHelper droneHelper;

    private Handler handler;

    public NotificationService() {
        super("com.dontcrashmydrone.dontcrashmydrone.NotificationService");
    }

    LocalBroadcastManager broadcastManager;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("SERVICE", "Recieved intent");

            String location = intent.getStringExtra(LocationCheckingService.KEY_LOCATION);
            String[] components = location.split(",");

            String lat = components[0];
            String lng = components[1];

        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        droneHelper = new DroneHelper(this);
        broadcastManager = LocalBroadcastManager.getInstance(this.getApplicationContext());
        broadcastManager.registerReceiver(receiver, new IntentFilter(LocationCheckingService.DRONE_LOCATION_UPDATED));
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }

    /** Called when The service is no longer used and is being destroyed */
    @Override
    public void onDestroy() {
        broadcastManager.unregisterReceiver(receiver);
    }

    private void sendAlert(int receiverMessage) {

        //send message to our Receiver
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(NotificationReceiver.RECEIVER_ACTION);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(SEND_NOTIFICATION, receiverMessage);
        sendBroadcast(broadcastIntent);
    }
}
