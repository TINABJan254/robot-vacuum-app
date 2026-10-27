package com.ht.robotvacuumapp.viewmodel.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.ht.robotvacuumapp.model.entity.RobotDevice;
import com.ht.robotvacuumapp.model.repository.AuthRepository;
import com.ht.robotvacuumapp.model.repository.RobotRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainViewModel extends ViewModel {
    private final AuthRepository auth;
    private final RobotRepository robots;
    private final MutableLiveData<MainUiState> state;
    private int operation;
    private boolean cleared;
    private final Set<String> handledUnpairs = new HashSet<>();

    public MainViewModel(AuthRepository auth, RobotRepository robots) {
        this.auth = auth;
        this.robots = robots;
        MainUiState.Builder initial = new MainUiState.Builder();
        initial.requiresLogin = !auth.isLoggedIn();
        state = new MutableLiveData<>(initial.build());
        if (!initial.requiresLogin) loadRobots();
    }

    public LiveData<MainUiState> getUiState() { return state; }
    public String getUserName() { return auth.getUserName(); }
    public String getUsername() { return auth.getUsername(); }
    public String getUserRole() { return auth.getUserRole(); }

    public void loadRobots() {
        MainUiState s = state.getValue();
        if (cleared || s.requiresLogin || s.isLoadingRobots || s.isPairing) return;
        int current = ++operation;
        MainUiState.Builder next = s.toBuilder();
        next.isLoadingRobots = true;
        next.listError = null;
        state.setValue(next.build());
        robots.getRobots(new RobotRepository.Callback<List<RobotDevice>>() {
            @Override public void onSuccess(List<RobotDevice> result) {
                if (!isCurrent(current)) return;
                // Trust a fresh backend snapshot (including all robots visible to ADMIN).
                for (RobotDevice robot : result) handledUnpairs.remove(robot.getId());
                MainUiState.Builder b = state.getValue().toBuilder();
                b.robots = result;
                b.robotsLoaded = true;
                b.isLoadingRobots = false;
                b.listError = null;
                if (b.selectedRobotId != null && result.stream().noneMatch(r -> r.getId().equals(b.selectedRobotId))) {
                    b.selectedRobotId = null;
                }
                state.setValue(b.build());
            }
            @Override public void onError(int status, String message, Map<String, String> fields) {
                if (!isCurrent(current)) return;
                if (status == 401) {
                    expireSession(message);
                    return;
                }
                MainUiState.Builder b = state.getValue().toBuilder();
                b.isLoadingRobots = false;
                b.listError = message;
                state.setValue(b.build());
            }
        });
    }

    public void selectTab(int tab) {
        MainUiState.Builder b = state.getValue().toBuilder();
        b.selectedTab = tab;
        state.setValue(b.build());
    }

    public void openPairDialog() {
        if (state.getValue().requiresLogin || state.getValue().pairDialogOpen) return;
        MainUiState.Builder b = state.getValue().toBuilder();
        b.pairDialogOpen = true;
        state.setValue(b.build());
    }

    public void updatePairDraft(String id, String code) {
        MainUiState s = state.getValue();
        if (s.isPairing || !s.pairDialogOpen || (s.draftRobotId.equals(id) && s.draftPairCode.equals(code))) return;
        MainUiState.Builder b = s.toBuilder();
        b.draftRobotId = id;
        b.draftPairCode = code;
        b.robotIdError = null;
        b.robotCodeError = null;
        b.pairError = null;
        state.setValue(b.build());
    }

    public void closePairDialog() {
        MainUiState s = state.getValue();
        if (s.isPairing) return;
        MainUiState.Builder b = s.toBuilder();
        b.pairDialogOpen = false;
        b.draftRobotId = "";
        b.draftPairCode = "";
        b.robotIdError = null;
        b.robotCodeError = null;
        b.pairError = null;
        state.setValue(b.build());
    }

    public void pairRobot() {
        MainUiState s = state.getValue();
        if (cleared || s.requiresLogin || !s.pairDialogOpen || s.isPairing || s.isLoadingRobots) return;
        MainUiState.Builder b = s.toBuilder();
        String idText = s.draftRobotId.trim();
        b.robotIdError = null;
        b.robotCodeError = null;
        Long id = null;
        try {
            if (!idText.matches("[0-9]+")) throw new NumberFormatException();
            id = Long.parseLong(idText);
            if (id <= 0) throw new NumberFormatException();
        } catch (NumberFormatException error) {
            b.robotIdError = "ID robot phải là số nguyên dương hợp lệ.";
        }
        if (s.draftPairCode.trim().isEmpty()) b.robotCodeError = "Vui lòng nhập mã ghép nối.";
        else if (s.draftPairCode.length() > 255) b.robotCodeError = "Mã ghép nối không được quá 255 ký tự.";
        if (b.robotIdError != null || b.robotCodeError != null) {
            state.setValue(b.build());
            return;
        }
        b.isPairing = true;
        b.pairError = null;
        b.message = null;
        state.setValue(b.build());
        int current = ++operation;
        robots.pairRobot(id, s.draftPairCode, new RobotRepository.Callback<RobotDevice>() {
            @Override public void onSuccess(RobotDevice result) {
                if (!isCurrent(current)) return;
                handledUnpairs.remove(result.getId());
                MainUiState.Builder next = state.getValue().toBuilder();
                Map<String, RobotDevice> unique = new LinkedHashMap<>();
                for (RobotDevice device : next.robots) unique.put(device.getId(), device);
                unique.put(result.getId(), result);
                next.robots = new ArrayList<>(unique.values());
                next.robotsLoaded = true;
                next.selectedRobotId = result.getId();
                next.isPairing = false;
                next.pairDialogOpen = false;
                next.draftRobotId = "";
                next.draftPairCode = "";
                next.robotIdError = null;
                next.robotCodeError = null;
                next.pairError = null;
                next.message = "Đã thêm robot: " + result.getName();
                state.setValue(next.build());
            }
            @Override public void onError(int status, String message, Map<String, String> fields) {
                if (!isCurrent(current)) return;
                if (status == 401) {
                    expireSession(message);
                    return;
                }
                MainUiState.Builder next = state.getValue().toBuilder();
                next.isPairing = false;
                next.pairError = message;
                next.robotIdError = fields.get("robotId");
                next.robotCodeError = fields.get("pairCode");
                state.setValue(next.build());
            }
        });
    }

    public void selectRobot(RobotDevice robot) {
        if (state.getValue().requiresLogin || handledUnpairs.contains(robot.getId())) return;
        MainUiState.Builder b = state.getValue().toBuilder();
        b.selectedRobotId = robot.getId();
        b.robotToOpen = robot;
        state.setValue(b.build());
    }

    public void logout() { expireSession(null); }

    public void onRobotRenamed(String id, String name) {
        MainUiState s = state.getValue();
        if (cleared || s.requiresLogin || s.isPairing || id == null || handledUnpairs.contains(id) || name == null
                || name.trim().isEmpty() || name.length() > 255) return;
        // A GET started before opening Settings must not overwrite the confirmed name.
        ++operation;
        if (s.isLoadingRobots) robots.cancelPendingRequests();
        MainUiState.Builder b = s.toBuilder();
        b.isLoadingRobots = false;
        List<RobotDevice> updated = new ArrayList<>();
        boolean found = false;
        for (RobotDevice robot : s.robots) {
            if (robot.getId().equals(id)) {
                updated.add(robot.withName(name));
                found = true;
            } else {
                updated.add(robot);
            }
        }
        b.robots = updated;
        state.setValue(b.build());
        if (!found) loadRobots();
    }

    public void onRobotUnpaired(String id) {
        MainUiState s = state.getValue();
        if (cleared || s.requiresLogin || s.isPairing || id == null || !id.matches("[1-9][0-9]*")
                || handledUnpairs.contains(id)) return;
        try {
            Long.parseLong(id);
        } catch (NumberFormatException error) {
            return;
        }
        handledUnpairs.add(id);
        ++operation;
        if (s.isLoadingRobots) robots.cancelPendingRequests();
        MainUiState.Builder b = s.toBuilder();
        b.isLoadingRobots = false;
        List<RobotDevice> remaining = new ArrayList<>();
        for (RobotDevice robot : s.robots) {
            if (!robot.getId().equals(id)) remaining.add(robot);
        }
        b.robots = remaining;
        if (id.equals(s.selectedRobotId)) b.selectedRobotId = null;
        if (s.robotToOpen != null && id.equals(s.robotToOpen.getId())) b.robotToOpen = null;
        b.message = "Đã gỡ robot khỏi tài khoản.";
        state.setValue(b.build());
        if (!s.robotsLoaded) loadRobots();
    }

    private void expireSession(String message) {
        ++operation;
        robots.cancelPendingRequests();
        auth.cancelPendingRequests();
        auth.logout();
        MainUiState.Builder b = new MainUiState.Builder();
        b.requiresLogin = true;
        b.message = message;
        state.setValue(b.build());
    }

    public void acknowledgeFeedback() {
        MainUiState.Builder b = state.getValue().toBuilder();
        b.robotToOpen = null;
        b.message = null;
        state.setValue(b.build());
    }

    private boolean isCurrent(int current) {
        return !cleared && operation == current && !state.getValue().requiresLogin;
    }

    @Override protected void onCleared() {
        cleared = true;
        ++operation;
        robots.cancelPendingRequests();
        auth.cancelPendingRequests();
    }
}
