package com.example.projectcuoiky.session;

public class DeviceSession {
    public enum Type {
        SERVER,
        BLUETOOTH,
        NONE
    }

    private Type type = Type.NONE;
    private String serverUrl;
    private String bluetoothDeviceName;
    private String bluetoothMac;

    public void setServer(String url) {
        this.type = Type.SERVER;
        this.serverUrl = url;
    }

    public void setBluetooth(String name, String mac) {
        this.type = Type.BLUETOOTH;
        this.bluetoothDeviceName = name;
        this.bluetoothMac = mac;
    }

    public Type getType() {
        return type;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public String getBluetoothDeviceName() {
        return bluetoothDeviceName;
    }

    public String getBluetoothMac() {
        return bluetoothMac;
    }
}
