package com.ht.robotvacuumapp.auth;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.ht.robotvacuumapp.network.ApiClient;
import com.ht.robotvacuumapp.network.ApiConfig;
import com.ht.robotvacuumapp.network.AuthApiService;
import com.ht.robotvacuumapp.network.model.ErrorResponse;
import com.ht.robotvacuumapp.network.model.LoginRequest;
import com.ht.robotvacuumapp.network.model.LoginResponse;
import com.ht.robotvacuumapp.network.model.RegisterRequest;
import com.ht.robotvacuumapp.network.model.RegisterResponse;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {

    private static final String TAG = "AuthRepository";
    private final Context context;
    private final AuthApiService apiService;
    private final Gson gson;

    public interface AuthCallback<T> {
        void onSuccess(T data);
        void onError(String errorMessage);
    }

    public AuthRepository(Context context) {
        this.context = context.getApplicationContext();
        this.apiService = ApiClient.getAuthApiService(this.context);
        this.gson = new Gson();
    }

    public void register(String name, String username, String password, AuthCallback<RegisterResponse> callback) {
        RegisterRequest request = new RegisterRequest(name, username, password);
        apiService.register(request).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    String errorMsg = parseErrorMessage(response.code(), response.errorBody());
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                Log.e(TAG, "Register failure", t);
                callback.onError(getNetworkErrorMessage(t));
            }
        });
    }

    public void login(String username, String password, AuthCallback<LoginResponse> callback) {
        LoginRequest request = new LoginRequest(username, password);
        apiService.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Save tokens and user session
                    TokenManager.getInstance(context).saveSession(response.body());
                    callback.onSuccess(response.body());
                } else {
                    String errorMsg = parseErrorMessage(response.code(), response.errorBody());
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e(TAG, "Login failure", t);
                callback.onError(getNetworkErrorMessage(t));
            }
        });
    }

    private String parseErrorMessage(int statusCode, ResponseBody errorBody) {
        if (statusCode == 401) {
            return "Tên đăng nhập hoặc mật khẩu không chính xác.";
        }
        if (statusCode == 409) {
            return "Tên đăng nhập đã tồn tại, vui lòng chọn tên khác.";
        }

        if (errorBody != null) {
            try {
                String errorJson = errorBody.string();
                ErrorResponse errorResponse = gson.fromJson(errorJson, ErrorResponse.class);
                if (errorResponse != null) {
                    return errorResponse.getDisplayMessage();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing error body", e);
            }
        }

        if (statusCode == 400) {
            return "Dữ liệu yêu cầu không hợp lệ (400 Bad Request).";
        }
        return "Lỗi từ máy chủ (HTTP " + statusCode + ").";
    }

    private String getNetworkErrorMessage(Throwable t) {
        String serverUrl = ApiConfig.getBaseUrl(context);
        return "Không thể kết nối đến máy chủ LAN (" + serverUrl + "). Vui lòng kiểm tra cùng mạng Wi-Fi và backend đang chạy.";
    }
}
