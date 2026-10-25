package com.ht.robotvacuumapp.viewmodel.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.ht.robotvacuumapp.model.entity.RobotDevice;
import com.ht.robotvacuumapp.model.repository.AuthRepository;
import com.ht.robotvacuumapp.model.repository.DemoRobotRepository;

public class MainViewModel extends ViewModel {
    private final AuthRepository auth;
    private final DemoRobotRepository robots;
    private final MutableLiveData<MainUiState> state;

    public MainViewModel(AuthRepository auth, DemoRobotRepository robots) {
        this.auth = auth;
        this.robots = robots;
        state = new MutableLiveData<>(new MainUiState(robots.getDevices(), "1", 1, !auth.isLoggedIn(),
                null, null, false, null, null));
    }

    public LiveData<MainUiState> getUiState() { return state; }
    public String getUserName() { return auth.getUserName(); }
    public String getUsername() { return auth.getUsername(); }
    public String getUserRole() { return auth.getUserRole(); }

    public void selectTab(int tab) {
        MainUiState s = state.getValue();
        state.setValue(new MainUiState(s.robots, s.selectedRobotId, tab, s.requiresLogin,
                s.robotIdError, s.robotCodeError, s.robotAdded, s.robotToOpen, s.message));
    }

    public void addRobot(String rawId, String rawCode) {
        String id = rawId.trim();
        String code = rawCode.trim();
        MainUiState s = state.getValue();
        if (id.isEmpty() || code.isEmpty()) {
            state.setValue(new MainUiState(s.robots, s.selectedRobotId, s.selectedTab, s.requiresLogin,
                    id.isEmpty() ? "Vui lòng nhập ID robot" : null,
                    code.isEmpty() ? "Vui lòng nhập mã code" : null, false, null, null));
            return;
        }
        robots.add(id);
        state.setValue(new MainUiState(robots.getDevices(), s.selectedRobotId, s.selectedTab, s.requiresLogin,
                null, null, true, null, "Đã thêm thành công: " + id + " (Code: " + code + ")"));
    }

    public void clearRobots() {
        robots.clear();
        publishDevices(null, "Đang hiển thị giao diện: Chưa có robot");
    }

    public void restoreRobots() {
        robots.restoreSamples();
        publishDevices("1", "Đã khôi phục danh sách robot");
    }

    private void publishDevices(String selectedId, String message) {
        MainUiState s = state.getValue();
        state.setValue(new MainUiState(robots.getDevices(), selectedId, s.selectedTab, s.requiresLogin,
                null, null, false, null, message));
    }

    public void selectRobot(RobotDevice robot) {
        MainUiState s = state.getValue();
        state.setValue(new MainUiState(s.robots, robot.getId(), s.selectedTab, s.requiresLogin,
                null, null, false, robot, null));
    }

    public void logout() {
        auth.logout();
        MainUiState s = state.getValue();
        state.setValue(new MainUiState(s.robots, s.selectedRobotId, s.selectedTab, true,
                null, null, false, null, null));
    }

    public void acknowledgeFeedback() {
        MainUiState s = state.getValue();
        state.setValue(new MainUiState(s.robots, s.selectedRobotId, s.selectedTab, s.requiresLogin,
                null, null, false, null, null));
    }

    @Override protected void onCleared() { auth.cancelPendingRequests(); }
}
