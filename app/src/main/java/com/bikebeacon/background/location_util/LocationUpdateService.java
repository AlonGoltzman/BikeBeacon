package com.bikebeacon.background.location_util;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

public class LocationUpdateService extends Service {

    private GoogleApiClient apiClient;
    private CentralHandler mHandler;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        if (mHandler == null)
            mHandler = CentralHandler.getHandler(this, false);
        if (apiClient == null)
            apiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(mHandler)
                    .addOnConnectionFailedListener(mHandler)
                    .addApi(LocationServices.API)
                    .build();
        mHandler.setAPIClient(apiClient);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (mHandler != null)
            mHandler.start();
        return START_STICKY;
    }
}
