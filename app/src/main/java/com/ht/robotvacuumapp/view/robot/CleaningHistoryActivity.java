package com.ht.robotvacuumapp.view.robot;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ht.robotvacuumapp.R;

import java.util.ArrayList;
import java.util.List;

public class CleaningHistoryActivity extends AppCompatActivity {

    public static final String EXTRA_ROBOT_NAME = "extra_robot_name";
    public static final String EXTRA_ROBOT_ID = "extra_robot_id";

    private ImageView btnBack;
    private TextView tvTotalTimes;
    private TextView tvTotalDuration;
    private RecyclerView rvCleaningHistory;
    private CleaningHistoryAdapter adapter;
    private final List<CleaningHistoryItem> historyList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cleaning_history);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.cleaning_history_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupRecyclerView();
        loadMockData();
        setupActions();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        tvTotalTimes = findViewById(R.id.tv_total_times);
        tvTotalDuration = findViewById(R.id.tv_total_duration);
        rvCleaningHistory = findViewById(R.id.rv_cleaning_history);
    }

    private void setupRecyclerView() {
        adapter = new CleaningHistoryAdapter(historyList, item ->
                Toast.makeText(this, item.getDateTime() + " - " + item.getMode(), Toast.LENGTH_SHORT).show()
        );
        rvCleaningHistory.setLayoutManager(new LinearLayoutManager(this));
        rvCleaningHistory.setAdapter(adapter);
    }

    private void loadMockData() {
        tvTotalTimes.setText("198");
        tvTotalDuration.setText("194");

        historyList.clear();
        historyList.add(new CleaningHistoryItem("09/02/2021 11:15", "AUTO Cleaning", "54min", CleaningHistoryItem.Status.SUCCESS));
        historyList.add(new CleaningHistoryItem("08/02/2021 16:24", "Custom Cleaning", "14min", CleaningHistoryItem.Status.CUSTOM_OR_WARNING));
        historyList.add(new CleaningHistoryItem("08/02/2021 11:15", "AUTO Cleaning", "69min", CleaningHistoryItem.Status.SUCCESS));
        historyList.add(new CleaningHistoryItem("07/02/2021 14:00", "AUTO Cleaning", "58min", CleaningHistoryItem.Status.SUCCESS));
        historyList.add(new CleaningHistoryItem("06/02/2021 14:00", "AUTO Cleaning", "59min", CleaningHistoryItem.Status.SUCCESS));

        adapter.notifyDataSetChanged();
    }

    private void setupActions() {
        btnBack.setOnClickListener(v -> finish());
    }
}
