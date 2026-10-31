package com.ht.robotvacuumapp.viewmodel.robot;

import java.util.*;

public final class NewScheduleUiState {
    public final Long robotId, createdScheduleId;
    public final int hour, minute;
    public final boolean repeat, isSaving, requiresLogin;
    public final Set<Integer> days;
    public final String error;
    private NewScheduleUiState(Builder b) {
        robotId = b.robotId; createdScheduleId = b.createdScheduleId;
        hour = b.hour; minute = b.minute; repeat = b.repeat;
        isSaving = b.isSaving; requiresLogin = b.requiresLogin;
        days = Collections.unmodifiableSet(new TreeSet<>(b.days)); error = b.error;
    }
    Builder toBuilder() { return new Builder(this); }
    static final class Builder {
        Long robotId, createdScheduleId;
        int hour = 8, minute;
        boolean repeat, isSaving, requiresLogin;
        Set<Integer> days = new TreeSet<>(Arrays.asList(1, 2, 3, 4, 5));
        String error;
        Builder() { }
        Builder(NewScheduleUiState s) {
            robotId = s.robotId; createdScheduleId = s.createdScheduleId;
            hour = s.hour; minute = s.minute; repeat = s.repeat; isSaving = s.isSaving;
            requiresLogin = s.requiresLogin; days = new TreeSet<>(s.days); error = s.error;
        }
        NewScheduleUiState build() { return new NewScheduleUiState(this); }
    }
}
