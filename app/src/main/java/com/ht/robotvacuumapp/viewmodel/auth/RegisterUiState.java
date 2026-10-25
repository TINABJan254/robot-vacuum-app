package com.ht.robotvacuumapp.viewmodel.auth;

public final class RegisterUiState {
    public final boolean loading;
    public final String nameError;
    public final String usernameError;
    public final String passwordError;
    public final String confirmError;
    public final int passwordStrength;
    public final String message;
    public final String registeredUsername;

    public RegisterUiState(boolean loading, String nameError, String usernameError, String passwordError,
                           String confirmError, int passwordStrength, String message, String registeredUsername) {
        this.loading = loading;
        this.nameError = nameError;
        this.usernameError = usernameError;
        this.passwordError = passwordError;
        this.confirmError = confirmError;
        this.passwordStrength = passwordStrength;
        this.message = message;
        this.registeredUsername = registeredUsername;
    }
}
