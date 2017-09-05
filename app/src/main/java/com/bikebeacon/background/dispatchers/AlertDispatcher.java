package com.bikebeacon.background.dispatchers;

import com.bikebeacon.pojo.Alert;

import okhttp3.Callback;

public final class AlertDispatcher {


    private static AlertDispatcher mDispatcher;

    private AlertDispatcher() {
        mDispatcher = this;
    }

    public static AlertDispatcher getDispatcher() {
        return mDispatcher == null ? new AlertDispatcher() : mDispatcher;
    }

    public void fireAlert(final Alert alert, Callback callback) {
        NetworkDispatcher
                .getDispatcher()
                .createRequest()
                .url(NetworkDispatcher.URL_TYPES.ALERT.toString())
                .body(alert.toJSON().getBytes())
                .method("POST")
                .build()
                .execute(callback);
    }

}
