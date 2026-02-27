package com.readyrecipe.android.network;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Lightweight session store backed by ReadyRecipePrefs so both legacy and new screens share JWTs.
 */
public class SessionManager {
    private static final String PREF_NAME = "ReadyRecipePrefs";
    private static final String KEY_JWT = "jwt";
    private static final String KEY_USER_ID = "userId";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveSession(String jwt, String userId) {
        prefs.edit()
                .putString(KEY_JWT, jwt)
                .putString(KEY_USER_ID, userId)
                .apply();
    }

    public String getJwt() {
        return prefs.getString(KEY_JWT, null);
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
