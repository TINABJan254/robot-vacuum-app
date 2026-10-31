package com.ht.robotvacuumapp.viewmodel.robot;

import androidx.lifecycle.*;
import com.ht.robotvacuumapp.api.dto.*;
import com.ht.robotvacuumapp.model.repository.AuthRepository;
import com.ht.robotvacuumapp.model.repository.ScheduleRepository;
import java.util.*;

public class RobotScheduleViewModel extends ViewModel {
    private final ScheduleRepository repository;
    private final AuthRepository auth;
    private final Long robotId;
    private final MutableLiveData<RobotScheduleUiState> state;
    private boolean cleared;
    private int nextPage;
    private long listOperation, writeOperation, revision;
    private final Map<Long, Long> writes = new HashMap<>();
    private final Map<Long, Change> changes = new HashMap<>();
    private final Set<Long> handledCreates = new HashSet<>();
    private static final class Change {
        final long revision;
        final ScheduleResponse schedule;
        Change(long revision, ScheduleResponse schedule) { this.revision = revision; this.schedule = schedule; }
    }
    public RobotScheduleViewModel(String id, ScheduleRepository repository, AuthRepository auth) {
        this.repository = repository; this.auth = auth;
        robotId = parseId(id);
        RobotScheduleUiState.Builder initial = new RobotScheduleUiState.Builder();
        initial.robotId = robotId;
        if (robotId == null) initial.listError = "Thiếu ID robot hợp lệ. Vui lòng chọn lại robot từ danh sách.";
        state = new MutableLiveData<>(initial.build());
        if (robotId != null) refresh();
    }
    static Long parseId(String value) {
        try {
            if (value == null || !value.matches("[0-9]+")) return null;
            long id = Long.parseLong(value);
            return id > 0 ? id : null;
        } catch (NumberFormatException error) { return null; }
    }
    public LiveData<RobotScheduleUiState> getUiState() { return state; }
    private boolean usable() { return !cleared && robotId != null && !state.getValue().requiresLogin; }
    public void refresh() { if (usable() && !state.getValue().isLoading) reload(); }
    private void reload() { repository.cancelListRequests(); loadPage(0); }
    public void loadNextPage() {
        RobotScheduleUiState s = state.getValue();
        if (usable() && s.loaded && s.hasMore && !s.isLoading && !s.isLoadingMore && s.listError == null) loadPage(nextPage);
    }
    private void loadPage(int page) {
        long current = ++listOperation, startedAt = revision;
        RobotScheduleUiState.Builder b = state.getValue().toBuilder();
        b.isLoading = page == 0; b.isLoadingMore = page != 0;
        b.listError = b.moreError = null;
        state.setValue(b.build());
        repository.getSchedules(robotId, page, new ScheduleRepository.Callback<SchedulePageResponse>() {
            @Override public void onSuccess(SchedulePageResponse result) {
                if (!usable() || current != listOperation) return;
                RobotScheduleUiState.Builder next = state.getValue().toBuilder();
                Map<Long, ScheduleResponse> merged = new LinkedHashMap<>();
                if (page != 0) for (ScheduleResponse s : next.schedules) merged.put(s.getId(), s);
                for (ScheduleResponse s : result.getContent()) {
                    Change change = changes.get(s.getId());
                    ScheduleResponse value = change != null && change.revision > startedAt ? change.schedule : s;
                    if (value != null) merged.put(value.getId(), value);
                }
                next.schedules = new ArrayList<>(merged.values());
                next.isLoading = next.isLoadingMore = false;
                next.loaded = true; next.hasMore = !result.isLast(); nextPage = page + 1;
                state.setValue(next.build());
            }
            @Override public void onError(int status, String message) {
                if (!usable() || current != listOperation) return;
                if (status == 401) { expire(message); return; }
                RobotScheduleUiState.Builder next = state.getValue().toBuilder();
                next.isLoading = next.isLoadingMore = false;
                if (page == 0) next.listError = message; else next.moreError = message;
                state.setValue(next.build());
            }
        });
    }
    private ScheduleResponse find(long id) {
        for (ScheduleResponse s : state.getValue().schedules) if (s.getId() == id) return s;
        return null;
    }
    public void setEnabled(long id, boolean enabled) {
        if (!usable() || writes.containsKey(id)) return;
        ScheduleResponse existing = find(id);
        if (existing == null || existing.isEnabled() == enabled) return;
        long current = ++writeOperation;
        writes.put(id, current);
        RobotScheduleUiState.Builder b = state.getValue().toBuilder();
        b.pendingEnabled.put(id, enabled); b.message = null;
        state.setValue(b.build());
        repository.setEnabled(robotId, id, enabled, new ScheduleRepository.Callback<ScheduleResponse>() {
            @Override public void onSuccess(ScheduleResponse result) {
                if (!currentWrite(id, current)) return;
                writes.remove(id);
                changes.put(id, new Change(++revision, result));
                RobotScheduleUiState.Builder next = state.getValue().toBuilder();
                next.pendingEnabled.remove(id);
                List<ScheduleResponse> items = new ArrayList<>();
                for (ScheduleResponse s : next.schedules) items.add(s.getId() == id ? result : s);
                next.schedules = items;
                next.message = enabled ? "Đã bật lịch." : "Đã tắt lịch.";
                state.setValue(next.build());
            }
            @Override public void onError(int status, String message) { writeFailed(id, current, status, message); }
        });
    }
    public void deleteSchedule(long id) {
        if (!usable() || writes.containsKey(id) || find(id) == null) return;
        long current = ++writeOperation;
        writes.put(id, current);
        RobotScheduleUiState.Builder b = state.getValue().toBuilder();
        b.deleting.add(id); b.message = null;
        state.setValue(b.build());
        repository.deleteSchedule(id, new ScheduleRepository.Callback<Void>() {
            @Override public void onSuccess(Void ignored) {
                if (!currentWrite(id, current)) return;
                writes.remove(id);
                changes.put(id, new Change(++revision, null));
                RobotScheduleUiState.Builder next = state.getValue().toBuilder();
                next.deleting.remove(id);
                List<ScheduleResponse> items = new ArrayList<>();
                for (ScheduleResponse s : next.schedules) if (s.getId() != id) items.add(s);
                next.schedules = items; next.message = "Đã xóa lịch.";
                state.setValue(next.build());
                reload(); // Offset pages shift after deletion; restart without canceling other writes.
            }
            @Override public void onError(int status, String message) { writeFailed(id, current, status, message); }
        });
    }
    private boolean currentWrite(long id, long current) { return usable() && Objects.equals(writes.get(id), current); }
    private void writeFailed(long id, long current, int status, String message) {
        if (!currentWrite(id, current)) return;
        writes.remove(id);
        if (status == 401) { expire(message); return; }
        RobotScheduleUiState.Builder b = state.getValue().toBuilder();
        b.pendingEnabled.remove(id); b.deleting.remove(id); b.message = message;
        state.setValue(b.build());
    }
    public void onCreated(long sourceRobotId, long scheduleId) {
        if (!usable() || sourceRobotId != robotId || scheduleId <= 0 || !handledCreates.add(scheduleId)) return;
        RobotScheduleUiState.Builder b = state.getValue().toBuilder();
        b.message = "Đã lưu lịch.";
        state.setValue(b.build());
        reload();
    }
    public void acknowledgeMessage() {
        RobotScheduleUiState.Builder b = state.getValue().toBuilder(); b.message = null; state.setValue(b.build());
    }
    private void expire(String message) {
        ++listOperation; writes.clear(); changes.clear();
        repository.cancelPendingRequests(); auth.cancelPendingRequests(); auth.logout();
        RobotScheduleUiState.Builder b = new RobotScheduleUiState.Builder();
        b.requiresLogin = true; b.message = message; state.setValue(b.build());
    }
    @Override protected void onCleared() {
        cleared = true; ++listOperation; writes.clear(); changes.clear();
        repository.cancelPendingRequests(); auth.cancelPendingRequests();
    }
}
