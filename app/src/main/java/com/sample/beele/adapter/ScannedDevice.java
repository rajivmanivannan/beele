/**
 * @author Rajiv Manivannan <rajiv@contus.in>
 * @copyright Copyright (C) 2014 VSNMobil. All rights reserved.
 * @license http://www.apache.org/licenses/LICENSE-2.0
 */
package com.sample.beele.adapter;

import android.bluetooth.BluetoothDevice;

/**
 * ScannedDevice.java
 * Model class that provides details about device name, device MAC address and RSSI value.
 */
public class ScannedDevice {
    private static final String UNKNOWN = "Unknown";
    /**
     * BluetoothDevice
     */
    private BluetoothDevice bluetoothDevice;
    /**
     * RSSI
     */
    private int rssiValue;
    /**
     * Display Name
     */
    private String deviceDisplayName;
    /**
     * Device MAC Address
     */
    private String deviceDiplayAddress;

    public ScannedDevice(BluetoothDevice device, int rssi) {
        if (device == null) {
            throw new IllegalArgumentException("BluetoothDevice is null");
        }
        bluetoothDevice = device;
        deviceDisplayName = device.getName();
        if ((deviceDisplayName == null) || (deviceDisplayName.length() == 0)) {
            deviceDisplayName = UNKNOWN;
        }
        rssiValue = rssi;
        deviceDiplayAddress = device.getAddress();
    }

    public BluetoothDevice getDevice() {
        return bluetoothDevice;
    }

    public int getRssi() {
        return rssiValue;
    }

    public void setRssi(int rssi) {
        rssiValue = rssi;
    }

    public String getDisplayName() {
        return deviceDisplayName;
    }

    public void setDisplayName(String displayName) {
        deviceDisplayName = displayName;
    }

    public String getDeviceMac() {
        return deviceDiplayAddress;
    }

    public void setDeviceMac(String deviceAddress) {
        deviceDiplayAddress = deviceAddress;
    }
}
