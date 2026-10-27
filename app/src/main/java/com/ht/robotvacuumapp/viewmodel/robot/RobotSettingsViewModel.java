package com.ht.robotvacuumapp.viewmodel.robot;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.ht.robotvacuumapp.model.entity.RobotDevice;
import com.ht.robotvacuumapp.model.repository.AuthRepository;
import com.ht.robotvacuumapp.model.repository.RobotRepository;
import java.util.Map;

public class RobotSettingsViewModel extends ViewModel {
    private final RobotRepository robots;
    private final AuthRepository auth;
    private final Long id;
    private final MutableLiveData<RobotSettingsUiState> state;
    private boolean cleared;
    private int operation;

    public RobotSettingsViewModel(String robotId, String name, RobotRepository robots, AuthRepository auth) {
        this.robots = robots;
        this.auth = auth;
        id = parseId(robotId);
        RobotSettingsUiState.Builder initial = new RobotSettingsUiState.Builder();
        initial.robotId = id == null ? null : String.valueOf(id);
        initial.robotName = name == null ? "" : name;
        initial.draftName = initial.robotName;
        if (id == null) initial.message = "Thiếu ID robot hợp lệ. Vui lòng chọn lại robot từ danh sách.";
        state = new MutableLiveData<>(initial.build());
    }

    private Long parseId(String value) {
        try {
            if (value == null || !value.matches("[0-9]+")) return null;
            long parsed = Long.parseLong(value);
            return parsed > 0 ? parsed : null;
        } catch (NumberFormatException error) {
            return null;
        }
    }

    public LiveData<RobotSettingsUiState> getUiState() { return state; }
    public boolean hasValidRobotId() { return id != null; }

    public void openRenameDialog() {
        RobotSettingsUiState s = state.getValue();
        if (cleared || id == null || s.requiresLogin || s.isSaving || s.dialogOpen) return;
        RobotSettingsUiState.Builder b = s.toBuilder();
        b.dialogOpen = true;
        b.draftName = s.robotName;
        b.nameError = null;
        b.requestError = null;
        state.setValue(b.build());
    }

    public void updateDraft(String name) {
        RobotSettingsUiState s = state.getValue();
        if (!s.dialogOpen || s.isSaving || s.draftName.equals(name)) return;
        RobotSettingsUiState.Builder b = s.toBuilder();
        b.draftName = name;
        b.nameError = null;
        b.requestError = null;
        state.setValue(b.build());
    }

    public void closeRenameDialog() {
        RobotSettingsUiState s = state.getValue();
        if (s.isSaving) return;
        RobotSettingsUiState.Builder b = s.toBuilder();
        b.dialogOpen = false;
        b.draftName = s.robotName;
        b.nameError = null;
        b.requestError = null;
        state.setValue(b.build());
    }

    public void saveName() {
        RobotSettingsUiState s = state.getValue();
        if (cleared || id == null || s.requiresLogin || !s.dialogOpen || s.isSaving) return;
        String name = s.draftName.trim();
        RobotSettingsUiState.Builder b = s.toBuilder();
        b.nameError = name.isEmpty() ? "Vui lòng nhập tên robot."
                : name.length() > 255 ? "Tên robot không được quá 255 ký tự." : null;
        b.requestError = null;
        if (b.nameError != null) {
            state.setValue(b.build());
            return;
        }
        if (name.equals(s.robotName)) {
            closeRenameDialog();
            return;
        }
        int current = ++operation;
        b.isSaving = true;
        state.setValue(b.build());
        robots.renameRobot(id, name, new RobotRepository.Callback<RobotDevice>() {
            @Override public void onSuccess(RobotDevice result) {
                if (!isCurrent(current)) return;
                RobotSettingsUiState.Builder next = state.getValue().toBuilder();
                next.robotName = result.getName();
                next.draftName = result.getName();
                next.updatedRobot = result;
                next.dialogOpen = false;
                next.isSaving = false;
                next.nameError = null;
                next.requestError = null;
                next.message = "Đã đổi tên thành: " + result.getName();
                state.setValue(next.build());
            }
            @Override public void onError(int status, String message, Map<String, String> fields) {
                if (!isCurrent(current)) return;
                if (status == 401) {
                    ++operation;
                    robots.cancelPendingRequests();
                    auth.cancelPendingRequests();
                    auth.logout();
                    RobotSettingsUiState.Builder expired = new RobotSettingsUiState.Builder();
                    expired.requiresLogin = true;
                    expired.message = message;
                    state.setValue(expired.build());
                    return;
                }
                RobotSettingsUiState.Builder next = state.getValue().toBuilder();
                next.isSaving = false;
                next.nameError = fields.get("name");
                next.requestError = message;
                state.setValue(next.build());
            }
        });
    }

    public void acknowledgeFeedback() {
        RobotSettingsUiState.Builder b = state.getValue().toBuilder();
        b.message = null;
        state.setValue(b.build());
    }

    private boolean isCurrent(int current) {
        return !cleared && current == operation && !state.getValue().requiresLogin;
    }

    @Override protected void onCleared() {
        cleared = true;
        ++operation;
        robots.cancelPendingRequests();
        auth.cancelPendingRequests();
    }
}
