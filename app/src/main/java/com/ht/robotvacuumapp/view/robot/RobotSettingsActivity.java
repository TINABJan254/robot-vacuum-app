package com.ht.robotvacuumapp.view.robot;

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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import com.ht.robotvacuumapp.viewmodel.robot.RobotSettingsUiState;
import com.ht.robotvacuumapp.viewmodel.robot.RobotSettingsViewModel;

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

    private RobotSettingsViewModel viewModel;
    private AlertDialog renameDialog;
    private EditText edtNewName;
    private TextView tvRenameError;
    private boolean syncingDraft;
    private boolean navigatingToLogin;

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

        initViews();
        viewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                return modelClass.cast(new RobotSettingsViewModel(
                        getIntent().getStringExtra(EXTRA_ROBOT_ID), getIntent().getStringExtra(EXTRA_ROBOT_NAME),
                        new RobotRepository(getApplicationContext()), new AuthRepository(getApplicationContext())));
            }
        }).get(RobotSettingsViewModel.class);
        setupActions();
        viewModel.getUiState().observe(this, this::render);
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

    }

    private void setupActions() {
        btnBack.setOnClickListener(v -> finish());

        rowRobotName.setOnClickListener(v -> viewModel.openRenameDialog());

        rowHistory.setOnClickListener(v ->
                Toast.makeText(this, "Nhật ký hoạt động", Toast.LENGTH_SHORT).show()
        );

        rowSchedule.setOnClickListener(v -> {
            Intent intent = new Intent(this, RobotScheduleActivity.class);
            intent.putExtra(RobotScheduleActivity.EXTRA_ROBOT_NAME, viewModel.getUiState().getValue().robotName);
            startActivity(intent);
        });

        rowOtherSettings.setOnClickListener(v ->
                Toast.makeText(this, "Cài đặt khác", Toast.LENGTH_SHORT).show()
        );

        rowDeviceInfo.setOnClickListener(v ->
                Toast.makeText(this, "Thông tin thiết bị", Toast.LENGTH_SHORT).show()
        );

        btnDeleteDevice.setOnClickListener(v -> showDeleteConfirmDialog());
    }

    private void createRenameDialog(RobotSettingsUiState state) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_edit_robot_name, null);
        edtNewName = dialogView.findViewById(R.id.edt_new_robot_name);
        tvRenameError = dialogView.findViewById(R.id.tv_rename_error);
        edtNewName.setText(state.draftName);
        edtNewName.setSelection(edtNewName.getText().length());
        edtNewName.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!syncingDraft) viewModel.updateDraft(s.toString());
            }
            @Override public void afterTextChanged(Editable s) { }
        });
        renameDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_edit_robot_name_title)
                .setView(dialogView)
                .setPositiveButton(R.string.action_save, null)
                .setNegativeButton(R.string.action_cancel, null)
                .create();
        renameDialog.setOnCancelListener(dialog -> viewModel.closeRenameDialog());
        renameDialog.show();
        renameDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            viewModel.saveName();
            if (edtNewName != null && viewModel.getUiState().getValue().nameError != null) edtNewName.requestFocus();
        });
        renameDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> viewModel.closeRenameDialog());
    }

    private void render(RobotSettingsUiState state) {
        if (state.requiresLogin) {
            openLogin(state.message);
            return;
        }
        tvRobotName.setText(state.robotName);
        rowRobotName.setEnabled(!state.isSaving && viewModel.hasValidRobotId());
        for (View action : new View[]{btnBack, rowHistory, rowSchedule, rowOtherSettings, rowDeviceInfo, btnDeleteDevice}) {
            action.setEnabled(!state.isSaving);
        }
        if (state.updatedRobot != null) {
            setResult(RESULT_OK, new Intent().putExtra(EXTRA_ROBOT_ID, state.updatedRobot.getId())
                    .putExtra(EXTRA_ROBOT_NAME, state.updatedRobot.getName()));
        }
        if (state.dialogOpen) {
            if (renameDialog == null) createRenameDialog(state);
            syncingDraft = true;
            if (!edtNewName.getText().toString().equals(state.draftName)) edtNewName.setText(state.draftName);
            syncingDraft = false;
            edtNewName.setEnabled(!state.isSaving);
            edtNewName.setError(state.nameError);
            tvRenameError.setText(state.requestError);
            tvRenameError.setVisibility(state.requestError == null ? View.GONE : View.VISIBLE);
            renameDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(!state.isSaving && viewModel.hasValidRobotId());
            renameDialog.getButton(AlertDialog.BUTTON_POSITIVE).setText(state.isSaving ? R.string.saving_robot_name : R.string.action_save);
            renameDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(!state.isSaving);
            renameDialog.setCancelable(!state.isSaving);
            renameDialog.setCanceledOnTouchOutside(!state.isSaving);
        } else {
            dismissRenameDialog();
        }
        if (state.message != null) {
            viewModel.acknowledgeFeedback();
            Toast.makeText(this, state.message, Toast.LENGTH_LONG).show();
        }
    }

    private void dismissRenameDialog() {
        if (renameDialog == null) return;
        renameDialog.setOnCancelListener(null);
        renameDialog.dismiss();
        renameDialog = null;
        edtNewName = null;
        tvRenameError = null;
    }

    private void openLogin(String message) {
        if (navigatingToLogin) return;
        navigatingToLogin = true;
        dismissRenameDialog();
        if (message != null) Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        startActivity(new Intent(this, LoginActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }

    @Override protected void onDestroy() {
        dismissRenameDialog();
        super.onDestroy();
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
