package com.beele;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

import java.util.HashMap;
import java.util.UUID;

/**
 * BluetoothLeService.java
 * <p/>
 * The communication between the Bluetooth Low Energy device will be communicated through this service class only
 * The initial connect request and disconnect request will be executed in this class.Also, all the status from the Bluetooth device
 * will be notified in the corresponding callback methods.
 */
public class BluetoothLe {

    private static UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private ProcessQueueExecutor processQueueExecutor = new ProcessQueueExecutor();
    // To add and maintain the BluetoothGatt object of each BLE device.
    private HashMap<String, BluetoothGatt> bluetoothGattHashMap = new HashMap<>();
    private BluetoothLeListener mBluetoothLeListener;
    private BluetoothManager mBluetoothManager;
    private Context context;
    // The connection status of the Blue tooth Low energy Device will be
    // notified in the below callback.
    private BluetoothGattCallback mGattCallbacks = new BluetoothGattCallback() {

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            mBluetoothLeListener.onServicesDiscovered(gatt, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            mBluetoothLeListener.onReadRemoteRssi(gatt, rssi, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            mBluetoothLeListener.onCharacteristicChanged(gatt, characteristic);
        }


        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            mBluetoothLeListener.onConnectionStateChange(gatt, status, newState);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            mBluetoothLeListener.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            mBluetoothLeListener.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            mBluetoothLeListener.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            mBluetoothLeListener.onDescriptorWrite(gatt, descriptor, status);
        }

    };

    public BluetoothLe(Context context, BluetoothManager mBluetoothManager, BluetoothLeListener callback) {
        mBluetoothLeListener = callback;
        this.mBluetoothManager = mBluetoothManager;
        this.context = context;

        if (!processQueueExecutor.isAlive()) {
            processQueueExecutor.start();
        }
    }

    //---------------------------------------------- Read / Write / Set Notification Functions --------------------------------------------------------------//

    /**
     * To read the value from the BLE Device
     *
     * @param mGatt          BluetoothGatt object of the device.
     * @param characteristic BluetoothGattCharacteristic of the device.
     */
    public void readCharacteristic(BluetoothGatt mGatt, BluetoothGattCharacteristic characteristic) {
        ReadWriteCharacteristic readWriteCharacteristic = new ReadWriteCharacteristic(ProcessQueueExecutor.REQUEST_TYPE_READ_CHAR, mGatt, characteristic);
        ProcessQueueExecutor.addProcess(readWriteCharacteristic);
    }

    /**
     * To write the value to BLE Device
     *
     * @param mGatt          BluetoothGatt object of the device.
     * @param characteristic BluetoothGattCharacteristic of the device.
     * @param b              value to write on to the BLE device.
     */
    public void writeCharacteristic(BluetoothGatt mGatt, BluetoothGattCharacteristic characteristic, byte[] b) {
        characteristic.setValue(b);
        ReadWriteCharacteristic readWriteCharacteristic = new ReadWriteCharacteristic(ProcessQueueExecutor.REQUEST_TYPE_WRITE_CHAR, mGatt, characteristic);
        ProcessQueueExecutor.addProcess(readWriteCharacteristic);
    }

    /**
     * To read the descriptor value from the BLE Device
     *
     * @param mGatt          BluetoothGatt object of the device.
     * @param characteristic BluetoothGattCharacteristic of the device.
     */
    public void readDescriptor(BluetoothGatt mGatt, BluetoothGattCharacteristic characteristic) {
        ReadWriteCharacteristic readWriteCharacteristic = new ReadWriteCharacteristic(ProcessQueueExecutor.REQUEST_TYPE_READ_DESCRIPTOR, mGatt, characteristic);
        ProcessQueueExecutor.addProcess(readWriteCharacteristic);
    }

    /**
     * To write the descriptor value to BLE Device
     *
     * @param mGatt          BluetoothGatt object of the device.
     * @param characteristic BluetoothGattCharacteristic of the device.
     * @param b              value to write on to the BLE device.
     */
    public void writeDescriptor(BluetoothGatt mGatt, BluetoothGattCharacteristic characteristic, byte[] b) {
        characteristic.setValue(b);
        ReadWriteCharacteristic readWriteCharacteristic = new ReadWriteCharacteristic(ProcessQueueExecutor.REQUEST_TYPE_WRITE_DESCRIPTOR, mGatt, characteristic);
        ProcessQueueExecutor.addProcess(readWriteCharacteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification. False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGatt mGatt, BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (!mGatt.setCharacteristicNotification(characteristic, enabled)) {
            return;
        }
        final BluetoothGattDescriptor clientConfig = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG);
        if (clientConfig == null) {
            return;
        }
        clientConfig.setValue(enabled ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        ReadWriteCharacteristic readWriteCharacteristic = new ReadWriteCharacteristic(ProcessQueueExecutor.REQUEST_TYPE_WRITE_DESCRIPTOR, mGatt, clientConfig);
        ProcessQueueExecutor.addProcess(readWriteCharacteristic);
    }
    // -----------------------------------------------------------***-------------------------------------------------------------------//


    /**
     * Connect bluetooth gatt.
     *
     * @param device      the device
     * @param autoConnect the auto connect
     * @return the bluetooth gatt
     */
    public BluetoothGatt connect(BluetoothDevice device, boolean autoConnect) {
        if (mBluetoothManager==null) {
            mBluetoothLeListener.onError("BluetoothManager is null");
        }
        if (device==null) {
            mBluetoothLeListener.onError("BluetoothDevice is null");
        }
        BluetoothGatt bluetoothGatt = bluetoothGattHashMap.get(device.getAddress());
        if (bluetoothGatt!=null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
        }
        int connectionState = mBluetoothManager.getConnectionState(device, BluetoothProfile.GATT);
        BluetoothGatt mBluetoothGatt = null;
        if (connectionState == BluetoothProfile.STATE_DISCONNECTED) {
            mBluetoothGatt = device.connectGatt(context, autoConnect, mGattCallbacks);
            // Add the each BluetoothGatt in to an array list.
            if (!bluetoothGattHashMap.containsKey(device.getAddress())) {
                bluetoothGattHashMap.put(device.getAddress(), mBluetoothGatt);
            } else {
                bluetoothGattHashMap.remove(device.getAddress());
                bluetoothGattHashMap.put(device.getAddress(), mBluetoothGatt);
            }
        }
        return mBluetoothGatt;
    }

    /**
     * Disconnect bluetooth gatt.
     *
     * @param mBluetoothGatt the BluetoothGatt of the device.
     */
    public void disconnect(BluetoothGatt mBluetoothGatt) {
        try {
            bluetoothGattHashMap.remove(mBluetoothGatt.getDevice().getAddress());
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
        } catch (Exception e) {
        }
    }

    // Destructor
    @Override
    protected void finalize() throws Throwable {
        if (!processQueueExecutor.equals(null))
            processQueueExecutor.interrupt();
    }

    public interface BluetoothLeListener {
        //Core methods.
        void onServicesDiscovered(BluetoothGatt gatt, int status);

        void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status);

        void onConnectionStateChange(BluetoothGatt gatt, int status, int newState);

        void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic);

        //Read / Write Response method.
        void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status);

        void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status);

        void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status);

        void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status);

        void onError(String errorMessage);
    }
}
