package com.adamhueniken.localsharing;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.Calendar;

/**
 * Created by Adam Hueniken on 12/9/2014.
 */
public class WifiReceiver extends BroadcastReceiver {
    AlarmManager mAlarm;
    private Context mContext;
    PendingIntent mPintent;
    Intent mIntent;

    public boolean connectedToCorrectWifi = false;

    public WifiReceiver(Context context) {
        super();
        mContext = context;
        checkConnectedToDesiredWifi();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(Constants.TAG, "WifiManager onReceive");
        String action = intent.getAction();
        if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION .equals(action)) {
            SupplicantState state = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
            if (SupplicantState.isValidState(state)
                    && state == SupplicantState.COMPLETED) {
                mContext = context;
                boolean connected = checkConnectedToDesiredWifi();
                Log.i(Constants.TAG, "Is Valid state");

                if(connected) {
                    startCurrentAppService();
                } else {
                    stopCurrentAppService();
                }
            } else {
                stopCurrentAppService();
            }
        }
        Log.i(Constants.TAG, "Action is: " + action);

    }

    private void stopCurrentAppService() {
        Log.d(Constants.TAG, "Canceling Sharing Service");
        if (mAlarm == null && mPintent == null) {
            mIntent = new Intent(mContext, CurrentAppService.class);
            mPintent = PendingIntent.getService(mContext, 0, mIntent, 0);
            Calendar cal = Calendar.getInstance();
            mAlarm = (AlarmManager) mContext.getSystemService(mContext.ALARM_SERVICE);
        }
        mAlarm.cancel(mPintent);
    }

    private void startCurrentAppService() {
        Log.d(Constants.TAG, "Starting Sharing Service");
        mIntent = new Intent(mContext, CurrentAppService.class);
        mPintent = PendingIntent.getService(mContext, 0, mIntent, 0);
        Calendar cal = Calendar.getInstance();
        mAlarm = (AlarmManager) mContext.getSystemService(mContext.ALARM_SERVICE);
        mAlarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
                3000, mPintent);
    }


    /** Detect you are connected to a specific network. */
    private boolean checkConnectedToDesiredWifi() {
        Log.d("TEST", "Check Connected to desired wifi");
        boolean connected = false;

        String desiredMacAddress = "00:18:4d:44:f4:60";

        WifiManager wifiManager =
                (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);

        WifiInfo wifi = wifiManager.getConnectionInfo();
        if (wifi != null) {
            // get current router Mac address
            String bssid = wifi.getBSSID();
            connected = desiredMacAddress.equals(bssid);
            if (connected) {
                connectedToCorrectWifi = true;
            }
            Log.d(Constants.TAG, bssid);
        }
        if(!connected) {
            connectedToCorrectWifi = false;
        }
        return connected;
    }
}
