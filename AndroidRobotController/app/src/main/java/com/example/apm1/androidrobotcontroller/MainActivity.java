package com.example.apm1.androidrobotcontroller;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.vuforia.CameraDevice;
import com.vuforia.DataSet;
import com.vuforia.Matrix34F;
import com.vuforia.Matrix44F;
import com.vuforia.ObjectTracker;
import com.vuforia.STORAGE_TYPE;
import com.vuforia.State;
import com.vuforia.Trackable;
import com.vuforia.Tracker;
import com.vuforia.TrackerManager;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static java.lang.Math.abs;

public class MainActivity extends AppCompatActivity implements VuforiaActInterface {

    Boolean initted = false;

    UsbAccessory megaADK;
    UsbManager usbManager;
    ParcelFileDescriptor fileDescriptor;
    FileInputStream iS;
    FileOutputStream oS;
    PendingIntent permissionIntent;
    IntentFilter usbFilter;
    Boolean rP;
    GLSurfaceView glSurfaceView;

    VuforiaParameters VParams;

    IntentFilter wifiP2P;
    Channel wifiChannel;
    WifiP2pManager wifiManager;

    BroadcastReceiver wifiReceiver;

    Boolean isWifiP2pEnabled = false;
    Context context;

    Vector vector;

    private List<WifiP2pDevice> peers;

    boolean extendedTracking = true;

    boolean bigO = false;

    boolean servo = false;
    boolean motor = false;
//    boolean wifiP2PEnabled;

    DataSet dataSet;
    State state;
    VuforiaClass vClass;

    boolean first = true;
    int Camera = -1;

    int countLoss = 0;

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
                        TextView t = (TextView) findViewById(R.id.TextView);
                        t.setTextSize(15);
                        t.setText("Action USB Permission");
                    } else {
                        TextView t = (TextView) findViewById(R.id.TextView);
                        t.setTextSize(15);
                        t.setText("Action USB Permission Denied");
                        Log.d("MEGA ADK ", "Permission denied" + accessory);
                    }
                    rP = false;
                }
            } else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {

                UsbAccessory accessory = usbManager.getAccessoryList()[0];
                if(accessory == null) Log.d("Detached", "accessory no longer findable");
                Toast.makeText(context,"Accessory is detached", Toast.LENGTH_SHORT);
                oS = null;
                iS = null;
//              if (accessory != null && accessory.equals(megaADK)) {
//                    closeAccessory();
//              }
            }else if(UsbManager.ACTION_USB_ACCESSORY_ATTACHED.equals(action)){
                UsbAccessory accessory = usbManager.getAccessoryList()[0];
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    megaADK = accessory;
                    setUp(megaADK);
                    TextView t = (TextView) findViewById(R.id.TextView);
                    t.setTextSize(15);
                    t.setText("Action USB Attached");
                } else {
                    Log.d("MEGA ADK ", "Permission denied" + accessory);
                }
                rP = false;
            }
        }
    };



    //ALREADY IMPLEMENTED IN WIFIDIRECTBROADCASTRECEIVER


//    private final BroadcastReceiver wifiReceiver = new BroadcastReceiver(){
//        @Override
//        public void onReceive(Context context, Intent intent){
//            String action = intent.getAction();
//            switch(action){
//                case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION:
//                    wifiP2PEnabled = (intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE,-1)==WifiP2pManager.WIFI_P2P_STATE_ENABLED);
//                case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION:
//                    if(!wifiP2PEnabled)break;
//                    if(wifiManager!=null){
//                        wifiManager.requestPeers(wifiChannel,peerListener);
//                    }
//                    Toast.makeText(context, "Peers changed, updating",Toast.LENGTH_SHORT).show();
//
//                    //OH NOOOOOOOOOOOOOOOOOOOOO
//                    try {
//                        oS.write(new byte[]{(byte) 1, (byte) 23, (byte) 4});
//                    }catch (IOException e){
//                    }
//                case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION:
//                    Toast.makeText(context, "WE BE CONNECTED BOISSSSSSSSSSSS", Toast.LENGTH_SHORT).show();
//                case WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION:
//                    // Do Something
//            }
//        }
//    };




    //Sets up the on create
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("hello", "jello");
        wifiP2P = new IntentFilter();

        wifiP2P.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        wifiP2P.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        wifiP2P.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        wifiP2P.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        wifiManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        wifiChannel = wifiManager.initialize(this, getMainLooper(), null);

       // wifiReceiver = new WifiDirectBroadcastReceiver(wifiManager, wifiChannel, this);
