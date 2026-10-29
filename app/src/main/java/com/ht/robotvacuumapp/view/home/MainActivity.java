package com.ht.robotvacuumapp.view.home;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ht.robotvacuumapp.R;
import com.ht.robotvacuumapp.model.entity.RobotDevice;
import com.ht.robotvacuumapp.model.repository.AuthRepository;
import com.ht.robotvacuumapp.model.repository.RobotRepository;
import com.ht.robotvacuumapp.view.auth.LoginActivity;
import com.ht.robotvacuumapp.view.robot.RobotControlActivity;
import com.ht.robotvacuumapp.viewmodel.home.MainUiState;
import com.ht.robotvacuumapp.viewmodel.home.MainViewModel;

public class MainActivity extends AppCompatActivity implements RobotAdapter.OnRobotClickListener {
    private MainViewModel viewModel;
    private RobotAdapter adapter;
    private View emptyState, devicesState, loadState, loadProgress, retryRobots, containerTabSpace;
    private ImageView imgTabRobot, imgTabSpace, imgTabMine;
    private TextView tvTabRobot, tvTabMine, tvDevicesHeader, tvRefresh, tvListError, tvLoadMessage;
    private AlertDialog addRobotDialog;
    private EditText edtRobotId, edtRobotCode;
    private TextView tvPairError, btnPair, btnCancel;
    private boolean syncingDraft;
    private boolean navigatingToLogin;

