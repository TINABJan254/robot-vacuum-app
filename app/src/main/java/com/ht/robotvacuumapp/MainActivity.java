package com.ht.robotvacuumapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements RobotAdapter.OnRobotClickListener {

    private ImageView btnTopAdd;
    private ImageView btnTopNotification;
    private View layoutEmptyState;
    private AppCompatButton btnEmptyAddRobot;
    private View layoutDevicesState;
    private TextView tvDevicesHeader;
    private TextView tvSort;
    private RecyclerView rvRobots;

    // Bottom Navigation
    private View tabRobot, tabSpace, tabMine;
    private ImageView imgTabRobot, imgTabSpace, imgTabMine;
    private TextView tvTabRobot, tvTabSpace, tvTabMine;

    private RobotAdapter robotAdapter;
    private final List<RobotDevice> robotList = new ArrayList<>();
    private int robotCounter = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupRecyclerView();
        setupBottomNavigation();
        setupHeaderActions();

        // Load initial devices as shown in the mockup image (Right side)
        loadSampleDevices();
        updateUIState();
    }

    private void initViews() {
        btnTopAdd = findViewById(R.id.btn_top_add);
        btnTopNotification = findViewById(R.id.btn_top_notification);
        layoutEmptyState = findViewById(R.id.layout_empty_state);
        btnEmptyAddRobot = findViewById(R.id.btn_empty_add_robot);
        layoutDevicesState = findViewById(R.id.layout_devices_state);
        tvDevicesHeader = findViewById(R.id.tv_devices_header);
        tvSort = findViewById(R.id.tv_sort);
        rvRobots = findViewById(R.id.rv_robots);

        // Bottom nav
        tabRobot = findViewById(R.id.tab_robot);
        tabSpace = findViewById(R.id.tab_space);
        tabMine = findViewById(R.id.tab_mine);
        imgTabRobot = findViewById(R.id.img_tab_robot);
        imgTabSpace = findViewById(R.id.img_tab_space);
        imgTabMine = findViewById(R.id.img_tab_mine);
        tvTabRobot = findViewById(R.id.tv_tab_robot);
        tvTabSpace = findViewById(R.id.tv_tab_space);
        tvTabMine = findViewById(R.id.tv_tab_mine);
    }

    private void setupRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        rvRobots.setLayoutManager(layoutManager);
        robotAdapter = new RobotAdapter(robotList, this);
        rvRobots.setAdapter(robotAdapter);
    }

    private void setupHeaderActions() {
        // Top '+' button: opens Add Robot dialog
        btnTopAdd.setOnClickListener(v -> showAddRobotDialog());

        // Empty state 'Thêm Robot' button: opens Add Robot dialog
        btnEmptyAddRobot.setOnClickListener(v -> showAddRobotDialog());

        // Top notification bell
        btnTopNotification.setOnClickListener(v -> {
            Toast.makeText(this, "Bạn không có thông báo mới", Toast.LENGTH_SHORT).show();
        });

        // Sort text click: Allows toggling or clearing list to test empty state vs grid state
        tvSort.setOnClickListener(v -> showSortOrStateDialog());
    }

    private void setupBottomNavigation() {
        tabRobot.setOnClickListener(v -> selectTab(0));
        tabSpace.setOnClickListener(v -> selectTab(1));
        tabMine.setOnClickListener(v -> selectTab(2));
    }

    private void selectTab(int index) {
        int colorActive = ContextCompat.getColor(this, R.color.primary_green);
        int colorInactive = ContextCompat.getColor(this, R.color.text_secondary);

        // Reset all tabs
        imgTabRobot.setColorFilter(colorInactive);
        tvTabRobot.setTextColor(colorInactive);
        imgTabSpace.setColorFilter(colorInactive);
        tvTabSpace.setTextColor(colorInactive);
        imgTabMine.setColorFilter(colorInactive);
        tvTabMine.setTextColor(colorInactive);

        switch (index) {
            case 0:
                imgTabRobot.setColorFilter(colorActive);
                tvTabRobot.setTextColor(colorActive);
                break;
            case 1:
                imgTabSpace.setColorFilter(colorActive);
                tvTabSpace.setTextColor(colorActive);
                Toast.makeText(this, "Màn hình Không gian / Bản đồ", Toast.LENGTH_SHORT).show();
                break;
            case 2:
                imgTabMine.setColorFilter(colorActive);
                tvTabMine.setTextColor(colorActive);
                Toast.makeText(this, "Màn hình Cá nhân (Mine)", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    /**
     * Loads the 3 sample robots shown in the right design
     */
    private void loadSampleDevices() {
        robotList.clear();

        // 1. Robot_01: Phòng khách - Sẵn sàng 83% - Active (Green outline)
        robotList.add(new RobotDevice(
                "1", "Robot_01", "Phòng khách", "Sẵn sàng", 83, true, R.drawable.ic_robot_card_circle_green
        ));

        // 2. Robot_02: Phòng bếp - Đang sạc 100% - Normal
        robotList.add(new RobotDevice(
                "2", "Robot_02", "Phòng bếp", "Đang sạc", 100, false, R.drawable.ic_robot_card_circle_gray
        ));

        // 3. Robot_03: Phòng ngủ 2 - Chờ 45% - Square
        robotList.add(new RobotDevice(
                "3", "Robot_03", "Phòng ngủ 2", "Chờ", 45, false, R.drawable.ic_robot_card_square_gray
        ));

        // 4. Last item: "+ Thêm Robot" card
        robotList.add(RobotDevice.createAddButtonItem());
    }

    /**
     * Updates UI visibility between Empty State (left) and Devices Grid State (right)
     */
    private void updateUIState() {
        int deviceCount = getDeviceCount();

        if (deviceCount == 0) {
            // State 1: Empty state (Left mockup)
            layoutEmptyState.setVisibility(View.VISIBLE);
            layoutDevicesState.setVisibility(View.GONE);
        } else {
            // State 2: Devices state (Right mockup)
            layoutEmptyState.setVisibility(View.GONE);
            layoutDevicesState.setVisibility(View.VISIBLE);
            tvDevicesHeader.setText(getString(R.string.header_online_devices, deviceCount));
        }

        if (robotAdapter != null) {
            robotAdapter.notifyDataSetChanged();
        }
    }

    private int getDeviceCount() {
        int count = 0;
        for (RobotDevice device : robotList) {
            if (!device.isAddButton()) {
                count++;
            }
        }
        return count;
    }

    private void showAddRobotDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_add_robot, null);

        EditText edtRobotId = dialogView.findViewById(R.id.edt_robot_id);
        EditText edtRobotCode = dialogView.findViewById(R.id.edt_robot_code);
        TextView btnCancel = dialogView.findViewById(R.id.btn_dialog_cancel);
        View btnAdd = dialogView.findViewById(R.id.btn_dialog_add);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnAdd.setOnClickListener(v -> {
            String robotId = edtRobotId.getText().toString().trim();
            String robotCode = edtRobotCode.getText().toString().trim();

            if (robotId.isEmpty()) {
                edtRobotId.setError("Vui lòng nhập ID robot");
                edtRobotId.requestFocus();
                return;
            }

            if (robotCode.isEmpty()) {
                edtRobotCode.setError("Vui lòng nhập mã code");
                edtRobotCode.requestFocus();
                return;
            }

            // Remove the AddButton before adding new device, then re-append AddButton
            if (!robotList.isEmpty() && robotList.get(robotList.size() - 1).isAddButton()) {
                robotList.remove(robotList.size() - 1);
            }

            robotCounter++;
            RobotDevice newDevice = new RobotDevice(
                    robotId,
                    robotId,
                    "Phòng mới",
                    "Sẵn sàng",
                    100,
                    false,
                    R.drawable.ic_robot_card_circle_gray
            );

            robotList.add(newDevice);
            robotList.add(RobotDevice.createAddButtonItem());

            updateUIState();
            dialog.dismiss();

            Toast.makeText(this, "Đã thêm thành công: " + robotId + " (Code: " + robotCode + ")", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }

    private void showSortOrStateDialog() {
        CharSequence[] options = {"Xem giao diện khi chưa có robot (Xóa danh sách)", "Khôi phục danh sách 3 robot mẫu", "Đóng"};
        new AlertDialog.Builder(this)
                .setTitle("Tùy chọn hiển thị Home")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        robotList.clear();
                        updateUIState();
                        Toast.makeText(this, "Đang hiển thị giao diện: Chưa có robot", Toast.LENGTH_SHORT).show();
                    } else if (which == 1) {
                        loadSampleDevices();
                        updateUIState();
                        Toast.makeText(this, "Đã khôi phục danh sách robot", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    @Override
    public void onRobotClick(RobotDevice robot, int position) {
        // Toggle selected active state
        for (RobotDevice item : robotList) {
            item.setActive(item.getId() != null && item.getId().equals(robot.getId()));
        }
        robotAdapter.notifyDataSetChanged();

        // Launch RobotControlActivity with selected robot details
        Intent intent = new Intent(this, RobotControlActivity.class);
        intent.putExtra(RobotControlActivity.EXTRA_ROBOT_NAME, robot.getName());
        intent.putExtra(RobotControlActivity.EXTRA_ROBOT_BATTERY, robot.getBatteryPercentage());
        intent.putExtra(RobotControlActivity.EXTRA_ROBOT_STATUS, robot.getStatus());
        startActivity(intent);
    }

    @Override
    public void onAddRobotClick() {
        showAddRobotDialog();
    }
}