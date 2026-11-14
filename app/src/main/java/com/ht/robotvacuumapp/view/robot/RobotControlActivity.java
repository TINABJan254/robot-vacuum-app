package com.ht.robotvacuumapp.view.robot;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.ht.robotvacuumapp.R;
import com.ht.robotvacuumapp.model.entity.RobotTelemetry;
import com.ht.robotvacuumapp.model.entity.RobotCommand;
import com.ht.robotvacuumapp.model.repository.AuthRepository;
import com.ht.robotvacuumapp.model.repository.RobotRepository;
import com.ht.robotvacuumapp.model.repository.WebSocketTelemetryStream;
import com.ht.robotvacuumapp.view.auth.LoginActivity;
import com.ht.robotvacuumapp.viewmodel.robot.RobotControlUiState;
import com.ht.robotvacuumapp.viewmodel.robot.RobotControlViewModel;

public class RobotControlActivity extends AppCompatActivity {

    public static final String EXTRA_ROBOT_NAME = "extra_robot_name";
    public static final String EXTRA_ROBOT_ID = "extra_robot_id";
    public static final String EXTRA_ROBOT_UNPAIRED = "extra_robot_unpaired";
    public static final String EXTRA_ROBOT_BATTERY = "extra_robot_battery";
    public static final String EXTRA_ROBOT_STATUS = "extra_robot_status";

    private ImageView btnBack;
    private TextView tvRobotTitle;
    private ImageView btnSettings;
    private TextView tvBatteryPercent;
    private TextView tvStatus;
    private View statusUnderline;

    // D-Pad
    private ImageView btnDpadUp;
    private ImageView btnDpadDown;
    private ImageView btnDpadLeft;
    private ImageView btnDpadRight;
    private FrameLayout btnDpadCenter;

    // Bottom Action Bar
    private TextView btnToggleMode;
    private ImageView btnPlayPause;
    private TextView btnCharge;

    private RobotControlViewModel viewModel;
    private boolean finishingAfterUnpair;
    private boolean navigatingToLogin;

