package com.bikebeacon.pojo;

import com.bikebeacon.background.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import static com.bikebeacon.background.Constants.JSON_ACTION;
import static com.bikebeacon.background.Constants.JSON_CELLTOWERS;
import static com.bikebeacon.background.Constants.JSON_GPS;
import static com.bikebeacon.background.Constants.JSON_IS_CLOSED;
import static com.bikebeacon.background.Constants.JSON_OWNER;
import static com.bikebeacon.background.Constants.JSON_PREVIOUS_ALERT;

/**
 * Created by Alon on 8/18/2017.
 */

public class Alert extends Action {

    private String mOwner;
    private String mGPSCoords;
    private String mPreviousAlertId;
    private String mCellTowersID;
    private boolean mIsClosed;

    public Alert(Constants.AlertAction action) {
        super(action.toString());
    }

    public void setOwner(String mOwner) {
        this.mOwner = mOwner;
    }

    public void setGPSCoords(String mGPSCoords) {
        this.mGPSCoords = mGPSCoords;
    }

    public void setPreviousAlertId(String mPreviousAlertId) {
        this.mPreviousAlertId = mPreviousAlertId;
    }

    public void setCellTowersID(String mCellTowersID) {
        this.mCellTowersID = mCellTowersID;
    }

    public void setIsClosed(boolean mIsClosed) {
        this.mIsClosed = mIsClosed;
    }

    @Override
    public String toJSON() {
        JSONObject object = new JSONObject();
        try {
            object.put(JSON_ACTION, getAction() == null || getAction().isEmpty() ? "null" : getAction());
            object.put(JSON_IS_CLOSED, mIsClosed);
            object.put(JSON_OWNER, mOwner);
            object.put(JSON_GPS, mGPSCoords);
            object.put(JSON_PREVIOUS_ALERT, mPreviousAlertId);
            object.put(JSON_CELLTOWERS, mCellTowersID);
            return object.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
