package com.example.apm1.androidrobotcontroller;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.vuforia.CameraDevice;
import com.vuforia.DataSet;
import com.vuforia.ObjectTracker;
import com.vuforia.PIXEL_FORMAT;
import com.vuforia.STORAGE_TYPE;
import com.vuforia.State;
import com.vuforia.Trackable;
import com.vuforia.Tracker;
import com.vuforia.TrackerManager;
import com.vuforia.Vuforia;

import com.example.apm1.androidrobotcontroller.Utils.*;

import java.util.Vector;

public class VuforiaActivity extends AppCompatActivity implements VuforiaActInterface{

    DataSet dataSet;

    CameraDevice device;

    Boolean cameraW;

    private Vector<Texture> textures;

    VuforiaClass vuforiaClass;
//    SampleApplicationSession session;

    private Boolean eTracking;

    ImageTargetRenderer renderer;

    OpenGlView view;

    boolean vuforiaInitComp = false;
    Boolean initAppARDone = false;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        VuforiaParameters parameters = new VuforiaParameters();
        parameters.setVuforiaLicenseKey(
                "ATFMRrb/////AAAAGd0LzE71kkPjnoWoigFinSJp/L4eGD/p4zlkw3hvVdhzoBV4onBi+nxzNEWkwxwc6pc" +
                        "rRfNsnn62e67HaHM7OaAllEOmreJBAd1WzwI23lN0lGRcPec5ZQEyPIItZs+rI1nODhoPLAPLwsY6GUYw33" +
                        "pyAQg79ZDabU27EzC8aUM3IsFyB0J/gklWtitN51sRIeNTiiL1NV1O8fpHxSdVqtSJ3WyLb5rv/2kutb/Rn" +
                        "tJD/dKXKE0T7l+ipW91d4b7u92eNZegfMzM79ooF3pmuUR1vxOn6N71zGWrnCFXsRVTnwgVc0QXemUJyTsd" +
                        "yc2+7cgHTD4Fop2EPwPTyzen4gqUtaOuo018L9IOpx1v/2MC");
        Log.i("oncreate vuforiaact","wbefore initializing vuforiaclass");
        vuforiaClass = new VuforiaClass(this,parameters);
        vuforiaClass.initAr(this);
  //      session = new SampleApplicationSession(this);
        Log.i("oncreate vuforiaact", "after initializing");

        setExtendedTracking(true);

        textures = new Vector<Texture>();
        addTextures();

//        TrackerManager tManager = TrackerManager.getInstance();
//        ObjectTracker oTracker = (ObjectTracker) tManager.getTracker(ObjectTracker.getClassType());
//
//        dataSet = oTracker.createDataSet();
//        dataSet.load("Datathingy.xml", STORAGE_TYPE.STORAGE_APP);

