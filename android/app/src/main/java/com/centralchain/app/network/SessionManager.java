package com.centralchain.app.network;

import android.content.Context;
import android.content.SharedPreferences;

import com.centralchain.app.models.LoginResponse;

// saves the logged in user
public class SessionManager {
    private static final String PREFS_NAME = "session";

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static void saveLogin(Context context, LoginResponse response) {
        getPrefs(context).edit()
                .putString("token", response.token)
                .putString("fullName", response.fullName)
                .putString("role", response.roleName)
                .apply();
    }

    // login does not give the id so we save it after
    public static void saveUserId(Context context, long userId) {
        getPrefs(context).edit().putLong("userId", userId).apply();
    }

    public static String getToken(Context context) {
        return getPrefs(context).getString("token", null);
    }

    public static long getUserId(Context context) {
        return getPrefs(context).getLong("userId", 0);
    }

    public static String getFullName(Context context) {
        return getPrefs(context).getString("fullName", "");
    }

    public static String getRole(Context context) {
        return getPrefs(context).getString("role", null);
    }

    public static boolean isLoggedIn(Context context) {
        return getToken(context) != null;
    }

    public static void logout(Context context) {
        getPrefs(context).edit().clear().apply();
    }
}
