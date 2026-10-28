package com.ht.robotvacuumapp.view.robot;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.ht.robotvacuumapp.R;
import com.ht.robotvacuumapp.model.repository.AuthRepository;
import com.ht.robotvacuumapp.model.repository.RobotRepository;
import com.ht.robotvacuumapp.view.auth.LoginActivity;
import com.ht.robotvacuumapp.viewmodel.robot.DeviceInfoUiState;
import com.ht.robotvacuumapp.viewmodel.robot.DeviceInfoViewModel;

public class DeviceInfoActivity extends AppCompatActivity {

    public static final String EXTRA_DEVICE_NAME = "extra_device_name";
    public static final String EXTRA_DEVICE_ID = "extra_device_id";

    private ImageView btnBack;
    private TextView tvDeviceName;
    private TextView tvDeviceId;
    private TextView tvDeviceCode;
    private TextView tvDeviceSerial;
    private View rowServiceHistory;
    private View feedback, progress, retry;
    private TextView tvFeedback;
    private DeviceInfoViewModel viewModel;
    private boolean navigatingToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_device_info);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.device_info_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        viewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                return modelClass.cast(new DeviceInfoViewModel(getIntent().getStringExtra(EXTRA_DEVICE_ID),
                        getIntent().getStringExtra(EXTRA_DEVICE_NAME), new RobotRepository(getApplicationContext()),
                        new AuthRepository(getApplicationContext())));
            }
        }).get(DeviceInfoViewModel.class);
        setupActions();
        viewModel.getUiState().observe(this, this::render);
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        tvDeviceName = findViewById(R.id.tv_device_name);
        tvDeviceId = findViewById(R.id.tv_device_id);
        tvDeviceCode = findViewById(R.id.tv_device_code);
        tvDeviceSerial = findViewById(R.id.tv_device_serial);
        rowServiceHistory = findViewById(R.id.row_service_history);
        feedback = findViewById(R.id.layout_device_info_feedback);
        progress = findViewById(R.id.progress_device_info);
        retry = findViewById(R.id.btn_retry_device_info);
        tvFeedback = findViewById(R.id.tv_device_info_feedback);
        tvDeviceSerial.setText(R.string.device_info_unknown);
    }

    private void render(DeviceInfoUiState state) {
        if (state.requiresLogin) {
            if (!navigatingToLogin) {
                navigatingToLogin = true;
                if (state.error != null) Toast.makeText(this, state.error, Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, LoginActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
            }
            return;
        }
        tvDeviceName.setText(displayValue(state.robotName));
        tvDeviceId.setText(displayValue(state.robotId));
        tvDeviceCode.setText(displayValue(state.pairCode));
        feedback.setVisibility(state.isLoading || state.error != null ? View.VISIBLE : View.GONE);
        progress.setVisibility(state.isLoading ? View.VISIBLE : View.GONE);
        tvFeedback.setText(state.error != null ? state.error : getString(R.string.device_info_loading));
        retry.setVisibility(state.error != null && state.robotId != null ? View.VISIBLE : View.GONE);
        retry.setEnabled(!state.isLoading);
    }

    private String displayValue(String value) {
        return value == null || value.trim().isEmpty() ? getString(R.string.device_info_unknown) : value;
    }

    private void setupActions() {
        btnBack.setOnClickListener(v -> finish());
        retry.setOnClickListener(v -> viewModel.loadRobot());

        rowServiceHistory.setOnClickListener(v ->
                Toast.makeText(this, getString(R.string.device_info_history_service), Toast.LENGTH_SHORT).show()
        );
    }
}
