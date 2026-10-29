package com.ht.robotvacuumapp.view.notification;

import android.os.Bundle;
import android.widget.ImageView;
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

public class NotificationActivity extends AppCompatActivity {

    private ImageView btnBack;
    private RecyclerView rvNotifications;
    private NotificationAdapter adapter;
    private final List<NotificationItem> notificationList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notification);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.notification_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupRecyclerView();
        loadMockNotifications();
        setupActions();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        rvNotifications = findViewById(R.id.rv_notifications);
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter(notificationList, (item, position) -> {
            if (item.isUnread()) {
                item.setUnread(false);
                adapter.notifyItemChanged(position);
            }
            Toast.makeText(this, item.getTitle() + " (" + item.getDeviceName() + ")", Toast.LENGTH_SHORT).show();
        });
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        rvNotifications.setAdapter(adapter);
    }

    private void loadMockNotifications() {
        notificationList.clear();
        notificationList.add(new NotificationItem("Hoàn thành làm sạch", "ROBOT PHÒNG KHÁCH", "10:56", "2020/10/19", true));
        notificationList.add(new NotificationItem("Tác vụ theo lịch trình bắt đầu", "ROBOT PHÒNG KHÁCH", "10:14", "2020/10/19", true));
        notificationList.add(new NotificationItem("Cảnh báo sự cố !", "ROBOT PHÒNG KHÁCH", "10:53", "2020/10/18", true));
        notificationList.add(new NotificationItem("Scheduled task started.", "DEEBOT T8 AIVI", "10:14", "2020/10/18", true));
        notificationList.add(new NotificationItem("Cleaning complete", "DEEBOT T8 AIVI", "10:52", "2020/10/17", true));
        notificationList.add(new NotificationItem("Scheduled task started.", "DEEBOT T8 AIVI", "10:14", "2020/10/17", true));
        notificationList.add(new NotificationItem("Cleaning complete", "DEEBOT T8 AIVI", "10:50", "2020/10/16", true));
        notificationList.add(new NotificationItem("Scheduled task started.", "DEEBOT T8 AIVI", "10:14", "2020/10/16", true));

        adapter.notifyDataSetChanged();
    }

    private void setupActions() {
        btnBack.setOnClickListener(v -> finish());
    }
}
