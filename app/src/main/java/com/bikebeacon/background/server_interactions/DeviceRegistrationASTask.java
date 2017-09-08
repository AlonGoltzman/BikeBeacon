package com.bikebeacon.background.server_interactions;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.bikebeacon.background.dispatchers.NetworkDispatcher;
import com.bikebeacon.background.utility.SharedPreferencesManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.bikebeacon.background.utility.Constants.MAILING_LIST_NAME;
import static com.bikebeacon.background.utility.Constants.MAILING_LIST_NUMBER;
import static com.bikebeacon.background.utility.Constants.SHARED_PREFERENCES_FIRST_RUN;
import static com.bikebeacon.background.utility.Constants.SHARED_PREFERENCES_UUID;
import static com.bikebeacon.background.utility.GeneralUtility.setUUID;

public class DeviceRegistrationASTask extends AsyncTask<Void, Void, Void> implements Callback {

    private final String TAG = "DeviceRegistrationAST";
    private SharedPreferencesManager mgr;

    public DeviceRegistrationASTask(SharedPreferencesManager spMgr) {
        mgr = spMgr;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        JSONArray object = new JSONArray();
        JSONObject adam = new JSONObject();
        try {
            adam.put(MAILING_LIST_NAME, "Adam").put(MAILING_LIST_NUMBER, "0526340773");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        object.put(adam);
        NetworkDispatcher.getDispatcher()
                .createRequest()
                .url(NetworkDispatcher.URL_TYPES.REGISTRATION.toString())
                .method("POST")
                .body(object.toString().getBytes())
                .build()
                .execute(this);
        return null;
    }

    @Override
    public void onFailure(@NonNull Call call, @NonNull IOException e) {
        Log.e(TAG, "onFailure: Failed registering device.", e);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
        if (response.code() != 200) {
            Log.i(TAG, "onResponse: response from registration isn't 200.\n" + response.body().string());
            return;
        }
        try {
            InputStream stream = response.body().byteStream();

            byte[] buffer = new byte[1024];
            int aRead;
            StringBuilder builder = new StringBuilder();

            while ((aRead = stream.read(buffer)) != -1)
                builder.append(new String(buffer, 0, aRead));

            stream.close();

            mgr.change(SHARED_PREFERENCES_UUID, builder.toString());
            mgr.change(SHARED_PREFERENCES_FIRST_RUN, true);
            setUUID(builder.toString());
        } catch (IOException e) {
            Log.e(TAG, "onResponse: error reading input stream from response.", e);
        }
    }
}
