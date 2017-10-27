package com.example.apm1.androidrobotcontroller;

import android.app.Activity;
import android.os.AsyncTask;

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

    public VuforiaClass(VuforiaActInterface vuforiaAct, VuforiaParameters params){
        this.vuforiaAct = vuforiaAct;
        VParams = params;
        tManager = TrackerManager.getInstance();
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
            Vuforia.setInitParameters(activity,1,VParams.vuforiaLicenseKey);

            do {
                mProgressValue = Vuforia.init();
                publishProgress(mProgressValue);

            }while (!isCancelled() && mProgressValue>=0 && mProgressValue<100);
            return (mProgressValue > 0);
        }

        protected void onProgressUpdate(Integer... values){
            //these be values man, u can do some things with them like meme
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
        tManager.deinitTracker(ObjectTracker.getClassType());
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