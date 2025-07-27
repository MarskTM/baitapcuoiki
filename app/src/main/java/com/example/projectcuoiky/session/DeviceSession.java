package com.example.projectcuoiky.session;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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

    // ====== BỔ SUNG STATIC ĐỂ LƯU NHIỀU THIẾT BỊ BLE =======
    private static final String PREF_NAME = "ble_session";
    private static final String KEY_DEVICES = "connected_devices";

    // Model nhỏ gọn luôn nếu không muốn tạo BLEDevice riêng
    public static void addBLEDevice(Context context, String name, String mac) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String devicesJson = prefs.getString(KEY_DEVICES, "[]");
        try {
            JSONArray array = new JSONArray(devicesJson);
            for (int i = 0; i < array.length(); i++) {
                if (array.getJSONObject(i).getString("mac").equals(mac)) return; // tránh trùng
            }
            JSONObject obj = new JSONObject();
            obj.put("name", name);
            obj.put("mac", mac);
            array.put(obj);
            prefs.edit().putString(KEY_DEVICES, array.toString()).apply();
        } catch (JSONException e) { e.printStackTrace(); }
    }

    public static List<BluetoothDeviceInfo> getBLEDevices(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String devicesJson = prefs.getString(KEY_DEVICES, "[]");
        List<BluetoothDeviceInfo> devices = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(devicesJson);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                devices.add(new BluetoothDeviceInfo(obj.getString("name"), obj.getString("mac")));
            }
        } catch (JSONException e) { e.printStackTrace(); }
        return devices;
    }

    // Có thể tạo inner class nhỏ cho tiện:
    public static class BluetoothDeviceInfo {
        public String name;
        public String mac;
        public BluetoothDeviceInfo(String name, String mac) {
            this.name = name;
            this.mac = mac;
        }
    }
}
