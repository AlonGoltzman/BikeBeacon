package com.bikebeacon.ui;

import android.app.Activity;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.bikebeacon.R;
import com.bikebeacon.background.audio_recording.RecordingInitiationReceiver;
import com.bikebeacon.background.dispatchers.AlertDispatcher;
import com.bikebeacon.background.fcm.FCMTokenUploadASTask;
import com.bikebeacon.background.utility.Constants;
import com.bikebeacon.background.utility.SharedPreferencesManager;
import com.bikebeacon.pojo.Alert;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.bikebeacon.background.utility.Constants.SHARED_PREFERENCES_UUID;
import static com.bikebeacon.background.utility.Constants.START_RECORDING;

public class TestActivity extends Activity implements View.OnClickListener, Callback {

    private static final String TAG = TestActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        String token = FirebaseInstanceId.getInstance().getToken();
        if (token != null && !token.isEmpty())
            new FCMTokenUploadASTask().execute(token);
        findViewById(R.id.btn_fire_alert).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        SharedPreferencesManager mgr = SharedPreferencesManager.getManager(this);
        switch (view.getId()) {
            case R.id.btn_fire_alert:
                if (mgr.get(SHARED_PREFERENCES_UUID) == null) {
                    Toast.makeText(this, "Device not registered yet.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(this, "Alert Shot Started", Toast.LENGTH_SHORT).show();
                Alert alert = new Alert(Constants.AlertAction.ALERT_NEW);
                alert.setGPSCoords("test,test");
                alert.setCellTowersID("test,test,test");
                alert.setIsClosed(false);
                alert.setPreviousAlertId("null");
                alert.setUUID(mgr.get(SHARED_PREFERENCES_UUID).toString());
                AlertDispatcher.getDispatcher().fireAlert(alert, this);
                LocalBroadcastManager.getInstance(this).registerReceiver(new RecordingInitiationReceiver(this), new IntentFilter(START_RECORDING));
                break;
        }
    }

    @Override
    public void onFailure(@NonNull Call call, @NonNull IOException e) {
        Log.e(TAG, "onFailure: Failed alert shot with exception", e);
    }

    @Override
    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
        if (response.code() != 200)
            Log.i(TAG, "onResponse: Failed alert shot with message: " + response.message());
    }
}