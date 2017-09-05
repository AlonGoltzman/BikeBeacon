package com.bikebeacon.background.dispatchers;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.BufferedSink;

/**
 * Created by Alon on 8/19/2017.
 */

public final class NetworkDispatcher {

    private static final boolean mIsEmulator = false;
    private static NetworkDispatcher mNetDispatcher;

    private NetworkDispatcher() {
        mNetDispatcher = this;
    }

    public static NetworkDispatcher getDispatcher() {
        return mNetDispatcher == null ? new NetworkDispatcher() : mNetDispatcher;
    }

    public NetworkRequestBuilder createRequest() {
        return new NetworkRequestBuilder();
    }

    public enum URL_TYPES {
        ALERT("/alert_api"),
        FILE("/jerry_api"),
        CONVERSION("/response_api"),
        REGISTRATION("/registration_api"),
        TOKEN("/token_api");

        private String URL;

        URL_TYPES(String path) {
            URL = path;
        }

        @Override
        public String toString() {
            return (mIsEmulator ? "http://10.0.0.2:8080" : "http://10.0.0.2:75") + URL;
        }

        public String toStringWithParams(String... params) {
            StringBuilder builder = new StringBuilder();
            builder.append(toString()).append("?").append(params[0]);
            for (int i = 1; i < params.length; i++) {
                String param = params[i];
                builder.append("&").append(param);
            }
            return builder.toString();
        }
    }

    @SuppressWarnings("WeakerAccess")
    public class NetworkRequestBuilder {

        private URL mURL;
        private String mHTTPMethod;
        private InputStream mBodyInput;
        private byte[] mInformation;
        private RequestBody mBody;
        private int timeout;

        private Request mRequest;

        public NetworkRequestBuilder url(String url) {
            try {
                return url(new URL(url));
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            }
        }

        public NetworkRequestBuilder url(URL url) {
            mURL = url;
            return this;
        }

        public NetworkRequestBuilder method(String method) {
            if (method.equals("POST") || method.equals("GET"))
                mHTTPMethod = method;
            else
                throw new IllegalArgumentException("Only POST or GET.");
            return this;
        }

        public NetworkRequestBuilder body(byte[] information) {
            if (information != null)
                mInformation = information;
            else
                throw new IllegalArgumentException("Byte[] can't be null.");
            return this;
        }

        public NetworkRequestBuilder body(InputStream stream) {
            mBodyInput = stream;
            return this;
        }

        public NetworkRequestBuilder body(RequestBody body) {
            mBody = body;
            return this;
        }

        public NetworkRequestBuilder timeout(int seconds) {
            timeout = seconds;
            return this;
        }

        public NetworkRequestBuilder build() {
            mRequest = new Request.Builder().url(mURL).method(mHTTPMethod, mHTTPMethod.toLowerCase().equals("get") ? null : mBody == null ? new RequestBody() {
                @Nullable
                @Override
                public MediaType contentType() {
                    return null;
                }

                @Override
                public void writeTo(@NonNull BufferedSink sink) throws IOException {
                    if (mInformation != null)
                        sink.write(mInformation);
                    else if (mBodyInput != null) {
                        byte[] buffer = new byte[1024];

                        while (mBodyInput.read(buffer) != -1)
                            sink.write(buffer);
                    }
                }
            } : mBody).build();
            return this;
        }

        public void execute(Callback callback) {
            Call call = null;
            if (mRequest != null)
                call = new OkHttpClient.Builder().connectTimeout(timeout, TimeUnit.SECONDS).writeTimeout(timeout, TimeUnit.SECONDS).readTimeout(timeout, TimeUnit.SECONDS).build().newCall(mRequest);
            if (call != null)
                if (callback == null)
                    try {
                        call.execute();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                else
                    call.enqueue(callback);
        }
    }
}