package com.sample.beele;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.beele.BluetoothLe;
import com.beele.BluetoothLe.BluetoothLeListener;

/**
 * DeviceControlActivity.java
 *
 */
public class DeviceControlActivity extends Activity implements BluetoothLeListener {

    private final static String TAG = DeviceControlActivity.class.getSimpleName();
    //UI
    private TextView mConnectionState;
    private TextView mBatteryLevel;
    private TextView mButtonStatus;
    //Local Variables
    private String mDeviceName;
    private String mDeviceAddress;
    private boolean mConnected;
    //Class instance variable
    private BluetoothLe mBluetoothLe;
    private BluetoothManager mBluetoothManager = null;
    private BluetoothGatt mBluetoothGatt = null;
    private BluetoothDevice mBluetoothDevice;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_control);

        Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(AppConstant.EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(AppConstant.EXTRAS_DEVICE_ADDRESS);
        Log.i(TAG, mDeviceAddress);
        // Sets up UI references.
        ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
        mConnectionState = (TextView) findViewById(R.id.connection_state_txt);
        mBatteryLevel = (TextView) findViewById(R.id.battery_level_txt);
        mButtonStatus = (TextView) findViewById(R.id.button_status_txt);
        getActionBar().setTitle(mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        /***************************************************************************************************
         * Used the Texas Instrument's CC2540 BLE Development kit for this demo
         * Refer the following link http://www.ti.com/tool/cc2540dk
         ***************************************************************************************************/
        init();
    }

    private void init() {
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothDevice = mBluetoothManager.getAdapter().getRemoteDevice(mDeviceAddress);
        mBluetoothLe = new BluetoothLe(this, mBluetoothManager, this);
        mBluetoothGatt = mBluetoothLe.connect(mBluetoothDevice, false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_device_control, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_connect:
                if (mBluetoothLe != null)
                    mBluetoothLe.connect(mBluetoothDevice, false);
                return true;
            case R.id.menu_disconnect:
                if (mBluetoothLe != null)
                    mBluetoothLe.disconnect(mBluetoothGatt);
                return true;
            case android.R.id.home:
                if (mBluetoothLe != null)
                    mBluetoothLe.disconnect(mBluetoothGatt);
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        Log.i(TAG, "onServicesDiscovered");

        if (status == BluetoothGatt.GATT_SUCCESS) {

            for (BluetoothGattService service : gatt.getServices()) {

                if ((service == null) || (service.getUuid() == null)) {
                    continue;
                }

                if (AppConstant.SERVICE_DEVICE_INFO.equals(service.getUuid())) {
                    //Read the device serial number
                    mBluetoothLe.readCharacteristic(gatt, service.getCharacteristic(AppConstant.CHAR_SERIAL_NUMBER));
                    //Read the device software version
                    mBluetoothLe.readCharacteristic(gatt, service.getCharacteristic(AppConstant.CHAR_SOFTWARE_REV));
                }

                if (AppConstant.SERVICE_BATTERY_LEVEL.equals(service.getUuid())) {
                    //Read the device battery percentage
                    mBluetoothLe.readCharacteristic(gatt, service.getCharacteristic(AppConstant.CHAR_BATTERY_LEVEL));
                }

                if (AppConstant.SERVICE_BUTTON_PRESS_SERVICE.equals(service.getUuid())) {
                    // Set notification for key press from BLE Device.
                    mBluetoothLe.setCharacteristicNotification(gatt, service.getCharacteristic(AppConstant.CHAR_BUTTON_PRESS), true);
                }
            }
        }
    }

    @Override
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {

    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

        switch (newState) {
            case BluetoothProfile.STATE_CONNECTED:
                Log.i(TAG, "Connected");
                updateConnectionState(getString(R.string.connected));
                mConnected = true;
                invalidateOptionsMenu();
                //Start the service discovery
                gatt.discoverServices();
                break;
            case BluetoothProfile.STATE_DISCONNECTED:
                Log.i(TAG, "Disconnected");
                updateConnectionState(getString(R.string.disconnected));
                mConnected = false;
                invalidateOptionsMenu();
                break;
            default:
                break;
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        Log.i(TAG, "onCharacteristicChanged");
        if (AppConstant.CHAR_BUTTON_PRESS.equals(characteristic.getUuid())) {
            int event = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
            Log.i(TAG, "Button Press Detected:: " + event);
            if(event==0)
                updateButtonState(getString(R.string.button_pressed));
            else if(event == 2){
                updateButtonState(getString(R.string.button_released));
            }
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        Log.i(TAG, "onCharacteristicRead");
        if (status == BluetoothGatt.GATT_SUCCESS) {
            if (AppConstant.CHAR_BATTERY_LEVEL.equals(characteristic.getUuid())) {
                int batteryLevel = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                updateBatteryStatus(batteryLevel+" %");
                Log.i(TAG, "Battery Level :: "+batteryLevel);
            }
            //Update the database with received serial number of device.
            if (AppConstant.CHAR_SERIAL_NUMBER.equals(characteristic.getUuid())) {
                String serialNo = new String(characteristic.getValue());
                Log.i(TAG, "serialNo :: "+serialNo);
            }
            //Update the database with received software version.
            if (AppConstant.CHAR_SOFTWARE_REV.equals(characteristic.getUuid())) {
                String softwareVersion = new String(characteristic.getValue());
                Log.i(TAG, "softwareVersion :: "+softwareVersion);
            }
        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic
            characteristic, int status) {
        Log.i(TAG, "onCharacteristicWrite :: Status:: " + status);
    }

    @Override
    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        Log.i(TAG, "onDescriptorRead :: Status:: " + status);
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        Log.i(TAG, "onDescriptorWrite :: Status:: " + status);
    }

    @Override
    public void onError(String errorMessage) {
        Log.e(TAG, "Error:: " + errorMessage);
    }

    private void updateConnectionState(final String status) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(status);
            }
        });
    }

    private void updateButtonState(final String status) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mButtonStatus.setText(status);
            }
        });
    }

    private void updateBatteryStatus(final String status) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mBatteryLevel.setText(status);
            }
        });
    }
}
