package com.readyrecipe.app.api;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "auth_prefs";
    private static final String KEY_JWT_TOKEN = "jwt_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveTokens(String token, String refreshToken) {
        prefs.edit()
                .putString(KEY_JWT_TOKEN, token)
                .putString(KEY_REFRESH_TOKEN, refreshToken)
                .apply();
    }

    public String getToken() {
        return prefs.getString(KEY_JWT_TOKEN, null);
    }

    public String getRefreshToken() {
        return prefs.getString(KEY_REFRESH_TOKEN, null);
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }
}
