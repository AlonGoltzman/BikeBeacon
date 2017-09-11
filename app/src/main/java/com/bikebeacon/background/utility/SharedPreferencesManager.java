package com.bikebeacon.background.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bikebeacon.pojo.SharedPreferencesChangeListener;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.bikebeacon.background.utility.Constants.PACKAGE_NAME;

/**
 * Created by Mgr on 9/5/2017.
 */

public final class SharedPreferencesManager {

    private static SharedPreferences prefs;
    private static SharedPreferencesManager instance;
    private List<SharedPreferencesChangeListener> allChangeListeners;
    private Map<String, SharedPreferencesChangeListener> specificChangeListeners;

    private SharedPreferencesManager(@NonNull Context caller) {
        instance = this;
        prefs = caller.getSharedPreferences(PACKAGE_NAME, Context.MODE_APPEND);
        allChangeListeners = new LinkedList<>();
        specificChangeListeners = new HashMap<>();
    }

    @Nullable
    public static SharedPreferencesManager getManager() {
        return instance;
    }

    public static SharedPreferencesManager getManager(@NonNull Context caller) {
        return instance == null ? new SharedPreferencesManager(caller) : instance;
    }

    public void addListener(@NonNull SharedPreferencesChangeListener listener) {
        allChangeListeners.add(listener);
    }

    public void addListener(@NonNull SharedPreferencesChangeListener listener, @NonNull String... keys) {
        for (String key : keys)
            specificChangeListeners.put(key, listener);
    }

    public SharedPreferencesManager change(String key, Object newValue) {
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
        if (specificChangeListeners.containsKey(key))
            specificChangeListeners.get(key).onChange(key, newValue);
        for (SharedPreferencesChangeListener allChangeListener : allChangeListeners)
            allChangeListener.onChange(key, newValue);

        return this;
    }

    public Object get(String key) {
        return prefs.getAll().get(key);
    }

    public void release() {
        prefs = null;
    }
}
