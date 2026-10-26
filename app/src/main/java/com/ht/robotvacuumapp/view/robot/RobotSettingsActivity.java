package com.ht.robotvacuumapp.view.robot;

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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ht.robotvacuumapp.R;

public class RobotSettingsActivity extends AppCompatActivity {

    public static final String EXTRA_ROBOT_NAME = "extra_robot_name";
    public static final String EXTRA_ROBOT_ID = "extra_robot_id";

    private ImageView btnBack;
    private View rowRobotName;
    private TextView tvRobotName;
    private View rowHistory;
    private View rowSchedule;
    private View rowOtherSettings;
    private View rowDeviceInfo;
    private View btnDeleteDevice;

    private String currentRobotName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_robot_settings);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.settings_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        currentRobotName = getIntent().getStringExtra(EXTRA_ROBOT_NAME);
        if (currentRobotName == null || currentRobotName.trim().isEmpty()) {
            currentRobotName = "Robot vacuum - Phòng khách";
        }

        initViews();
        setupActions();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        rowRobotName = findViewById(R.id.row_robot_name);
        tvRobotName = findViewById(R.id.tv_robot_name);
        rowHistory = findViewById(R.id.row_history);
        rowSchedule = findViewById(R.id.row_schedule);
        rowOtherSettings = findViewById(R.id.row_other_settings);
        rowDeviceInfo = findViewById(R.id.row_device_info);
        btnDeleteDevice = findViewById(R.id.btn_delete_device);

        tvRobotName.setText(currentRobotName);
    }

    private void setupActions() {
        btnBack.setOnClickListener(v -> finish());

        rowRobotName.setOnClickListener(v -> showEditRobotNameDialog());

        rowHistory.setOnClickListener(v ->
                Toast.makeText(this, "Nhật ký hoạt động", Toast.LENGTH_SHORT).show()
        );

        rowSchedule.setOnClickListener(v ->
                Toast.makeText(this, "Đặt lịch dọn dẹp", Toast.LENGTH_SHORT).show()
        );

        rowOtherSettings.setOnClickListener(v ->
                Toast.makeText(this, "Cài đặt khác", Toast.LENGTH_SHORT).show()
        );

        rowDeviceInfo.setOnClickListener(v ->
                Toast.makeText(this, "Thông tin thiết bị", Toast.LENGTH_SHORT).show()
        );

        btnDeleteDevice.setOnClickListener(v -> showDeleteConfirmDialog());
    }

    private void showEditRobotNameDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_edit_robot_name, null);
        EditText edtNewName = dialogView.findViewById(R.id.edt_new_robot_name);
        edtNewName.setText(tvRobotName.getText());
        edtNewName.setSelection(edtNewName.getText().length());

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_edit_robot_name_title)
                .setView(dialogView)
                .setPositiveButton(R.string.action_save, (dialog, which) -> {
                    String newName = edtNewName.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        currentRobotName = newName;
                        tvRobotName.setText(newName);
                        Toast.makeText(this, "Đã đổi tên thành: " + newName, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void showDeleteConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.setting_delete_device)
                .setMessage("Bạn có chắc chắn muốn xóa thiết bị này không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    Toast.makeText(this, "Đã yêu cầu xóa thiết bị", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
