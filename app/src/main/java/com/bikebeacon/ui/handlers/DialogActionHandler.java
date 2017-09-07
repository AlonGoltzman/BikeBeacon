package com.bikebeacon.ui.handlers;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;

import com.bikebeacon.R;
import com.bikebeacon.background.utility.SharedPreferencesManager;
import com.bikebeacon.ui.MapsActivity;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.bikebeacon.background.utility.Constants.SHARED_PREFERENCES_LOW_POWER_MODE;
import static com.bikebeacon.background.utility.Constants.SHARED_PREFERENCES_MAP_DRAW;
import static com.bikebeacon.background.utility.Constants.SHARED_PREFERENCES_REFRESH_TIME;

/**
 * Created by Mgr on 9/5/2017.
 */

public class DialogActionHandler implements CompoundButton.OnCheckedChangeListener, DiscreteSeekBar.OnProgressChangeListener {

    private SharedPreferencesManager mgr;

    public DialogActionHandler(Activity caller) {
        mgr = SharedPreferencesManager.getManager(caller);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()) {
            case R.id.low_power_mode:
                mgr.change(SHARED_PREFERENCES_LOW_POWER_MODE, compoundButton.isChecked());
                break;
            case R.id.map_draw:
                mgr.change(SHARED_PREFERENCES_MAP_DRAW, compoundButton.isChecked());
                break;
        }
    }

    @Override
    public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
        if (fromUser)
            mgr.change(SHARED_PREFERENCES_REFRESH_TIME, value);
    }

    @Override
    public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

    }
}
