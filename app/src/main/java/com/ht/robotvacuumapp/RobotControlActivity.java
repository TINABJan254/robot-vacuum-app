package com.ht.robotvacuumapp;

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

    private boolean isAutoMode = true;
    private boolean isWorking = true;

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
        loadRobotData();
        setupTopBarActions();
        setupModeToggle();
        setupPlayPauseToggle();
        setupDpadControls();
        setupChargeAction();
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

    private void loadRobotData() {
        String robotName = getIntent().getStringExtra(EXTRA_ROBOT_NAME);
        if (robotName == null || robotName.isEmpty()) {
            robotName = "Robot_01";
        }
        tvRobotTitle.setText(robotName);

        int battery = getIntent().getIntExtra(EXTRA_ROBOT_BATTERY, 93);
        tvBatteryPercent.setText(battery + " %");

        // Initial running state (Working by default)
        isWorking = true;
        updateWorkingStateUI(false);
    }

    private void setupTopBarActions() {
        btnBack.setOnClickListener(v -> finish());
        btnSettings.setOnClickListener(v -> {
            Toast.makeText(this, "Cài đặt thiết bị " + tvRobotTitle.getText(), Toast.LENGTH_SHORT).show();
        });
    }

    private void setupModeToggle() {
        btnToggleMode.setOnClickListener(v -> {
            isAutoMode = !isAutoMode;
            if (isAutoMode) {
                btnToggleMode.setText("Auto");
                Toast.makeText(this, "Chuyển sang chế độ Tự động (Auto)", Toast.LENGTH_SHORT).show();
            } else {
                btnToggleMode.setText("Manual");
                Toast.makeText(this, "Chuyển sang chế độ Thủ công (Manual)", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupPlayPauseToggle() {
        btnPlayPause.setOnClickListener(v -> {
            isWorking = !isWorking;
            updateWorkingStateUI(true);
        });
    }

    private void updateWorkingStateUI(boolean showToast) {
        int primaryGreen = ContextCompat.getColor(this, R.color.primary_green);
        int textSecondary = ContextCompat.getColor(this, R.color.text_secondary);

        if (isWorking) {
            btnPlayPause.setImageResource(R.drawable.ic_pause);
            tvStatus.setText("Working");
            tvStatus.setTextColor(primaryGreen);
            statusUnderline.setBackgroundColor(primaryGreen);
            if (showToast) {
                Toast.makeText(this, "Robot bắt đầu hoạt động (Working)", Toast.LENGTH_SHORT).show();
            }
        } else {
            btnPlayPause.setImageResource(R.drawable.ic_play);
            tvStatus.setText("Paused");
            tvStatus.setTextColor(textSecondary);
            statusUnderline.setBackgroundColor(textSecondary);
            if (showToast) {
                Toast.makeText(this, "Robot đã tạm dừng (Paused)", Toast.LENGTH_SHORT).show();
            }
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
        btnCharge.setOnClickListener(v -> {
            isWorking = false;
            updateWorkingStateUI(false);
            tvStatus.setText("Đang sạc");
            Toast.makeText(this, "Robot đang quay về trạm sạc...", Toast.LENGTH_SHORT).show();
        });
    }
}
