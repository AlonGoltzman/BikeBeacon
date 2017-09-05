package com.bikebeacon.ui;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bikebeacon.R;
import com.google.android.gms.maps.MapFragment;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

public class SettingsActivity extends MapsActivity {

    Switch lowPowerMode;
    Switch mapDraw;
    TextView statRefreshTimeTv;
    DiscreteSeekBar statRefreshTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        lowPowerMode = findViewById(R.id.low_power_mode);
        mapDraw = findViewById(R.id.map_draw);
        statRefreshTimeTv = findViewById(R.id.stat_refresh_time_tv);
        statRefreshTime = findViewById(R.id.stat_refresh_time);

        lowPowerMode.setChecked(true);
        lowPowerMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    Toast.makeText(getApplicationContext(), "Low Power Mode is ON", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Low Power Mode is OFF", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mapDraw.setChecked(true);
        mapDraw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {

                } else {

                }
            }
        });

        statRefreshTime.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

            }
        });
    }

    public void showHideFragmentMap(final MapFragment mapFragment) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);

        if (mapFragment.isHidden()) {
            fragmentTransaction.show(mapFragment);
        } else {
            fragmentTransaction.hide(mapFragment);
        }
        fragmentTransaction.commit();
    }
}
