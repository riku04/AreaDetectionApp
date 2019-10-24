package com.example.ueda_r.taiseiApp;

import android.bluetooth.BluetoothDevice;

public class ScanResultListItem {
    private BluetoothDevice bluetoothDevice = null;
    private String deviceName = "";
    private String deviceAddress = "";
    private String connectedAddress = "";
    private Boolean isChecked = false;
    private Boolean isConnectEnable = true;
    private String deviceStatus = "";
    private Boolean isAppeared = false;
    private int firstVisibleItem = 0;

    public ScanResultListItem() {

    }
    public ScanResultListItem(BluetoothDevice device) {
        bluetoothDevice  = device;
        if (device.getName() == null) {
            deviceName = "";
        } else {
            deviceName = device.getName();
        }
        deviceAddress = device.getAddress();
    }
    public BluetoothDevice getBluetoothDevice(){
        return bluetoothDevice;
    }
    public void setDeviceName(String name) {
        deviceName = name;
    }
    public String getDeviceName() {
        return deviceName;
    }
    public void setDeviceAddress(String address) {
        deviceAddress = address;
    }
    public String getDeviceAddress() {
        return deviceAddress;
    }
    public void setConnectedAddress(String address){
        connectedAddress = address;
    }
    public String getConnectedAddress(){
        return  connectedAddress;
    }
    public void setCheck(Boolean check) {
        isChecked = check;
    }
    public Boolean getIsChecked() {
        return isChecked;
    }
    public void setDeviceStatus(String status) {
        deviceStatus = status;
    }
    public String getDeviceStatus() {
        return deviceStatus;
    }
    public Boolean getIsConnectEnable(){
        return isConnectEnable;
    }
    public void setConnectEnable(Boolean enable) {
        isConnectEnable = enable;
    }
    public Boolean getIsAppeared() {
        return isAppeared;
    }
    public void setIsAppeared(Boolean bool) {
        isAppeared = bool;
    }
    public void setFirstVisibleItemNum(Integer num) {
        firstVisibleItem = num;
    }
    public Integer getFirstVisibleItemNum() {
        return firstVisibleItem;
    }
}
