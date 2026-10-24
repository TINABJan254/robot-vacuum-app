package com.ht.robotvacuumapp.network;

import android.content.Context;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static Retrofit retrofit = null;
    private static AuthApiService authApiService = null;

    public static synchronized void resetInstance() {
        retrofit = null;
        authApiService = null;
    }

    public static synchronized Retrofit getRetrofitInstance(Context context) {
        if (retrofit == null) {
            String baseUrl = ApiConfig.getBaseUrl(context.getApplicationContext());

            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .addInterceptor(loggingInterceptor)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static synchronized AuthApiService getAuthApiService(Context context) {
        if (authApiService == null) {
            authApiService = getRetrofitInstance(context).create(AuthApiService.class);
        }
        return authApiService;
    }
}
