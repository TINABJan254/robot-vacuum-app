package com.ht.robotvacuumapp.view.robot;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.ht.robotvacuumapp.R;
import com.ht.robotvacuumapp.model.repository.AuthRepository;
import com.ht.robotvacuumapp.model.repository.ScheduleRepository;
import com.ht.robotvacuumapp.view.auth.LoginActivity;
import com.ht.robotvacuumapp.viewmodel.robot.NewScheduleUiState;
import com.ht.robotvacuumapp.viewmodel.robot.NewScheduleViewModel;
import java.util.Locale;

public class NewScheduleActivity extends AppCompatActivity {
    public static final String EXTRA_ROBOT_ID = "extra_robot_id";
    public static final String EXTRA_RESULT_ROBOT_ID = "extra_result_robot_id";
    public static final String EXTRA_CREATED_SCHEDULE_ID = "extra_created_schedule_id";
    private NewScheduleViewModel viewModel;
    private TextView save, cancel, time, error;
    private View rowTime, rowRepeat, repeatDays;
    private SwitchCompat repeat;
    private final TextView[] dayButtons = new TextView[7];
    private final int[] dayValues = {7, 1, 2, 3, 4, 5, 6};
    private boolean syncing, finishingAfterSave, navigatingToLogin;
    private BottomSheetDialog timePicker;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_new_schedule);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.new_schedule_root), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        viewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> type) {
                return type.cast(new NewScheduleViewModel(getIntent().getStringExtra(EXTRA_ROBOT_ID),
                        new ScheduleRepository(getApplicationContext()), new AuthRepository(getApplicationContext())));
            }
        }).get(NewScheduleViewModel.class);
        save = findViewById(R.id.btn_save); cancel = findViewById(R.id.btn_cancel);
        time = findViewById(R.id.tv_selected_time); error = findViewById(R.id.schedule_form_error);
        rowTime = findViewById(R.id.row_set_time); rowRepeat = findViewById(R.id.row_repeat);
        repeat = findViewById(R.id.switch_repeat); repeatDays = findViewById(R.id.layout_repeat_days);
        int[] ids = {R.id.btn_day_cn, R.id.btn_day_t2, R.id.btn_day_t3, R.id.btn_day_t4, R.id.btn_day_t5, R.id.btn_day_t6, R.id.btn_day_t7};
        for (int i = 0; i < ids.length; i++) {
            dayButtons[i] = findViewById(ids[i]);
            final int day = dayValues[i];
            dayButtons[i].setOnClickListener(v -> viewModel.toggleDay(day));
        }
        cancel.setOnClickListener(v -> finish());
        save.setOnClickListener(v -> viewModel.save());
        rowTime.setOnClickListener(v -> showTimePicker());
        rowRepeat.setOnClickListener(v -> viewModel.setRepeat(!viewModel.getUiState().getValue().repeat));
        repeat.setOnCheckedChangeListener((button, checked) -> { if (!syncing) viewModel.setRepeat(checked); });
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() { if (!viewModel.getUiState().getValue().isSaving) finish(); }
        });
        viewModel.getUiState().observe(this, this::render);
    }
    private void render(NewScheduleUiState s) {
        if (s.requiresLogin) {
            if (!navigatingToLogin) {
                navigatingToLogin = true;
                if (s.error != null) Toast.makeText(this, s.error, Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
            }
            return;
        }
        if (s.createdScheduleId != null) {
            if (!finishingAfterSave) {
                finishingAfterSave = true;
                setResult(RESULT_OK, new Intent().putExtra(EXTRA_RESULT_ROBOT_ID, s.robotId.longValue())
                        .putExtra(EXTRA_CREATED_SCHEDULE_ID, s.createdScheduleId.longValue()));
                finish();
            }
            return;
        }
        boolean editable = s.robotId != null && !s.isSaving;
        time.setText(String.format(Locale.getDefault(), "%02d:%02d", s.hour, s.minute));
        save.setEnabled(editable);
        save.setText(s.isSaving ? R.string.schedule_saving : R.string.action_save_schedule);
        cancel.setEnabled(!s.isSaving);
        rowTime.setEnabled(editable); rowRepeat.setEnabled(editable); repeat.setEnabled(editable);
        syncing = true; repeat.setChecked(s.repeat); syncing = false;
        repeatDays.setVisibility(s.repeat ? View.VISIBLE : View.GONE);
        for (int i = 0; i < dayButtons.length; i++) {
            boolean selected = s.days.contains(dayValues[i]);
            dayButtons[i].setEnabled(editable);
            dayButtons[i].setBackgroundResource(selected ? R.drawable.bg_day_circle_selected : R.drawable.bg_day_circle_unselected);
            dayButtons[i].setTextColor(ContextCompat.getColor(this, selected ? R.color.white : R.color.text_secondary));
        }
        error.setText(s.error);
        error.setVisibility(s.error == null ? View.GONE : View.VISIBLE);
    }
    private void showTimePicker() {
        NewScheduleUiState s = viewModel.getUiState().getValue();
        if (s.isSaving || s.robotId == null || (timePicker != null && timePicker.isShowing())) return;
        timePicker = new BottomSheetDialog(this);
        View content = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_time_picker, null);
        timePicker.setContentView(content);
        if (content.getParent() instanceof View) ((View) content.getParent()).setBackgroundColor(Color.TRANSPARENT);
        NumberPicker hour = content.findViewById(R.id.picker_hour), minute = content.findViewById(R.id.picker_minute);
        hour.setMinValue(0); hour.setMaxValue(23); hour.setValue(s.hour);
        minute.setMinValue(0); minute.setMaxValue(59); minute.setValue(s.minute);
        hour.setFormatter(value -> String.format(Locale.getDefault(), "%02d", value));
        minute.setFormatter(value -> String.format(Locale.getDefault(), "%02d", value));
        hour.setWrapSelectorWheel(true); minute.setWrapSelectorWheel(true);
        content.findViewById(R.id.btn_cancel_time).setOnClickListener(v -> timePicker.dismiss());
        content.findViewById(R.id.btn_confirm_time).setOnClickListener(v -> {
            hour.clearFocus(); minute.clearFocus();
            viewModel.setTime(hour.getValue(), minute.getValue());
            timePicker.dismiss();
        });
        timePicker.show();
    }
    @Override protected void onDestroy() { if (timePicker != null) timePicker.dismiss(); super.onDestroy(); }
}
