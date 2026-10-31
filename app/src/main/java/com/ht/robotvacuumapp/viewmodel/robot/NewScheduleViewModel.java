package com.ht.robotvacuumapp.viewmodel.robot;

import androidx.lifecycle.*;
import com.ht.robotvacuumapp.api.dto.*;
import com.ht.robotvacuumapp.model.repository.AuthRepository;
import com.ht.robotvacuumapp.model.repository.ScheduleRepository;
import java.util.*;

public class NewScheduleViewModel extends ViewModel {
    private final ScheduleRepository repository;
    private final AuthRepository auth;
    private final MutableLiveData<NewScheduleUiState> state;
    private boolean cleared;
    private long operation;
    public NewScheduleViewModel(String robotId, ScheduleRepository repository, AuthRepository auth) {
        this.repository = repository; this.auth = auth;
        NewScheduleUiState.Builder b = new NewScheduleUiState.Builder();
        b.robotId = RobotScheduleViewModel.parseId(robotId);
        if (b.robotId == null) b.error = "Thiếu ID robot hợp lệ. Vui lòng chọn lại robot từ danh sách.";
        state = new MutableLiveData<>(b.build());
    }
    public LiveData<NewScheduleUiState> getUiState() { return state; }
    private boolean editable() {
        NewScheduleUiState s = state.getValue();
        return !cleared && s.robotId != null && !s.isSaving && !s.requiresLogin && s.createdScheduleId == null;
    }
    public void setTime(int hour, int minute) {
        if (!editable()) return;
        NewScheduleUiState.Builder b = state.getValue().toBuilder(); b.hour = hour; b.minute = minute; b.error = null;
        state.setValue(b.build());
    }
    public void setRepeat(boolean repeat) {
        if (!editable()) return;
        NewScheduleUiState.Builder b = state.getValue().toBuilder(); b.repeat = repeat; b.error = null;
        state.setValue(b.build());
    }
    public void toggleDay(int day) {
        if (!editable() || day < 1 || day > 7) return;
        NewScheduleUiState.Builder b = state.getValue().toBuilder();
        if (!b.days.remove(day)) b.days.add(day);
        b.error = null; state.setValue(b.build());
    }
    public void save() {
        if (!editable()) return;
        NewScheduleUiState s = state.getValue();
        NewScheduleUiState.Builder b = s.toBuilder();
        b.error = s.hour < 0 || s.hour > 23 || s.minute < 0 || s.minute > 59 ? "Giờ hoặc phút không hợp lệ."
                : s.repeat && s.days.isEmpty() ? "Vui lòng chọn ít nhất một ngày lặp." : null;
        if (b.error != null) { state.setValue(b.build()); return; }
        CreateScheduleRequest request = new CreateScheduleRequest(s.robotId,
                String.format(Locale.US, "%02d:%02d:00", s.hour, s.minute), true,
                s.repeat ? new ArrayList<>(s.days) : Collections.emptyList());
        long current = ++operation;
        b.isSaving = true; state.setValue(b.build());
        repository.createSchedule(request, new ScheduleRepository.Callback<ScheduleResponse>() {
            private boolean accepted() { return !cleared && operation == current && state.getValue().isSaving && !state.getValue().requiresLogin; }
            @Override public void onSuccess(ScheduleResponse result) {
                if (!accepted()) return;
                NewScheduleUiState.Builder next = state.getValue().toBuilder();
                next.isSaving = false; next.createdScheduleId = result.getId(); next.error = null;
                state.setValue(next.build());
            }
            @Override public void onError(int status, String message) {
                if (!accepted()) return;
                NewScheduleUiState.Builder next = state.getValue().toBuilder();
                next.isSaving = false; next.error = message;
                if (status == 401) {
                    ++operation; repository.cancelPendingRequests(); auth.cancelPendingRequests(); auth.logout();
                    next.requiresLogin = true;
                }
                state.setValue(next.build());
            }
        });
    }
    @Override protected void onCleared() { cleared = true; ++operation; repository.cancelPendingRequests(); auth.cancelPendingRequests(); }
}
