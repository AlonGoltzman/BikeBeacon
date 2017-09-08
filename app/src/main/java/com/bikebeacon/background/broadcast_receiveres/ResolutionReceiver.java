package com.bikebeacon.background.broadcast_receiveres;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.LocationSettingsResult;

import static com.bikebeacon.background.utility.Constants.BROADCAST_ACTION_RESOLVE;
import static com.bikebeacon.background.utility.Constants.BROADCAST_CONNECTION_RESULT;
import static com.bikebeacon.background.utility.Constants.CONNECTION_FAILURE_RESOLUTION_REQUEST;

/**
 * Created by Alon on 9/7/2017.
 */

public class ResolutionReceiver extends BroadcastReceiver {

    private Activity mAct;


    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(BROADCAST_ACTION_RESOLVE)) {
            if (intent.getExtras() == null)
                throw new IllegalArgumentException("No extras provided.");
            if (intent.getExtras().getParcelable(BROADCAST_CONNECTION_RESULT) == null)
                throw new IllegalArgumentException("No connection parcel provided.");
            Parcelable p = intent.getExtras().getParcelable(BROADCAST_CONNECTION_RESULT);
            if (p instanceof ConnectionResult) {
                ConnectionResult result = (ConnectionResult) p;
                try {
                    result.startResolutionForResult(mAct, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            } else if (p instanceof LocationSettingsResult) {
                LocationSettingsResult result = (LocationSettingsResult) p;
                try {
                    result.getStatus().startResolutionForResult(mAct, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }
        }
        LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
    }


    public BroadcastReceiver setActivity(Activity mAct) {
        this.mAct = mAct;
        return this;
    }
}
