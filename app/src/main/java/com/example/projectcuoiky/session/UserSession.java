package com.example.projectcuoiky.session;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class UserSession {
    private static final String KEY_ID = "user_id";
    private static final String KEY_NAME = "username";
    private static final String KEY_ROLE = "role";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public UserSession(Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        editor = prefs.edit();
    }

    public void setUser(int id, String name, String phone, String role) {
        editor.putInt(KEY_ID, id);
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_ROLE, role);
        editor.apply(); // Áp dụng ngay
    }

    public int getId() {
        return prefs.getInt(KEY_ID, 0);
    }

    public String getName() {
        return prefs.getString(KEY_NAME, null);
    }

    public String getRole() {
        return prefs.getString(KEY_ROLE, "user"); // default là "user"
    }

    public boolean isLoggedIn() {
        return getId() > 0;
    }

    public void clear() {
        editor.clear();
        editor.apply();
    }
}
