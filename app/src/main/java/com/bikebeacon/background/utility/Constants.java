package com.bikebeacon.background.utility;

import com.bikebeacon.ui.MapsActivity;

/**
 * Created by Alon on 8/18/2017.
 */

public final class Constants {
    public static final String PACKAGE_NAME = "com.bikebeacon";

    public static final String JSON_ID = "id";
    public static final String JSON_UUID = "uuid";
    public static final String JSON_GPS = "coords";
    public static final String JSON_OWNER = "owner";
    public static final String JSON_ACTION = "action";
    public static final String JSON_IS_CLOSED = "closed";
    public static final String JSON_CELLTOWERS = "towers";
    public static final String JSON_PREVIOUS_ALERT = "alert";

    public static final String ALERT_NEW = "newAlert";
    public static final String ALERT_DELTE = "deleteAlert";
    public static final String ALERT_UPDATE = "updateAlert";
    public static final String ALERT_CONVERSATION_RECEIVED = "conversationStarted";

    public static final String FCM_URL = "trackDownloadURL";
    public static final String FCM_CALL = "callNumber";

    public static final String START_RECORDING = "com.bikebeacon.START_RECORDING";

    public static final String SHARED_PREFERENCES_LON = PACKAGE_NAME + ".lon";
    public static final String SHARED_PREFERENCES_LAT = PACKAGE_NAME + ".lat";
    public static final String SHARED_PREFERENCES_UUID = PACKAGE_NAME + ".UUID";
    public static final String SHARED_PREFERENCES_SPEED = PACKAGE_NAME + ".speed";
    public static final String SHARED_PREFERENCES_MAP_DRAW = PACKAGE_NAME + ".mapDraw";
    public static final String SHARED_PREFERENCES_FIRST_RUN = PACKAGE_NAME + ".firstRun";
    public static final String SHARED_PREFERENCES_NUMBER_TO_CALL = PACKAGE_NAME + ".call";
    public static final String SHARED_PREFERENCES_LOW_POWER_MODE = PACKAGE_NAME + ".lowPower";
    public static final String SHARED_PREFERENCES_REFRESH_TIME = PACKAGE_NAME + ".refreshTime";

    public static final String RESPONSE_INPUT = "inputFormat";
    public static final String RESPONSE_OUTPUT = "outputFormat";

    public static final String MAILING_LIST_NAME = "name";
    public static final String MAILING_LIST_NUMBER = "number";

    public static final float EARTH_RADIUS_KM = 6371;
    public static final int REQUEST_CHECK_SETTINGS = 2;
    public static final int PERMISSION_REQUEST_CODE = 845012;
    public static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    public static final String BROADCAST_ACTION_RESOLVE = MapsActivity.class.getSimpleName() + ".BROADCAST_ACTION_RESOLVE";
    public static final String BROADCAST_ACTION_RESOLVED = MapsActivity.class.getSimpleName() + ".BROADCAST_ACTION_RESOLVED";
    public static final String BROADCAST_UPDATED_LOCATION = MapsActivity.class.getSimpleName() + ".BROADCAST_UPDATED_LOCATION";
    public static final String BROADCAST_CONNECTION_RESULT = MapsActivity.class.getSimpleName() + ".BROADCAST_CONNECTION_RESULT";
    public static final String BROADCAST_ACTION_GOT_PERMISSIONS = MapsActivity.class.getSimpleName() + ".BROADCAST_ACTION_GOT_PERMISSIONS";
    public static final String BROADCAST_ACTION_UPDATED_LOCATION = MapsActivity.class.getSimpleName() + ".BROADCAST_ACTION_UPDATED_LOCATION";

    public enum AlertAction {
        ALERT_NEW("newAlert"),
        ALERT_DELETE("deleteAlert"),
        ALERT_UPDATE("updateAlert"),
        ALERT_CONVERSATION_RECEIVED("conversationStarted");

        private String mAction;

        AlertAction(String action) {
            mAction = action;
        }

        @Override
        public String toString() {
            return mAction;
        }
    }

}
