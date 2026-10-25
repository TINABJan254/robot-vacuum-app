package com.ht.robotvacuumapp.view.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
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
import com.ht.robotvacuumapp.model.repository.DemoRobotRepository;
import com.ht.robotvacuumapp.view.auth.LoginActivity;
import com.ht.robotvacuumapp.view.robot.RobotControlActivity;
import com.ht.robotvacuumapp.viewmodel.home.MainUiState;
import com.ht.robotvacuumapp.viewmodel.home.MainViewModel;

public class MainActivity extends AppCompatActivity implements RobotAdapter.OnRobotClickListener {
    private MainViewModel viewModel;
    private RobotAdapter adapter;
    private View emptyState, devicesState, containerTabSpace;
    private ImageView imgTabRobot, imgTabSpace, imgTabMine;
    private TextView tvTabRobot, tvTabMine, tvDevicesHeader;
    private AlertDialog addRobotDialog;
    private EditText edtRobotId, edtRobotCode;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                return modelClass.cast(new MainViewModel(new AuthRepository(getApplicationContext()),
                        new DemoRobotRepository()));
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
        findViewById(R.id.btn_top_add).setOnClickListener(v -> showAddRobotDialog());
        findViewById(R.id.btn_empty_add_robot).setOnClickListener(v -> showAddRobotDialog());
        findViewById(R.id.btn_top_notification).setOnClickListener(v ->
                Toast.makeText(this, "Bạn không có thông báo mới", Toast.LENGTH_SHORT).show());
        findViewById(R.id.tv_sort).setOnClickListener(v -> showSampleOptions());
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
        boolean empty = state.robots.isEmpty();
        emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        devicesState.setVisibility(empty ? View.GONE : View.VISIBLE);
        tvDevicesHeader.setText(getString(R.string.header_online_devices, state.robots.size()));
        adapter.submitRobots(state.robots, state.selectedRobotId);
        renderTab(state.selectedTab);
        if (addRobotDialog != null) {
            edtRobotId.setError(state.robotIdError);
            edtRobotCode.setError(state.robotCodeError);
            if (state.robotIdError != null) edtRobotId.requestFocus();
            else if (state.robotCodeError != null) edtRobotCode.requestFocus();
            if (state.robotAdded) addRobotDialog.dismiss();
        }
        if (state.robotToOpen != null) {
            viewModel.acknowledgeFeedback();
            RobotDevice robot = state.robotToOpen;
            startActivity(new Intent(this, RobotControlActivity.class)
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

    private void showAddRobotDialog() {
        if (addRobotDialog != null) return;
        viewModel.acknowledgeFeedback();
        View content = LayoutInflater.from(this).inflate(R.layout.dialog_add_robot, null);
        edtRobotId = content.findViewById(R.id.edt_robot_id);
        edtRobotCode = content.findViewById(R.id.edt_robot_code);
        addRobotDialog = new AlertDialog.Builder(this).setView(content).create();
        addRobotDialog.setOnDismissListener(dialog -> {
            addRobotDialog = null;
            edtRobotId = null;
            edtRobotCode = null;
        });
        content.findViewById(R.id.btn_dialog_cancel).setOnClickListener(v -> addRobotDialog.dismiss());
        content.findViewById(R.id.btn_dialog_add).setOnClickListener(v ->
                viewModel.addRobot(edtRobotId.getText().toString(), edtRobotCode.getText().toString()));
        addRobotDialog.show();
        if (addRobotDialog.getWindow() != null) {
            addRobotDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    private void showSampleOptions() {
        new AlertDialog.Builder(this).setTitle("Tùy chọn hiển thị Home")
                .setItems(new CharSequence[]{"Xem giao diện khi chưa có robot (Xóa danh sách)",
                        "Khôi phục danh sách 3 robot mẫu", "Đóng"}, (dialog, which) -> {
                    if (which == 0) viewModel.clearRobots();
                    else if (which == 1) viewModel.restoreRobots();
                }).show();
    }

    private void openLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override public void onRobotClick(RobotDevice robot) { viewModel.selectRobot(robot); }
    @Override public void onAddRobotClick() { showAddRobotDialog(); }

    @Override protected void onDestroy() {
        if (addRobotDialog != null) addRobotDialog.dismiss();
        super.onDestroy();
    }
}
