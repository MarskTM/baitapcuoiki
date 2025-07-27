package com.example.projectcuoiky;

import android.app.Application;

import com.example.projectcuoiky.session.DeviceSession;
import com.example.projectcuoiky.session.UserSession;

public class MyApp extends Application {
    private static DeviceSession deviceSession;
    private static UserSession userSession;

    @Override
    public void onCreate() {
        super.onCreate();
        deviceSession = new DeviceSession();
        userSession = new UserSession(getApplicationContext()); // Đã sửa
    }

    public static DeviceSession getDeviceSession() {
        return deviceSession;
    }

    public static UserSession getUserSession() {
        return userSession;
    }
}
