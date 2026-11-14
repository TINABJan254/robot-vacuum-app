package com.ht.robotvacuumapp.model.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ht.robotvacuumapp.api.ApiConfig;
import com.ht.robotvacuumapp.api.RetrofitClient;
import com.ht.robotvacuumapp.api.dto.RobotTelemetryResponse;
import com.ht.robotvacuumapp.model.entity.RobotTelemetry;
import com.ht.robotvacuumapp.model.local.TokenManager;
import java.util.concurrent.TimeUnit;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * Telemetry over {@code GET /api/v1/robots/{id}/telemetry/ws} (WebSocket upgrade, Bearer token).
 * The backend sends {@code {"type":"telemetry","robotId":..,"data":{..}|null}} on connect, on every new
 * reading and when the reading turns stale. Reconnects automatically with exponential backoff.
 */
public class WebSocketTelemetryStream implements TelemetryStream {

    private static final long PING_INTERVAL_SECONDS = 15;
    private static final long MIN_BACKOFF_MS = 1_000;
    private static final long MAX_BACKOFF_MS = 30_000;
    private static final int CLOSE_NORMAL = 1000;
    /** Backend: access token expired -> reconnect with the stored token (handshake decides). */
    private static final int CLOSE_TOKEN_EXPIRED = 4401;
    /** Backend: robot ownership changed -> reconnect so the handshake re-checks access. */
    private static final int CLOSE_ACCESS_CHANGED = 4403;

    private final Context context;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final Gson gson = new Gson();
    private final Runnable reconnect = this::connect;

    // Main-thread state.
    private OkHttpClient client;
    private WebSocket socket;
    private Listener listener;
    private long robotId;
    private int connection;
    private long backoffMs = MIN_BACKOFF_MS;

    public WebSocketTelemetryStream(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public void start(long robotId, Listener listener) {
        stop();
        this.robotId = robotId;
        this.listener = listener;
        backoffMs = MIN_BACKOFF_MS;
        connect();
    }

    @Override
    public void stop() {
        connection++;
        main.removeCallbacks(reconnect);
        listener = null;
        closeSocket();
    }

    private void connect() {
        if (listener == null) return;
        closeSocket();
        String authorization = TokenManager.getInstance(context).getAuthHeader();
        if (authorization == null) {
            unauthorized();
            return;
        }
        HttpUrl base = HttpUrl.parse(ApiConfig.getBaseUrl(context));
        if (base == null) {
            reject(0, "Địa chỉ máy chủ không hợp lệ.");
            return;
        }
        // OkHttp upgrades http(s):// URLs to ws(s):// itself.
        HttpUrl url = base.newBuilder()
                .addPathSegments("api/v1/robots/" + robotId + "/telemetry/ws")
                .build();
        Request request = new Request.Builder().url(url).header("Authorization", authorization).build();
        final int id = ++connection;
        final long expectedRobotId = robotId;
        socket = client().newWebSocket(request, new WebSocketListener() {
            @Override public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
                Parsed parsed = parse(text, expectedRobotId);
                if (parsed != null) post(id, () -> deliver(parsed.snapshot));
            }

            @Override public void onClosing(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                webSocket.close(CLOSE_NORMAL, null);
            }

            @Override public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                post(id, () -> closed(code));
            }

            @Override public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable error,
                                            @Nullable Response response) {
                int status = response == null ? 0 : response.code();
                if (response != null) response.close();
                post(id, () -> failed(status));
            }
        });
    }

    private void deliver(RobotTelemetry snapshot) {
        backoffMs = MIN_BACKOFF_MS;
        listener.onTelemetry(snapshot);
    }

    private void closed(int code) {
        socket = null;
        if (code == CLOSE_TOKEN_EXPIRED || code == CLOSE_ACCESS_CHANGED) {
            // The next handshake answers with 401/403/404 if access is really gone.
            main.post(reconnect);
        } else {
            connectionLost();
        }
    }

    private void failed(int httpStatus) {
        socket = null;
        if (httpStatus == 401) {
            unauthorized();
        } else if (httpStatus == 403) {
            reject(403, "Bạn không có quyền xem dữ liệu robot này.");
        } else if (httpStatus == 404) {
            reject(404, "Robot không tồn tại hoặc bạn không còn quyền truy cập.");
        } else if (httpStatus == 400) {
            reject(400, "ID robot không hợp lệ.");
        } else {
            connectionLost();
        }
    }

    private void connectionLost() {
        listener.onConnectionLost("Mất kết nối thời gian thực. Đang kết nối lại…");
        main.removeCallbacks(reconnect);
        main.postDelayed(reconnect, backoffMs);
        backoffMs = Math.min(backoffMs * 2, MAX_BACKOFF_MS);
    }

    private void unauthorized() {
        Listener current = listener;
        stop();
        if (current != null) current.onUnauthorized();
    }

    private void reject(int status, String message) {
        Listener current = listener;
        stop();
        if (current != null) current.onRejected(status, message);
    }

    /** Runs on the main thread only if this connection is still the current one. */
    private void post(int id, Runnable action) {
        main.post(() -> {
            if (id == connection && listener != null) action.run();
        });
    }

    private void closeSocket() {
        if (socket != null) {
            socket.close(CLOSE_NORMAL, null);
            socket = null;
        }
    }

    private OkHttpClient client() {
        if (client == null) {
            // Shares the REST connection pool; no read timeout (the server may be quiet while the robot
            // is offline) - dead connections are detected by WebSocket pings instead.
            client = RetrofitClient.getHttpClient().newBuilder()
                    .readTimeout(0, TimeUnit.MILLISECONDS)
                    .pingInterval(PING_INTERVAL_SECONDS, TimeUnit.SECONDS)
                    .build();
        }
        return client;
    }

    private static final class Parsed {
        final RobotTelemetry snapshot;
        Parsed(RobotTelemetry snapshot) { this.snapshot = snapshot; }
    }

    /** Returns null for unknown message types or invalid payloads (ignored). */
    private Parsed parse(String text, long expectedRobotId) {
        try {
            JsonObject message = JsonParser.parseString(text).getAsJsonObject();
            JsonElement type = message.get("type");
            if (type == null || !type.isJsonPrimitive() || !"telemetry".equals(type.getAsString())) return null;
            JsonElement data = message.get("data");
            if (data == null || data.isJsonNull()) return new Parsed(null);
            RobotTelemetryResponse response = gson.fromJson(data, RobotTelemetryResponse.class);
            RobotTelemetry snapshot = response == null ? null : response.toSnapshot(expectedRobotId);
            return snapshot == null ? null : new Parsed(snapshot);
        } catch (RuntimeException invalid) {
            return null;
        }
    }
}
