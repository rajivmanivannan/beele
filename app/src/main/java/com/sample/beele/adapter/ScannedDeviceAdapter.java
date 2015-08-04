package com.sample.beele.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sample.beele.R;
import com.sample.beele.model.ScannedDevice;

import java.util.List;

/**
 * ScannedDeviceAdapter.java
 * <p/>
 * This adapter is used to load the scanned Ble Device in list view.
 */
public class ScannedDeviceAdapter extends ArrayAdapter<ScannedDevice> {
    private List<ScannedDevice> list;
    private LayoutInflater inflater;
    private int resId;

    // Constructor
    public ScannedDeviceAdapter(Context context, int resId,
                                List<ScannedDevice> objects) {
        super(context, resId, objects);
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.resId = resId;
        list = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ScannedDevice item = (ScannedDevice) getItem(position);
        if (convertView == null) {
            convertView = inflater.inflate(resId, null);
        }
        TextView deviceNameTextview = (TextView) convertView
                .findViewById(R.id.device_name);
        TextView deviceAddress = (TextView) convertView
                .findViewById(R.id.device_address);
        deviceNameTextview.setText(item.getDisplayName());
        deviceAddress.setText(item.getDevice().getAddress());
        return convertView;
    }

    /**
     * add or update BluetoothDevice
     */
    public void update(BluetoothDevice newDevice, int rssi, byte[] scanRecord) {
        if ((newDevice == null) || (newDevice.getAddress() == null)) {
            return;
        }

        boolean contains = false;
        for (ScannedDevice device : list) {
            if (newDevice.getAddress().equals(device.getDevice().getAddress())) {
                contains = true;
                device.setRssi(rssi); // update
                break;
            }
        }

        if (!contains) {
            // add new BluetoothDevice into the adapter.
            list.add(new ScannedDevice(newDevice, rssi));
        }
        // Refresh the list view.
        notifyDataSetChanged();
    }
}
