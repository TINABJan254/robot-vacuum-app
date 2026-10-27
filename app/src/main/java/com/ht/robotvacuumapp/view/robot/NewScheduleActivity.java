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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.ht.robotvacuumapp.R;

import java.util.Locale;

public class NewScheduleActivity extends AppCompatActivity {

    public static final String EXTRA_RESULT_TIME = "extra_result_time";
    public static final String EXTRA_RESULT_MODE = "extra_result_mode";
    public static final String EXTRA_RESULT_REPEAT = "extra_result_repeat";

    private TextView btnCancel;
    private TextView btnSave;
    private View rowSetTime;
    private TextView tvSelectedTime;
    private View rowRepeat;
    private SwitchCompat switchRepeat;
    private View layoutRepeatDays;

    private TextView[] dayButtons = new TextView[7];
    private boolean[] selectedDays = new boolean[]{false, true, true, true, true, true, false}; // CN: 0, T2: 1, T3: 2, T4: 3, T5: 4, T6: 5, T7: 6

    private TextView btnSegAuto;
    private TextView btnSegRoom;
    private TextView btnSegArea;

    private int selectedHour = 8;
    private int selectedMinute = 0;
    private int selectedCleanOption = 0; // 0: Auto, 1: Room, 2: Area

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_new_schedule);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.new_schedule_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupActions();
        setupDaySelector();
        setupSegmentedControl();
    }

    private void initViews() {
        btnCancel = findViewById(R.id.btn_cancel);
        btnSave = findViewById(R.id.btn_save);
        rowSetTime = findViewById(R.id.row_set_time);
        tvSelectedTime = findViewById(R.id.tv_selected_time);
        rowRepeat = findViewById(R.id.row_repeat);
        switchRepeat = findViewById(R.id.switch_repeat);
        layoutRepeatDays = findViewById(R.id.layout_repeat_days);

        dayButtons[0] = findViewById(R.id.btn_day_cn);
        dayButtons[1] = findViewById(R.id.btn_day_t2);
        dayButtons[2] = findViewById(R.id.btn_day_t3);
        dayButtons[3] = findViewById(R.id.btn_day_t4);
        dayButtons[4] = findViewById(R.id.btn_day_t5);
        dayButtons[5] = findViewById(R.id.btn_day_t6);
        dayButtons[6] = findViewById(R.id.btn_day_t7);

        btnSegAuto = findViewById(R.id.btn_seg_auto);
        btnSegRoom = findViewById(R.id.btn_seg_room);
        btnSegArea = findViewById(R.id.btn_seg_area);

        updateTimeDisplay();
        updateDayViews();
    }

    private void setupActions() {
        btnCancel.setOnClickListener(v -> finish());

        rowSetTime.setOnClickListener(v -> showTimePickerBottomSheet());

        rowRepeat.setOnClickListener(v -> switchRepeat.setChecked(!switchRepeat.isChecked()));

        switchRepeat.setOnCheckedChangeListener((buttonView, isChecked) -> {
            layoutRepeatDays.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        btnSave.setOnClickListener(v -> saveAndFinish());
    }

    private void setupDaySelector() {
        for (int i = 0; i < 7; i++) {
            final int dayIndex = i;
            if (dayButtons[dayIndex] != null) {
                dayButtons[dayIndex].setOnClickListener(v -> {
                    selectedDays[dayIndex] = !selectedDays[dayIndex];
                    updateDayViews();
                });
            }
        }
    }

    private void updateDayViews() {
        int colorSelected = ContextCompat.getColor(this, R.color.white);
        int colorUnselected = ContextCompat.getColor(this, R.color.text_secondary);

        for (int i = 0; i < 7; i++) {
            if (dayButtons[i] != null) {
                if (selectedDays[i]) {
                    dayButtons[i].setBackgroundResource(R.drawable.bg_day_circle_selected);
                    dayButtons[i].setTextColor(colorSelected);
                } else {
                    dayButtons[i].setBackgroundResource(R.drawable.bg_day_circle_unselected);
                    dayButtons[i].setTextColor(colorUnselected);
                }
            }
        }
    }

    private void setupSegmentedControl() {
        btnSegAuto.setOnClickListener(v -> selectCleanOption(0));
        btnSegRoom.setOnClickListener(v -> selectCleanOption(1));
        btnSegArea.setOnClickListener(v -> selectCleanOption(2));
    }

    private void selectCleanOption(int index) {
        selectedCleanOption = index;
        int activeColor = ContextCompat.getColor(this, R.color.white);
        int inactiveColor = ContextCompat.getColor(this, R.color.icon_tint);

        btnSegAuto.setBackgroundResource(index == 0 ? R.drawable.bg_segmented_item_active : android.R.color.transparent);
        btnSegAuto.setTextColor(index == 0 ? activeColor : inactiveColor);
        btnSegAuto.getPaint().setFakeBoldText(index == 0);

        btnSegRoom.setBackgroundResource(index == 1 ? R.drawable.bg_segmented_item_active : android.R.color.transparent);
        btnSegRoom.setTextColor(index == 1 ? activeColor : inactiveColor);
        btnSegRoom.getPaint().setFakeBoldText(index == 1);

        btnSegArea.setBackgroundResource(index == 2 ? R.drawable.bg_segmented_item_active : android.R.color.transparent);
        btnSegArea.setTextColor(index == 2 ? activeColor : inactiveColor);
        btnSegArea.getPaint().setFakeBoldText(index == 2);
    }

    private void showTimePickerBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_time_picker, null);
        bottomSheetDialog.setContentView(sheetView);

        // Make background transparent so rounded top corners display properly
        if (sheetView.getParent() != null) {
            ((View) sheetView.getParent()).setBackgroundColor(Color.TRANSPARENT);
        }

        NumberPicker pickerHour = sheetView.findViewById(R.id.picker_hour);
        NumberPicker pickerMinute = sheetView.findViewById(R.id.picker_minute);
        TextView btnCancelTime = sheetView.findViewById(R.id.btn_cancel_time);
        TextView btnConfirmTime = sheetView.findViewById(R.id.btn_confirm_time);

        // Configure Hour Picker (00 - 23)
        pickerHour.setMinValue(0);
        pickerHour.setMaxValue(23);
        pickerHour.setValue(selectedHour);
        pickerHour.setFormatter(value -> String.format(Locale.getDefault(), "%02d", value));
        pickerHour.setWrapSelectorWheel(true);

        // Configure Minute Picker (00 - 59)
        pickerMinute.setMinValue(0);
        pickerMinute.setMaxValue(59);
        pickerMinute.setValue(selectedMinute);
        pickerMinute.setFormatter(value -> String.format(Locale.getDefault(), "%02d", value));
        pickerMinute.setWrapSelectorWheel(true);

        btnCancelTime.setOnClickListener(v -> bottomSheetDialog.dismiss());

        btnConfirmTime.setOnClickListener(v -> {
            selectedHour = pickerHour.getValue();
            selectedMinute = pickerMinute.getValue();
            updateTimeDisplay();
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    private void updateTimeDisplay() {
        String timeStr = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
        tvSelectedTime.setText(timeStr);
    }

    private void saveAndFinish() {
        String timeStr = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
        String modeStr;
        switch (selectedCleanOption) {
            case 1:
                modeStr = getString(R.string.clean_opt_room);
                break;
            case 2:
                modeStr = getString(R.string.clean_opt_area);
                break;
            case 0:
            default:
                modeStr = getString(R.string.schedule_mode_auto);
                break;
        }
        String repeatStr;
        if (!switchRepeat.isChecked()) {
            repeatStr = "Chỉ một lần";
        } else {
            int count = 0;
            for (boolean b : selectedDays) {
                if (b) count++;
            }
            if (count == 7) {
                repeatStr = "Hàng ngày";
            } else if (count == 5 && !selectedDays[0] && selectedDays[1] && selectedDays[2] && selectedDays[3] && selectedDays[4] && selectedDays[5] && !selectedDays[6]) {
                repeatStr = getString(R.string.schedule_days_workdays);
            } else if (count == 2 && selectedDays[0] && selectedDays[6]) {
                repeatStr = "Cuối tuần";
            } else if (count == 0) {
                repeatStr = "Chỉ một lần";
            } else {
                String[] dayNames = {"CN", "T2", "T3", "T4", "T5", "T6", "T7"};
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < 7; i++) {
                    if (selectedDays[i]) {
                        if (sb.length() > 0) sb.append(", ");
                        sb.append(dayNames[i]);
                    }
                }
                repeatStr = sb.toString();
            }
        }

        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_RESULT_TIME, timeStr);
        resultIntent.putExtra(EXTRA_RESULT_MODE, modeStr);
        resultIntent.putExtra(EXTRA_RESULT_REPEAT, repeatStr);
        setResult(RESULT_OK, resultIntent);

        Toast.makeText(this, "Đã lưu lịch dọn dẹp: " + timeStr, Toast.LENGTH_SHORT).show();
        finish();
    }
}
