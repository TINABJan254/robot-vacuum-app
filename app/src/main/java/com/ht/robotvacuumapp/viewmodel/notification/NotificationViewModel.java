package com.ht.robotvacuumapp.viewmodel.notification;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.ht.robotvacuumapp.api.dto.AlertPageResponse;
import com.ht.robotvacuumapp.api.dto.AlertResponse;
import com.ht.robotvacuumapp.model.repository.AlertRepository;
import com.ht.robotvacuumapp.model.repository.AuthRepository;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Supplier;

public class NotificationViewModel extends ViewModel {
    private final AlertRepository repository;
    private final AuthRepository auth;
    private final Supplier<String> now;
    private final MutableLiveData<NotificationUiState> state = new MutableLiveData<>(new NotificationUiState.Builder().build());
    private boolean active, cleared;
    private int nextPage;
    private long listOperation, detailOperation, readOperation, revision;
    private final Map<Long, Long> reading = new HashMap<>();
    private final Map<Long, Change> changes = new HashMap<>();

    private static final class Change {
        final long revision;
        final AlertResponse alert;
        Change(long revision, AlertResponse alert) { this.revision = revision; this.alert = alert; }
    }

    public NotificationViewModel(AlertRepository repository, AuthRepository auth) { this(repository, auth, NotificationViewModel::utcNow); }
    public NotificationViewModel(AlertRepository repository, AuthRepository auth, Supplier<String> now) {
        this.repository = repository;
        this.auth = auth;
        this.now = now;
        refresh();
    }
    private static String utcNow() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return format.format(new Date());
    }
    public LiveData<NotificationUiState> getUiState() { return state; }
    public void setActive(boolean active) { this.active = active; }
    private boolean usable() { return !cleared && !state.getValue().requiresLogin; }

    public void refresh() {
        if (!usable() || state.getValue().isLoading) return;
        repository.cancelListRequests();
        loadPage(0);
    }
    public void loadNextPage() {
        NotificationUiState s = state.getValue();
        if (!usable() || !s.loaded || !s.hasMore || s.isLoading || s.isLoadingMore || s.listError != null) return;
        loadPage(nextPage);
    }
    private void loadPage(int page) {
        long current = ++listOperation;
        long startedAt = revision;
        NotificationUiState.Builder b = state.getValue().toBuilder();
        b.isLoading = page == 0;
        b.isLoadingMore = page != 0;
        b.listError = null;
        b.loadMoreError = null;
        state.setValue(b.build());
        repository.getAlerts(page, new AlertRepository.Callback<AlertPageResponse>() {
            @Override public void onSuccess(AlertPageResponse response) {
                if (!usable() || current != listOperation) return;
                NotificationUiState.Builder next = state.getValue().toBuilder();
                Map<Long, AlertResponse> merged = new LinkedHashMap<>();
                if (page != 0) for (AlertResponse alert : next.alerts) merged.put(alert.getId(), alert);
                for (AlertResponse alert : response.getContent()) merged.put(alert.getId(), reconcile(alert, startedAt));
                next.alerts = new ArrayList<>(merged.values());
                next.isLoading = next.isLoadingMore = false;
                next.loaded = true;
                next.hasMore = !response.isLast();
                nextPage = page + 1;
                state.setValue(next.build());
            }
            @Override public void onError(int status, String message) {
                if (!usable() || current != listOperation) return;
                if (status == 401) { expireSession(message); return; }
                NotificationUiState.Builder next = state.getValue().toBuilder();
                next.isLoading = next.isLoadingMore = false;
                if (page == 0) next.listError = message;
                else next.loadMoreError = message;
                state.setValue(next.build());
            }
        });
    }

    public void openAlert(long id) {
        if (!usable() || id <= 0 || Objects.equals(state.getValue().selectedId, id)) return;
        AlertResponse item = null;
        for (AlertResponse alert : state.getValue().alerts) if (alert.getId() == id) { item = alert; break; }
        if (item == null) return;
        NotificationUiState.Builder b = state.getValue().toBuilder();
        b.selectedId = id;
        b.selectedTitle = item.getTitle();
        b.detail = null;
        b.markError = null;
        state.setValue(b.build());
        loadDetail(id);
    }

    public void retrySelected() {
        NotificationUiState s = state.getValue();
        if (!usable() || s.selectedId == null || s.detailLoading || reading.containsKey(s.selectedId)) return;
        loadDetail(s.selectedId); // Reconcile with backend before retrying a PATCH whose outcome may be unknown.
    }

    private void loadDetail(long id) {
        repository.cancelDetailRequests();
        long current = ++detailOperation;
        long startedAt = revision;
        NotificationUiState.Builder b = state.getValue().toBuilder();
        b.detailToken = current;
        b.detailLoading = true;
        b.detailError = b.markError = null;
        b.isMarkingRead = reading.containsKey(id);
        b.readAttempted = b.isMarkingRead;
        state.setValue(b.build());
        repository.getAlert(id, new AlertRepository.Callback<AlertResponse>() {
            @Override public void onSuccess(AlertResponse result) {
                if (!currentDetail(id, current)) return;
                AlertResponse alert = reconcile(result, startedAt);
                recordChange(alert);
                NotificationUiState.Builder next = state.getValue().toBuilder();
                next.alerts = replace(next.alerts, alert);
                next.detail = alert;
                next.detailLoading = false;
                next.isMarkingRead = reading.containsKey(id);
                next.readAttempted = next.isMarkingRead;
                state.setValue(next.build());
            }
            @Override public void onError(int status, String message) {
                if (!currentDetail(id, current)) return;
                if (status == 401) { expireSession(message); return; }
                NotificationUiState.Builder next = state.getValue().toBuilder();
                next.detailLoading = false;
                next.detailError = message;
                state.setValue(next.build());
            }
        });
    }

    private boolean currentDetail(long id, long token) {
        return usable() && detailOperation == token && Objects.equals(state.getValue().selectedId, id);
    }

    /** The Activity reports this only after rendering the selected message in a visible dialog. */
    public void onDetailShown(long id, long token) {
        NotificationUiState s = state.getValue();
        if (!active || !currentDetail(id, token) || s.detailLoading || s.detailError != null || s.detail == null
                || !s.detail.isUnread() || s.readAttempted || reading.containsKey(id)) return;
        long current = ++readOperation;
        reading.put(id, current);
        NotificationUiState.Builder b = s.toBuilder();
        b.readAttempted = true;
        b.isMarkingRead = true;
        b.markError = null;
        state.setValue(b.build());
        repository.markRead(id, s.detail.getRobotId(), now.get(), new AlertRepository.Callback<AlertResponse>() {
            private boolean accepted() { return usable() && Objects.equals(reading.get(id), current); }
            @Override public void onSuccess(AlertResponse alert) {
                if (!accepted()) return;
                reading.remove(id);
                recordChange(alert);
                NotificationUiState.Builder next = state.getValue().toBuilder();
                next.alerts = replace(next.alerts, alert);
                if (Objects.equals(next.selectedId, id)) {
                    next.detail = alert;
                    next.isMarkingRead = false;
                    next.readAttempted = true;
                    next.markError = next.detailError = null;
                }
                state.setValue(next.build());
            }
            @Override public void onError(int status, String message) {
                if (!accepted()) return;
                reading.remove(id);
                if (status == 401) { expireSession(message); return; }
                NotificationUiState.Builder next = state.getValue().toBuilder();
                if (Objects.equals(next.selectedId, id)) {
                    next.isMarkingRead = false;
                    next.readAttempted = true;
                    next.markError = message;
                }
                state.setValue(next.build());
            }
        });
    }

    public void closeDetail() {
        if (!usable()) return;
        ++detailOperation;
        repository.cancelDetailRequests();
        NotificationUiState.Builder b = state.getValue().toBuilder();
        b.selectedId = null;
        b.selectedTitle = null;
        b.detail = null;
        b.detailLoading = b.isMarkingRead = b.readAttempted = false;
        b.detailError = b.markError = null;
        state.setValue(b.build());
    }

    private AlertResponse reconcile(AlertResponse alert, long startedAt) {
        Change change = changes.get(alert.getId());
        return change != null && change.revision > startedAt ? change.alert : alert;
    }
    private void recordChange(AlertResponse alert) { changes.put(alert.getId(), new Change(++revision, alert)); }
    private List<AlertResponse> replace(List<AlertResponse> source, AlertResponse alert) {
        List<AlertResponse> result = new ArrayList<>(source.size());
        for (AlertResponse item : source) result.add(item.getId() == alert.getId() ? alert : item);
        return result;
    }
    private void expireSession(String message) {
        ++listOperation;
        ++detailOperation;
        reading.clear();
        changes.clear();
        repository.cancelPendingRequests();
        auth.cancelPendingRequests();
        auth.logout();
        NotificationUiState.Builder b = new NotificationUiState.Builder();
        b.requiresLogin = true;
        b.sessionMessage = message;
        state.setValue(b.build());
    }
    @Override protected void onCleared() {
        cleared = true;
        active = false;
        ++listOperation;
        ++detailOperation;
        reading.clear();
        changes.clear();
        repository.cancelPendingRequests();
        auth.cancelPendingRequests();
    }
}
