package com.ht.robotvacuumapp.api;

import android.content.Context;
import android.content.SharedPreferences;
import okhttp3.HttpUrl;

public class ApiConfig {

    private static final String PREF_NAME = "ApiConfigPrefs";
    private static final String KEY_BASE_URL = "base_url";

    // Default LAN backend URL
    public static final String DEFAULT_BASE_URL = "http://172.20.10.2:8080/";

    public static String getBaseUrl(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String url = prefs.getString(KEY_BASE_URL, DEFAULT_BASE_URL);
        if (url == null || url.trim().isEmpty()) {
            url = DEFAULT_BASE_URL;
        }
        if (!url.endsWith("/")) {
            url = url + "/";
        }
        return url;
    }

    public static void setBaseUrl(Context context, String newUrl) {
        if (newUrl == null || newUrl.trim().isEmpty()) {
            newUrl = DEFAULT_BASE_URL;
        } else {
            newUrl = newUrl.trim();
            if (!newUrl.startsWith("http://") && !newUrl.startsWith("https://")) {
                newUrl = "http://" + newUrl;
            }
            if (!newUrl.endsWith("/")) {
                newUrl = newUrl + "/";
            }
        }
        if (HttpUrl.parse(newUrl) == null) {
            throw new IllegalArgumentException("Invalid backend URL");
        }
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_BASE_URL, newUrl).apply();
        RetrofitClient.resetInstance();
    }
}
