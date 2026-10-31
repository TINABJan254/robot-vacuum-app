package com.ht.robotvacuumapp.view.robot;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.*;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ht.robotvacuumapp.R;
import com.ht.robotvacuumapp.model.repository.AuthRepository;
import com.ht.robotvacuumapp.model.repository.ScheduleRepository;
import com.ht.robotvacuumapp.viewmodel.robot.RobotScheduleUiState;
import com.ht.robotvacuumapp.viewmodel.robot.RobotScheduleViewModel;

public class RobotScheduleActivity extends AppCompatActivity {
    public static final String EXTRA_ROBOT_NAME = "extra_robot_name";
    public static final String EXTRA_ROBOT_ID = "extra_robot_id";
    private RobotScheduleViewModel viewModel;
    private ScheduleAdapter adapter;
    private RecyclerView list;
    private LinearLayoutManager manager;
    private View listState, emptyState, addTop, addBottom, progress, retry, footer, moreProgress, moreRetry;
    private TextView feedback, moreText;
    private boolean navigatingToLogin;
    private final ActivityResultLauncher<Intent> newScheduleLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    viewModel.onCreated(result.getData().getLongExtra(NewScheduleActivity.EXTRA_RESULT_ROBOT_ID, -1),
                            result.getData().getLongExtra(NewScheduleActivity.EXTRA_CREATED_SCHEDULE_ID, -1));
                }
            });

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_robot_schedule);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.schedule_root), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        viewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> type) {
                return type.cast(new RobotScheduleViewModel(getIntent().getStringExtra(EXTRA_ROBOT_ID),
                        new ScheduleRepository(getApplicationContext()), new AuthRepository(getApplicationContext())));
            }
        }).get(RobotScheduleViewModel.class);
        listState = findViewById(R.id.layout_list_state);
        emptyState = findViewById(R.id.layout_empty_state);
        addTop = findViewById(R.id.btn_top_add);
        addBottom = findViewById(R.id.btn_bottom_add);
        progress = findViewById(R.id.schedule_progress);
        retry = findViewById(R.id.schedule_retry);
        feedback = findViewById(R.id.schedule_feedback);
        footer = findViewById(R.id.schedule_footer);
        moreProgress = findViewById(R.id.schedule_more_progress);
        moreRetry = findViewById(R.id.schedule_more_retry);
        moreText = findViewById(R.id.schedule_more_text);
        list = findViewById(R.id.rv_schedules);
        manager = new LinearLayoutManager(this);
        list.setLayoutManager(manager);
        adapter = new ScheduleAdapter(new ScheduleAdapter.OnScheduleActionListener() {
            @Override public void onToggle(long id, boolean enabled) { viewModel.setEnabled(id, enabled); }
            @Override public void onDelete(long id) { viewModel.deleteSchedule(id); }
        });
        list.setAdapter(adapter);
        list.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override public void onScrolled(@NonNull RecyclerView view, int dx, int dy) { maybeLoadMore(); }
        });
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        addTop.setOnClickListener(v -> openNewSchedule());
        addBottom.setOnClickListener(v -> openNewSchedule());
        retry.setOnClickListener(v -> viewModel.refresh());
        moreRetry.setOnClickListener(v -> viewModel.loadNextPage());
        viewModel.getUiState().observe(this, this::render);
    }
    private void openNewSchedule() {
        RobotScheduleUiState s = viewModel.getUiState().getValue();
        if (s.robotId == null || s.requiresLogin || !s.loaded || s.listError != null || s.isLoading) return;
        newScheduleLauncher.launch(new Intent(this, NewScheduleActivity.class)
                .putExtra(NewScheduleActivity.EXTRA_ROBOT_ID, String.valueOf(s.robotId)));
    }
    private void render(RobotScheduleUiState s) {
        if (s.requiresLogin) {
            if (!navigatingToLogin) {
                navigatingToLogin = true;
                if (s.message != null) Toast.makeText(this, s.message, Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, com.ht.robotvacuumapp.view.auth.LoginActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
            }
            return;
        }
        boolean hasItems = !s.schedules.isEmpty();
        boolean canAdd = s.robotId != null && s.loaded && s.listError == null && !s.isLoading;
        adapter.submitSchedules(s.schedules, s.pendingEnabled, s.deleting);
        listState.setVisibility(hasItems ? View.VISIBLE : View.GONE);
        emptyState.setVisibility(!hasItems && s.loaded && !s.isLoading && s.listError == null ? View.VISIBLE : View.GONE);
        addTop.setVisibility(hasItems ? View.VISIBLE : View.GONE);
        addTop.setEnabled(canAdd);
        addBottom.setEnabled(canAdd);
        progress.setVisibility(s.isLoading ? View.VISIBLE : View.GONE);
        feedback.setText(s.listError != null ? s.listError : getString(R.string.schedule_loading));
        feedback.setVisibility(s.isLoading || s.listError != null ? View.VISIBLE : View.GONE);
        retry.setVisibility(s.listError != null && s.robotId != null ? View.VISIBLE : View.GONE);
        footer.setVisibility(s.isLoadingMore || s.moreError != null ? View.VISIBLE : View.GONE);
        moreProgress.setVisibility(s.isLoadingMore ? View.VISIBLE : View.GONE);
        moreText.setText(s.moreError != null ? s.moreError : getString(R.string.schedule_loading_more));
        moreRetry.setVisibility(s.moreError == null ? View.GONE : View.VISIBLE);
        moreRetry.setEnabled(!s.isLoadingMore);
        if (s.message != null) {
            viewModel.acknowledgeMessage();
            Toast.makeText(this, s.message, Toast.LENGTH_SHORT).show();
        }
        list.post(this::maybeLoadMore);
    }
    private void maybeLoadMore() {
        if (isFinishing() || !getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) return;
        RobotScheduleUiState s = viewModel.getUiState().getValue();
        if (s.moreError == null && !s.schedules.isEmpty() && manager.findLastVisibleItemPosition() >= adapter.getItemCount() - 4) {
            viewModel.loadNextPage();
        }
    }
    @Override protected void onStart() { super.onStart(); list.post(this::maybeLoadMore); }
}
