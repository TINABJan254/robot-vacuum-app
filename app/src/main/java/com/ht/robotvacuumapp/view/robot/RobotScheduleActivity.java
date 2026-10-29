package com.ht.robotvacuumapp.view.robot;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

    private final ActivityResultLauncher<Intent> newScheduleLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    String timeStr = data.getStringExtra(NewScheduleActivity.EXTRA_RESULT_TIME);
                    String modeStr = data.getStringExtra(NewScheduleActivity.EXTRA_RESULT_MODE);
                    String repeatStr = data.getStringExtra(NewScheduleActivity.EXTRA_RESULT_REPEAT);

                    if (timeStr == null || timeStr.isEmpty()) timeStr = "12:04";
                    if (modeStr == null || modeStr.isEmpty()) modeStr = getString(R.string.schedule_mode_auto);
                    if (repeatStr == null || repeatStr.isEmpty()) repeatStr = getString(R.string.schedule_days_workdays);

                    ScheduleItem newItem = new ScheduleItem(
                            String.valueOf(System.currentTimeMillis()),
                            timeStr,
                            "GMT+07:00",
                            modeStr,
                            repeatStr,
                            true
                    );
                    scheduleList.add(newItem);
                    adapter.notifyItemInserted(scheduleList.size() - 1);
                    updateUIState();
                }
            }
    );

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
        adapter = new ScheduleAdapter(scheduleList, new ScheduleAdapter.OnScheduleActionListener() {
            @Override
            public void onToggle(ScheduleItem item, boolean isChecked, int position) {
                String status = isChecked ? "Bật" : "Tắt";
                Toast.makeText(RobotScheduleActivity.this, status + " lịch: " + item.getTime(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDelete(ScheduleItem item, int position) {
                if (position >= 0 && position < scheduleList.size()) {
                    String deletedTime = item.getTime();
                    scheduleList.remove(position);
                    adapter.notifyItemRemoved(position);
                    adapter.notifyItemRangeChanged(position, scheduleList.size() - position);
                    Toast.makeText(RobotScheduleActivity.this, "Đã xóa lịch: " + deletedTime, Toast.LENGTH_SHORT).show();
                    updateUIState();
                }
            }
        });
        rvSchedules.setAdapter(adapter);
    }

    private void setupActions() {
        btnBack.setOnClickListener(v -> finish());

        btnTopAdd.setOnClickListener(v -> openNewScheduleScreen());
        btnBottomAdd.setOnClickListener(v -> openNewScheduleScreen());
    }

    private void openNewScheduleScreen() {
        Intent intent = new Intent(this, NewScheduleActivity.class);
        newScheduleLauncher.launch(intent);
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
