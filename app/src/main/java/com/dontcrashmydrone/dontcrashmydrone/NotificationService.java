package com.dontcrashmydrone.dontcrashmydrone;

import android.app.IntentService;
import android.content.Intent;
import android.nfc.Tag;
import android.os.IBinder;
import android.util.Log;

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

    public NotificationService() {
        super("com.dontcrashmydrone.dontcrashmydrone.NotificationService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        droneHelper = new DroneHelper(this);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String receive = intent.getStringExtra(SERVICE_IN_PARAM); //todo: not needed

        //where our actions will take place








        switch (mNotificationType) {
            case SEND_FIVE_MILE:

                break;

            case SEND_ONE_MILE:


                break;

            case SEND_CONTACT:


                break;

            case SEND_STOP_CONTACT:


                break;


            default:
                Log.e(TAG, "invalid input");
                break;
        }



    }

    /** Called when The service is no longer used and is being destroyed */
    @Override
    public void onDestroy() {

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
