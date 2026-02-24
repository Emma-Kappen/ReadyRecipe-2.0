package com.readyrecipe.android;

import android.app.Activity;
import android.os.Handler;
import android.widget.Button;
import android.widget.EditText;

public class DebugLoginHelper {
    public static void enableDebugAutoLogin(Activity activity, EditText emailField, EditText passwordField, Button loginButton) {
        if (!BuildConfig.DEBUG) return;
        // Pre-fill known test credentials used earlier when seeding DB
        emailField.setText("test1@example.com");
        passwordField.setText("testpass");
        // Auto-submit after a short delay so the UI can finish initializing
        new Handler(activity.getMainLooper()).postDelayed(() -> loginButton.performClick(), 700);
    }
}
