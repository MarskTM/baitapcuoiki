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

    // Cũ: Dùng cho logic gộp
    public void setServer(String url) {
        this.type = Type.SERVER;
        this.serverUrl = url;
    }

    public void setBluetooth(String name, String mac) {
        this.type = Type.BLUETOOTH;
        this.bluetoothDeviceName = name;
        this.bluetoothMac = mac;
    }

    // ✅ MỚI: bổ sung các setter riêng cho type và url
    public void setType(Type type) {
        this.type = type;
    }

    public void setServerUrl(String url) {
        this.serverUrl = url;
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
