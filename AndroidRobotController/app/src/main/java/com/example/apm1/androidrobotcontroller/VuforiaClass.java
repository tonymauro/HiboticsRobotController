package com.example.apm1.androidrobotcontroller;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.vuforia.CameraDevice;
import com.vuforia.ObjectTracker;
import com.vuforia.State;
import com.vuforia.TrackerManager;
import com.vuforia.Vuforia;

/**
 * Created by WILLIAM LIN on 10/10/2017 for the Android Robot Controller.
 * THIS IS A CLASS FOR IMPLEMENTING VUFORIA
 * Sort of based off of the application session
 */

public class VuforiaClass implements Vuforia.UpdateCallbackInterface{

    VuforiaActInterface vuforiaAct;
    Activity activity;

    int cameraType;
    Boolean started;

    Boolean cameraRunning;

    VuforiaParameters VParams = new VuforiaParameters();

    TrackerManager tManager;

    public final Object lifecycleLock = new Object();

    public VuforiaClass(VuforiaActInterface vuforiaAct, VuforiaParameters params){
        Log.i("VUforiaClass", "before initializing");
        this.vuforiaAct = vuforiaAct;
        VParams = params;
   //     tManager = TrackerManager.getInstance();
        Log.i("VUforiaClass", "successfully initialized");
    }

    public void initAr(Activity activity){
        this.activity = activity;
        initVuforiaTask task = new initVuforiaTask();
        task.execute();
//        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
//                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void Vuforia_onUpdate(State state) {
        vuforiaAct.onVuforiaUpdate(state);
    }

    private class initVuforiaTask extends AsyncTask<Void, Integer, Boolean> {
        private int mProgressValue = -1;

        @Override
        protected Boolean doInBackground(Void... params){
            synchronized(lifecycleLock) {
                Log.i("VuforiaClass", "Before setInitParameters");

                Vuforia.setInitParameters(activity, 0, "ATFMRrb/////AAAAGd0LzE71kkPjnoWoigFinSJp/L4eGD/p4zlkw3hvVdhzoBV4onBi+nxzNEWkwxwc6pcrRfNsnn62e67HaHM7OaAllEOmreJBAd1WzwI23lN0lGRcPec5ZQEyPIItZs+rI1nODhoPLAPLwsY6GUYw33pyAQg79ZDabU27EzC8aUM3IsFyB0J/gklWtitN51sRIeNTiiL1NV1O8fpHxSdVqtSJ3WyLb5rv/2kutb/RntJD/dKXKE0T7l+ipW91d4b7u92eNZegfMzM79ooF3pmuUR1vxOn6N71zGWrnCFXsRVTnwgVc0QXemUJyTsdyc2+7cgHTD4Fop2EPwPTyzen4gqUtaOuo018L9IOpx1v/2MC");
                Log.i("VuforiaClass", "After setinitparameters");
                do {
                    Log.i("VuforiaClass", " Inside the initializing loop");
                    mProgressValue = Vuforia.init();
//                    publishProgress(mProgressValue);

                } while (!isCancelled() && mProgressValue >= -1 && mProgressValue < 100);
                Log.i("VuforiaClass", String.valueOf(mProgressValue));
                return (mProgressValue > 0);
            }
        }

        protected void onProgressUpdate(Integer... values){
            //
        }

        protected void onPostExecute(Boolean result){
            if(result){
                initTrackerTask trackerTask = new initTrackerTask();
                trackerTask.execute();
            }

        }
    }


    private class initTrackerTask extends AsyncTask<Void, Integer,Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            Log.i("trackerTask", "initializing tracker");
            return vuforiaAct.initTrackers();
        }

        protected void onPostExecute(Boolean result){
            if (result) {
                loadTrackerTask trackerTask = new loadTrackerTask();
                trackerTask.execute();
            }
        }
    }

    private class loadTrackerTask extends AsyncTask<Void, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            return vuforiaAct.loadTrackerData();
        }

        protected void onPostExecute(Boolean result){
            System.gc();
            Vuforia.registerCallback(VuforiaClass.this);
            started = true;
        }
    }

    private class startVuforiaTask extends AsyncTask<Void, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            startCameraAndTrackers(cameraType);
            return true;
        }
    }
    private void deInitTracker(){
        TrackerManager.getInstance().deinitTracker(ObjectTracker.getClassType());
    }

    protected void onSurfaceCreated(){
        Vuforia.onSurfaceCreated();
    }



    protected void onSurfaceChanged(int width, int height){
        Vuforia.onSurfaceChanged(width, height);
    }

    public void startAR(int camera){
        cameraType = camera;
        startVuforiaTask vuforiaTask = new startVuforiaTask();
        vuforiaTask.execute();
        vuforiaAct.onInitArDone();
    }

    public void startCameraAndTrackers(int camera){
        cameraType = camera;
        CameraDevice cam = CameraDevice.getInstance();
        if(!cam.init());
        if(!cam.selectVideoMode(CameraDevice.MODE.MODE_DEFAULT));
        if(!cam.start());
        vuforiaAct.startTrackers();
        cameraRunning=true;
    }


}