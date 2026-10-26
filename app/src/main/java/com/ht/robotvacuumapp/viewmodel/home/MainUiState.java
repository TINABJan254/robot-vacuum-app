package com.ht.robotvacuumapp.viewmodel.home;

import com.ht.robotvacuumapp.model.entity.RobotDevice;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MainUiState {
    public final List<RobotDevice> robots;
    public final String selectedRobotId;
    public final int selectedTab;
    public final boolean requiresLogin;
    public final boolean isLoadingRobots;
    public final boolean robotsLoaded;
    public final String listError;
    public final boolean pairDialogOpen;
    public final String draftRobotId;
    public final String draftPairCode;
    public final boolean isPairing;
    public final String robotIdError;
    public final String robotCodeError;
    public final String pairError;
    public final RobotDevice robotToOpen;
    public final String message;

    private MainUiState(Builder b) {
        robots = Collections.unmodifiableList(new ArrayList<>(b.robots));
        selectedRobotId = b.selectedRobotId;
        selectedTab = b.selectedTab;
        requiresLogin = b.requiresLogin;
        isLoadingRobots = b.isLoadingRobots;
        robotsLoaded = b.robotsLoaded;
        listError = b.listError;
        pairDialogOpen = b.pairDialogOpen;
        draftRobotId = b.draftRobotId;
        draftPairCode = b.draftPairCode;
        isPairing = b.isPairing;
        robotIdError = b.robotIdError;
        robotCodeError = b.robotCodeError;
        pairError = b.pairError;
        robotToOpen = b.robotToOpen;
        message = b.message;
    }

    // Only the ViewModel can construct new state; the View receives immutable snapshots.
    Builder toBuilder() { return new Builder(this); }

    static final class Builder {
        List<RobotDevice> robots = Collections.emptyList();
        String selectedRobotId;
        int selectedTab = 1;
        boolean requiresLogin;
        boolean isLoadingRobots;
        boolean robotsLoaded;
        String listError;
        boolean pairDialogOpen;
        String draftRobotId = "";
        String draftPairCode = "";
        boolean isPairing;
        String robotIdError;
        String robotCodeError;
        String pairError;
        RobotDevice robotToOpen;
        String message;

        Builder() { }

        Builder(MainUiState s) {
            robots = s.robots;
            selectedRobotId = s.selectedRobotId;
            selectedTab = s.selectedTab;
            requiresLogin = s.requiresLogin;
            isLoadingRobots = s.isLoadingRobots;
            robotsLoaded = s.robotsLoaded;
            listError = s.listError;
            pairDialogOpen = s.pairDialogOpen;
            draftRobotId = s.draftRobotId;
            draftPairCode = s.draftPairCode;
            isPairing = s.isPairing;
            robotIdError = s.robotIdError;
            robotCodeError = s.robotCodeError;
            pairError = s.pairError;
            robotToOpen = s.robotToOpen;
            message = s.message;
        }

        MainUiState build() { return new MainUiState(this); }
    }
}
