package com.bikebeacon.background.audio_recording;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.IntentFilter;
import android.media.MediaRecorder;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.bikebeacon.background.dispatchers.NetworkDispatcher;
import com.bikebeacon.pojo.RecordingDoneCallback;
import com.bikebeacon.ui.RecordingDialogFragment;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.bikebeacon.background.utility.Constants.RESPONSE_INPUT;
import static com.bikebeacon.background.utility.Constants.RESPONSE_OUTPUT;
import static com.bikebeacon.background.utility.Constants.START_RECORDING;
import static com.bikebeacon.background.utility.GeneralUtility.getExternalStorageDir;

/**
 * Created by Alon on 8/22/2017.
 */

class RecordController implements RecordingDoneCallback {


    private static RecordController mController;
    private static boolean isRecording = false;
    private RecordingDialogFragment mRecordDialog;
    private MediaRecorder mRecorder;

    private File mOutputFile;

    private RecordController() {
        mController = this;
        mOutputFile = new File(getExternalStorageDir(), "/messageToServer.3gp");
        mRecordDialog = new RecordingDialogFragment();
        mRecordDialog.setStopListener(this);
    }

    static RecordController getController() {
        return mController == null ? new RecordController() : mController;
    }

    void startRecording(Activity activity) {
        synchronized (RecordController.class) {
            if (isRecording)
                return;
            RecordingDialogFragment.run = true;
            if (mRecordDialog.isAdded())
                return;
            mRecordDialog.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
            mRecordDialog.setCancelable(false);
            mRecordDialog.show(activity.getFragmentManager(), "");

            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setOutputFile(mOutputFile.getPath());
            Log.i("RecordController", "startRecording: " + mOutputFile.getPath());
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            try {
                mRecorder.prepare();
            } catch (IOException e) {
                Log.e("RecordController", "prepare() failed");
            }

            mRecorder.start();
            isRecording = true;
            LocalBroadcastManager.getInstance(activity).registerReceiver(new RecordingInitiationReceiver(activity), new IntentFilter(START_RECORDING));
        }
    }

    @Override
    public void onDone(Activity caller) {
        mRecordDialog.dismiss();
        RecordingDialogFragment.run = false;
        try {
            mRecorder.stop();
        } catch (RuntimeException e) {
            //continue.
            e.printStackTrace();
        }
        isRecording = false;
        mRecorder.reset();
        mRecorder.release();
        mRecorder = null;
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", mOutputFile.getName(),
                        RequestBody.create(MediaType.parse("audio/*"), mOutputFile))
                .build();
        NetworkDispatcher.getDispatcher().createRequest()
                .timeout(30)
                .url(NetworkDispatcher.URL_TYPES.CONVERSION.toStringWithParams(RESPONSE_OUTPUT + "=wav", RESPONSE_INPUT + "=3gp"))
                .body(requestBody)
                .method("POST")
                .build()
                .execute(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        if (response.code() != 200) {
                            Log.e("RecordController", "onResponse: Failed " + response.toString());
                        }
                    }
                });

    }

}
