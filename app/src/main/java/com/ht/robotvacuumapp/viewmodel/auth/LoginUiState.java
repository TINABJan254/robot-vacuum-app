package com.ht.robotvacuumapp.viewmodel.auth;

public final class LoginUiState {
    public final boolean loading;
    public final String usernameError;
    public final String passwordError;
    public final String message;
    public final boolean loginSucceeded;
    public final String displayName;

    public LoginUiState(boolean loading, String usernameError, String passwordError,
                        String message, boolean loginSucceeded, String displayName) {
        this.loading = loading;
        this.usernameError = usernameError;
        this.passwordError = passwordError;
        this.message = message;
        this.loginSucceeded = loginSucceeded;
        this.displayName = displayName;
    }

    public static LoginUiState idle() {
        return new LoginUiState(false, null, null, null, false, null);
    }
}
