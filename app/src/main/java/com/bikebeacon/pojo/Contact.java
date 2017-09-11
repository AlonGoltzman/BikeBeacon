package com.bikebeacon.pojo;

import org.json.JSONException;
import org.json.JSONObject;

import static com.bikebeacon.background.utility.Constants.MAILING_LIST_NAME;
import static com.bikebeacon.background.utility.Constants.MAILING_LIST_NUMBER;

/**
 * Created by Alon on 9/10/2017.
 */

public class Contact {

    private String mName;
    private String mPhoneNum;
    private boolean mSelected = false;

    public Contact(String name, String phoneNumber) {
        mName = name;
        mPhoneNum = phoneNumber;
    }


    public String getPhoneNum() {
        return mPhoneNum;
    }

    public String getName() {
        return mName;
    }

    public boolean isSelected() {
        return mSelected;
    }

    public void setSelected(boolean mSelected) {
        this.mSelected = mSelected;
    }

    @Override
    public String toString() {
        return "Name: " + mName + ", Phone: " + mPhoneNum + ", is selected: " + mSelected;
    }

    public JSONObject toJSON() {
        try {
            return new JSONObject().put(MAILING_LIST_NAME, mName).put(MAILING_LIST_NUMBER, mPhoneNum);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
