package com.ht.robotvacuumapp.view.notification;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ht.robotvacuumapp.R;
import com.ht.robotvacuumapp.api.dto.AlertResponse;
import com.ht.robotvacuumapp.model.repository.AlertRepository;
import com.ht.robotvacuumapp.model.repository.AuthRepository;
import com.ht.robotvacuumapp.view.auth.LoginActivity;
import com.ht.robotvacuumapp.viewmodel.notification.NotificationUiState;
import com.ht.robotvacuumapp.viewmodel.notification.NotificationViewModel;

public class NotificationActivity extends AppCompatActivity {
    private NotificationViewModel viewModel;
    private NotificationAdapter adapter;
    private RecyclerView list;
    private LinearLayoutManager layoutManager;
    private View statePanel, progress, retry, footer, footerProgress, footerRetry, refresh;
    private TextView stateText, footerText;
    private AlertDialog detailDialog;
    private TextView detailMeta, detailMessage, detailError, readProgress;
    private View detailProgress;
    private Long dialogId;
    private boolean navigatingToLogin;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notification);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.notification_root), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        viewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                return modelClass.cast(new NotificationViewModel(new AlertRepository(getApplicationContext()),
                        new AuthRepository(getApplicationContext())));
            }
        }).get(NotificationViewModel.class);
        statePanel = findViewById(R.id.notification_state);
        progress = findViewById(R.id.notification_progress);
        retry = findViewById(R.id.notification_retry);
        stateText = findViewById(R.id.notification_state_text);
        footer = findViewById(R.id.notification_footer);
        footerProgress = findViewById(R.id.notification_more_progress);
        footerRetry = findViewById(R.id.notification_more_retry);
        footerText = findViewById(R.id.notification_more_text);
        refresh = findViewById(R.id.btn_refresh_notifications);
        list = findViewById(R.id.rv_notifications);
        layoutManager = new LinearLayoutManager(this);
        list.setLayoutManager(layoutManager);
        adapter = new NotificationAdapter(id -> viewModel.openAlert(id));
        list.setAdapter(adapter);
        list.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override public void onScrolled(@NonNull RecyclerView view, int dx, int dy) { maybeLoadMore(); }
        });
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        retry.setOnClickListener(v -> viewModel.refresh());
        refresh.setOnClickListener(v -> viewModel.refresh());
        footerRetry.setOnClickListener(v -> viewModel.loadNextPage());
        viewModel.getUiState().observe(this, this::render);
    }

    private void render(NotificationUiState s) {
        if (s.requiresLogin) {
            dismissDetailDialog();
            if (!navigatingToLogin) {
                navigatingToLogin = true;
                if (s.sessionMessage != null) Toast.makeText(this, s.sessionMessage, Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, LoginActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
            }
            return;
        }
        adapter.submitAlerts(s.alerts);
        statePanel.setVisibility(s.isLoading || s.listError != null || (s.loaded && s.alerts.isEmpty()) ? View.VISIBLE : View.GONE);
        progress.setVisibility(s.isLoading ? View.VISIBLE : View.GONE);
        stateText.setText(s.listError != null ? s.listError : getString(
                s.isLoading ? R.string.notification_loading : R.string.notification_empty));
        retry.setVisibility(s.listError != null ? View.VISIBLE : View.GONE);
        refresh.setEnabled(!s.isLoading);
        footer.setVisibility(s.isLoadingMore || s.loadMoreError != null ? View.VISIBLE : View.GONE);
        footerProgress.setVisibility(s.isLoadingMore ? View.VISIBLE : View.GONE);
        footerText.setText(s.loadMoreError != null ? s.loadMoreError : getString(R.string.notification_loading_more));
        footerRetry.setVisibility(s.loadMoreError != null ? View.VISIBLE : View.GONE);
        footerRetry.setEnabled(!s.isLoadingMore);
        renderDetail(s);
        list.post(this::maybeLoadMore);
    }

    private void maybeLoadMore() {
        if (isFinishing() || !getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) return;
        NotificationUiState s = viewModel.getUiState().getValue();
        if (s.loadMoreError == null && !s.alerts.isEmpty()
                && layoutManager.findLastVisibleItemPosition() >= adapter.getItemCount() - 4) viewModel.loadNextPage();
    }

    private void renderDetail(NotificationUiState s) {
        if (s.selectedId == null) { dismissDetailDialog(); return; }
        if (!s.selectedId.equals(dialogId)) {
            dismissDetailDialog();
            createDetailDialog(s.selectedId);
        }
        AlertResponse alert = s.detail;
        detailDialog.setTitle(alert == null ? s.selectedTitle : alert.getTitle());
        if (alert != null) {
            NotificationItem item = new NotificationItem(alert);
            setTextIfChanged(detailMeta, item.getDeviceName() + "\n" + item.getTime() + " · " + item.getDate());
            setTextIfChanged(detailMessage, alert.getMessage() == null || alert.getMessage().trim().isEmpty()
                    ? getString(R.string.notification_no_message) : alert.getMessage());
        } else {
            setTextIfChanged(detailMeta, "");
            setTextIfChanged(detailMessage, "");
        }
        detailMessage.setVisibility(alert == null ? View.GONE : View.VISIBLE);
        detailProgress.setVisibility(s.detailLoading ? View.VISIBLE : View.GONE);
        String error = s.detailError != null ? s.detailError : s.markError;
        detailError.setText(error);
        detailError.setVisibility(error == null ? View.GONE : View.VISIBLE);
        readProgress.setVisibility(s.isMarkingRead ? View.VISIBLE : View.GONE);
        detailDialog.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(error == null ? View.GONE : View.VISIBLE);
        detailDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(!s.detailLoading && !s.isMarkingRead);
        if (alert != null && !s.detailLoading && s.detailError == null) {
            long id = s.selectedId;
            long token = s.detailToken;
            detailMessage.post(() -> {
                if (detailDialog != null && detailDialog.isShowing() && Long.valueOf(id).equals(dialogId)
                        && getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED) && !isFinishing()) {
                    viewModel.onDetailShown(id, token);
                }
            });
        }
    }

    private void createDetailDialog(long id) {
        View content = LayoutInflater.from(this).inflate(R.layout.dialog_notification_detail, null);
        detailMeta = content.findViewById(R.id.notification_detail_meta);
        detailMessage = content.findViewById(R.id.notification_detail_message);
        detailError = content.findViewById(R.id.notification_detail_error);
        detailProgress = content.findViewById(R.id.notification_detail_progress);
        readProgress = content.findViewById(R.id.notification_read_progress);
        dialogId = id;
        detailDialog = new AlertDialog.Builder(this).setTitle(viewModel.getUiState().getValue().selectedTitle).setView(content)
                .setNegativeButton(R.string.notification_close, (dialog, which) -> viewModel.closeDetail())
                .setPositiveButton(R.string.action_retry, null).create();
        detailDialog.setOnCancelListener(dialog -> viewModel.closeDetail());
        detailDialog.show();
        detailDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> viewModel.retrySelected());
    }

    private void setTextIfChanged(TextView view, String text) {
        if (!TextUtils.equals(view.getText(), text)) view.setText(text);
    }
    private void dismissDetailDialog() {
        if (detailDialog != null) {
            detailDialog.setOnCancelListener(null);
            detailDialog.dismiss();
            detailDialog = null;
            dialogId = null;
        }
    }
    @Override protected void onStart() {
        super.onStart();
        viewModel.setActive(true);
        render(viewModel.getUiState().getValue());
    }
    @Override protected void onStop() {
        viewModel.setActive(false);
        super.onStop();
    }
    @Override protected void onDestroy() {
        dismissDetailDialog();
        super.onDestroy();
    }
}
