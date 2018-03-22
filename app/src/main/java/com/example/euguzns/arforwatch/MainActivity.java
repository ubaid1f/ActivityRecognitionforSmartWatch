package com.example.euguzns.arforwatch;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import dataobject.MessageType;
import dataobject.SensorObject;

public class MainActivity extends WearableActivity implements SensorEventListener {

    // Sensor setup
    private SensorManager mSensorManager = null;
//    private Sensor mLinearACCSensor = null;
    private Sensor mACCSensor = null;

    private double[] accXArray;
    private double[] accYArray;
    private double[] accZArray;

    // Array to store ACC data
    private double accLinearX;
    private double accLinearY;
    private double accLinearZ;
    private double accX;
    private double accY;
    private double accZ;

    // SensorObject types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_SHOW = 2;
    public static final int MESSAGE_DEVICE_NAME = 3;
    public static final int MESSAGE_TOAST = 4;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Temporary values related to ACC data
    private int accCount = 0;
    private int sensorSize = 50;
    private boolean sensorStarted = false;
    private boolean bluetoothConnected = false;
    private String selectedActivity = null;
    private String previousActivity = null;
    private String previousRealActivity = null;
    private boolean firstActivity = true;

    // Intent request codes
    private static final int REQUEST_ENABLE_BT = 2;

    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;

    private static SensorObject mSensorObject = null;
    private static MessageType msg = null;

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;

    // Member object for the chat services
    private BluetoothChatService mChatService = null;

    private final MyHandler mHandler = new MyHandler(this);

    Timer timerSensor;
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Enables Always-on
        setAmbientEnabled();

        accXArray = new double[sensorSize];
        accYArray = new double[sensorSize];
        accZArray = new double[sensorSize];

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Setup to listen the sensor
        // You can change the sensor type anytime on the second line "Sensor.TYPE_LINEAR_ACCELERATION"
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        mACCSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        final Spinner spi = (Spinner) findViewById(R.id.activitySpinner);
        spi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedActivity = spi.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            // Initialize the BluetoothChatService to perform bluetooth connections
            if (mChatService == null)
                mChatService = new BluetoothChatService(this, mHandler);
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if (mChatService != null) {
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                mChatService.start();
            }
        }
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (mChatService != null) mChatService.stop();

        bluetoothConnected = false;
        mSensorManager.unregisterListener(this);

        // Close the app completely when you terminate from the phone
        // If this is not included, the sensor reading will run till battery depletion
        moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        synchronized (this) {

            accXArray[accCount] = event.values[0];
            accYArray[accCount] = event.values[1];
            accZArray[accCount] = event.values[2];

            accCount++;

            if (accCount == 50) {
                mSensorObject = new SensorObject(MessageType.Data, selectedActivity);
                mSensorObject.setData(accXArray, accYArray, accZArray);
                sendMessage(mSensorObject);
                accCount = 0;
            }
        }
    }

    // Nothing to do here but necessary function
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    // Initialize the BluetoothChatService to perform bluetooth connections
                    mChatService = new BluetoothChatService(this, mHandler);
                } else {
                    // User did not enable Bluetooth or an error occured
                    Toast.makeText(this, "bt_not_enabled_leaving", Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    private static class MyHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        private MyHandler(MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivity.get();
            if (activity != null) {
                activity.handleMessage(msg);
            }
        }
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_SHOW:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
                showActivity(readMessage);
                break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                        + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                bluetoothConnected = true;
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                        Toast.LENGTH_SHORT).show();
                if (msg.arg1 == 2) {
                    bluetoothConnected = false;
                }
                break;
        }
    }

    private void sendMessage(Object message) {

        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, "not_connected", Toast.LENGTH_SHORT).show();
            return;
        }
        // Check that there's actually something to send
        if (message != null) {

            mChatService.write(message);
        }
    }

    public void showActivity(String act) {

        String[] splitting = act.split(",");
        String watchActivity = splitting[0];
        String currentActivity = splitting[1];

        TextView textview = (TextView) findViewById(R.id.activityTextView);
        timeStamp = getTime();
        if (firstActivity) {
            previousRealActivity = previousActivity = act;
            textview.append(currentActivity + "\n");
            firstActivity = false;
        } else {
            if ((previousRealActivity.equals(previousActivity) && !previousActivity.equals(act)) || (!previousRealActivity.equals(previousActivity) && !previousActivity.equals(act))) {
                previousActivity = act;
            } else if (!previousRealActivity.equals(previousActivity) && previousActivity.equals(act)) {
                previousRealActivity = previousActivity = act;
                textview.append(currentActivity + "\n");

            }
        }
    }

    public void start(View v) {
        Button buttonClick = (Button) findViewById(R.id.start);
        if (bluetoothConnected) {
            if (sensorStarted) {
                sendMessage(new SensorObject(MessageType.Stop));
                mSensorManager.unregisterListener(this);
                sensorStarted = false;
                firstActivity = true;
                previousRealActivity = previousActivity = null;
                buttonClick.setText("Start");
            } else {
                sendMessage(new SensorObject(MessageType.Start));

                mSensorManager.registerListener(this, mACCSensor, SensorManager.SENSOR_DELAY_FASTEST);
                sensorStarted = true;
                buttonClick.setText("Stop");
            }
        } else {
            Toast.makeText(getApplicationContext(), "Connect the device first!", Toast.LENGTH_SHORT).show();
        }
    }


    public String getTime() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        String result = "";

        SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");//yyyy_MM_dd HH:mm:ss"yyyy:MM:dd_HH:mm:ss.SSS"
        result = format.format(new Date());

        return result;
    }
}
