package com.bikebeacon.background.utility;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.bikebeacon.background.utility.Constants.PACKAGE_NAME;

/**
 * Created by Mgr on 9/5/2017.
 */

public final class SharedPreferencesManager {

    private List<SharedPreferencesChangeListener> allChangeListeners;
    private Map<String, SharedPreferencesChangeListener> specificChangeListeners;
    private static SharedPreferences prefs;

    private static SharedPreferencesManager instance;

    public static SharedPreferencesManager getManager(@NonNull Activity caller) {
        return instance == null ? new SharedPreferencesManager(caller) : instance;
    }

    private SharedPreferencesManager(@NonNull Activity caller) {
        instance = this;
        prefs = caller.getSharedPreferences(PACKAGE_NAME, Context.MODE_APPEND);
        allChangeListeners = new LinkedList<>();
        specificChangeListeners = new HashMap<>();
    }


    public void addListener(@NonNull SharedPreferencesChangeListener listener) {
        allChangeListeners.add(listener);
    }

    public void addListener(@NonNull SharedPreferencesChangeListener listener, @NonNull String... keys) {
        for (String key : keys)
            specificChangeListeners.put(key, listener);
    }

    public void change(String key, Object newValue) {
        SharedPreferences.Editor editor = prefs.edit();
        if (newValue instanceof Boolean)
            editor.putBoolean(key, (Boolean) newValue);
        else if (newValue instanceof String)
            editor.putString(key, newValue.toString());
        else if (newValue instanceof Float)
            editor.putFloat(key, (Float) newValue);
        else if (newValue instanceof Integer)
            editor.putInt(key, (Integer) newValue);
        else if (newValue instanceof Long)
            editor.putLong(key, (Long) newValue);
        else
            throw new IllegalArgumentException("Only Boolean, String, Float, Integer & Long.");
        editor.apply();
    }

    public void release() {
        prefs = null;
    }
}