    private final ActivityResultLauncher<Intent> settingsLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String id = result.getData().getStringExtra(RobotSettingsActivity.EXTRA_ROBOT_ID);
                    if (result.getData().getBooleanExtra(RobotSettingsActivity.EXTRA_ROBOT_UNPAIRED, false)) {
                        viewModel.applyUnpairedRobot(id);
                    } else {
                        viewModel.applyRenamedRobot(id, result.getData().getStringExtra(RobotSettingsActivity.EXTRA_ROBOT_NAME));
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_robot_control);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.control_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        viewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                return modelClass.cast(new RobotControlViewModel(
                        getIntent().getStringExtra(EXTRA_ROBOT_ID),
                        getIntent().getStringExtra(EXTRA_ROBOT_NAME),
                        new WebSocketTelemetryStream(getApplicationContext()),
                        new RobotRepository(getApplicationContext()),
                        new AuthRepository(getApplicationContext())));
            }
        }).get(RobotControlViewModel.class);
        setupTopBarActions();
        setupModeToggle();
        setupPlayPauseToggle();
        setupDpadControls();
        setupChargeAction();
        viewModel.getUiState().observe(this, this::render);
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        tvRobotTitle = findViewById(R.id.tv_robot_title);
        btnSettings = findViewById(R.id.btn_settings);
        tvBatteryPercent = findViewById(R.id.tv_battery_percent);
        tvStatus = findViewById(R.id.tv_status);
        statusUnderline = findViewById(R.id.status_underline);

        btnDpadUp = findViewById(R.id.btn_dpad_up);
        btnDpadDown = findViewById(R.id.btn_dpad_down);
        btnDpadLeft = findViewById(R.id.btn_dpad_left);
        btnDpadRight = findViewById(R.id.btn_dpad_right);
        btnDpadCenter = findViewById(R.id.btn_dpad_center);

        btnToggleMode = findViewById(R.id.btn_toggle_mode);
        btnPlayPause = findViewById(R.id.btn_play_pause);
        btnCharge = findViewById(R.id.btn_charge);
    }

    private void setupTopBarActions() {
        btnBack.setOnClickListener(v -> finish());
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(RobotControlActivity.this, RobotSettingsActivity.class);
            RobotControlUiState state = viewModel.getUiState().getValue();
            intent.putExtra(RobotSettingsActivity.EXTRA_ROBOT_ID, state.robotId);
            intent.putExtra(RobotSettingsActivity.EXTRA_ROBOT_NAME, state.robotName);
            settingsLauncher.launch(intent);
        });
    }

    private void setupModeToggle() {
        btnToggleMode.setOnClickListener(v -> viewModel.toggleMode());
    }

    private void setupPlayPauseToggle() {
        btnPlayPause.setOnClickListener(v -> viewModel.toggleWorking());
    }

    private void render(RobotControlUiState state) {
        if (state.requiresLogin) {
            if (!navigatingToLogin) {
                navigatingToLogin = true;
                if (state.message != null) Toast.makeText(this, state.message, Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, LoginActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
            }
            return;
        }
        if (state.robotUnpaired) {
            if (!finishingAfterUnpair) {
                finishingAfterUnpair = true;
                setResult(RESULT_OK, new Intent().putExtra(EXTRA_ROBOT_ID, state.robotId).putExtra(EXTRA_ROBOT_UNPAIRED, true));
                finish();
            }
            return;
        }
        if (state.nameUpdated) {
            setResult(RESULT_OK, new Intent().putExtra(EXTRA_ROBOT_ID, state.robotId)
                    .putExtra(EXTRA_ROBOT_NAME, state.robotName));
        }
        tvRobotTitle.setText(state.robotName);
        renderTelemetry(state);
        renderCommandControls(state);
        if (state.publishedCommand != null) {
            viewModel.acknowledgeFeedback();
            Toast.makeText(this, commandFeedback(state.publishedCommand), Toast.LENGTH_SHORT).show();
        } else if (state.message != null) {
            viewModel.acknowledgeFeedback();
            Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show();
        }
    }

    /** Accessibility (TalkBack double-tap) has no press/release: move briefly, then stop. */
    private static final long ACCESSIBILITY_MOVE_PULSE_MS = 600;

    private void setupDpadControls() {
        bindHoldToMove(btnDpadUp, RobotCommand.Direction.FORWARD);
        bindHoldToMove(btnDpadDown, RobotCommand.Direction.BACKWARD);
        bindHoldToMove(btnDpadLeft, RobotCommand.Direction.LEFT);
        bindHoldToMove(btnDpadRight, RobotCommand.Direction.RIGHT);
        btnDpadCenter.setOnClickListener(v -> viewModel.stopMovement());
    }

    // Touch is handled entirely here (press = move, release = stop); clicks only come from accessibility.
    @SuppressLint("ClickableViewAccessibility")
    private void bindHoldToMove(View button, RobotCommand.Direction direction) {
        button.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    v.setPressed(true);
                    viewModel.startMove(direction);
                    return true;
                case MotionEvent.ACTION_MOVE:
                    // Finger slid off the button: treat as release so the robot never keeps going unseen.
                    if (!isInside(v, event) && v.isPressed()) {
                        v.setPressed(false);
                        viewModel.stopMove(direction);
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.setPressed(false);
                    viewModel.stopMove(direction);
                    return true;
                default:
                    return true;
            }
        });
        button.setOnClickListener(v -> {
            viewModel.startMove(direction);
            v.postDelayed(() -> viewModel.stopMove(direction), ACCESSIBILITY_MOVE_PULSE_MS);
        });
    }

    private static boolean isInside(View v, MotionEvent event) {
        return event.getX() >= 0 && event.getY() >= 0 && event.getX() <= v.getWidth() && event.getY() <= v.getHeight();
    }

    private void setupChargeAction() {
        btnCharge.setOnClickListener(v -> viewModel.returnToDock());
    }

    private void renderCommandControls(RobotControlUiState state) {
        boolean available = state.robotId != null && !state.requiresLogin && !state.robotUnpaired;
        // The D-pad is never disabled by a pending one-shot command: a disabled view stops receiving
        // touch events, so the finger-up (STOP) would be lost while the robot is moving.
        for (View control : new View[]{btnDpadUp, btnDpadDown, btnDpadLeft, btnDpadRight, btnDpadCenter}) {
            control.setEnabled(available);
            control.setAlpha(available ? 1f : 0.45f);
        }
        boolean enabled = available && !state.isSendingCommand;
        for (View control : new View[]{btnPlayPause, btnCharge, btnToggleMode}) {
            boolean canUse = enabled && (control != btnToggleMode
                    || (state.telemetry != null && state.telemetry.mode != null));
            control.setEnabled(canUse);
            control.setAlpha(canUse ? 1f : 0.45f);
            ViewCompat.setStateDescription(control, state.isSendingCommand ? getString(R.string.command_sending) : null);
        }
    }

    private int commandFeedback(RobotCommand.Action action) {
        switch (action) {
            case START: return R.string.command_sent_start;
            case STOP: return R.string.command_sent_stop;
            case SET_MODE: return R.string.command_sent_mode;
            case MOVE: return R.string.command_sent_move;
            case STOP_MOVEMENT: return R.string.command_sent_stop_movement;
            default: return R.string.command_sent_dock;
        }
    }

    private void renderTelemetry(RobotControlUiState state) {
        RobotTelemetry t = state.telemetry;
        tvBatteryPercent.setText(t == null ? getString(R.string.telemetry_battery_unknown)
                : getString(R.string.telemetry_battery_percent, t.batteryPercent));
        btnToggleMode.setText(t == null || t.mode == null ? R.string.telemetry_mode_unknown
                : t.mode == RobotTelemetry.Mode.AUTO ? R.string.telemetry_mode_auto : R.string.telemetry_mode_manual);
        boolean working = t != null && t.status == RobotTelemetry.Status.WORKING;
        btnPlayPause.setImageResource(working ? R.drawable.ic_pause : R.drawable.ic_play);
        btnPlayPause.setContentDescription(getString(working
                ? R.string.telemetry_action_pause : R.string.telemetry_action_start));

        String label;
        int color = R.color.text_secondary;
        if (t == null) {
            label = state.telemetryError != null ? state.telemetryError
                    : getString(state.isLoading || !state.telemetryLoaded
                    ? R.string.telemetry_loading : R.string.telemetry_no_data);
        } else if (t.stale) {
            // Backend pushes this when the robot stopped sending telemetry (> 30 s).
            label = getString(R.string.telemetry_robot_offline);
            color = R.color.color_danger;
        } else if (!state.live && state.telemetryError != null) {
            label = state.telemetryError; // Reconnecting: last snapshot may be outdated.
        } else {
            label = getString(statusLabel(t.status));
            if (t.status == RobotTelemetry.Status.ERROR) {
                color = R.color.color_danger;
            } else if (working) {
                color = R.color.primary_green;
            }
        }
        tvStatus.setText(label);
        tvStatus.setContentDescription(label);
        tvStatus.setTextColor(ContextCompat.getColor(this, color));
        statusUnderline.setBackgroundColor(ContextCompat.getColor(this, color));
    }

    private int statusLabel(RobotTelemetry.Status status) {
        switch (status) {
            case WORKING: return R.string.telemetry_status_working;
            case RETURNING_TO_DOCK: return R.string.telemetry_status_returning;
            case CHARGING: return R.string.telemetry_status_charging;
            case ERROR: return R.string.telemetry_status_error;
            default: return R.string.telemetry_status_idle;
        }
    }

    @Override protected void onStart() {
        super.onStart();
        if (!isFinishing()) viewModel.startTelemetry();
    }

    @Override protected void onStop() {
        // Leaving the screen while a direction is held (no ACTION_UP will arrive): stop the wheels.
        for (View button : new View[]{btnDpadUp, btnDpadDown, btnDpadLeft, btnDpadRight}) button.setPressed(false);
        viewModel.stopMove();
        viewModel.stopTelemetry();
        super.onStop();
    }
}
