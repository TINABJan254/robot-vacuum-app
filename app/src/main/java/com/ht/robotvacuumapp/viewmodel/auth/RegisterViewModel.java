package com.ht.robotvacuumapp.viewmodel.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.ht.robotvacuumapp.api.dto.RegisterResponse;
import com.ht.robotvacuumapp.model.repository.AuthRepository;

public class RegisterViewModel extends ViewModel {
    private final AuthRepository repository;
    private final MutableLiveData<RegisterUiState> state = new MutableLiveData<>(
            new RegisterUiState(false, null, null, null, null, 0, null, null));
    private boolean cleared;

    public RegisterViewModel(AuthRepository repository) { this.repository = repository; }
    public LiveData<RegisterUiState> getUiState() { return state; }

    public void updatePasswordStrength(String password) {
        int score = 0;
        if (!password.isEmpty()) {
            if (password.length() >= 8) score++;
            if (password.matches(".*[a-z].*") && password.matches(".*[A-Z].*")) score++;
            if (password.matches(".*[0-9].*")) score++;
            if (password.matches(".*[!@#$%^&*()\\-_=+\\[\\]{}|;:,.<>?].*")) score++;
            score = Math.max(1, score);
        }
        RegisterUiState s = state.getValue();
        state.setValue(new RegisterUiState(s.loading, s.nameError, s.usernameError, s.passwordError,
                s.confirmError, score, s.message, s.registeredUsername));
    }

    public void register(String rawName, String rawUsername, String rawPassword, String rawConfirm) {
        if (state.getValue().loading || state.getValue().registeredUsername != null) return;
        String name = rawName.trim();
        String username = rawUsername.trim();
        String password = rawPassword.trim();
        String confirm = rawConfirm.trim();
        String nameError = name.isEmpty() ? "Vui lòng nhập họ và tên" : null;
        String usernameError = username.isEmpty() ? "Vui lòng nhập tên đăng nhập" : null;
        String passwordError = password.isEmpty() ? "Vui lòng nhập mật khẩu"
                : password.length() < 8 ? "Mật khẩu phải có tối thiểu 8 ký tự" : null;
        String confirmError = password.equals(confirm) ? null : "Mật khẩu xác nhận không khớp";
        if (nameError != null || usernameError != null || passwordError != null || confirmError != null) {
            state.setValue(new RegisterUiState(false, nameError, usernameError, passwordError, confirmError,
                    state.getValue().passwordStrength, null, null));
            return;
        }
        publish(true, null, null);
        repository.register(name, username, password, new AuthRepository.AuthCallback<RegisterResponse>() {
            @Override public void onSuccess(RegisterResponse data) {
                if (!cleared) publish(false, null, username);
            }
            @Override public void onError(String message) {
                if (!cleared) publish(false, message, null);
            }
        });
    }

    private void publish(boolean loading, String message, String registeredUsername) {
        state.setValue(new RegisterUiState(loading, null, null, null, null,
                state.getValue().passwordStrength, message, registeredUsername));
    }

    public void acknowledgeFeedback() {
        RegisterUiState s = state.getValue();
        state.setValue(new RegisterUiState(s.loading, s.nameError, s.usernameError, s.passwordError,
                s.confirmError, s.passwordStrength, null, null));
    }

    @Override protected void onCleared() {
        cleared = true;
        repository.cancelPendingRequests();
    }
}
