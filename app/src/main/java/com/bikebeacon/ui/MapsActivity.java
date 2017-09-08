package com.bikebeacon.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.TextView;
import android.widget.Toast;

import com.bikebeacon.R;
import com.bikebeacon.background.broadcast_receiveres.ResolutionReceiver;
import com.bikebeacon.background.fcm.FCMTokenUploadASTask;
import com.bikebeacon.background.location_util.CentralHandler;
import com.bikebeacon.background.location_util.LocationUpdateService;
import com.bikebeacon.background.server_interactions.DeviceRegistrationASTask;
import com.bikebeacon.background.utility.SharedPreferencesManager;
import com.bikebeacon.pojo.SharedPreferencesChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.firebase.iid.FirebaseInstanceId;

import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static com.bikebeacon.background.utility.Constants.BROADCAST_ACTION_GOT_PERMISSIONS;
import static com.bikebeacon.background.utility.Constants.BROADCAST_ACTION_RESOLVE;
import static com.bikebeacon.background.utility.Constants.BROADCAST_ACTION_RESOLVED;
import static com.bikebeacon.background.utility.Constants.PERMISSION_REQUEST_CODE;
import static com.bikebeacon.background.utility.Constants.REQUEST_CHECK_SETTINGS;
import static com.bikebeacon.background.utility.Constants.SHARED_PREFERENCES_LAT;
import static com.bikebeacon.background.utility.Constants.SHARED_PREFERENCES_LON;
import static com.bikebeacon.background.utility.Constants.SHARED_PREFERENCES_SPEED;
import static com.bikebeacon.background.utility.GeneralUtility.isFirstRun;
import static com.bikebeacon.background.utility.GeneralUtility.requestRuntimePermissions;
import static com.bikebeacon.background.utility.GeneralUtility.setHasPermissions;

/**
 * GoogleApiClient.ConnectionCallbacks provides callbacks that are triggered when the client is connected (onConnected())
 * or temporarily disconnected (onConnectionSuspended()) from the service.
 * <p>
 * GoogleApiClient.OnConnectionFailedListener provides a callback method (onConnectionFailed())
 * that is triggered when an attempt to connect the client to the service results in a failure.
 * <p>
 * GoogleMap.OnMarkerClickListener defines the onMarkerClick() which is called when a marker is clicked or tapped.
 * <p>
 * LocationListener defines the onLocationChanged() which is called when a user’s location changes.
 * This method is only called if the LocationListener has been registered.
 */

@SuppressWarnings("MissingPermission")
public class MapsActivity extends FragmentActivity implements SharedPreferencesChangeListener {

    private TextView mSpeedVal;
    private TextView mLatVal;
    private TextView mLonVal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        requestRuntimePermissions(this);

        SharedPreferencesManager mgr = SharedPreferencesManager.getManager(this);

        if (isFirstRun(this)) {
            new DeviceRegistrationASTask(mgr).execute();
        }

        String token = FirebaseInstanceId.getInstance().getToken();
        if (token != null && !token.isEmpty()) {
            new FCMTokenUploadASTask().execute(token);
        }

        startService(new Intent(MapsActivity.this, LocationUpdateService.class));
        LocalBroadcastManager.getInstance(this).registerReceiver(new ResolutionReceiver().setActivity(this), new IntentFilter(BROADCAST_ACTION_RESOLVE));

        init();

        mgr.addListener(this);
    }

    private void init() {
        SupportMapFragment fragment = SupportMapFragment.newInstance();
        try {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.map, fragment).commit();
            fragment.getMapAsync(CentralHandler.getHandler(this, true));
        } catch (Exception ignored) {

        }

        //   FloatingActionButton settings = findViewById(R.id.settings);
        mSpeedVal = findViewById(R.id.info_sec_speed);
        mLatVal = findViewById(R.id.info_sec_lat);
        mLonVal = findViewById(R.id.info_sec_lon);

//        settings.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                DialogPlus dialog = DialogPlus.newDialog(view.getContext())
//                        .setContentHolder(new ViewHolder(R.layout.settings_dialog))
//                        .create();
//                DialogActionHandler handler = new DialogActionHandler(MapsActivity.this);
//                Switch lowPower = dialog.getHolderView().findViewById(R.id.low_power_mode);
//                Switch mapDraw = dialog.getHolderView().findViewById(R.id.map_draw);
//                DiscreteSeekBar refresh = dialog.getHolderView().findViewById(R.id.stat_refresh_time);
//                lowPower.setOnCheckedChangeListener(handler);
//                mapDraw.setOnCheckedChangeListener(handler);
//                refresh.setOnProgressChangeListener(handler);
//                dialog.show();
//
//            }
//        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                for (int grantResult : grantResults)
                    if (grantResult == PERMISSION_DENIED) {
                        Toast.makeText(this, getString(R.string.permission_alert), Toast.LENGTH_SHORT).show();
                        return;
                    }
                setHasPermissions(true);
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(BROADCAST_ACTION_GOT_PERMISSIONS));
        }
    }

    /**
     * Start the update request regarding the user's location.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS)
            if (resultCode == RESULT_OK)
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(BROADCAST_ACTION_RESOLVED));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CentralHandler.getHandler(this, true).destroy();
    }

    @Override
    public void onChange(String key, Object newValue) {
        TextView view = null;
        int stringID = -1;
        switch (key) {
            case SHARED_PREFERENCES_SPEED:
                view = mSpeedVal;
                stringID = R.string.speed;
                break;
            case SHARED_PREFERENCES_LAT:
                view = mLatVal;
                stringID = R.string.lat;
                break;
            case SHARED_PREFERENCES_LON:
                view = mLonVal;
                stringID = R.string.lon;
                break;
        }
        if (key.equals(SHARED_PREFERENCES_SPEED) || key.equals(SHARED_PREFERENCES_LAT) || key.equals(SHARED_PREFERENCES_LON))
            updateValue(view, stringID, (Float.valueOf(newValue.toString())));
    }

    @SuppressLint("DefaultLocale")
    private void updateValue(final TextView viewToUpdate, final int stringID, final float value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                viewToUpdate.setText(getString(stringID, value));
            }
        });
    }
}
