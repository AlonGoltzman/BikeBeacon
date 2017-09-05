package com.bikebeacon.background.utility;

/**
 * Created by Alon on 8/18/2017.
 */

public final class Constants {
    public static final String PACKAGE_NAME = "com.bikebeacon";

    public static final String JSON_ACTION = "action";
    public static final String JSON_OWNER = "owner";
    public static final String JSON_CELLTOWERS = "towers";
    public static final String JSON_GPS = "coords";
    public static final String JSON_PREVIOUS_ALERT = "alert";
    public static final String JSON_IS_CLOSED = "closed";
    public static final String JSON_ID = "id";
    public static final String JSON_UUID = "uuid";

    public static final String ALERT_NEW = "newAlert";
    public static final String ALERT_CONVERSATION_RECEIVED = "conversationStarted";
    public static final String ALERT_DELTE = "deleteAlert";
    public static final String ALERT_UPDATE = "updateAlert";

    public static final String FCM_URL = "trackDownloadURL";
    public static final String FCM_CALL = "callNumber";

    public static final String START_RECORDING = "com.bikebeacon.START_RECORDING";

    public static final String SHARED_PREFERENCES_FIRST_RUN = PACKAGE_NAME + ".firstRun";
    public static final String SHARED_PREFERENCES_UUID = PACKAGE_NAME + ".UUID";
    public static final String SHARED_PREFERENCES_NUMBER_TO_CALL = PACKAGE_NAME + ".call";

    public static final String RESPONSE_INPUT = "inputFormat";
    public static final String RESPONSE_OUTPUT = "outputFormat";

    public static final String MAILING_LIST_NUMBER = "number";
    public static final String MAILING_LIST_NAME = "name";

    public static final int PERMISSION_REQUEST_CODE = 845012;

    public enum AlertAction {
        ALERT_NEW("newAlert"),
        ALERT_CONVERSATION_RECEIVED("conversationStarted"),
        ALERT_DELETE("deleteAlert"),
        ALERT_UPDATE("updateAlert");

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
