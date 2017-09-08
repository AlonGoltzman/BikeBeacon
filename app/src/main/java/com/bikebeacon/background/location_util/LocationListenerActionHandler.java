package com.bikebeacon.background.location_util;

import android.location.Location;

import com.bikebeacon.pojo.ProblemListener;
import com.google.android.gms.location.LocationListener;

/**
 * Created by Alon on 9/7/2017.
 */

class LocationListenerActionHandler implements LocationListener {
    private CentralHandler mHandler;
    private SpeedManager mSpeedMgr;

    public LocationListenerActionHandler(CentralHandler handler) {
        mHandler = handler;
        mSpeedMgr = SpeedManager.getInstance(ProblemListener.getInstance());
    }


    @Override
    public void onLocationChanged(Location location) {
        mHandler.updateCameraPos(location);
        mSpeedMgr.addPing(location);
    }
}
