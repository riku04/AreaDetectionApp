package com.example.ueda_r.taiseiApp;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.ueda_r.osmdroidtest1022.R;

import java.util.ArrayList;

public class ScanResultListAdapter extends ArrayAdapter<ScanResultListItem> {

    private int mResource;
    private ArrayList<ScanResultListItem> mItems;
    private LayoutInflater mInflater;

    private ScanResultListAdapterCallback callback;

    private Button connectButton;
    private Button settingButton;

    private CheckBox checkBox;
    private ProgressBar progressBar;

    public ScanResultListAdapter(Context context, int resource, ArrayList<ScanResultListItem> items) {
        super(context, resource, items);
        mResource = resource;
        mItems = items;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        Log.d("ScanResult", "position[" + Integer.toString(position) + "]");

//        if (convertView != null) {
//            view = convertView;
//        } else {
//            view = mInflater.inflate(mResource, null);
//        }

        if (view == null) {
            view = mInflater.inflate(mResource, null);
        }

        final ScanResultListItem item = mItems.get(position);

        final TextView deviceName = (TextView) view.findViewById(R.id.deviceName);
        deviceName.setText(item.getDeviceName());

        final TextView deviceAddress = (TextView) view.findViewById(R.id.deviceAddress);
        deviceAddress.setText(item.getDeviceAddress());

        final TextView deviceStatus = (TextView) view.findViewById(R.id.deviceStatus);
        Log.d("STATUS", item.getDeviceStatus());
        deviceStatus.setText(item.getDeviceStatus());


        final BluetoothDevice bluetoothDevice = item.getBluetoothDevice();

        progressBar = view.findViewById(R.id.progressBar);

        connectButton = view.findViewById(R.id.connectButton);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("BLE", "Connect button pressed.");

                //progressBar.setVisibility(ProgressBar.VISIBLE);

                callback.connectPressed(bluetoothDevice, connectButton, progressBar);
            }
        });

        //画面外のViewは操作しない
        if (item.getIsConnectEnable()) {
            if ((position >= item.getFirstVisibleItemNum()) && (position <= (item.getFirstVisibleItemNum() + 10))) { //visible item size : atom = 6 , zenfone = 10
                try {
                    connectButton.setEnabled(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Log.d("ScanResultListAdapter", "connect button set enable, out of view");
            }
        } else {
            if ((position >= item.getFirstVisibleItemNum()) && (position <= (item.getFirstVisibleItemNum() + 10))) { //visible item size : atom = 6 , zenfone = 10
                try {
                    connectButton.setEnabled(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Log.d("ScanResultListAdapter", "connect button set disable, out of view");
            }
        }

//        if (item.getDeviceAddress() == item.getConnectedAddress()) {
//            connectButton.setEnabled(false);
//            connectButton.setText("接続中");
//        } else {
//            connectButton.setEnabled(true);
//            connectButton.setText("接続");
//        }

//        settingButton = view.findViewById(R.id.settingButton);
//        settingButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.i("BLE", "Setting button pressed.");
//                callback.settingPressed(bluetoothDevice, settingButton, progressBar);
//            }
//        });
//        if (item.getDeviceAddress() == item.getConnectedAddress()) {
//            settingButton.setEnabled(true);
//        } else {
//            settingButton.setEnabled(false);
//        }

        checkBox = view.findViewById(R.id.checkBox);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i("BLE", "CheckBox changed => " + Boolean.toString(isChecked));
                item.setCheck(isChecked);
                callback.deviceChecked(bluetoothDevice, isChecked);
            }
        });
        if (item.getIsChecked()) {
            checkBox.setChecked(true);
        } else {
            checkBox.setChecked(false);
        }
        return view;
    }

    public void setCallback(ScanResultListAdapterCallback callback) {
        this.callback = callback;
    }

    public interface ScanResultListAdapterCallback {
        void connectPressed(BluetoothDevice bluetoothDevice, Button connectButton, ProgressBar progressBar);

        //void settingPressed(BluetoothDevice bluetoothDevice, Button settingButton, ProgressBar progressBar);

        void deviceChecked(BluetoothDevice bluetoothDevice, Boolean isChecked);
    }
}
