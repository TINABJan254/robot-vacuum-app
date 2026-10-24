package com.ht.robotvacuumapp.auth;

import android.content.Context;
import android.content.SharedPreferences;

import com.ht.robotvacuumapp.network.model.LoginResponse;
import com.ht.robotvacuumapp.network.model.UserDto;

public class TokenManager {

    private static final String PREF_NAME = "AuthTokenPrefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_TOKEN_TYPE = "token_type";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_USERNAME = "user_username";
    private static final String KEY_USER_ROLE = "user_role";

    private static TokenManager instance;
    private final SharedPreferences prefs;

    private TokenManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized TokenManager getInstance(Context context) {
        if (instance == null) {
            instance = new TokenManager(context);
        }
        return instance;
    }

    public void saveSession(LoginResponse response) {
        SharedPreferences.Editor editor = prefs.edit();
        if (response != null) {
            editor.putString(KEY_ACCESS_TOKEN, response.getAccessToken());
            editor.putString(KEY_REFRESH_TOKEN, response.getRefreshToken());
            editor.putString(KEY_TOKEN_TYPE, response.getTokenType());

            UserDto user = response.getUser();
            if (user != null) {
                if (user.getId() != null) editor.putLong(KEY_USER_ID, user.getId());
                editor.putString(KEY_USER_NAME, user.getName());
                editor.putString(KEY_USER_USERNAME, user.getUsername());
                editor.putString(KEY_USER_ROLE, user.getRole());
            }
        }
        editor.apply();
    }

    public boolean isLoggedIn() {
        String token = prefs.getString(KEY_ACCESS_TOKEN, null);
        return token != null && !token.trim().isEmpty();
    }

    public String getAccessToken() {
        return prefs.getString(KEY_ACCESS_TOKEN, null);
    }

    public String getRefreshToken() {
        return prefs.getString(KEY_REFRESH_TOKEN, null);
    }

    public String getAuthHeader() {
        String token = getAccessToken();
        if (token != null && !token.isEmpty()) {
            return "Bearer " + token;
        }
        return null;
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "");
    }

    public String getUsername() {
        return prefs.getString(KEY_USER_USERNAME, "");
    }

    public String getUserRole() {
        return prefs.getString(KEY_USER_ROLE, "USER");
    }

    public long getUserId() {
        return prefs.getLong(KEY_USER_ID, -1);
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }
}
