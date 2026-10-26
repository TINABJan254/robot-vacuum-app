package com.ht.robotvacuumapp.view.robot;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.ht.robotvacuumapp.R;
import com.ht.robotvacuumapp.viewmodel.robot.RobotControlUiState;
import com.ht.robotvacuumapp.viewmodel.robot.RobotControlViewModel;

public class RobotControlActivity extends AppCompatActivity {

    public static final String EXTRA_ROBOT_NAME = "extra_robot_name";
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
    private ImageView btnPlus;

    // Bottom Action Bar
    private TextView btnToggleMode;
    private ImageView btnPlayPause;
    private TextView btnCharge;

    private RobotControlViewModel viewModel;

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
                        getIntent().getStringExtra(EXTRA_ROBOT_NAME),
                        getIntent().getIntExtra(EXTRA_ROBOT_BATTERY, 93)));
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
        btnPlus = findViewById(R.id.btn_plus);

        btnToggleMode = findViewById(R.id.btn_toggle_mode);
        btnPlayPause = findViewById(R.id.btn_play_pause);
        btnCharge = findViewById(R.id.btn_charge);
    }

    private void setupTopBarActions() {
        btnBack.setOnClickListener(v -> finish());
        btnSettings.setOnClickListener(v -> {
            Toast.makeText(this, "Cài đặt thiết bị " + tvRobotTitle.getText(), Toast.LENGTH_SHORT).show();
        });
    }

    private void setupModeToggle() {
        btnToggleMode.setOnClickListener(v -> viewModel.toggleMode());
    }

    private void setupPlayPauseToggle() {
        btnPlayPause.setOnClickListener(v -> viewModel.toggleWorking());
    }

    private void render(RobotControlUiState state) {
        tvRobotTitle.setText(state.robotName);
        tvBatteryPercent.setText(state.batteryPercent < 0 ? "-- %" : state.batteryPercent + " %");
        btnToggleMode.setText(state.autoMode ? "Auto" : "Manual");
        int primaryGreen = ContextCompat.getColor(this, R.color.primary_green);
        int textSecondary = ContextCompat.getColor(this, R.color.text_secondary);

        if (state.status == RobotControlUiState.Status.WORKING) {
            btnPlayPause.setImageResource(R.drawable.ic_pause);
            tvStatus.setText("Working");
            tvStatus.setTextColor(primaryGreen);
            statusUnderline.setBackgroundColor(primaryGreen);
        } else {
            btnPlayPause.setImageResource(R.drawable.ic_play);
            tvStatus.setText(state.status == RobotControlUiState.Status.CHARGING ? "Đang sạc" : "Paused");
            tvStatus.setTextColor(textSecondary);
            statusUnderline.setBackgroundColor(textSecondary);
        }
        if (state.message != null) {
            viewModel.acknowledgeFeedback();
            Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show();
        }
    }

    private void setupDpadControls() {
        btnDpadUp.setOnClickListener(v -> {
            Toast.makeText(this, "Tiến lên phía trước ▲", Toast.LENGTH_SHORT).show();
        });

        btnDpadDown.setOnClickListener(v -> {
            Toast.makeText(this, "Lùi về phía sau ▼", Toast.LENGTH_SHORT).show();
        });

        btnDpadLeft.setOnClickListener(v -> {
            Toast.makeText(this, "Rẽ sang trái ◀", Toast.LENGTH_SHORT).show();
        });

        btnDpadRight.setOnClickListener(v -> {
            Toast.makeText(this, "Rẽ sang phải ▶", Toast.LENGTH_SHORT).show();
        });

        btnDpadCenter.setOnClickListener(v -> {
            Toast.makeText(this, "Dừng tại vị trí hiện tại", Toast.LENGTH_SHORT).show();
        });

        btnPlus.setOnClickListener(v -> {
            Toast.makeText(this, "Tăng công suất hút bụi (+)", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupChargeAction() {
        btnCharge.setOnClickListener(v -> viewModel.returnToDock());
    }
}