    private final ActivityResultLauncher<Intent> controlLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String id = result.getData().getStringExtra(RobotControlActivity.EXTRA_ROBOT_ID);
                    if (result.getData().getBooleanExtra(RobotControlActivity.EXTRA_ROBOT_UNPAIRED, false)) {
                        viewModel.onRobotUnpaired(id);
                    } else {
                        viewModel.onRobotRenamed(id, result.getData().getStringExtra(RobotControlActivity.EXTRA_ROBOT_NAME));
                    }
                }
            });

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                return modelClass.cast(new MainViewModel(new AuthRepository(getApplicationContext()),
                        new RobotRepository(getApplicationContext())));
            }
        }).get(MainViewModel.class);
        if (viewModel.getUiState().getValue().requiresLogin) {
            openLogin();
            return;
        }
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_root), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        emptyState = findViewById(R.id.layout_empty_state);
        devicesState = findViewById(R.id.layout_devices_state);
        loadState = findViewById(R.id.layout_robot_load_state);
        loadProgress = findViewById(R.id.progress_robot_list);
        retryRobots = findViewById(R.id.btn_retry_robots);
        tvLoadMessage = findViewById(R.id.tv_robot_load_message);
        tvListError = findViewById(R.id.tv_list_error);
        tvRefresh = findViewById(R.id.tv_sort);
        tvDevicesHeader = findViewById(R.id.tv_devices_header);
        containerTabSpace = findViewById(R.id.container_tab_space);
        imgTabRobot = findViewById(R.id.img_tab_robot);
        imgTabSpace = findViewById(R.id.img_tab_space);
        imgTabMine = findViewById(R.id.img_tab_mine);
        tvTabRobot = findViewById(R.id.tv_tab_robot);
        tvTabMine = findViewById(R.id.tv_tab_mine);
        RecyclerView robots = findViewById(R.id.rv_robots);
        robots.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new RobotAdapter(this);
        robots.setAdapter(adapter);
        findViewById(R.id.btn_top_add).setOnClickListener(v -> viewModel.openPairDialog());
        findViewById(R.id.btn_empty_add_robot).setOnClickListener(v -> viewModel.openPairDialog());
        findViewById(R.id.btn_top_notification).setOnClickListener(v ->
                startActivity(new Intent(this, com.ht.robotvacuumapp.view.notification.NotificationActivity.class)));
        tvRefresh.setOnClickListener(v -> viewModel.loadRobots());
        retryRobots.setOnClickListener(v -> viewModel.loadRobots());
        findViewById(R.id.tab_robot).setOnClickListener(v -> viewModel.selectTab(0));
        findViewById(R.id.tab_space).setOnClickListener(v -> viewModel.selectTab(1));
        findViewById(R.id.tab_mine).setOnClickListener(v -> {
            viewModel.selectTab(2);
            showAccount();
        });
        viewModel.getUiState().observe(this, this::render);
    }

    private void render(MainUiState state) {
        if (state.requiresLogin) {
            openLogin();
            return;
        }
        boolean hasDevices = !state.robots.isEmpty();
        boolean showLoadState = !hasDevices
                && (state.isLoadingRobots || state.listError != null || !state.robotsLoaded);
        emptyState.setVisibility(!hasDevices && !showLoadState ? View.VISIBLE : View.GONE);
        devicesState.setVisibility(hasDevices ? View.VISIBLE : View.GONE);
        loadState.setVisibility(showLoadState ? View.VISIBLE : View.GONE);
        loadProgress.setVisibility(state.isLoadingRobots ? View.VISIBLE : View.GONE);
        tvLoadMessage.setText(state.listError != null ? state.listError : getString(R.string.loading_robots));
        retryRobots.setVisibility(state.listError != null ? View.VISIBLE : View.GONE);
        retryRobots.setEnabled(!state.isLoadingRobots && !state.isPairing);
        tvDevicesHeader.setText(getString(R.string.header_online_devices, state.robots.size()));
        tvRefresh.setEnabled(!state.isLoadingRobots && !state.isPairing);
        tvRefresh.setText(state.isLoadingRobots ? R.string.loading_robots : R.string.action_refresh);
        tvListError.setText(state.listError);
        tvListError.setVisibility(state.listError == null ? View.GONE : View.VISIBLE);
        adapter.submitRobots(state.robots, state.selectedRobotId);
        renderTab(state.selectedTab);
        renderPairDialog(state);
        if (state.robotToOpen != null) {
            viewModel.acknowledgeFeedback();
            RobotDevice robot = state.robotToOpen;
            controlLauncher.launch(new Intent(this, RobotControlActivity.class)
                    .putExtra(RobotControlActivity.EXTRA_ROBOT_ID, robot.getId())
                    .putExtra(RobotControlActivity.EXTRA_ROBOT_NAME, robot.getName())
                    .putExtra(RobotControlActivity.EXTRA_ROBOT_BATTERY, robot.getBatteryPercentage())
                    .putExtra(RobotControlActivity.EXTRA_ROBOT_STATUS, robot.getStatus()));
        } else if (state.message != null) {
            viewModel.acknowledgeFeedback();
            Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show();
        }
    }

    private void renderTab(int selected) {
        int active = ContextCompat.getColor(this, R.color.primary_green);
        int inactive = ContextCompat.getColor(this, R.color.text_secondary);
        imgTabRobot.setColorFilter(selected == 0 ? active : inactive);
        tvTabRobot.setTextColor(selected == 0 ? active : inactive);
        imgTabSpace.setColorFilter(selected == 1 ? active : inactive);
        containerTabSpace.setBackgroundResource(selected == 1
                ? R.drawable.bg_nav_center_cube_active : R.drawable.bg_nav_center_cube_inactive);
        imgTabMine.setColorFilter(selected == 2 ? active : inactive);
        tvTabMine.setTextColor(selected == 2 ? active : inactive);
    }

    private void showAccount() {
        String name = viewModel.getUserName();
        new AlertDialog.Builder(this)
                .setTitle("Tài khoản người dùng")
                .setMessage("Họ tên: " + (name != null && !name.isEmpty() ? name : "Người dùng")
                        + "\nTên đăng nhập: " + viewModel.getUsername() + "\nVai trò: " + viewModel.getUserRole())
                .setPositiveButton("Đăng xuất", (dialog, which) -> viewModel.logout())
                .setNegativeButton("Đóng", null).show();
    }

    private void renderPairDialog(MainUiState state) {
        if (!state.pairDialogOpen) {
            dismissPairDialog();
            return;
        }
        if (addRobotDialog == null) createPairDialog(state);
        syncingDraft = true;
        if (!edtRobotId.getText().toString().equals(state.draftRobotId)) edtRobotId.setText(state.draftRobotId);
        if (!edtRobotCode.getText().toString().equals(state.draftPairCode)) edtRobotCode.setText(state.draftPairCode);
        syncingDraft = false;
        edtRobotId.setEnabled(!state.isPairing);
        edtRobotCode.setEnabled(!state.isPairing);
        edtRobotId.setError(state.robotIdError);
        edtRobotCode.setError(state.robotCodeError);
        tvPairError.setText(state.pairError);
        tvPairError.setVisibility(state.pairError == null ? View.GONE : View.VISIBLE);
        btnPair.setEnabled(!state.isPairing && !state.isLoadingRobots);
        btnPair.setText(state.isPairing ? R.string.pairing_robot
                : state.isLoadingRobots ? R.string.loading_robots : R.string.action_pair);
        btnCancel.setEnabled(!state.isPairing);
        addRobotDialog.setCancelable(!state.isPairing);
        addRobotDialog.setCanceledOnTouchOutside(!state.isPairing);
    }

    private void createPairDialog(MainUiState state) {
        View content = LayoutInflater.from(this).inflate(R.layout.dialog_add_robot, null);
        edtRobotId = content.findViewById(R.id.edt_robot_id);
        edtRobotCode = content.findViewById(R.id.edt_robot_code);
        tvPairError = content.findViewById(R.id.tv_pair_error);
        btnPair = content.findViewById(R.id.btn_dialog_add);
        btnCancel = content.findViewById(R.id.btn_dialog_cancel);
        edtRobotId.setText(state.draftRobotId);
        edtRobotCode.setText(state.draftPairCode);
        TextWatcher draftWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!syncingDraft) viewModel.updatePairDraft(edtRobotId.getText().toString(), edtRobotCode.getText().toString());
            }
            @Override public void afterTextChanged(Editable s) { }
        };
        edtRobotId.addTextChangedListener(draftWatcher);
        edtRobotCode.addTextChangedListener(draftWatcher);
        addRobotDialog = new AlertDialog.Builder(this).setView(content).create();
        addRobotDialog.setOnCancelListener(dialog -> viewModel.closePairDialog());
        btnCancel.setOnClickListener(v -> viewModel.closePairDialog());
        btnPair.setOnClickListener(v -> {
            viewModel.pairRobot();
            // Focus is a presentation concern; don't move focus on every draft update.
            MainUiState result = viewModel.getUiState().getValue();
            if (edtRobotId != null && result.robotIdError != null) edtRobotId.requestFocus();
            else if (edtRobotCode != null && result.robotCodeError != null) edtRobotCode.requestFocus();
        });
        addRobotDialog.show();
        if (addRobotDialog.getWindow() != null) {
            addRobotDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    private void dismissPairDialog() {
        if (addRobotDialog != null) {
            addRobotDialog.setOnCancelListener(null);
            addRobotDialog.dismiss();
            addRobotDialog = null;
            edtRobotId = null;
            edtRobotCode = null;
        }
    }

    private void openLogin() {
        if (navigatingToLogin) return;
        navigatingToLogin = true;
        dismissPairDialog();
        String message = viewModel.getUiState().getValue().message;
        if (message != null) Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override public void onRobotClick(RobotDevice robot) { viewModel.selectRobot(robot); }
    @Override public void onAddRobotClick() { viewModel.openPairDialog(); }

    @Override protected void onDestroy() {
        dismissPairDialog();
        super.onDestroy();
    }
}
