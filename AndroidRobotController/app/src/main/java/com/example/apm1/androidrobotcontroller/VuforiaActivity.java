package com.example.apm1.androidrobotcontroller;

import android.app.ActionBar;
import android.content.Intent;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Process;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.vuforia.CameraDevice;
import com.vuforia.DataSet;
import com.vuforia.Matrix34F;
import com.vuforia.Matrix44F;
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
        setContentView(R.layout.activity_vuforia);
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

//    public void imageFound(String string){
//        Toast.makeText(this,string + " was found", Toast.LENGTH_SHORT).show();
//    }

    @Override
    protected void onPause(){
        super.onPause();
//        if(view!= null){
//            view.setVisibility(View.INVISIBLE);
//            view.onPause();
//        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        CoordinatorLayout layout = (CoordinatorLayout) LayoutInflater.from(this).inflate(R.layout.activity_vuforia,null);

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
    public void onVuforiaResumed() {
//        if (view != null) {
//            view.setVisibility(View.VISIBLE);
//            view.onResume();
//        }
        CoordinatorLayout layout = (CoordinatorLayout) LayoutInflater.from(this).inflate(R.layout.activity_vuforia,null);

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



        if(state.getNumTrackableResults()>0){
//            float[] pose = translation(state.getTrackableResult(0).getPose().getData());



            Log.i("onVuforiaUpdate",
                    "x: " +
                    String.valueOf(state.getTrackableResult(0).getPose().getData()[7]*100/2.54) + "\n"+
                    "y: " + String.valueOf(state.getTrackableResult(0).getPose().getData()[3]*100/2.54)+ "\n"+
                    "z: " + String.valueOf(state.getTrackableResult(0).getPose().getData()[11]*100/2.54));
        }
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
//        vuforiaClass.startAR(CameraDevice.CAMERA_DIRECTION.CAMERA_DIRECTION_BACK);
        vuforiaClass.startAR(CameraDevice.CAMERA_DIRECTION.CAMERA_DIRECTION_BACK);
//        view.setVisibility(View.INVISIBLE);

    }

    public void setExtendedTracking(Boolean value){
        eTracking = value;
    }

    public boolean getExtendedTracking(){
        return eTracking;
    }


    //Inits some of the useful app stuff like the OpenGl View and says stuff about it
    //Doesn't say anything about textures I think
    public void initAppAr(){
        int depthSize = 16;
        int stencilSize = 0;
        boolean translucent = Vuforia.requiresAlpha();

        view = new OpenGlView(this);
        view.init(translucent,depthSize,stencilSize);

        renderer = new ImageTargetRenderer(this,vuforiaClass);

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

    public float[] translation(float[] pose){
        Matrix34F matrix34 = new Matrix34F();
        matrix34.setData(pose);
        Matrix44F matrix44 = com.vuforia.Tool.convert2GLMatrix(matrix34);
        float[] newRes = new float[16];
        Matrix.multiplyMM(newRes
                ,0, back, 0 , matrix44.getData(),0);
//        for(int i =0; i < newRes.length-1; i++){
//            Log.i("translation", String.valueOf(i)+ "  " + String.valueOf(newRes[i]) + "\n");
//        }
        float[] edit = {newRes[3],newRes[7],newRes[11],newRes[15]};
        float[] translation = {edit[0]/edit[3],edit[1]/edit[3],edit[2]/edit[3]};
//        Log.i("translation       ",String.valueOf(edit[0]) + "\n" + String.valueOf(edit[1]) + "\n" + String.valueOf(edit[3]));
        return translation;
    }



    float[] back = {
            0,-1,0,0,
            -1,0,0,0,
            0,0,-1,0,
            0,0,0,1
    };

    float[] front = {
            0,1,0,0
            -1,0,0,0,
            0,0,1,0,
            0,0,0,1
    };

}