package com.bikebeacon.pojo;

/**
 * Created by Alon on 8/18/2017.
 */

abstract class Action {

    private String mAction;

    Action(String action) {
        mAction = action;
    }

    String getAction() {
        return mAction;
    }

    public abstract String toJSON();

}
