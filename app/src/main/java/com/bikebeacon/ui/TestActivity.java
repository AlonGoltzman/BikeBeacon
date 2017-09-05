package com.bikebeacon.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bikebeacon.R;
import com.bikebeacon.background.audio_recording.RecordingInitiationReceiver;
import com.bikebeacon.background.dispatchers.AlertDispatcher;
import com.bikebeacon.background.fcm.FCMTokenUploadASTask;
import com.bikebeacon.background.server_interactions.DeviceRegistrationASTask;
import com.bikebeacon.background.utility.Constants;
import com.bikebeacon.pojo.Alert;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.bikebeacon.background.utility.Constants.PACKAGE_NAME;
import static com.bikebeacon.background.utility.Constants.START_RECORDING;
import static com.bikebeacon.background.utility.GeneralUtility.getUUID;
import static com.bikebeacon.background.utility.GeneralUtility.isFirstRun;

public class TestActivity extends Activity implements View.OnClickListener, Callback {

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CALL_PHONE}, 123);
        }
        //Registering the device.
        if (isFirstRun(this))
            new DeviceRegistrationASTask(getSharedPreferences(PACKAGE_NAME, MODE_APPEND).edit()).execute();

        //Uploading the token regardless of if it was uploaded or not.
        String token = FirebaseInstanceId.getInstance().getToken();
        if (token != null && !token.isEmpty())
            new FCMTokenUploadASTask().execute(token);
        findViewById(R.id.btn_fire_alert).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_fire_alert:
                if (getUUID(this) == null) {
                    Toast.makeText(this, "Device not registered yet.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(this, "Alert Shot Started", Toast.LENGTH_SHORT).show();
                Alert alert = new Alert(Constants.AlertAction.ALERT_NEW);
                alert.setGPSCoords("test,test");
                alert.setCellTowersID("test,test,test");
                alert.setIsClosed(false);
                alert.setPreviousAlertId("null");
                alert.setUUID(getUUID(this));
                AlertDispatcher.getDispatcher().fireAlert(alert, this);
                LocalBroadcastManager.getInstance(this).registerReceiver(new RecordingInitiationReceiver(this), new IntentFilter(START_RECORDING));
                break;
        }
    }

    @Override
    public void onFailure(@NonNull Call call, @NonNull IOException e) {
        Log.e(TestActivity.class.getSimpleName(), "onFailure: Failed alert shot with exception", e);
    }

    @Override
    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
        if (response.code() != 200)
            Log.i(TestActivity.class.getSimpleName(), "onResponse: Failed alert shot with message: " + response.message());
    }
}