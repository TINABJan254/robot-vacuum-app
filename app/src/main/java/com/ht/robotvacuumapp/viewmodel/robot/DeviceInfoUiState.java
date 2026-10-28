package com.ht.robotvacuumapp.viewmodel.robot;

/** Only the fields displayed by the device information screen. Serial is always a placeholder. */
public final class DeviceInfoUiState {
    public final String robotId;
    public final String robotName;
    public final String pairCode;
    public final boolean isLoading;
    public final String error;
    public final boolean requiresLogin;

    DeviceInfoUiState(String robotId, String robotName, String pairCode,
                      boolean isLoading, String error, boolean requiresLogin) {
        this.robotId = robotId;
        this.robotName = robotName;
        this.pairCode = pairCode;
        this.isLoading = isLoading;
        this.error = error;
        this.requiresLogin = requiresLogin;
    }
}
