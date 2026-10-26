package com.ht.robotvacuumapp.view.robot;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ht.robotvacuumapp.R;

import java.util.ArrayList;
import java.util.List;

public class RobotScheduleActivity extends AppCompatActivity {

    public static final String EXTRA_ROBOT_NAME = "extra_robot_name";
    public static final String EXTRA_ROBOT_ID = "extra_robot_id";

    private ImageView btnBack;
    private ImageView btnTopAdd;
    private View layoutListState;
    private View layoutEmptyState;
    private RecyclerView rvSchedules;
    private AppCompatButton btnBottomAdd;

    private ScheduleAdapter adapter;
    private final List<ScheduleItem> scheduleList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_robot_schedule);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.schedule_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupRecyclerView();
        setupActions();

        // Load sample schedule shown in design mockup
        loadSampleSchedules();
        updateUIState();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        btnTopAdd = findViewById(R.id.btn_top_add);
        layoutListState = findViewById(R.id.layout_list_state);
        layoutEmptyState = findViewById(R.id.layout_empty_state);
        rvSchedules = findViewById(R.id.rv_schedules);
        btnBottomAdd = findViewById(R.id.btn_bottom_add);
    }

    private void setupRecyclerView() {
        rvSchedules.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ScheduleAdapter(scheduleList, (item, isChecked, position) -> {
            String status = isChecked ? "Bật" : "Tắt";
            Toast.makeText(this, status + " lịch: " + item.getTime(), Toast.LENGTH_SHORT).show();
        });
        rvSchedules.setAdapter(adapter);
    }

    private void setupActions() {
        btnBack.setOnClickListener(v -> finish());

        btnTopAdd.setOnClickListener(v -> addNewSchedule());
        btnBottomAdd.setOnClickListener(v -> addNewSchedule());
    }

    private void loadSampleSchedules() {
        scheduleList.clear();
        // Add sample schedule: 08:00 - Dọn dẹp tự động - Ngày làm việc
        scheduleList.add(new ScheduleItem(
                "1",
                "08:00",
                "GMT+07:00",
                getString(R.string.schedule_mode_auto),
                getString(R.string.schedule_days_workdays),
                true
        ));
    }

    private void addNewSchedule() {
        int nextHour = 8 + scheduleList.size();
        String timeStr = String.format("%02d:00", (nextHour % 24));
        ScheduleItem newItem = new ScheduleItem(
                String.valueOf(System.currentTimeMillis()),
                timeStr,
                "GMT+07:00",
                getString(R.string.schedule_mode_auto),
                getString(R.string.schedule_days_workdays),
                true
        );
        scheduleList.add(newItem);
        adapter.notifyItemInserted(scheduleList.size() - 1);
        updateUIState();
        Toast.makeText(this, "Đã thêm lịch làm việc: " + timeStr, Toast.LENGTH_SHORT).show();
    }

    private void updateUIState() {
        if (scheduleList.isEmpty()) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            layoutListState.setVisibility(View.GONE);
            btnTopAdd.setVisibility(View.GONE);
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            layoutListState.setVisibility(View.VISIBLE);
            btnTopAdd.setVisibility(View.VISIBLE);
        }
    }
}
