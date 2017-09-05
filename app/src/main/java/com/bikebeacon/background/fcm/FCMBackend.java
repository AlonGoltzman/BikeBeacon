package com.bikebeacon.background.fcm;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.bikebeacon.background.dispatchers.NetworkDispatcher;
import com.bikebeacon.pojo.ExoPlayerEventAdapter;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.bikebeacon.background.utility.Constants.FCM_CALL;
import static com.bikebeacon.background.utility.Constants.FCM_URL;
import static com.bikebeacon.background.utility.Constants.PACKAGE_NAME;
import static com.bikebeacon.background.utility.Constants.SHARED_PREFERENCES_NUMBER_TO_CALL;
import static com.bikebeacon.background.utility.Constants.START_RECORDING;
import static com.bikebeacon.background.utility.GeneralUtility.getExternalStorageDir;
import static com.google.android.gms.wearable.DataMap.TAG;

/**
 * Created by Alon on 8/19/2017.
 */

public class FCMBackend extends FirebaseMessagingService implements Callback {

    private SimpleExoPlayer mPlayer;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.i("FCMBackend", "onMessageReceived: Received message");
        if (mPlayer == null) {
            Log.i("FCMBackend", "onMessageReceived: Starting message sequence");
            String key = remoteMessage.getData().get(FCM_URL);
            String phoneNumber = remoteMessage.getData().get(FCM_CALL);
            getSharedPreferences(PACKAGE_NAME, MODE_APPEND).edit().putString(SHARED_PREFERENCES_NUMBER_TO_CALL, phoneNumber).apply();
            BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
            TrackSelection.Factory videoTrackSelectionFactory =
                    new AdaptiveTrackSelection.Factory(bandwidthMeter);
            TrackSelector trackSelector =
                    new DefaultTrackSelector(videoTrackSelectionFactory);
            mPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
            if (!key.equals("null"))
                NetworkDispatcher
                        .getDispatcher()
                        .createRequest()
                        .url(NetworkDispatcher.URL_TYPES.FILE.toStringWithParams(key))
                        .method("GET")
                        .timeout(30).build()
                        .execute(this);
        }
    }

    @Override
    public void onFailure(@NonNull Call call, @NonNull IOException e) {
        e.printStackTrace();
    }

    @Override
    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
        File output = new File(getExternalStorageDir(), "/messageFromServer.wav");
        if (!getExternalStorageDir().exists())
            if (!getExternalStorageDir().mkdirs())
                Log.i(TAG, "onResponse: failed creating parent directory.");
        if (output.exists())
            if (!output.delete())
                Log.i(TAG, "onResponse: failed deleting messageFromServer.wav");
        if (!output.exists())
            if (!output.createNewFile())
                Log.i(TAG, "onResponse: failed creating new file.");
        byte[] bytes = null;
        if (response.body() != null)
            //noinspection ConstantConditions
            bytes = response.body().bytes();
        if (output.exists() && bytes != null) {
            FileOutputStream stream = new FileOutputStream(output);
            stream.write(bytes);
            stream.flush();
            stream.close();
            response.close();
// Measures bandwidth during playback. Can be null if not required.
            DefaultBandwidthMeter bandwidthMeter1 = new DefaultBandwidthMeter();
// Produces DataSource instances through which media data is loaded.
            DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
                    Util.getUserAgent(this, "BikeBeacon"), bandwidthMeter1);
// Produces Extractor instances for parsing the media data.
            ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
// This is the MediaSource representing the media to be played.
            MediaSource videoSource = new ExtractorMediaSource(Uri.fromFile(output),
                    dataSourceFactory, extractorsFactory, null, null);
// Prepare the player with the source.
            mPlayer.prepare(videoSource);
            mPlayer.addListener(new ExoPlayerEventAdapter() {
                @Override
                public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                    switch (playbackState) {
                        case Player.STATE_ENDED:
                            mPlayer.release();
                            mPlayer = null;
                            LocalBroadcastManager.getInstance(FCMBackend.this).sendBroadcast(new Intent(START_RECORDING));
                            break;
                    }
                }
            });
            mPlayer.setPlayWhenReady(true);
        } else if (output.exists()) {
            Log.i(TAG, "onResponse: No response body.");
        }
    }

}
