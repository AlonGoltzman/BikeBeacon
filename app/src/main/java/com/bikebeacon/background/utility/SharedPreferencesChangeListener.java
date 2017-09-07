package com.bikebeacon.background.utility;

/**
 * Created by Mgr on 9/5/2017.
 */

public interface SharedPreferencesChangeListener {

    void onChange(String key, Object newValue);
}