//        registerReceiver(wifiReceiver,wifiP2P);

        vector = new Vector(new double[]{999,999,999});

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        usbFilter = new IntentFilter(ACTION_USB_PERMISSION);
        usbFilter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        usbFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);

        beginMqtt();

        context = getApplicationContext();
        glSurfaceView = new GLSurfaceView(context);
//        registerReceiver(usbReceiver, usbFilter);
        Log.i("Create", "Finished on create");
//        if (getLastNonConfigurationInstance() != null) {
//            if(megaADK != null) {
//                megaADK = (UsbAccessory) getLastNonConfigurationInstance();
//                setUp(megaADK);
//            }else{
//                Toast.makeText(this,"megaADK wasn't defined",Toast.LENGTH_SHORT).show();
//            }
//        }

//        final EditText editText = (EditText) findViewById(R.id.editText);
//
//        editText.setOnEditorActionListener(new TextView.OnEditorActionListener(){
//            @Override
//            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent){
//                boolean handled = false;
//                if (i == EditorInfo.IME_ACTION_DONE){
//                    String et1 = editText.getText().toString();
//                    if(et1.equals("Help")){
//                        textView.setText("Can set to these commands:" +
//                                "Set Motor Speed" +
//                                "Motor Speed" +
//                                "Forward time" +
//                                "Forward distance" +
//                                "Stop");
//                    }else if(et1.equals("Set Motor Speed")){
//                        textView.setText("Say speed on the attributes area (0 to 100)");
//                        //set the motor speed to something
//                    }else if(et1.equals("Motor Speed")){
//
//                    }
//                    editText.setText(et1);
//                }
//                return handled;
//            }
//
//        });
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(usbReceiver,usbFilter);
        //Redifining iS and oS ewould be pointless
        if (iS != null && oS != null){
            Toast.makeText(context,"is&&os null", Toast.LENGTH_SHORT).show();
            return;
        }
        //There sohuld only be 1 USB accessory which would be the Mega
        if(usbManager==null){
            if(first){
                first = false;
                return;
            }
            Toast.makeText(context,"usbManager is null", Toast.LENGTH_SHORT).show();
            return;
        }
        UsbAccessory accessory = usbManager.getAccessoryList()[0];
        if (accessory != null) {
            if (usbManager.hasPermission(accessory)) {
                setUp(accessory);
            } else {
                synchronized (usbReceiver) {
                    if (!rP) {
                        Toast.makeText(context, "requesting permission", Toast.LENGTH_SHORT).show();
                        usbManager.requestPermission(accessory, permissionIntent);
                        rP = true;
                    }
                }
            }
        } else {
            Log.d("Android Accessory", "Accessory is null");
        }
  //      wifiReceiver = new WiFiDirectBroadcastReceiver(wifiManager,wifiChannel,this);
        registerReceiver(wifiReceiver,wifiP2P);
    }



    @Override
    public void onPause() {
        if(oS!= null){
            try{
                oS.write(new byte[]{(byte)10, (byte) 9});
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        super.onPause();
        //unregisterReceiver(usbReceiver);
        //unregisterReceiver(wifiReceiver);
        //closeAccessory();
    }


    @Override
    public void onDestroy() {
        unregisterReceiver(usbReceiver);
        super.onDestroy();
    }



    //Dont use this yet cus I didn't test it;

//    private void closeAccessory() {
//        try {
//            fileDescriptor.close();
//        } catch (IOException e) {
//        }
//        fileDescriptor = null;
//        megaADK = null;
//    }





    //THIS STUFF IS FOR WIFI DIRECT

//    public void connect(){
//        if(peers!=null&&peers.size()!=0){
//            WifiP2pDevice device = peers.get(0);
//            WifiP2pConfig config = new WifiP2pConfig();
//            config.deviceAddress = device.deviceAddress;
//            config.wps.setup = WpsInfo.PBC;
//            wifiManager.connect(wifiChannel, config, new ActionListener() {
//                @Override
//                public void onSuccess() {
//                    //BRoadcast receiver should say connected
//                }
//
//                @Override
//                public void onFailure(int reason){
//                    Toast.makeText(MainActivity.this, "Connect failed", Toast.LENGTH_SHORT).show();
//                }
//            });
//        }
//    }

    boolean successful;


    public void peerDiscovery(View v){
        wifiManager.discoverPeers(wifiChannel, new WifiP2pManager.ActionListener(){
            @Override
            public void onSuccess(){
                Toast.makeText(context,"hello this is successful", Toast.LENGTH_SHORT).show();
                //this is pointless but i have to init this
                successful = true;
            }

            @Override
            public void onFailure(int reasonCode){
                //this is also pointless, shucks to shuck
            }
        });
    }

    public void requestPeers(View v){
        if(peers.size()==0){
            Toast.makeText(context, "NO PEErS FOUND FEELSBADMAN", Toast.LENGTH_SHORT).show();
            return;
        }
        WifiP2pDevice device = peers.get(0);
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        wifiManager.connect(wifiChannel,config,new ActionListener(){
            @Override
            public void onSuccess() {
                Toast.makeText(context, "CONNECteD BABY", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason){
                //hecking bamboozled
            }
        });
    }





    private PeerListListener peerListener = new PeerListListener(){
        @Override
        public void onPeersAvailable(WifiP2pDeviceList list){
            List<WifiP2pDevice> newPeers = (List) list.getDeviceList();
            if(!newPeers.equals(peers)){
                peers.clear();
                peers.addAll(newPeers);
            }

            if(peers.size() == 0){
                Toast.makeText(context, "No peers found", Toast.LENGTH_SHORT).show();
            }
        }
    };


//    public void debugArea(View v) {
//        TextView T = (TextView) findViewById(R.id.TextView);
//        T.setTextSize(15);
//        if (iS == null) {
//            T.setText("iS and oS have never been inialized");
//        } else {
////            TextView T = (TextView) findViewById(R.id.TextView);
////            T.setTextSize(15);
//            if (usbManager != null) {
//
//                String accessories1 = " ";
//                if (usbManager.getAccessoryList() == null) {
//                    //This is likely to get called
//                } else {
////                    for (int i = 0; i < usbManager.getAccessoryList().length; i++) {
////                        accessories1 = accessories1 + " " + String.valueOf(usbManager.getAccessoryList()[i]);
////                    }
////                    T.setText(accessories1);
//                }
//            } else {
//            }
//        }
//    }




//    DEPRECATED DEBUG THINGY
//    public void debugArea(View v){
//        TextView T = (TextView) findViewById(R.id.TextView);
//        T.setTextSize(15);
//        byte[] buffer = {(byte)0, (byte)1};
//        if(oS!=null){
//            try{
//                Toast.makeText(this,"Attempting to send msg", Toast.LENGTH_LONG).show();
//                T.setText("debugButton attempting to talk");
//                oS.write(buffer);
//            }catch(IOException e){
//            }
//        }
//    }

    public void clearDisplay(View v) {
        byte[] buffer = {(byte)3, (byte)4};
        if(oS!= null){
            try {
                Toast.makeText(this,"Clear Display", Toast.LENGTH_SHORT).show();
                oS.write(buffer);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void Forward(View v) {
        byte[] buffer = {(byte)125,(byte) 10, (byte)5};
        if(oS!= null){
            try {
                oS.write(buffer);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void Backward(View v) {
        byte[] buffer = {(byte) 125, (byte)10, (byte)6};
        if(oS!= null){
            try {
                oS.write(buffer);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void Stop(View v) {
        byte[] buffer = {(byte) 125, (byte) 10, (byte) 7};
        if(oS != null){
            try{
                oS.write(buffer);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public void vArduino(View v){
        bigO = ((ToggleButton) v).isChecked();
        Log.i("toggle button    ", bigO?"v checked":"v unchecked");
    }

    public void vServo(View v){
        servo = ((ToggleButton) v).isChecked();
        Log.i("servo button    ", servo?"checked":"unchecked");
    }

    public void vMotor(View v){
        motor = ((ToggleButton) v).isChecked();
        Log.i("motor button    ", motor?"checked":"unchecked");
    }

    public void Left(View v){
        byte[] buffer = {(byte)125,(byte) 27, (byte)5};
        if(oS!= null){
            try {
                oS.write(buffer);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void Right(View v){
        byte[] buffer = {(byte)125,(byte) 26, (byte)5};
        if(oS!= null){
            try {
                oS.write(buffer);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void sendAndReceive(View v){
        byte[] buffer = {(byte)7,(byte) 420, (byte)32, (byte) 2};
        byte[] sForBytes = {(byte)0,(byte)0,(byte)0,(byte)0};
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
           //     if(iS.read() == 1) Toast.makeText(this,"Second byte received",Toast.LENGTH_LONG).show();

//                int numRead = iS.read(sForBytes);
//                if(numRead>0){
//                    Toast.makeText(this,sForBytes[0] + "   " + sForBytes[1] + "   " + sForBytes[2] + "   "
//                    + sForBytes[3], Toast.LENGTH_SHORT).show();
//                }


            }catch (IOException e){
                Toast.makeText(this, "Can't write/Receive", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void bVuforia(View v){
        if(!initted) {
            Toast.makeText(context,"Initting for the first time", Toast.LENGTH_SHORT).show();
            initted = true;
            VParams = new VuforiaParameters();
            VParams.setVuforiaLicenseKey(
                    "ATFMRrb/////AAAAGd0LzE71kkPjnoWoigFinSJp/L4eGD/p4zlkw3hvVdhzoBV4onBi+nxzNEWkwxwc6pc" +
                            "rRfNsnn62e67HaHM7OaAllEOmreJBAd1WzwI23lN0lGRcPec5ZQEyPIItZs+rI1nODhoPLAPLwsY6GUYw33" +
                            "pyAQg79ZDabU27EzC8aUM3IsFyB0J/gklWtitN51sRIeNTiiL1NV1O8fpHxSdVqtSJ3WyLb5rv/2kutb/Rn" +
                            "tJD/dKXKE0T7l+ipW91d4b7u92eNZegfMzM79ooF3pmuUR1vxOn6N71zGWrnCFXsRVTnwgVc0QXemUJyTsd" +
                            "yc2+7cgHTD4Fop2EPwPTyzen4gqUtaOuo018L9IOpx1v/2MC");
            vClass = new VuforiaClass(this, VParams);
            vClass.initAr(this);
        }else{
            Log.i("bVuforia  ", "Vuforia initialized");
        }
    }

    public void sendCircle(View v) {
        byte[] buffer = {(byte)10,(byte)-90};

        // byte[] message = {(byte) 1};
        TextView T = (TextView) findViewById(R.id.TextView);
        T.setTextSize(15);

        if (oS != null) {
            try {
                T.setText("Sending message to arduino");
                oS.write(buffer);
            } catch (IOException e) {
//                T.setText("Wasn't able to send text to arduino");
                Log.e("Android Accessory", "write failed", e);
            }
        } //else {

//            T.setText("moutput is null");
       // }

//        if(buttonLED.isChecked())
//            buffer[0]=(byte)0; // button says on, light is off
//        else
//            buffer[0]=(byte)1; // button says off, light is on
//
//
//        if (mOutputStream != null) {
//            try {
//                mOutputStream.write(buffer);
//            } catch (IOException e) {
//                Log.e(TAG, "write failed", e);
//            }
//        }

        //WHat is up my dudes i am writing a program right now

//        permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
//        Context context = getApplicationContext();
//        context.registerReceiver(usbReceiver, usbFilter);
//        Log.i("Yo", "Registered Receiver");
//        if (megaADK == null) {
//
//            megaADK = usbManager.getAccessoryList()[0];
//
//            if (megaADK == null) {
//                TextView T = (TextView) findViewById(R.id.TextView);
//                T.setTextSize(15);
//                T.setText("NO MEGAADK FOUND");
//            }
//        }
//        usbManager.requestPermission(megaADK, permissionIntent);
//
//        byte[] message = {(byte) 1};
////        UsbAccessory[] accessories = usbManager.getAccessoryList();
////        UsbAccessory megaADK = (accessories == null ? null : accessories[0]);
//        if (megaADK != null) {
//            if (usbManager.getAccessoryList().length != 0) {
//                fileDescriptor = usbManager.openAccessory(megaADK);
//
//                iS = new FileInputStream(fileDescriptor.getFileDescriptor());
//                oS = new FileOutputStream(fileDescriptor.getFileDescriptor());
//                try {
//                    oS.write(message);
//                } catch (Exception e) {
//                }
//            }
//        }
    }

    public void switchAct(View v){
        Log.i("switchAct", "Before creating intent and starting");
        Intent goVuforia = new Intent(this,VuforiaActivity.class);
        startActivity(goVuforia);
        Log.i("switchAct", "After");
    }

    public void beginUsb(View v){
        permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);

        if(usbManager == null) usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        if(usbManager == null) return;
        if(usbManager.getAccessoryList()==null){
            Toast.makeText(context, "Accessory null from BeginUSB", Toast.LENGTH_SHORT).show();
            return;
        }
        if(megaADK != null) {
            setUp(megaADK);
        }else{
            megaADK = usbManager.getAccessoryList()[0];
            setUp(megaADK);
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

//    public void setUp(UsbDevice device) {
//        UsbDeviceConnection usb = usbManager.openDevice(device);
//        //  fileDescriptor = usbManager.openAccessory(device);
//        if (fileDescriptor != null) {
//            iS = new FileInputStream(fileDescriptor.getFileDescriptor());
//            oS = new FileOutputStream(fileDescriptor.getFileDescriptor());
//        }
//    }



    //Not done yet
//    public void setIsWifiP2pEnabled(Boolean value)
//    {
//        isWifiP2pEnabled = value;
//    }
//
//    public boolean getIsWifiP2pEnabled(){
//        return isWifiP2pEnabled;
//    }
//    public void resetData(){
//
//    }

    @Override
    public boolean initTrackers() {
        boolean toReturn = true;
        TrackerManager manager = TrackerManager.getInstance();
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

    }

    @Override
    public boolean startTrackers() {
        Tracker objectTracker = TrackerManager.getInstance().getTracker(ObjectTracker.getClassType());

        if(objectTracker!= null) objectTracker.start();
        return true;
    }

    @Override
    public boolean stopTrackers() {
        return false;
    }

    @Override
    public boolean unloadTrackerData() {
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
    public void onVuforiaUpdate(State state) {
        this.state = state;

        //from the back camera the numbesr are as expected
        //From the front camera, the right is negative and left is positiev
        //and down is positive and up is negative

        if(state.getNumTrackableResults()>0) {
            Log.i("onVuforiaUpdate", "at least 1 trackable found");
            //    Toast.makeText(context, "found a trackable my dude",Toast.LENGTH_SHORT).show();
            float[] pose = state.getTrackableResult(0).getPose().getData();
            double x = pose[7] * 100 / 2.54;
            double y = pose[3] * 100 / 2.54;
            double z = pose[11] * 100 / 2.54;
            if (Camera == CameraDevice.CAMERA_DIRECTION.CAMERA_DIRECTION_FRONT) {
                x = -x;
                y = -y;
            } else if (Camera == CameraDevice.CAMERA_DIRECTION.CAMERA_DIRECTION_BACK) {

            } else {
                //houston we have a problem
            }
            Log.i("x", String.valueOf(x));
            Log.i("y", String.valueOf(y));
            Log.i("z", String.valueOf(z));
            vector.updateVector(new double[]{x, y, z});

            if (motor) {
                boolean mm = Math.abs(x / z) >= 1;
                if (servo && !mm) {
                    try {
                        oS.write(new byte[]{(byte) 10, (byte) 7});
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    double xz;
                    xz = x / z;
                    xz = Math.atan(xz) * 180 / Math.PI;

                    int toreturn = (int) xz / 3;
                    //Moves a maximum of 30 degrees to a side, keeps it smooth
                    //Doesn't move as fast, but moves smoothly

                    try {
                        if (oS != null) {
                            TextView T = (TextView) findViewById(R.id.TextView);
                            T.setTextSize(15);
                            T.setText("before sending servo Message");
                            if (xz >= 0) {
                                oS.write(new byte[]{(byte) 125, (byte) 16, (byte) toreturn});
                            } else {
                                oS.write(new byte[]{(byte) 125, (byte) 17, (byte) (-toreturn)});
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        if (vector.x > 1) {
                            countLoss = 0;
                            Log.i("onVuforiaUpdate   ", "Right command");
                            oS.write(new byte[]{(byte) 125, (byte) 10, (byte) 5});
                        } else if (vector.x < -1 && vector.x > -999) {
                            // go left my dude
                            countLoss = 0;
                            Log.i("onVuforiaUpdate   ", "Left command");
                            oS.write(new byte[]{(byte) 125, (byte) 10, (byte) 6});
                        } else {
//                            countLoss = 0;
                            countLoss++;
                            Log.i("onVuforiaUpdate   ", "Stop command");
                            oS.write(new byte[]{(byte) 125, (byte) 10, (byte) 7});
                            //yeah we stop my dude
                        }
                        if (vector.x == -999 && vector.y == -999 && vector.z == -999) {
                            countLoss++;
                            if(countLoss > 7){
                                oS.write(new byte[]{(byte) 125, (byte) 10., (byte) 7});
                            }
                            Log.i("onVuforiaUpdate   ", "None found");
//                            oS.write(new byte[]{(byte) 10, (byte) 9});
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }else if (servo) {
                double xz;

                if (z != 0.0) {
                    xz = x / z;
                    xz = Math.atan(xz) * 180 / Math.PI;

                    int toreturn = (int) xz / 3;
                    //Moves a maximum of 30 degrees to a side, keeps it smooth
                    //Doesn't move as fast, but moves smoothly

                    try {
                        if (oS != null) {
                            TextView T = (TextView) findViewById(R.id.TextView);
                            T.setTextSize(15);
                            T.setText("before sending servo Message");
                            if (xz >= 0) {
                                oS.write(new byte[]{(byte) 125, (byte) 16, (byte) toreturn});
                            } else {
                                oS.write(new byte[]{(byte) 125, (byte) 17, (byte) (-toreturn)});
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Log.i("Pan degrees horizontal", "     " + String.valueOf(toreturn));

                } else {
                    TextView T = (TextView) findViewById(R.id.TextView);
                    T.setTextSize(15);
                    T.setText("z == 0?");
                }
            }
        }else{
            countLoss++;
            if(countLoss>7){
                try {
                    oS.write(new byte[]{(byte) 125, (byte) 10, (byte) 7});
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }
                //16 will be key to panning left and right

                //xz will be the servo that is turning in the x and z axis

//            if(z!=0){
//                yz = y/z;
//                yz = Math.atan(yz)*180/Math.PI;
//
//                int toreturn = (int) yz;
//                try{
//                    if(servo && oS!=null) {
//                        Toast.makeText(context,"SEnding a servo message",Toast.LENGTH_SHORT).show();
//                        if(yz>=0){
//                           oS.write(new byte[]{(byte) 125, (byte) 18, (byte) toreturn});
//                        }else{
//                            oS.write(new byte[]{(byte) 125, (byte) 19, (byte) (-toreturn)});
//                        }
//                    }
//                }catch(Exception e){
//                    e.printStackTrace();
//                }
//
//                Log.i("Pan degrees vertical", "     " + String.valueOf(toreturn));
//                //y should be negative while z should be positive, but it depends
//            }
            }
//            if(motor){
//                try {
//                    if (oS != null) {
//                        if (vector.x > 1) {
//                            Log.i("onVuforiaUpdate   ", "Right command");
//                            oS.write(new byte[]{(byte)10,(byte)2});
//                        } else if (vector.x < -1 && vector.x>-50) {
//                            // go left my dude
//                            Log.i("onVuforiaUpdate   ", "Left command");
//                            oS.write(new byte[]{(byte)10,(byte)3});
//                        }else{
//                            Log.i("onVuforiaUpdate   ","Stop command");
//                            oS.write(new byte[]{(byte)10,(byte) 7});
//                            //yeah we stop my dude
//                        }
//                        if(vector.x == -999 && vector.y == -999 && vector.z == -999){
//                            Log.i("onVuforiaUpdate   ", "None found");
//                            oS.write(new byte[]{(byte) 10, (byte) 9});
//                        }
//                    }
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//                //here we do the stuff
//            }
//        }else{
//            Log.i("onVuforiaUpdate","vuforia updated without finding");
//            vector.updateVector(new double[]{-999,-999,-999});
//            if(bigO){
//                try{
//                    if(oS!= null){
//                        oS.write(new byte[]{(byte) 10,(byte) 9});
//                    }
//                }catch(Exception e){
//                    e.printStackTrace();
//                }
//            }
//        }

//        this.state = state;

    @Override
    public void onInitARDone() {

        Log.i("onInitARDone", "we done my dude");
        //I CAN ADD STUFF FROM VUFORIAACTIVITY IF WE WANT AN OVERLAY
        Camera = CameraDevice.CAMERA_DIRECTION.CAMERA_DIRECTION_BACK;
        vClass.startAR(Camera);

    }

    public Boolean getExtendedTracking(){
        return extendedTracking;
    }

//    public void setExtendedTracking(Boolean et){
//        extendedTracking = et;
//    }

//    public float[] translation(float[] pose){
//        Matrix34F matrix34 = new Matrix34F();
//        matrix34.setData(pose);
//        Matrix44F matrix44 = com.vuforia.Tool.convert2GLMatrix(matrix34);
//        float[] newRes = new float[16];
//        Matrix.multiplyMM(newRes
//                ,0, front, 0 , matrix44.getData(),0);
//        float[] edit = {newRes[3],newRes[7],newRes[11],newRes[15]};
//        return new float[] {edit[0]/edit[3],edit[1]/edit[3],edit[2]/edit[3]};
//    }



//        float[] back = {
//                0,-1,0,0,
//                -1,0,0,0,
//                0,0,-1,0,
//                0,0,0,1
//        };

//        float[] front = {
//                0,1,0,0
//                -1,0,0,0,
//                0,0,1,0,
//                0,0,0,1
//        };


    public class Vector{
        private double x;
        private double y;
        private double z;

        private double[] values;

        public Vector(double x, double y, double z){
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public Vector(double[] values){
            this.x = values[0];
            this.y = values[1];
            this.z = values[2];
        }

        public double getX(){
            return x;
        }

        public double getY(){
            return y;
        }

        public double getZ(){
            return z;
        }

        public double[] getValues(){
            return values;
        }

        private void updateVector(double[] values){
            this.values = values;
            x = values[0];
            y = values[1];
            z = values[2];
        }
    }

    //This doesn't work and is unnecessary

//    public class vuforiaSender implements Runnable{
//
//        Vector vVector;
//        FileOutputStream outputStream;
//
//        public vuforiaSender(Vector vector, FileOutputStream oS){
//            outputStream = oS;
//            vVector = vector;
//        }
//
//        @Override
//        public void run() {
//            while(bigO) {
//                vVector = vector;
//                if (vVector.getValues()[0] == 999 && vVector.getValues()[1] == 999 && vVector.getValues()[2] == 999) {
//                    break;
//                } else if (vVector.getX() > 1) {
//                    Log.i("vuforiaSender Thread  ", "we are going right my dude");
//                    //go right my man
//                } else if (vVector.getX() < -1) {
//                    Log.i("vuforiaSender Thread ", " we are going left my dude");
//                    //go left my man
//                }
//                Log.i("vuforiaSender Thread", "we are cool");
//            }
//        }
//
//
//    }

    private void beginMqtt(){
        MqttHelper helper = new MqttHelper(getApplicationContext());
        helper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

            }

            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Toast.makeText(context, message.toString(),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }

}