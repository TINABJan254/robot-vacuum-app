package com.ht.robotvacuumapp.model.local;

import android.content.Context;
import android.content.SharedPreferences;

public class LoginPreferences {
    private final SharedPreferences prefs;

    public LoginPreferences(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE);
    }

    public boolean isRememberMe() {
        return prefs.getBoolean("remember_me", false);
    }

    public String getSavedUsername() {
        return isRememberMe() ? prefs.getString("saved_username", "") : "";
    }

    public void save(String username, boolean rememberMe) {
        SharedPreferences.Editor editor = prefs.edit().putBoolean("remember_me", rememberMe);
        if (rememberMe) editor.putString("saved_username", username);
        else editor.remove("saved_username");
        editor.apply();
    }
}
