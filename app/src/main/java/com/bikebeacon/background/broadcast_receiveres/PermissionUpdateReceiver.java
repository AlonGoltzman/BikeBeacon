package com.bikebeacon.background.broadcast_receiveres;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.bikebeacon.background.location_util.CentralHandler;

import static com.bikebeacon.background.utility.Constants.BROADCAST_ACTION_GOT_PERMISSIONS;

public class PermissionUpdateReceiver extends BroadcastReceiver {

    private CentralHandler mHandler;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(BROADCAST_ACTION_GOT_PERMISSIONS))
            mHandler.resume();
        LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
    }

    public BroadcastReceiver setHandler(CentralHandler mHandler) {
        this.mHandler = mHandler;
        return this;
    }
}
