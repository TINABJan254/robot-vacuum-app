package com.ht.robotvacuumapp.model.repository;

import android.content.Context;
import com.google.gson.Gson;
import com.ht.robotvacuumapp.api.RetrofitClient;
import com.ht.robotvacuumapp.api.dto.ErrorResponse;
import com.ht.robotvacuumapp.api.dto.FieldError;
import com.ht.robotvacuumapp.api.dto.PairRobotRequest;
import com.ht.robotvacuumapp.api.dto.RobotPageResponse;
import com.ht.robotvacuumapp.api.dto.RobotResponse;
import com.ht.robotvacuumapp.api.dto.UpdateRobotNameRequest;
import com.ht.robotvacuumapp.api.service.RobotApiService;
import com.ht.robotvacuumapp.model.entity.RobotDevice;
import com.ht.robotvacuumapp.model.local.TokenManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Supplier;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

/** Backend robot data and requests owned by one screen's ViewModel. */
public class RobotRepository {
    private enum Operation { PAIR, READ, RENAME, UNPAIR }
    private final Supplier<RobotApiService> services;
    private final Supplier<String> authorization;
    private final Set<Call<?>> requests = ConcurrentHashMap.newKeySet();
    private final AtomicLong generation = new AtomicLong();
    private final Gson gson = new Gson();

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(int httpStatus, String message, Map<String, String> fieldErrors);
    }

    public RobotRepository(Context context) {
        Context application = context.getApplicationContext();
        services = () -> RetrofitClient.getRetrofitInstance(application).create(RobotApiService.class);
        authorization = () -> TokenManager.getInstance(application).getAuthHeader();
    }

    /** Allows HTTP service and session substitutes without an Android context. */
    public RobotRepository(Supplier<RobotApiService> services, Supplier<String> authorization) {
        this.services = services;
        this.authorization = authorization;
    }

    public void pairRobot(long id, String pairCode, Callback<RobotDevice> callback) {
        String header = requireAuthorization(callback);
        if (header == null) return;
        long current = generation.get();
        enqueue(services.get().pairRobot(header, new PairRobotRequest(id, pairCode)), current, Operation.PAIR, callback, response -> {
            if (!validRobot(response) || response.getId() != id
                    || response.getUserId() == null || response.getUserId() <= 0) {
                invalidResponse(callback);
                return;
            }
            callback.onSuccess(toDevice(response));
        });
    }

    public void getRobots(Callback<List<RobotDevice>> callback) {
        loadPage(0, generation.get(), new LinkedHashMap<>(), callback);
    }

    public void unpairRobot(long id, Callback<RobotDevice> callback) {
        String header = requireAuthorization(callback);
        if (header == null) return;
        enqueue(services.get().unpairRobot(header, id), generation.get(), Operation.UNPAIR, callback, response -> {
            if (!validRobot(response) || response.getId() != id || !response.isUnpaired()) {
                invalidResponse(callback);
                return;
            }
            callback.onSuccess(toDevice(response));
        });
    }

    public void renameRobot(long id, String name, Callback<RobotDevice> callback) {
        String header = requireAuthorization(callback);
        if (header == null) return;
        enqueue(services.get().renameRobot(header, id, new UpdateRobotNameRequest(name)), generation.get(),
                Operation.RENAME, callback, response -> {
                    if (!validRobot(response) || response.getId() != id || response.getName().length() > 255) {
                        invalidResponse(callback);
                        return;
                    }
                    callback.onSuccess(toDevice(response));
                });
    }

    private void loadPage(int page, long current, Map<Long, RobotDevice> collected,
                          Callback<List<RobotDevice>> callback) {
        if (current != generation.get()) return;
        String header = requireAuthorization(callback);
        if (header == null) return;
        enqueue(services.get().getRobots(header, page, 100, "id,asc"), current, Operation.READ, callback, response -> {
            if (!validPage(response, page)) {
                invalidResponse(callback);
                return;
            }
            for (RobotResponse robot : response.getContent()) {
                if (!validRobot(robot)) {
                    invalidResponse(callback);
                    return;
                }
                collected.put(robot.getId(), toDevice(robot));
            }
            if (response.getLast()) {
                callback.onSuccess(Collections.unmodifiableList(new ArrayList<>(collected.values())));
            } else {
                loadPage(page + 1, current, collected, callback);
            }
        });
    }

    private boolean validPage(RobotPageResponse response, int expectedPage) {
        return response.getContent() != null && response.getPage() != null && response.getPage() == expectedPage
                && response.getTotalPages() != null && response.getTotalPages() >= 0
                && expectedPage < Math.max(1, response.getTotalPages()) && response.getLast() != null
                && (response.getLast() ? expectedPage + 1 == Math.max(1, response.getTotalPages())
                : expectedPage + 1 < response.getTotalPages());
    }

    private boolean validRobot(RobotResponse robot) {
        return robot != null && robot.getId() != null && robot.getId() > 0
                && robot.getName() != null && !robot.getName().trim().isEmpty();
    }

    private RobotDevice toDevice(RobotResponse response) {
        return new RobotDevice(String.valueOf(response.getId()), response.getName());
    }

    private String requireAuthorization(Callback<?> callback) {
        String header = authorization.get();
        if (header == null || header.trim().isEmpty()) {
            callback.onError(401, "Vui lòng đăng nhập lại để tiếp tục.", Collections.emptyMap());
            return null;
        }
        return header;
    }

    private <T> void enqueue(Call<T> request, long current, Operation operation, Callback<?> callback, Consumer<T> success) {
        requests.add(request);
        request.enqueue(new retrofit2.Callback<T>() {
            @Override public void onResponse(Call<T> call, Response<T> response) {
                requests.remove(call);
                if (call.isCanceled() || current != generation.get()) return;
                if (response.code() == 200 && response.body() != null) {
                    success.accept(response.body());
                } else if (response.isSuccessful()) {
                    invalidResponse(callback);
                } else {
                    readError(response.code(), response.errorBody(), operation, callback);
                }
            }
            @Override public void onFailure(Call<T> call, Throwable error) {
                requests.remove(call);
                if (call.isCanceled() || current != generation.get()) return;
                callback.onError(0, "Không thể xác nhận kết quả từ máy chủ. Kiểm tra kết nối rồi tải lại danh sách hoặc thử lại.",
                        Collections.emptyMap());
            }
        });
    }

    private void invalidResponse(Callback<?> callback) {
        callback.onError(0, "Chưa xác nhận được kết quả từ máy chủ. Vui lòng tải lại danh sách hoặc thử lại.", Collections.emptyMap());
    }

    private void readError(int status, ResponseBody body, Operation operation, Callback<?> callback) {
        Map<String, String> fields = new LinkedHashMap<>();
        try (ResponseBody errorBody = body) {
            if (errorBody != null) {
                ErrorResponse response = gson.fromJson(errorBody.string(), ErrorResponse.class);
                if (response != null && response.getFieldErrors() != null) {
                    for (FieldError field : response.getFieldErrors()) {
                        if (field != null && field.getField() != null && field.getMessage() != null) {
                            fields.put(field.getField(), field.getMessage());
                        }
                    }
                }
            }
        } catch (Exception ignored) {
            // HTML/empty errors still keep their HTTP status for the ViewModel.
        }
        String message;
        switch (status) {
            case 400: message = "Thông tin gửi lên không hợp lệ. Vui lòng kiểm tra lại."; break;
            case 401: message = "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại."; break;
            case 403: message = "Bạn không có quyền thực hiện thao tác này."; break;
            case 404:
                message = operation == Operation.PAIR ? "ID robot hoặc mã ghép nối không chính xác."
                        : "Robot không tồn tại hoặc bạn không còn quyền truy cập.";
                break;
            case 409:
                message = operation == Operation.PAIR ? "Robot đã thuộc một tài khoản khác."
                        : operation == Operation.UNPAIR ? "Chưa thể gỡ robot khỏi tài khoản do xung đột dữ liệu. Vui lòng thử lại."
                        : "Không thể cập nhật robot do xung đột dữ liệu. Vui lòng thử lại.";
                break;
            default: message = "Máy chủ gặp lỗi (HTTP " + status + "). Vui lòng thử lại.";
        }
        callback.onError(status, message, Collections.unmodifiableMap(fields));
    }

    public void cancelPendingRequests() {
        generation.incrementAndGet();
        for (Call<?> request : requests) request.cancel();
        requests.clear();
    }
}
