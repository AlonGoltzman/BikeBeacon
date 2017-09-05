package com.bikebeacon.background.utility;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;

import org.jetbrains.annotations.Contract;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import static com.bikebeacon.background.utility.Constants.PACKAGE_NAME;
import static com.bikebeacon.background.utility.Constants.PERMISSION_REQUEST_CODE;
import static com.bikebeacon.background.utility.Constants.SHARED_PREFERENCES_FIRST_RUN;
import static com.bikebeacon.background.utility.Constants.SHARED_PREFERENCES_UUID;

/**
 * Created by Alon on 8/20/2017.
 */

public final class GeneralUtility {

    private static String UUID;
    private static boolean hasPermissions = false;


    public static boolean hasExternalStorage() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) || Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY);
    }

    public static boolean canWriteToExternalStorage() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static File getExternalStorageDir() {
        return new File(Environment.getExternalStorageDirectory(), "/BikeBeacon");
    }

    public static boolean isFirstRun(Activity caller) {
        SharedPreferences prefs = caller.getSharedPreferences(PACKAGE_NAME, Context.MODE_APPEND);
        return !prefs.getBoolean(SHARED_PREFERENCES_FIRST_RUN, false);
    }

    public static boolean requestRuntimePermissions(Activity caller) {
        if (requiresRuntimePermissions()) {
            ArrayList<String> requiredPerms = new ArrayList<>(Arrays.asList(Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CALL_PHONE));
            for (String requiredPerm : requiredPerms)
                if (caller.checkSelfPermission(requiredPerm) == PackageManager.PERMISSION_GRANTED)
                    requiredPerms.remove(requiredPerm);
            if (requiredPerms.size() > 0) {
                String[] values = Arrays.copyOf(requiredPerms.toArray(), requiredPerms.size(), String[].class);
                caller.requestPermissions(values, PERMISSION_REQUEST_CODE);
                setHasPermissions(false);
                return true;
            }
        }
        setHasPermissions(true);
        return false;
    }

    public static boolean hasPermissions() {
        return hasPermissions;
    }

    public static void setHasPermissions(boolean flag) {
        hasPermissions = flag;
    }

    @Contract("null->fail")
    public static String getUUID(Activity caller) {
        if (UUID == null || UUID.isEmpty()) {
            UUID = caller.getSharedPreferences(PACKAGE_NAME, Context.MODE_APPEND).getString(SHARED_PREFERENCES_UUID, "null");
            if (UUID.equals("null"))
                return null;
        }
        return UUID;
    }

    @Contract("null->fail")
    public static void setUUID(String uuid) {
        if (uuid != null && !uuid.isEmpty())
            if (UUID != null && !UUID.isEmpty()) {
                if (!UUID.equals(uuid))
                    UUID = uuid;
            } else
                UUID = uuid;
        else
            throw new NullPointerException("UUID given is either null or empty.");
    }

    private static boolean requiresRuntimePermissions() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }
}
