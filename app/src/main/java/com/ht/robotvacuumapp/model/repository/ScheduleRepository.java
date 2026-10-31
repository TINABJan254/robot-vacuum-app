package com.ht.robotvacuumapp.model.repository;

import android.content.Context;
import com.ht.robotvacuumapp.api.RetrofitClient;
import com.ht.robotvacuumapp.api.dto.*;
import com.ht.robotvacuumapp.api.service.ScheduleApiService;
import com.ht.robotvacuumapp.model.local.TokenManager;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.function.Supplier;
import retrofit2.Call;
import retrofit2.Response;

/** Requests owned by one schedule screen. Canceling list reads never cancels a write. */
public class ScheduleRepository {
    public static final int PAGE_SIZE = 20;
    private final Supplier<ScheduleApiService> services;
    private final Supplier<String> authorization;
    private final Map<Call<?>, Boolean> requests = new ConcurrentHashMap<>();
    private final AtomicLong generation = new AtomicLong(), listGeneration = new AtomicLong();

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(int status, String message);
    }
    public ScheduleRepository(Context context) {
        Context app = context.getApplicationContext();
        services = () -> RetrofitClient.getRetrofitInstance(app).create(ScheduleApiService.class);
        authorization = () -> TokenManager.getInstance(app).getAuthHeader();
    }
    public ScheduleRepository(Supplier<ScheduleApiService> services, Supplier<String> authorization) {
        this.services = services;
        this.authorization = authorization;
    }
    public void getSchedules(long robotId, int page, Callback<SchedulePageResponse> callback) {
        if (robotId <= 0 || page < 0) { invalidRequest(callback); return; }
        String header = header(callback);
        if (header != null) enqueue(services.get().getSchedules(header, robotId, page, PAGE_SIZE,
                Arrays.asList("time,asc", "id,asc")), 200, true, callback,
                response -> response.isValidFor(robotId, page, PAGE_SIZE));
    }
    public void createSchedule(CreateScheduleRequest body, Callback<ScheduleResponse> callback) {
        if (body == null) { invalidRequest(callback); return; }
        String header = header(callback);
        if (header != null) enqueue(services.get().createSchedule(header, body), 201, false, callback,
                response -> response.isValidFor(body.getRobotId()) && response.getTime().equals(body.getTime())
                        && response.isEnabled() == body.isEnabled() && response.getDaysOfWeek().equals(body.getDaysOfWeek()));
    }
    public void setEnabled(long robotId, long id, boolean enabled, Callback<ScheduleResponse> callback) {
        if (robotId <= 0 || id <= 0) { invalidRequest(callback); return; }
        String header = header(callback);
        if (header != null) enqueue(services.get().setEnabled(header, id, new UpdateScheduleEnabledRequest(enabled)),
                200, false, callback, response -> response.isValidFor(robotId) && response.getId() == id && response.isEnabled() == enabled);
    }
    public void deleteSchedule(long id, Callback<Void> callback) {
        if (id <= 0) { invalidRequest(callback); return; }
        String header = header(callback);
        if (header != null) enqueue(services.get().deleteSchedule(header, id), 204, false, callback, ignored -> true);
    }
    private String header(Callback<?> callback) {
        String header = authorization.get();
        if (header == null || header.trim().isEmpty()) {
            callback.onError(401, "Vui lòng đăng nhập lại để quản lý lịch.");
            return null;
        }
        return header;
    }
    private <T> void enqueue(Call<T> request, int expectedStatus, boolean list, Callback<T> callback, Predicate<T> valid) {
        long current = generation.get(), listCurrent = listGeneration.get();
        requests.put(request, list);
        request.enqueue(new retrofit2.Callback<T>() {
            private boolean canceled(Call<T> call) {
                return call.isCanceled() || current != generation.get() || (list && listCurrent != listGeneration.get());
            }
            @Override public void onResponse(Call<T> call, Response<T> response) {
                requests.remove(call);
                if (canceled(call)) return;
                if (response.code() == expectedStatus && (expectedStatus == 204 || response.body() != null)) {
                    if (valid.test(response.body())) callback.onSuccess(response.body());
                    else invalidResponse(callback);
                } else if (response.isSuccessful()) invalidResponse(callback);
                else {
                    if (response.errorBody() != null) response.errorBody().close();
                    callback.onError(response.code(), errorMessage(response.code()));
                }
            }
            @Override public void onFailure(Call<T> call, Throwable error) {
                requests.remove(call);
                if (!canceled(call)) callback.onError(0, expectedStatus == 201
                        ? "Chưa xác nhận được kết quả lưu lịch. Vui lòng tải lại danh sách trước khi tạo lại."
                        : "Không xử lý được lịch. Vui lòng kiểm tra kết nối và thử lại.");
            }
        });
    }
    private void invalidRequest(Callback<?> callback) { callback.onError(400, "Thông tin lịch không hợp lệ."); }
    private void invalidResponse(Callback<?> callback) { callback.onError(0, "Dữ liệu lịch trả về không hợp lệ. Vui lòng tải lại danh sách."); }
    private String errorMessage(int status) {
        switch (status) {
            case 400: return "Thông tin lịch không hợp lệ. Vui lòng kiểm tra lại.";
            case 401: return "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.";
            case 403: return "Bạn không có quyền thao tác với lịch này.";
            case 404: return "Robot hoặc lịch không tồn tại, hoặc bạn không còn quyền truy cập.";
            case 409: return "Không thể lưu lịch do xung đột dữ liệu. Vui lòng thử lại.";
            default: return "Máy chủ gặp lỗi (HTTP " + status + "). Vui lòng thử lại.";
        }
    }
    public void cancelListRequests() {
        listGeneration.incrementAndGet();
        requests.forEach((call, list) -> { if (list) { call.cancel(); requests.remove(call); } });
    }
    public void cancelPendingRequests() {
        generation.incrementAndGet();
        for (Call<?> call : requests.keySet()) call.cancel();
        requests.clear();
    }
}
