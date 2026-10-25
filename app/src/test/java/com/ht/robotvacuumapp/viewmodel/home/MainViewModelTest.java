package com.ht.robotvacuumapp.viewmodel.home;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import com.ht.robotvacuumapp.model.entity.RobotDevice;
import com.ht.robotvacuumapp.model.repository.AuthRepository;
import com.ht.robotvacuumapp.model.repository.DemoRobotRepository;
import org.junit.Rule;
import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class MainViewModelTest {
    @Rule public InstantTaskExecutorRule executor = new InstantTaskExecutorRule();

    @Test public void demoListValidatesAddsClearsAndRestoresWithoutMutatingEarlierState() {
        AuthRepository auth = mock(AuthRepository.class);
        when(auth.isLoggedIn()).thenReturn(true);
        MainViewModel vm = new MainViewModel(auth, new DemoRobotRepository());
        List<RobotDevice> initial = vm.getUiState().getValue().robots;
        assertEquals(3, initial.size());
        vm.addRobot("", "");
        assertNotNull(vm.getUiState().getValue().robotIdError);
        assertEquals(3, vm.getUiState().getValue().robots.size());
        vm.addRobot(" Robot_04 ", " code ");
        assertEquals(4, vm.getUiState().getValue().robots.size());
        assertTrue(vm.getUiState().getValue().robotAdded);
        assertEquals("Robot_04", vm.getUiState().getValue().robots.get(3).getId());
        assertEquals(3, initial.size());
        vm.acknowledgeFeedback();
        assertFalse(vm.getUiState().getValue().robotAdded);
        vm.clearRobots();
        assertTrue(vm.getUiState().getValue().robots.isEmpty());
        vm.restoreRobots();
        assertEquals(3, vm.getUiState().getValue().robots.size());
        assertEquals("1", vm.getUiState().getValue().selectedRobotId);
    }

    @Test public void selectionSurvivesAcknowledgingNavigation() {
        AuthRepository auth = mock(AuthRepository.class);
        when(auth.isLoggedIn()).thenReturn(true);
        MainViewModel vm = new MainViewModel(auth, new DemoRobotRepository());
        RobotDevice robot = vm.getUiState().getValue().robots.get(1);
        vm.selectTab(0);
        vm.selectRobot(robot);
        assertSame(robot, vm.getUiState().getValue().robotToOpen);
        vm.acknowledgeFeedback();
        assertNull(vm.getUiState().getValue().robotToOpen);
        assertEquals("2", vm.getUiState().getValue().selectedRobotId);
        assertEquals(0, vm.getUiState().getValue().selectedTab);
    }

    @Test public void missingSessionAndLogoutRequireLogin() {
        AuthRepository auth = mock(AuthRepository.class);
        MainViewModel vm = new MainViewModel(auth, new DemoRobotRepository());
        assertTrue(vm.getUiState().getValue().requiresLogin);
        when(auth.isLoggedIn()).thenReturn(true);
        vm = new MainViewModel(auth, new DemoRobotRepository());
        assertFalse(vm.getUiState().getValue().requiresLogin);
        vm.logout();
        verify(auth).logout();
        assertTrue(vm.getUiState().getValue().requiresLogin);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void callersCannotModifyListSnapshot() {
        new DemoRobotRepository().getDevices().clear();
    }
}
