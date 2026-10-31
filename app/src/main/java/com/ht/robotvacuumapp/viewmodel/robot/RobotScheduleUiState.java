package com.ht.robotvacuumapp.viewmodel.robot;

import com.ht.robotvacuumapp.api.dto.ScheduleResponse;
import java.util.*;

public final class RobotScheduleUiState {
    public final Long robotId;
    public final List<ScheduleResponse> schedules;
    public final Map<Long, Boolean> pendingEnabled;
    public final Set<Long> deleting;
    public final boolean isLoading, isLoadingMore, loaded, hasMore, requiresLogin;
    public final String listError, moreError, message;
    private RobotScheduleUiState(Builder b) {
        robotId = b.robotId;
        schedules = Collections.unmodifiableList(new ArrayList<>(b.schedules));
        pendingEnabled = Collections.unmodifiableMap(new HashMap<>(b.pendingEnabled));
        deleting = Collections.unmodifiableSet(new HashSet<>(b.deleting));
        isLoading = b.isLoading; isLoadingMore = b.isLoadingMore; loaded = b.loaded;
        hasMore = b.hasMore; requiresLogin = b.requiresLogin;
        listError = b.listError; moreError = b.moreError; message = b.message;
    }
    Builder toBuilder() { return new Builder(this); }
    static final class Builder {
        Long robotId;
        List<ScheduleResponse> schedules = Collections.emptyList();
        Map<Long, Boolean> pendingEnabled = new HashMap<>();
        Set<Long> deleting = new HashSet<>();
        boolean isLoading, isLoadingMore, loaded, hasMore, requiresLogin;
        String listError, moreError, message;
        Builder() { }
        Builder(RobotScheduleUiState s) {
            robotId = s.robotId; schedules = s.schedules;
            pendingEnabled = new HashMap<>(s.pendingEnabled); deleting = new HashSet<>(s.deleting);
            isLoading = s.isLoading; isLoadingMore = s.isLoadingMore; loaded = s.loaded;
            hasMore = s.hasMore; requiresLogin = s.requiresLogin;
            listError = s.listError; moreError = s.moreError; message = s.message;
        }
        RobotScheduleUiState build() { return new RobotScheduleUiState(this); }
    }
}
