package com.example.apm1.androidrobotcontroller;

import android.app.Activity;
import android.support.annotation.IdRes;

/**
 * Created by WILLIAM LIN on 10/10/2017 for the Android Robot Controller.
 * THIS IS PROBABLY A USEFUL CLASS
 */

public class VuforiaParameters {

    public String vuforiaLicenseKey = "turtleman";

    public int CameraDirection = -1;

    public boolean useExtendedTracking = true;

    public String cameraMonitor = "AXES";

    public @IdRes
    int cameraMonitorViewParent = 0;

    public Activity activity = null;

    public VuforiaParameters(){}

    public VuforiaParameters(@IdRes int cameraMonitorViewIdParent) {
        cameraMonitorViewParent = cameraMonitorViewIdParent;
    }

    public void setVuforiaLicenseKey(String key){
        vuforiaLicenseKey=key;
    }

    public String getVuforiaLicenseKey(){ return vuforiaLicenseKey;}
}
