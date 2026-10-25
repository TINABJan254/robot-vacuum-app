package com.ht.robotvacuumapp.viewmodel.robot;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.*;
import static com.ht.robotvacuumapp.viewmodel.robot.RobotControlUiState.Status.*;

public class RobotControlViewModelTest {
    @Rule public InstantTaskExecutorRule executor = new InstantTaskExecutorRule();

    @Test public void modePauseDockAndRestartPreserveRobotDetails() {
        RobotControlViewModel vm = new RobotControlViewModel("Robot_02", 45);
        assertTrue(vm.getUiState().getValue().autoMode);
        assertEquals(WORKING, vm.getUiState().getValue().status);
        vm.toggleMode();
        assertFalse(vm.getUiState().getValue().autoMode);
        vm.toggleWorking();
        assertEquals(PAUSED, vm.getUiState().getValue().status);
        vm.returnToDock();
        assertEquals(CHARGING, vm.getUiState().getValue().status);
        vm.toggleWorking();
        assertEquals(WORKING, vm.getUiState().getValue().status);
        assertFalse(vm.getUiState().getValue().autoMode);
        assertEquals("Robot_02", vm.getUiState().getValue().robotName);
        assertEquals(45, vm.getUiState().getValue().batteryPercent);
    }

    @Test public void acknowledgingMessageDoesNotResetControlState() {
        RobotControlViewModel vm = new RobotControlViewModel(null, 93);
        assertEquals("Robot_01", vm.getUiState().getValue().robotName);
        vm.returnToDock();
        assertNotNull(vm.getUiState().getValue().message);
        vm.acknowledgeFeedback();
        assertNull(vm.getUiState().getValue().message);
        assertEquals(CHARGING, vm.getUiState().getValue().status);
    }
}
