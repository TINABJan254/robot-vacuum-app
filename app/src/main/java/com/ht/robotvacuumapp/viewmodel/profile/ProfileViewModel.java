package com.ht.robotvacuumapp.viewmodel.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.ht.robotvacuumapp.api.dto.UserDto;
import com.ht.robotvacuumapp.model.repository.AuthRepository;

public class ProfileViewModel extends ViewModel {
    private final AuthRepository auth;
    private final MutableLiveData<ProfileUiState> state;
    private long operation;
    private boolean cleared;

    public ProfileViewModel(AuthRepository auth) {
        this.auth = auth;
        state = new MutableLiveData<>(new ProfileUiState(null, false, false, null, !auth.isLoggedIn()));
        if (!state.getValue().requiresLogin) loadProfile();
    }

    public LiveData<ProfileUiState> getUiState() { return state; }

    public void loadProfile() {
        ProfileUiState s = state.getValue();
        if (cleared || s.isLoading || s.isLoggingOut || s.requiresLogin) return;
        long current = ++operation;
        state.setValue(new ProfileUiState(s.account, true, false, null, false));
        auth.getAccount(new AuthRepository.AuthCallback<UserDto>() {
            @Override public void onSuccess(UserDto user) {
                if (isCurrent(current)) state.setValue(new ProfileUiState(user, false, false, null, false));
            }
            @Override public void onError(int status, String message) {
                if (!isCurrent(current)) return;
                if (status == 401) finishLogout(message);
                else state.setValue(new ProfileUiState(state.getValue().account, false, false, message, false));
            }
        });
    }

    public void logout() {
        ProfileUiState s = state.getValue();
        if (cleared || s.requiresLogin || s.isLoggingOut) return;
        long current = ++operation;
        auth.cancelPendingRequests();
        state.setValue(new ProfileUiState(s.account, false, true, null, false));
        auth.logoutAccount(new AuthRepository.AuthCallback<Void>() {
            @Override public void onSuccess(Void ignored) {
                if (isCurrent(current)) finishLogout(null);
            }
            @Override public void onError(int status, String message) {
                if (isCurrent(current)) finishLogout(status == 401 ? null
                        : "Đã đăng xuất trên thiết bị. Chưa thể thu hồi phiên trên máy chủ do lỗi kết nối hoặc máy chủ.");
            }
        });
    }

    private void finishLogout(String message) {
        ++operation;
        auth.cancelPendingRequests();
        auth.logout();
        state.setValue(new ProfileUiState(null, false, false, message, true));
    }

    private boolean isCurrent(long current) {
        return !cleared && current == operation && !state.getValue().requiresLogin;
    }

    @Override protected void onCleared() {
        cleared = true;
        ++operation;
        auth.cancelPendingRequests();
    }
}
