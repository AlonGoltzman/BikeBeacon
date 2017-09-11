package com.bikebeacon.background.location_util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.bikebeacon.background.broadcast_receiveres.PermissionUpdateReceiver;
import com.bikebeacon.background.broadcast_receiveres.ResolutionDoneReceiver;
import com.bikebeacon.background.utility.SharedPreferencesManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import static com.bikebeacon.background.utility.Constants.BROADCAST_ACTION_GOT_PERMISSIONS;
import static com.bikebeacon.background.utility.Constants.BROADCAST_ACTION_RESOLVE;
import static com.bikebeacon.background.utility.Constants.BROADCAST_ACTION_RESOLVED;
import static com.bikebeacon.background.utility.Constants.BROADCAST_ACTION_UPDATED_LOCATION;
import static com.bikebeacon.background.utility.Constants.BROADCAST_CONNECTION_RESULT;
import static com.bikebeacon.background.utility.Constants.BROADCAST_UPDATED_LOCATION;
import static com.bikebeacon.background.utility.GeneralUtility.getCellInfo;
import static com.bikebeacon.background.utility.GeneralUtility.hasPermissions;

/**
 * Created by Alon on 9/7/2017.
 */

@SuppressWarnings("MissingPermission")
public class CentralHandler implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback {

    private static final String TAG = CentralHandler.class.getSimpleName();

    private static final int MAX_INTERVAL = 2500;
    private static final int MIN_INTERVAL = 500;
    @SuppressLint("StaticFieldLeak")
    private static CentralHandler mInstance;
    private Activity mActivity;
    private Context mContext;
    private GoogleApiClient mAPI;
    private Location mLastLoc;
    private LocationRequest mLocRequest;
    private SharedPreferencesManager mPrefsMgr;
    private GoogleMap mMap;
    private LocationListenerActionHandler mLocHandler;
    private boolean mListeningForLocationUpdated = false;

    private CentralHandler(Context invoker, boolean isActivity) {
        mInstance = this;
        if (isActivity)
            mActivity = (Activity) invoker;
        else
            mContext = invoker;
        mPrefsMgr = SharedPreferencesManager.getManager(invoker);
        mLocHandler = new LocationListenerActionHandler(this);

        LocalBroadcastManager mgr = null;
        if (mActivity != null)
            mgr = LocalBroadcastManager.getInstance(mActivity);
        else if (mContext != null)
            mgr = LocalBroadcastManager.getInstance(mContext);
        assert mgr != null;
        mgr.registerReceiver(new PermissionUpdateReceiver().setHandler(this), new IntentFilter(BROADCAST_ACTION_GOT_PERMISSIONS));
        mgr.registerReceiver(new ResolutionDoneReceiver().setHandler(this), new IntentFilter(BROADCAST_ACTION_RESOLVED));
    }

    public static CentralHandler getHandler(Context invoker, boolean isActivity) {
        if (mInstance != null)
            if (isActivity && invoker instanceof Activity)
                mInstance.setActivity((Activity) invoker);
            else if (!isActivity)
                mInstance.setContext(invoker);
        return mInstance == null ? new CentralHandler(invoker, isActivity) : mInstance;
    }

    @Nullable
    public static CentralHandler getHandler() {
        return mInstance;
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (mMap == null)
            return;
        if (!hasPermissions())
            return;
        //Enable the 'current location' layer (the light blue dot) to indicate the user's location,
        //and also add a button (at the top right side), which centers the map on the user's location.
        mMap.setMyLocationEnabled(true);
        //Set the Map type
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        //Determine the availability of location data on the device.
        LocationAvailability locationAvailability = LocationServices.FusedLocationApi
                .getLocationAvailability(mAPI);
        if (locationAvailability != null && locationAvailability.isLocationAvailable()) {
            //Give the most recent current location available
            mLastLoc = LocationServices.FusedLocationApi.getLastLocation(mAPI);
            if (mLastLoc != null)
                updateCameraPos(mLastLoc);
        }
        if (!mListeningForLocationUpdated)
            start();

    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution())
            requestResolution(connectionResult);
        else
            Log.e("Error", "Location services connection failed with code " + connectionResult.getErrorCode());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        onConnected(null);
    }

    public void updateCameraPos(Location newLocation) {
        LatLng latLng = new LatLng(newLocation.getLatitude(), newLocation.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 13);
        mMap.animateCamera(cameraUpdate);
        Intent intent = new Intent(BROADCAST_ACTION_UPDATED_LOCATION);
        Bundle bundle = new Bundle();
        bundle.putParcelable(BROADCAST_UPDATED_LOCATION, newLocation);
        intent.putExtras(bundle);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    public void setAPIClient(GoogleApiClient APIClient) {
        mAPI = APIClient;
        if (mLocRequest == null)
            genLocationRequest();
    }

    private void registerForLocationUpdated() {
        if (!hasPermissions())
            return;
        Log.d(TAG, "registerForLocationUpdated() called Requesting location updates.");
        LocationServices.FusedLocationApi.requestLocationUpdates(mAPI, mLocRequest, mLocHandler);
        mListeningForLocationUpdated = true;
    }

    public void setActivity(Activity activity) {
        this.mActivity = activity;
    }

    public String getCellTowerInfo() {
        return getCellInfo(mContext);
    }

    public Context getContext() {
        return mContext;
    }

    public void setContext(Context context) {
        mContext = context;
    }

    private void requestResolution(Parcelable p) {
        Bundle extras = new Bundle();
        extras.putParcelable(BROADCAST_CONNECTION_RESULT, p);
        Intent intent = new Intent(BROADCAST_ACTION_RESOLVE);
        intent.putExtras(extras);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }


    private void genLocationRequest() {
        mLocRequest = new LocationRequest();
        //Specify the rate at which your App will like to receive updates.
        mLocRequest.setInterval(MAX_INTERVAL);
        //Specify the fastest rate at which the App can handle updates.
        mLocRequest.setFastestInterval(MIN_INTERVAL);
        mLocRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        //Check the state of the user’s location settings.
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocRequest);
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi
                .checkLocationSettings(mAPI, builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        mListeningForLocationUpdated = true;
                        registerForLocationUpdated();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        requestResolution(result);
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });
    }

    //====================================================================================
    public void start() {
        if (mAPI != null)
            if (!mAPI.isConnected() && !mAPI.isConnecting()) {
                mAPI.connect();
                Log.d(TAG, "start() called but not connected");
            } else {
                resume();
                Log.d(TAG, "start() called and resume called");
            }
    }

    public void stop() {
        if (mAPI.isConnected() && mListeningForLocationUpdated)
            LocationServices.FusedLocationApi.removeLocationUpdates(mAPI, mLocHandler);
    }

    public void resume() {
        Log.d(TAG, "resume() called");
        if (mAPI.isConnected() && !mListeningForLocationUpdated) {
            Log.d(TAG, "resume() called and requested updates.");
            registerForLocationUpdated();
        }

    }

    public void pause() {
        if (mAPI != null && mAPI.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mAPI, mLocHandler);
            mAPI.disconnect();
        }
    }

    public void destroy() {
        if (mActivity != null)
            mActivity = null;
    }
    //====================================================================================


}
