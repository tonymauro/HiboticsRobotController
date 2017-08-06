package com.example.apm1.androidrobotcontroller;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.hardware.usb.*;
import android.util.Log;
import android.view.View;
//import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
//import android.widget.ToggleButton;
//import com.vuforia.*;
//import java.io.FileDescriptor;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {

    UsbAccessory megaADK;
    UsbManager usbManager;
    ParcelFileDescriptor fileDescriptor;
    FileInputStream iS;
    FileOutputStream oS;
    PendingIntent permissionIntent;
    IntentFilter usbFilter;
    Boolean rP;
    GLSurfaceView glSurfaceView;

    boolean extendedTracking = true;

    //   Vuforia vuforia = new Vuforia();
    private static final String ACTION_USB_PERMISSION = "com.google.android.DemoKit.action.USB_PERMISSION";

    //First case checks if we do not have permission to perform an action
    //Second case checks if the action incoming is that we were detatched
    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    //Theres only 1 accessory
                    UsbAccessory accessory = usbManager.getAccessoryList()[0];
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        megaADK = accessory;
                        setUp(megaADK);
                    } else {
                        Log.d("MEGA ADK ", "Permission denied" + accessory);
                    }
                    rP = false;
                }
            } else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {

                UsbAccessory accessory = usbManager.getAccessoryList()[0];
                if(accessory == null) Log.d("Detached", "accessory no longer findable");

//                if (accessory != null && accessory.equals(megaADK)) {
//                    closeAccessory();
//              }
            }
        }
    };


    //Sets up the on create
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        usbFilter = new IntentFilter(ACTION_USB_PERMISSION);
        usbFilter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);

        permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        Context context = getApplicationContext();
        glSurfaceView = new GLSurfaceView(context);
        context.registerReceiver(usbReceiver, usbFilter);
        //usbManager.requestPermission(megaADK,permissionIntent);
        if (getLastNonConfigurationInstance() != null) {
            if(megaADK != null) {
                megaADK = (UsbAccessory) getLastNonConfigurationInstance();
                setUp(megaADK);
            }else{
                Toast.makeText(this,"megaADK wasn' defined",Toast.LENGTH_SHORT).show();
            }
        }
        Log.i("Create", "Finished on create");

    }


    @Override
    public void onResume() {
        super.onResume();
        //Redifining iS and oS ewould be pointless
        if (iS != null && oS != null) return;
        //There sohuld only be 1 USB accessory which would be the Mega
        UsbAccessory accessory = usbManager.getAccessoryList()[0];
        if (accessory != null) {
            if (usbManager.hasPermission(accessory)) {
                setUp(accessory);
            } else {
                synchronized (usbReceiver) {
                    if (!rP) {
                        usbManager.requestPermission(accessory, permissionIntent);
                        rP = true;
                    }
                }
            }
        } else {
            Log.d("Android Accessory", "Accessory is null");
        }
    }



    @Override
    public void onPause() {
        super.onPause();
        //  closeAccessory();
    }


    @Override
    public void onDestroy() {
        unregisterReceiver(usbReceiver);
        super.onDestroy();
    }


    private void closeAccessory() {
        try {
            fileDescriptor.close();
        } catch (IOException e) {
        }
        fileDescriptor = null;
        megaADK = null;
    }


    public void debugArea(View v){
        TextView T = (TextView) findViewById(R.id.textViewDebug);
        T.setTextSize(15);
        byte[] buffer = {(byte)0, (byte)1};
        if(oS!=null){
            try{
                Toast.makeText(this,"Attempting to send msg", Toast.LENGTH_LONG).show();
                T.setText("debugButton attempting to talk");
                oS.write(buffer);
            }catch(IOException e){
            }
        }
    }

    public void clearDisplay(View v) {
        byte[] buffer = {(byte)3, (byte)4};
        if(oS!= null){
            try {
                Toast.makeText(this,"Clear Display",Toast.LENGTH_SHORT).show();
                oS.write(buffer);
            }catch (Exception e){
            }
        }
    }

    public void sendAndReceive(View v){
        byte[] buffer = {(byte)15, (byte)20, (byte)100, (byte)2};
        byte[] bytesReadfromIS = {(byte)0,(byte)0,(byte)0,(byte)0,};
        if(oS != null && iS != null){
            try{
                Toast.makeText(this, "Attempting to Send", Toast.LENGTH_SHORT).show();
                oS.write(buffer);
//                byte[] inputs = new byte[4];
//                for(int i =0; i < 4; i++){
//                    inputs[i]=(byte)iS.read();
//                }
//                byte[] tester = {(byte)5,(byte)1,(byte)4,(byte)3};
//                if(tester == inputs) Toast.makeText(this,"Hello from Arduino",Toast.LENGTH_LONG).show();
//     //           Toast.makeText(this,String.valueOf(iS.read()),Toast.LENGTH_LONG).show();
//                else Toast.makeText(this,inputs[0] + " " + inputs[1],Toast.LENGTH_LONG).show();
//                String inputs = " ";
//                for(int i =0; i < iS.available(); i++){
//                    inputs = inputs + String.valueOf(iS.read()) + " ";
//                }
                //        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                //        ByteArrayInputStream bais = new ByteArrayInputStream();
                //       byte[] bytes =
                //     if(iS.read() == 5) Toast.makeText(this,"Hello from Arduino",Toast.LENGTH_LONG).show();
                //     if(iS.read() == 1) Toast.makeText(this,"Second byte received",Toast.LENGTH_LONG).show();
                int numBytesRead = iS.read(bytesReadfromIS);
                if (numBytesRead > 0) {
                    Toast.makeText(this,"Read " + numBytesRead + " bytes",Toast.LENGTH_SHORT).show();
                    Toast.makeText(this,bytesReadfromIS[0]+", "+ bytesReadfromIS[1]+", "+
                            bytesReadfromIS[2]+", "+ bytesReadfromIS[3]+", ", Toast.LENGTH_SHORT).show();

                }

            }catch (IOException e){
                Toast.makeText(this, "Can't write/Receive", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void sendAndReceive2(View v){
        byte[] buffer = {(byte)1, (byte)7, (byte)3, (byte)8};
        byte[] bytesReadfromIS = {(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0};
        if(oS != null && iS != null){
            try{
                Toast.makeText(this, "Attempting to Send", Toast.LENGTH_SHORT).show();
                oS.write(buffer);
//                byte[] inputs = new byte[4];
//                for(int i =0; i < 4; i++){
//                    inputs[i]=(byte)iS.read();
//                }
//                byte[] tester = {(byte)5,(byte)1,(byte)4,(byte)3};
//                if(tester == inputs) Toast.makeText(this,"Hello from Arduino",Toast.LENGTH_LONG).show();
//     //           Toast.makeText(this,String.valueOf(iS.read()),Toast.LENGTH_LONG).show();
//                else Toast.makeText(this,inputs[0] + " " + inputs[1],Toast.LENGTH_LONG).show();
//                String inputs = " ";
//                for(int i =0; i < iS.available(); i++){
//                    inputs = inputs + String.valueOf(iS.read()) + " ";
//                }
                //        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                //        ByteArrayInputStream bais = new ByteArrayInputStream();
                //       byte[] bytes =
                //     if(iS.read() == 5) Toast.makeText(this,"Hello from Arduino",Toast.LENGTH_LONG).show();
                //     if(iS.read() == 1) Toast.makeText(this,"Second byte received",Toast.LENGTH_LONG).show();
                int numBytesRead = iS.read(bytesReadfromIS);
                if (numBytesRead > 0) {
                    Toast.makeText(this,"Read " + numBytesRead + " bytes",Toast.LENGTH_SHORT).show();
                    Toast.makeText(this,bytesReadfromIS[0]+", "+ bytesReadfromIS[1]+", "+
                            bytesReadfromIS[2]+", "+ bytesReadfromIS[3]+", "+
                            bytesReadfromIS[4]+", "+ bytesReadfromIS[5]+", ", Toast.LENGTH_SHORT).show();

                }

            }catch (IOException e){
                Toast.makeText(this, "Can't write/Receive", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void sendCircle(View v) {
        byte[] buffer = {(byte)1,(byte)0};

        // byte[] message = {(byte) 1};
        TextView T = (TextView) findViewById(R.id.textViewCircle);
        T.setTextSize(15);

        if (oS != null) {
            try {
                T.setText("Sending message to arduino");
                oS.write(buffer);
            } catch (IOException e) {
                T.setText("Wasn't able to send text to arduino");
                Log.e("Android Accessory", "write failed", e);
            }
        } else {
            T.setText("moutput is null");
        }


    }

    public void setUp(UsbAccessory accessory) {
        fileDescriptor = usbManager.openAccessory(accessory);
        if (fileDescriptor != null) {
            megaADK = accessory;
            iS = new FileInputStream(fileDescriptor.getFileDescriptor());
            oS = new FileOutputStream(fileDescriptor.getFileDescriptor());
        }
    }



//    private class InitVuforiaTask extends AsyncTask<Void, Integer, Boolean> {
//        private int mProgressValue = -1;
//        private Activity mActivity;
//        private Object mShutdownLock = new Object();
//        protected Boolean doInBackground(Void... params){
//            synchronized (mShutdownLock){
//                Vuforia.setInitParameters(mActivity,
//                        0,
//                        "ASmxFXn/////AAAAGULt39uVqk5Apdm/e2Oz3PgxOgTnKuKirQ2RNnHtSMdoJ" +
//                                "/f4oikqW0F6fHIwUp2EBmXxcFj976SqvXYsBR0oSITs92OjWfHjhj" +
//                                "r55Xxcr73LGe0T7NC7iAWoK+AgEp/3YFed5TDdJPuLnpj9vVKZhxs" +
//                                "SbhUlCVOLEmoPVrlQ9hN4XI/FfzAIJjNKRyifq+686U0kS9BEB/WD" +
//                                "bMZyzxbb69nZXemrAJiDaSItce5cQRIG9LCId3uj7kqum5XyvGZnC" +
//                                "vJWnpJ1fXqnrcGdZplXKbI/4GQIfuRWNltPF+uV7DohhKaKgUMvpO" +
//                                "8O9PfS2WFeXSzUMgzZ+yWSaAZ+D48D4uqnoY0cyU+zyMI2+dMog3g7");
//                do
//                {
//                    mProgressValue = Vuforia.init();
//                    publishProgress(mProgressValue);
//                }while (!isCancelled() && mProgressValue >= 0 && mProgressValue < 100);
//                return (mProgressValue > 0);
//            }
//        }
//    }
//
//    public boolean doLoadTrackersData()
//    {
//        TrackerManager tManager = TrackerManager.getInstance();
//        ObjectTracker objectTracker = (ObjectTracker) tManager
//                .getTracker(ObjectTracker.getClassType());
//        if (objectTracker == null)
//            return false;
//
//        if (mCurrentDataset == null)
//            mCurrentDataset = objectTracker.createDataSet();
//
//        if (mCurrentDataset == null)
//            return false;
//
//        if (!mCurrentDataset.load(" ######## ObjectRecognition/rigidBodyTarget.xml  ####### EXAMPLE INSERT NEW ONE IN HERE",
//                STORAGE_TYPE.STORAGE_APPRESOURCE))
//            return false;
//
//        if (!objectTracker.activateDataSet(mCurrentDataset))
//            return false;
//
//        int numTrackables = mCurrentDataset.getNumTrackables();
//        for (int count = 0; count < numTrackables; count++)
//        {
//            Trackable trackable = mCurrentDataset.getTrackable(count);
//            if(isExtendedTrackingActive())
//            {
//                trackable.startExtendedTracking();
//            }
//
//            String name = "Current Dataset : " + trackable.getName();
//            trackable.setUserData(name);
//            Log.d("Object Recognition", "UserData:Set the following user data "
//                    + (String) trackable.getUserData());
//        }
//
//        return true;
//    }

//    public boolean isExtendedTrackingActive(){
//        return extendedTracking;
//    }

//             Did'nt know for USBDevice or UsbAccessory to use
//    public void setUp(UsbDevice device) {
//        UsbDeviceConnection usb = usbManager.openDevice(device);
//        //  fileDescriptor = usbManager.openAccessory(device);
//        if (fileDescriptor != null) {
//            iS = new FileInputStream(fileDescriptor.getFileDescriptor());
//            oS = new FileOutputStream(fileDescriptor.getFileDescriptor());
//        }
//    }
}