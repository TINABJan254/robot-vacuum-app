package com.ht.robotvacuumapp.viewmodel.robot;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import com.ht.robotvacuumapp.api.dto.RobotCommandResponse;
import com.ht.robotvacuumapp.model.entity.RobotCommand;
import com.ht.robotvacuumapp.model.entity.RobotTelemetry;
import com.ht.robotvacuumapp.model.repository.AuthRepository;
import com.ht.robotvacuumapp.model.repository.RobotRepository;
import com.ht.robotvacuumapp.model.repository.TelemetryStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class RobotControlViewModelTest {
    @Rule public InstantTaskExecutorRule executor = new InstantTaskExecutorRule();

    /** Records start/stop and lets the test push events. */
    private static final class FakeStream implements TelemetryStream {
        Listener listener;
        int starts;
        int stops;
        @Override public void start(long robotId, Listener listener) { this.listener = listener; starts++; }
        @Override public void stop() { listener = null; stops++; }
    }

    /** Manual scheduler: tasks run only when the test calls runPending(). */
    private static final class FakeScheduler implements RobotControlViewModel.Scheduler {
        final List<Runnable> tasks = new ArrayList<>();
        @Override public void postDelayed(Runnable task, long delayMillis) { tasks.add(task); }
        @Override public void remove(Runnable task) { tasks.remove(task); }
        void runPending() {
            List<Runnable> copy = new ArrayList<>(tasks);
            tasks.clear();
            for (Runnable task : copy) task.run();
        }
    }

    /** Captures each sendCommand call so the test decides when/how it completes. */
    private static final class SentCommand {
        final RobotCommand command;
        final RobotRepository.Callback<RobotCommandResponse> callback;
        SentCommand(RobotCommand command, RobotRepository.Callback<RobotCommandResponse> callback) {
            this.command = command;
            this.callback = callback;
        }
        void succeed() { callback.onSuccess(mock(RobotCommandResponse.class)); }
    }

    private FakeStream stream;
    private FakeScheduler scheduler;
    private RobotRepository commands;
    private AuthRepository auth;
    private final List<SentCommand> sent = new ArrayList<>();
    private RobotControlViewModel vm;

    @SuppressWarnings("unchecked")
    @Before public void setUp() {
        stream = new FakeStream();
        scheduler = new FakeScheduler();
        commands = mock(RobotRepository.class);
        auth = mock(AuthRepository.class);
        doAnswer(call -> {
            sent.add(new SentCommand(call.getArgument(1), call.getArgument(2)));
            return null;
        }).when(commands).sendCommand(anyLong(), any(RobotCommand.class), any(RobotRepository.Callback.class));
        vm = new RobotControlViewModel("3", "Robot", stream, commands, auth, scheduler);
    }

    private static RobotTelemetry telemetry(boolean stale) {
        return new RobotTelemetry(3, 80, RobotTelemetry.Status.WORKING, RobotTelemetry.Mode.MANUAL,
                "2026-11-08T00:00:00Z", "2026-11-08T00:00:01Z", stale);
    }

    private RobotControlUiState state() { return vm.getUiState().getValue(); }

    // ---------------------------------------------------------------- telemetry

    @Test public void pushedTelemetryUpdatesStateWithoutPolling() {
        vm.startTelemetry();
        assertEquals(1, stream.starts);
        assertTrue(state().isLoading);

        stream.listener.onTelemetry(telemetry(false));
        assertEquals(80, state().telemetry.batteryPercent);
        assertTrue(state().live);
        assertFalse(state().isLoading);
        assertTrue("no polling task scheduled", scheduler.tasks.isEmpty());
    }

    @Test public void connectionLossKeepsLastSnapshotAndShowsMessage() {
        vm.startTelemetry();
        stream.listener.onTelemetry(telemetry(false));
        stream.listener.onConnectionLost("reconnecting");
        assertNotNull(state().telemetry);
        assertFalse(state().live);
        assertEquals("reconnecting", state().telemetryError);
    }

    @Test public void unauthorizedStreamExpiresSession() {
        vm.startTelemetry();
        stream.listener.onUnauthorized();
        assertTrue(state().requiresLogin);
        verify(auth).logout();
    }

    @Test public void stopTelemetryClosesStream() {
        vm.startTelemetry();
        vm.stopTelemetry();
        assertNull(stream.listener);
        assertFalse(state().live);
    }

    // ---------------------------------------------------------------- hold-to-move

    @Test public void holdingRepeatsMoveAndReleaseSendsStop() {
        vm.startMove(RobotCommand.Direction.FORWARD);
        assertEquals(1, sent.size());
        assertEquals(RobotCommand.Action.MOVE, sent.get(0).command.action);
        assertEquals(RobotCommand.Direction.FORWARD, sent.get(0).command.direction);

        sent.get(0).succeed();
        scheduler.runPending(); // repeat tick
        assertEquals(2, sent.size());
        assertEquals(RobotCommand.Action.MOVE, sent.get(1).command.action);
        sent.get(1).succeed();

        vm.stopMove(RobotCommand.Direction.FORWARD);
        assertEquals(3, sent.size());
        assertEquals(RobotCommand.Action.STOP_MOVEMENT, sent.get(2).command.action);
        assertTrue("repeat cancelled", scheduler.tasks.isEmpty());
        assertFalse(vm.isHoldingMove());
    }

    @Test public void slowNetworkSkipsTicksInsteadOfQueueingMoves() {
        vm.startMove(RobotCommand.Direction.LEFT);
        scheduler.runPending();
        scheduler.runPending();
        assertEquals("still only the first MOVE in flight", 1, sent.size());
    }

    @Test public void releaseWhileMoveInFlightStopsAgainAfterItCompletes() {
        vm.startMove(RobotCommand.Direction.RIGHT);
        vm.stopMove(RobotCommand.Direction.RIGHT);
        assertEquals(RobotCommand.Action.STOP_MOVEMENT, sent.get(1).command.action);

        sent.get(0).succeed(); // the MOVE may have reached the robot after the STOP
        assertEquals(3, sent.size());
        assertEquals(RobotCommand.Action.STOP_MOVEMENT, sent.get(2).command.action);
    }

    @Test public void releasingAnotherDirectionDoesNotStopTheHeldOne() {
        vm.startMove(RobotCommand.Direction.FORWARD);
        vm.stopMove(RobotCommand.Direction.LEFT);
        assertTrue(vm.isHoldingMove());
        assertEquals(1, sent.size());
    }

    @Test public void moveErrorIsReportedOncePerPress() {
        vm.startMove(RobotCommand.Direction.BACKWARD);
        sent.get(0).callback.onError(503, "offline", Collections.emptyMap());
        assertEquals("offline", state().message);
        vm.acknowledgeFeedback();

        scheduler.runPending();
        sent.get(1).callback.onError(503, "offline", Collections.emptyMap());
        assertNull(state().message);
    }

    @Test public void dpadMoveDoesNotBlockOtherControls() {
        vm.startMove(RobotCommand.Direction.FORWARD);
        assertFalse(state().isSendingCommand);
    }
}
