package com.bikebeacon.background.audio_recording;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;

import static com.bikebeacon.background.utility.Constants.PACKAGE_NAME;
import static com.bikebeacon.background.utility.Constants.SHARED_PREFERENCES_NUMBER_TO_CALL;

/**
 * Created by Alon on 8/27/2017.
 */

public class RecordingInitiationReceiver extends BroadcastReceiver {

    private Activity mCaller;

    public RecordingInitiationReceiver(Activity activity) {
        mCaller = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
        SharedPreferences prefs = context.getSharedPreferences(PACKAGE_NAME, Context.MODE_APPEND);
        String number = prefs.getString(SHARED_PREFERENCES_NUMBER_TO_CALL, "null");
        if (number.equals("null")) {
            RecordController.getController().startRecording(mCaller);
            return;
        }
        Intent callNumber = new Intent(Intent.ACTION_CALL);
        callNumber.setData(Uri.parse("tel:" + number));
        callNumber.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //noinspection MissingPermission
        context.startActivity(callNumber);
        prefs.edit().putString(SHARED_PREFERENCES_NUMBER_TO_CALL, "null").apply();
    }
}
