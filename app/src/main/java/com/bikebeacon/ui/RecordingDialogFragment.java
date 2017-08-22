package com.bikebeacon.ui;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bikebeacon.R;
import com.bikebeacon.pojo.RecordingDoneCallback;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Alon on 8/22/2017.
 */

public class RecordingDialogFragment extends DialogFragment {

    private int mTimeLeft = 30;
    private TextView mTitle;
    private Timer mExecutionTimer;

    private RecordingDoneCallback mStopListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recording_dialog, container, false);
        mTitle = view.findViewById(R.id.record_dialog_title);
        mExecutionTimer = new Timer();
        mExecutionTimer.schedule(new UpdateTask(), 0);
        return view;
    }

    public void setStopListener(RecordingDoneCallback mStopListener) {
        this.mStopListener = mStopListener;
    }

    private class UpdateTask extends TimerTask {

        @Override
        public void run() {

            RecordingDialogFragment.this.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String title = getString(R.string.recording_format);
                    title = String.format(title, String.valueOf(mTimeLeft--));
                    mTitle.setText(title);
                }
            });
            if (mTimeLeft > 0)
                mExecutionTimer.schedule(new UpdateTask(), 1000);
            else {
                if (mStopListener != null)
                    mStopListener.onDone();
                else
                    Log.e("ABC", "run: No RecordingDoneCallback set.");
            }

        }
    }
}
