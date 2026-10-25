package com.ht.robotvacuumapp.viewmodel.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.ht.robotvacuumapp.api.dto.LoginResponse;
import com.ht.robotvacuumapp.model.repository.AuthRepository;

public class LoginViewModel extends ViewModel {
    private final AuthRepository repository;
    private final MutableLiveData<LoginUiState> state = new MutableLiveData<>(LoginUiState.idle());
    private boolean cleared;

    public LoginViewModel(AuthRepository repository) { this.repository = repository; }
    public LiveData<LoginUiState> getUiState() { return state; }
    public String getSavedUsername() { return repository.getSavedUsername(); }
    public boolean isRememberMe() { return repository.isRememberMe(); }
    public String getBaseUrl() { return repository.getBaseUrl(); }

    public void login(String rawUsername, String rawPassword, boolean rememberMe) {
        if (state.getValue().loading || state.getValue().loginSucceeded) return;
        String username = rawUsername.trim();
        String password = rawPassword.trim();
        if (username.isEmpty() || password.isEmpty()) {
            state.setValue(new LoginUiState(false,
                    username.isEmpty() ? "Vui lòng nhập tên đăng nhập" : null,
                    password.isEmpty() ? "Vui lòng nhập mật khẩu" : null, null, false, null));
            return;
        }
        state.setValue(new LoginUiState(true, null, null, null, false, null));
        repository.login(username, password, new AuthRepository.AuthCallback<LoginResponse>() {
            @Override public void onSuccess(LoginResponse data) {
                if (cleared) return;
                repository.saveLoginPreference(username, rememberMe);
                String name = data.getUser() != null && data.getUser().getName() != null
                        ? data.getUser().getName() : username;
                state.setValue(new LoginUiState(false, null, null, null, true, name));
            }
            @Override public void onError(String message) {
                if (!cleared) state.setValue(new LoginUiState(false, null, null, message, false, null));
            }
        });
    }

    public void setServer(String url, boolean useDefault) {
        if (state.getValue().loading) return;
        try {
            if (useDefault) repository.resetBaseUrl();
            else if (!url.trim().isEmpty()) repository.setBaseUrl(url);
            else return;
            state.setValue(new LoginUiState(false, null, null,
                    (useDefault ? "Đã đặt về mặc định: " : "Đã lưu máy chủ: ") + getBaseUrl(), false, null));
        } catch (IllegalArgumentException error) {
            state.setValue(new LoginUiState(false, null, null, "Địa chỉ máy chủ không hợp lệ", false, null));
        }
    }

    public void acknowledgeFeedback() {
        LoginUiState current = state.getValue();
        state.setValue(new LoginUiState(current.loading, current.usernameError, current.passwordError, null, false, null));
    }

    @Override protected void onCleared() {
        cleared = true;
        repository.cancelPendingRequests();
    }
}
