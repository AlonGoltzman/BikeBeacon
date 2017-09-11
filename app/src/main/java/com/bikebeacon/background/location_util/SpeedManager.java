package com.bikebeacon.background.location_util;

import android.location.Location;

import com.bikebeacon.background.utility.SharedPreferencesManager;
import com.bikebeacon.pojo.Ping;
import com.bikebeacon.pojo.ProblemListener;

import static com.bikebeacon.background.utility.Constants.SHARED_PREFERENCES_LAT;
import static com.bikebeacon.background.utility.Constants.SHARED_PREFERENCES_LON;
import static com.bikebeacon.background.utility.Constants.SHARED_PREFERENCES_SPEED;

/**
 * Created by Alon on 9/7/2017.
 */

public final class SpeedManager {


    public static final String TAG = SpeedManager.class.getSimpleName();
    private static SpeedManager mInstance;

    private ProblemListener mAlertIssuer;
    private Ping[] pings = new Ping[10];
    private int pos = 0;

    private SpeedManager(ProblemListener listener) {
        mInstance = this;
        mAlertIssuer = listener;
    }

    public static SpeedManager getInstance(ProblemListener listener) {
        return mInstance == null ? new SpeedManager(listener) : mInstance;
    }

    void addPing(Location loc) {
        int nextPos = pos == 9 ? 0 : pos + 1;
        Ping currentPing = pings[pos];
        if (currentPing == null)
            pings[nextPos] = new Ping(loc.getLongitude(), loc.getLatitude());
        else
            pings[nextPos] = new Ping(pings[pos], loc.getLongitude(), loc.getLatitude());
        Ping newPing = pings[nextPos];
        if (currentPing != null)
            if (newPing.getSpeed() - currentPing.getSpeed() < -10 && currentPing.getSpeed() < 1)
                mAlertIssuer.raiseFlag(newPing);
        pos = nextPos;

        SharedPreferencesManager mgr = SharedPreferencesManager.getManager();
        if (mgr == null)
            return;
        mgr.change(SHARED_PREFERENCES_SPEED, Float.valueOf(Double.toString(newPing.getSpeed())));
        mgr.change(SHARED_PREFERENCES_LAT, Float.valueOf(Double.toString(newPing.getLat())));
        mgr.change(SHARED_PREFERENCES_LON, Float.valueOf(Double.toString(newPing.getLon())));
    }


}
