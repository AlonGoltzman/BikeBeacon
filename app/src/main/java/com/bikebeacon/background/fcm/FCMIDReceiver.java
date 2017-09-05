package com.bikebeacon.background.fcm;

import android.support.annotation.NonNull;
import android.util.Log;

import com.bikebeacon.background.dispatchers.NetworkDispatcher;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Alon on 8/18/2017.
 */

public class FCMIDReceiver extends FirebaseInstanceIdService implements Callback {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        NetworkDispatcher
                .getDispatcher()
                .createRequest()
                .url(NetworkDispatcher.URL_TYPES.TOKEN.toString())
                .body(FirebaseInstanceId.getInstance().getToken().getBytes())
                .method("POST")
                .build()
                .execute(this);

    }

    @Override
    public void onFailure(@NonNull Call call, @NonNull IOException e) {
        Log.e("FCMIDReceiver", "onFailure: Failed network operation", e);
    }

    @Override
    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
        if (response.code() != 200)
            Log.d("FCMIDReceiver", "onResponse: Failed network op: " + response.toString());
        else
            Log.i("FCMIDReceiver", "onResponse: Token updated");
    }
}
