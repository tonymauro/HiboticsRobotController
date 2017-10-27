package com.example.apm1.androidrobotcontroller;

import com.vuforia.State;

/**
 * Created by WILLIAM LIN on 10/19/2017 for the Android Robot Controller.
 * THIS IS AN INTERFACE SO THAT I CAN IMPLEMENT THSI IN MY ACTIVITY
 * BY DOING THIS, METHODS IN THE INTERFACE CAN BE CALLED BY THE VUFORIACLASS
 */

public interface VuforiaActInterface {

    boolean initTrackers();

    boolean loadData();

    boolean startTrackers();

    boolean stopTrackers();

    boolean unloadTrackerData();

    boolean loadTrackerData();

    void onVuforiaUpdate(State state);

    void onInitArDone();
}
