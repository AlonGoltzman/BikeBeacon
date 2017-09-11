package com.bikebeacon.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.TextView;

import com.bikebeacon.R;
import com.bikebeacon.background.broadcast_receiveres.ResolutionReceiver;
import com.bikebeacon.background.fcm.FCMTokenUploadASTask;
import com.bikebeacon.background.location_util.CentralHandler;
import com.bikebeacon.background.location_util.LocationUpdateService;
import com.bikebeacon.background.utility.SharedPreferencesManager;
import com.bikebeacon.pojo.SharedPreferencesChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.firebase.iid.FirebaseInstanceId;

import static com.bikebeacon.background.utility.Constants.BROADCAST_ACTION_RESOLVE;
import static com.bikebeacon.background.utility.Constants.BROADCAST_ACTION_RESOLVED;
import static com.bikebeacon.background.utility.Constants.REQUEST_CHECK_SETTINGS;
import static com.bikebeacon.background.utility.Constants.SHARED_PREFERENCES_LAT;
import static com.bikebeacon.background.utility.Constants.SHARED_PREFERENCES_LON;
import static com.bikebeacon.background.utility.Constants.SHARED_PREFERENCES_SPEED;
import static com.bikebeacon.background.utility.GeneralUtility.requestRuntimePermissions;

public class MapsActivity extends FragmentActivity implements SharedPreferencesChangeListener, View.OnClickListener {

    private TextView mSpeedVal;
    private TextView mLatVal;
    private TextView mLonVal;

    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        requestRuntimePermissions(this);

        SharedPreferencesManager mgr = SharedPreferencesManager.getManager(this);

        String token = FirebaseInstanceId.getInstance().getToken();
        if (token != null && !token.isEmpty())
            new FCMTokenUploadASTask().execute(token);


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
        mSpeedVal = findViewById(R.id.info_sec_speed);
        mLatVal = findViewById(R.id.info_sec_lat);
        mLonVal = findViewById(R.id.info_sec_lon);
        mLatVal.setOnClickListener(this);
        mLonVal.setOnClickListener(this);

    }

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

    @Override
    public void onClick(View v) {
        if (v.equals(mLonVal))
            if (count == 0 || count == 3 || count == 4)
                count++;
            else count = 0;
        else if (v.equals(mLatVal))
            if (count == 1 || count == 2 || count == 5)
                count++;
            else count = 0;

        if (count == 6)
            startActivity(new Intent(MapsActivity.this, TestActivity.class));
    }
}
