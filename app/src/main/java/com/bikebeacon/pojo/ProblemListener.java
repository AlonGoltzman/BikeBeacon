package com.bikebeacon.pojo;

import android.annotation.SuppressLint;
import android.content.Context;

import com.bikebeacon.background.dispatchers.AlertDispatcher;
import com.bikebeacon.background.location_util.CentralHandler;
import com.bikebeacon.background.utility.Constants;
import com.bikebeacon.background.utility.SharedPreferencesManager;

import static com.bikebeacon.background.utility.Constants.SHARED_PREFERENCES_UUID;
import static com.bikebeacon.background.utility.GeneralUtility.getCellInfo;

/**
 * Created by Alon on 9/8/2017.
 */

public class ProblemListener {

    private static ProblemListener mInstance;

    private ProblemListener() {
        mInstance = this;
    }

    public static ProblemListener getInstance() {
        return mInstance == null ? new ProblemListener() : mInstance;
    }

    @SuppressLint("DefaultLocale")
    public void raiseFlag(Ping location) {
        CentralHandler handler = CentralHandler.getHandler();
        if (handler == null)
            throw new RuntimeException("Handler is null.");
        Context context = handler.getContext();
        if (context == null)
            throw new RuntimeException("Context is null.");
        SharedPreferencesManager mgr = SharedPreferencesManager.getManager();
        if (mgr == null)
            throw new RuntimeException("SharedPreferencesManager is null.");
        String UUID = String.valueOf(mgr.get(SHARED_PREFERENCES_UUID));
        if (UUID.equals("null"))
            throw new RuntimeException("Device not registered yet.");
        Alert alert = new Alert(Constants.AlertAction.ALERT_NEW);
        alert.setGPSCoords(String.format("%f,%f", location.getLat(), location.getLon()));
        alert.setCellTowersID(getCellInfo(context));
        alert.setIsClosed(false);
        alert.setUUID(UUID);
        AlertDispatcher.getDispatcher().fireAlert(alert, null);
    }
}