        while(!vuforiaInitComp){
            vuforiaInitComp = vuforiaClass.vuforiaInitDone;
      //      Log.i("onCreate", "waiting for vuforiaInitDone");
        }
        Log.i("onCreate", "Vuforia init finished");
        //TODO: CLEAN THIS UP, THSI SHOULD ONLY BE IN START CAMERA AND TRACKERS
//        device = CameraDevice.getInstance();
//
//        cameraW=false;
//        if(CameraDevice.getInstance().init(2)) cameraW = true;
//
//        CameraDevice.getInstance().selectVideoMode(CameraDevice.MODE.MODE_DEFAULT);
//        CameraDevice.getInstance().start();
        Vuforia.setFrameFormat(PIXEL_FORMAT.RGB565,true);
        Log.i("onCreate vuforiaAct", "FINISHED");
    }

    public void returnMain(View v){
        Intent returnM = new Intent(this,MainActivity.class);
        startActivity(returnM);
    }

    public void imageFound(String string){
        Toast.makeText(this,string + " was found", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause(){
        super.onPause();
        if(view!= null){
            view.setVisibility(View.INVISIBLE);
            view.onPause();
        }
    }

    @Override
    public boolean initTrackers(){

        boolean toReturn = true;

        TrackerManager manager = TrackerManager.getInstance();
        if(manager == null) Log.i("inittrackres", "trackermanager is null");
        Tracker tracker = manager.initTracker(ObjectTracker.getClassType());
        if(tracker == null) Log.i("inittrackres", "can't init tracker");
        if(tracker == null) toReturn = false;
        return toReturn;
    }

    @Override
    public boolean deinitTrackers() {
        return false;
    }

    @Override
    public boolean loadData() {
        TrackerManager manager = TrackerManager.getInstance();
        ObjectTracker oTracker = (ObjectTracker) manager.getTracker(ObjectTracker.getClassType());

        dataSet = oTracker.createDataSet();

        if(!dataSet.load("StonesAndChips.xml", STORAGE_TYPE.STORAGE_APPRESOURCE)) return false;

        if(!oTracker.activateDataSet(dataSet)) return false;

        int numTrackables = dataSet.getNumTrackables();
        for(int i = 0; i < numTrackables; i++){
            Trackable trackable = dataSet.getTrackable(i);
            if(getExtendedTracking())
                trackable.startExtendedTracking();
        }
        return true;
    }

    @Override
    public void onVuforiaResumed() {
        if (view != null) {
            view.setVisibility(View.VISIBLE);
            view.onResume();
        }
    }

    @Override
    public boolean startTrackers() {

        boolean onReturn = true;
        Tracker objectTracker = TrackerManager.getInstance().getTracker(ObjectTracker.getClassType());

        if(objectTracker!= null) objectTracker.start();
        return true;

    }

    @Override
    public boolean stopTrackers() {
        return false;
    }

    @Override
    public boolean loadTrackerData() {
            TrackerManager tm = TrackerManager.getInstance();
            ObjectTracker ot = (ObjectTracker) tm.getTracker(ObjectTracker.getClassType());
            if (ot == null) return false;
            if (dataSet == null) dataSet = ot.createDataSet();
            Log.i("loadTrackerData", "after createdataset");
            if (dataSet == null) return false;
            if (!dataSet.load("StonesAndChips.xml", STORAGE_TYPE.STORAGE_APPRESOURCE)) return false;
            if (!ot.activateDataSet(dataSet)) return false;

            int numTrackables = dataSet.getNumTrackables();
            for (int i = 0; i < numTrackables; i++) {
                Trackable trackable = dataSet.getTrackable(i);
                if (getExtendedTracking()) trackable.startExtendedTracking();
            }
            return true;

    }

    @Override
    public boolean unloadTrackerData(){
        TrackerManager tm = TrackerManager.getInstance();
        ObjectTracker ot = (ObjectTracker)tm.getTracker(ObjectTracker.getClassType());
        if(ot==null) return false;
        if(dataSet != null && dataSet.isActive()){
            if(!ot.deactivateDataSet(dataSet)) return false;
            dataSet = null;
        }
        return true;
    }

    @Override
    public void onVuforiaUpdate(State state) {
//        TrackerManager tm = TrackerManager.getInstance();
//        ObjectTracker ot = (ObjectTracker) tm.getTracker(ObjectTracker.getClassType());
//        if(ot==null||dataSet==null||ot.getActiveDataSet(0)==null){
//            //bad news bears my man
//        }
//
//        unloadTrackerData();
//        loadTrackerData();
    }


    //Todo: FIX THE RENDERER AND ADD THE UI LAYOUT

    @Override
    public void onInitARDone() {

        Log.i("onInitARDone", "first line, we made it");
        //FIX THE RENDERER

        initAppAr();
        //  renderer.setActive(true);

        while(!initAppARDone){

        }

        renderer.setActive(true);
        addContentView(view, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT));

        //MAKE SOME UI LAYOUT

    //    vuforiaClass.startAR(CameraDevice.CAMERA_DIRECTION.CAMERA_DIRECTION_BACK);
        vuforiaClass.startAR(CameraDevice.CAMERA_DIRECTION.CAMERA_DIRECTION_BACK);
    }

    public void setExtendedTracking(Boolean value){
        eTracking = value;
    }

    public boolean getExtendedTracking(){
        return eTracking;
    }


    //Inits some of the useful app stuff like the OpenGl View and says stuff about it
    //Todo: Crosscheck with the ImageTargets file to make sure nothing about textures
    //Doesn't say anything about textures I think
    public void initAppAr(){
        int depthSize = 16;
        int stencilSize = 0;
        boolean translucent = Vuforia.requiresAlpha();

        view = new OpenGlView(this);
        view.init(translucent,depthSize,stencilSize);

        renderer = new ImageTargetRenderer(this,vuforiaClass);
  //      renderer = new ImageTargetRenderer(this,session,"targets");

        renderer.setTextures(textures);

        view.setRenderer(renderer);
        view.setVisibility(View.VISIBLE);

        initAppARDone = true;

    }

    void addTextures(){
        textures.add(Texture.loadTextureFromApk("TextureTeapotBrass.png",
                getAssets()));
        textures.add(Texture.loadTextureFromApk("TextureTeapotBlue.png",
                getAssets()));
        textures.add(Texture.loadTextureFromApk("TextureTeapotRed.png",
                getAssets()));
    }

}
