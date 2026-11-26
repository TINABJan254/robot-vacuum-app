package com.ht.robotvacuumapp.model.repository;

import android.content.Context;
import com.google.gson.Gson;
import com.ht.robotvacuumapp.api.ApiConfig;
import com.ht.robotvacuumapp.api.RetrofitClient;
import com.ht.robotvacuumapp.api.dto.*;
import com.ht.robotvacuumapp.api.service.AuthApiService;
import com.ht.robotvacuumapp.model.local.LoginPreferences;
import com.ht.robotvacuumapp.model.local.TokenManager;
import java.util.HashSet;
import java.util.Set;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** One instance per ViewModel; owns only that screen's requests. */
public class AuthRepository {
    private static final String SESSION_EXPIRED = "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.";
    private final Context context;
    private final TokenManager tokens;
    private final LoginPreferences preferences;
    private final Set<Call<?>> requests = new HashSet<>();
    private final Gson gson = new Gson();

    public interface AuthCallback<T> {
        void onSuccess(T data);
        void onError(int status, String message);
    }

    public AuthRepository(Context context) {
        this.context = context.getApplicationContext();
        tokens = TokenManager.getInstance(this.context);
        preferences = new LoginPreferences(this.context);
    }

    private AuthApiService service() {
        return RetrofitClient.getRetrofitInstance(context).create(AuthApiService.class);
    }

    public void login(String username, String password, AuthCallback<LoginResponse> callback) {
        enqueue(service().login(new LoginRequest(username, password)), true, new AuthCallback<LoginResponse>() {
            @Override public void onSuccess(LoginResponse data) {
                tokens.saveSession(data);
                callback.onSuccess(data);
            }
            @Override public void onError(int status, String message) {
                callback.onError(status, status == 401 ? "Tên đăng nhập hoặc mật khẩu không chính xác." : message);
            }
        });
    }

    public void register(String name, String username, String password, AuthCallback<RegisterResponse> callback) {
        enqueue(service().register(new RegisterRequest(name, username, password)), true, callback);
    }

    public void getAccount(AuthCallback<UserDto> callback) {
        String header = tokens.getAuthHeader();
        if (blank(header)) {
            callback.onError(401, SESSION_EXPIRED);
            return;
        }
        enqueue(service().getAccount(header), true, callback);
    }

    public void logoutAccount(AuthCallback<Void> callback) {
        String header = tokens.getAuthHeader();
        if (blank(header)) {
            callback.onError(401, SESSION_EXPIRED);
            return;
        }
        enqueue(service().logout(header), false, callback);
    }

    private boolean blank(String value) { return value == null || value.trim().isEmpty(); }

    private <T> void enqueue(Call<T> request, boolean requireBody, AuthCallback<T> callback) {
        requests.add(request);
        request.enqueue(new Callback<T>() {
            @Override public void onResponse(Call<T> call, Response<T> response) {
                requests.remove(call);
                if (call.isCanceled()) {
                    if (response.errorBody() != null) response.errorBody().close();
                    return;
                }
                if (!response.isSuccessful()) {
                    callback.onError(response.code(), parseError(response.code(), response.errorBody()));
                } else if (requireBody && response.body() == null) {
                    callback.onError(0, "Máy chủ trả về dữ liệu trống. Vui lòng thử lại.");
                } else {
                    callback.onSuccess(response.body());
                }
            }
            @Override public void onFailure(Call<T> call, Throwable error) {
                requests.remove(call);
                if (!call.isCanceled()) callback.onError(0, "Không thể kết nối đến máy chủ ("
                        + getBaseUrl() + "). Vui lòng kiểm tra kết nối và thử lại.");
            }
        });
    }

    private String parseError(int status, ResponseBody body) {
        try (ResponseBody errorBody = body) {
            if (status == 401) return SESSION_EXPIRED;
            if (status == 409) return "Tên đăng nhập đã tồn tại, vui lòng chọn tên khác.";
            if (errorBody != null) {
                ErrorResponse error = gson.fromJson(errorBody.string(), ErrorResponse.class);
                if (error != null) {
                    if (error.getFieldErrors() != null && !error.getFieldErrors().isEmpty()) {
                        StringBuilder message = new StringBuilder();
                        for (FieldError field : error.getFieldErrors()) {
                            if (field == null) continue;
                            if (message.length() > 0) message.append('\n');
                            message.append(field.getField()).append(": ").append(field.getMessage());
                        }
                        if (message.length() > 0) return message.toString();
                    }
                    if (error.getMessage() != null && !error.getMessage().isEmpty()) return error.getMessage();
                }
            }
        } catch (Exception ignored) {
            // Fall back to an HTTP message if the server did not return the documented JSON.
        }
        return status == 400 ? "Dữ liệu yêu cầu không hợp lệ (400 Bad Request)."
                : "Lỗi từ máy chủ (HTTP " + status + ").";
    }

    public void cancelPendingRequests() {
        for (Call<?> call : new HashSet<>(requests)) call.cancel();
        requests.clear();
    }

    public boolean isLoggedIn() { return tokens.isLoggedIn(); }
    public String getUserName() { return tokens.getUserName(); }
    public String getUsername() { return tokens.getUsername(); }
    public String getUserRole() { return tokens.getUserRole(); }
    public void logout() { tokens.clearSession(); }
    public boolean isRememberMe() { return preferences.isRememberMe(); }
    public String getSavedUsername() { return preferences.getSavedUsername(); }
    public void saveLoginPreference(String username, boolean rememberMe) { preferences.save(username, rememberMe); }
    public String getBaseUrl() { return ApiConfig.getBaseUrl(context); }
    public void setBaseUrl(String url) { ApiConfig.setBaseUrl(context, url); }
    public void resetBaseUrl() { ApiConfig.setBaseUrl(context, ApiConfig.DEFAULT_BASE_URL); }
}
