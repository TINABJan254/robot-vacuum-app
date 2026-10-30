package com.ht.robotvacuumapp.model.repository;

import android.content.Context;
import com.ht.robotvacuumapp.api.RetrofitClient;
import com.ht.robotvacuumapp.api.dto.*;
import com.ht.robotvacuumapp.api.service.AlertApiService;
import com.ht.robotvacuumapp.model.local.TokenManager;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Supplier;
import retrofit2.Call;
import retrofit2.Response;

/** One screen owns its requests; page, detail and read operations have independent lifetimes. */
public class AlertRepository {
    public static final int PAGE_SIZE = 20;
    private enum Scope { LIST, DETAIL, READ }
    private final Supplier<AlertApiService> services;
    private final Supplier<String> authorization;
    private final Map<Call<?>, Scope> requests = new ConcurrentHashMap<>();
    private final AtomicLong allVersion = new AtomicLong();
    private final AtomicLong listVersion = new AtomicLong();
    private final AtomicLong detailVersion = new AtomicLong();

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(int status, String message);
    }

    public AlertRepository(Context context) {
        Context app = context.getApplicationContext();
        services = () -> RetrofitClient.getRetrofitInstance(app).create(AlertApiService.class);
        authorization = () -> TokenManager.getInstance(app).getAuthHeader();
    }
    public AlertRepository(Supplier<AlertApiService> services, Supplier<String> authorization) {
        this.services = services;
        this.authorization = authorization;
    }

    public void getAlerts(int page, Callback<AlertPageResponse> callback) {
        if (page < 0) { invalidRequest(callback); return; }
        String header = header(callback);
        if (header == null) return;
        enqueue(services.get().getAlerts(header, page, PAGE_SIZE, Arrays.asList("createdAt,desc", "id,desc")),
                Scope.LIST, callback, response -> {
                    if (!response.isValidFor(page, PAGE_SIZE)) invalidResponse(callback);
                    else callback.onSuccess(response);
                });
    }

    public void getAlert(long id, Callback<AlertResponse> callback) {
        if (id <= 0) { invalidRequest(callback); return; }
        String header = header(callback);
        if (header == null) return;
        enqueue(services.get().getAlert(header, id), Scope.DETAIL, callback, response -> {
            if (!response.isValid() || response.getId() != id) invalidResponse(callback);
            else callback.onSuccess(response);
        });
    }

    public void markRead(long id, long robotId, String readAt, Callback<AlertResponse> callback) {
        if (id <= 0 || robotId <= 0) { invalidRequest(callback); return; }
        UpdateAlertReadAtRequest body;
        try { body = new UpdateAlertReadAtRequest(readAt); }
        catch (IllegalArgumentException | ArithmeticException error) { invalidRequest(callback); return; }
        String header = header(callback);
        if (header == null) return;
        enqueue(services.get().markRead(header, id, body), Scope.READ, callback, response -> {
            if (!response.isValid() || response.getId() != id || response.getRobotId() != robotId || response.isUnread()) {
                invalidResponse(callback);
            } else callback.onSuccess(response);
        });
    }

    private String header(Callback<?> callback) {
        String value = authorization.get();
        if (value == null || value.trim().isEmpty()) {
            callback.onError(401, "Vui lòng đăng nhập lại để xem thông báo.");
            return null;
        }
        return value;
    }

    private long scopeVersion(Scope scope) {
        return scope == Scope.LIST ? listVersion.get() : scope == Scope.DETAIL ? detailVersion.get() : 0;
    }

    private <T> void enqueue(Call<T> request, Scope scope, Callback<T> callback, Consumer<T> success) {
        long all = allVersion.get();
        long version = scopeVersion(scope);
        requests.put(request, scope);
        request.enqueue(new retrofit2.Callback<T>() {
            private boolean canceled(Call<T> call) {
                return call.isCanceled() || all != allVersion.get() || version != scopeVersion(scope);
            }
            @Override public void onResponse(Call<T> call, Response<T> response) {
                requests.remove(call);
                if (canceled(call)) return;
                if (response.code() == 200 && response.body() != null) success.accept(response.body());
                else if (response.isSuccessful()) invalidResponse(callback);
                else {
                    if (response.errorBody() != null) response.errorBody().close();
                    callback.onError(response.code(), errorMessage(response.code()));
                }
            }
            @Override public void onFailure(Call<T> call, Throwable error) {
                requests.remove(call);
                if (!canceled(call)) callback.onError(0, scope == Scope.READ
                        ? "Chưa cập nhật được trạng thái đã đọc. Vui lòng thử lại."
                        : "Không tải được thông báo. Vui lòng kiểm tra kết nối và thử lại.");
            }
        });
    }

    private void invalidRequest(Callback<?> callback) { callback.onError(400, "Thông tin yêu cầu không hợp lệ."); }
    private void invalidResponse(Callback<?> callback) { callback.onError(0, "Dữ liệu thông báo trả về không hợp lệ. Vui lòng thử lại."); }
    private String errorMessage(int status) {
        switch (status) {
            case 400: return "Thông tin yêu cầu không hợp lệ.";
            case 401: return "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.";
            case 403: return "Bạn không có quyền xem hoặc cập nhật thông báo này.";
            case 404: return "Thông báo không tồn tại hoặc bạn không còn quyền truy cập.";
            default: return "Không xử lý được thông báo (HTTP " + status + "). Vui lòng thử lại.";
        }
    }

    public void cancelListRequests() { listVersion.incrementAndGet(); cancel(Scope.LIST); }
    public void cancelDetailRequests() { detailVersion.incrementAndGet(); cancel(Scope.DETAIL); }
    private void cancel(Scope scope) {
        requests.forEach((call, type) -> { if (type == scope) { call.cancel(); requests.remove(call); } });
    }
    public void cancelPendingRequests() {
        allVersion.incrementAndGet();
        for (Call<?> call : requests.keySet()) call.cancel();
        requests.clear();
    }
}
