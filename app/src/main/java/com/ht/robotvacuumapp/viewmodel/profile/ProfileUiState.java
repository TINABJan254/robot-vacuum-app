package com.ht.robotvacuumapp.viewmodel.profile;

import com.ht.robotvacuumapp.api.dto.UserDto;

public final class ProfileUiState {
    public final UserDto account;
    public final boolean isLoading;
    public final boolean isLoggingOut;
    public final String error;
    public final boolean requiresLogin;

    ProfileUiState(UserDto account, boolean isLoading, boolean isLoggingOut,
                   String error, boolean requiresLogin) {
        this.account = account;
        this.isLoading = isLoading;
        this.isLoggingOut = isLoggingOut;
        this.error = error;
        this.requiresLogin = requiresLogin;
    }
}
