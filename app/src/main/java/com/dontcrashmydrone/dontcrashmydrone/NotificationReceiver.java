package com.dontcrashmydrone.dontcrashmydrone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;

/**
 * Created by amohnacs on 4/23/16.
 */
public class NotificationReceiver extends BroadcastReceiver {
    private final String TAG = getClass().getSimpleName();

    public static final String RECEIVER_ACTION = "notificationreceiver.receiveraction";
    private static int SMALL_VIBRATE = 500;
    private static int MEDIUM_VIBRATE = 1000;
    private static int LONG_VIBRATE = 2000;

    private Vibrator mVibrator;
    private Handler mHandler;

    private boolean mContact;



    @Override
    public void onReceive(Context context, Intent intent) {
        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        mHandler = new Handler();

        int mReceiveInfo = intent.getIntExtra(NotificationService.SEND_NOTIFICATION, 0);

        switch (mReceiveInfo) {
            case NotificationService.SEND_FIVE_MILE:

                fiveMileAlert();

                break;

            case NotificationService.SEND_ONE_MILE:

                oneMileAlert();

                break;

            case NotificationService.SEND_CONTACT:

                contactAlert();

                break;

            case NotificationService.SEND_STOP_CONTACT:

                stopContact();

                break;

            default:
                Log.e(TAG, "invalid input");
                break;
        }
    }

    private void fiveMileAlert() {
        mVibrator.vibrate(SMALL_VIBRATE);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                mVibrator.vibrate(SMALL_VIBRATE);
            }
        }, SMALL_VIBRATE);
    }

    private void oneMileAlert() {
        mVibrator.vibrate(MEDIUM_VIBRATE);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                mVibrator.vibrate(MEDIUM_VIBRATE);

            }
        }, MEDIUM_VIBRATE);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                mVibrator.vibrate(MEDIUM_VIBRATE);
            }
        }, (MEDIUM_VIBRATE * 2));
    }

    private void contactAlert() {

        mContact = true;

        while(mContact) {

            mVibrator.vibrate(LONG_VIBRATE);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        }
    }

    private void stopContact() {

        mContact = false;
    }



}
