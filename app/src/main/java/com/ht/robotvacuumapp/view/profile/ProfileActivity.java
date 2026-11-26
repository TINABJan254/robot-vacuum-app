package com.ht.robotvacuumapp.view.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.ht.robotvacuumapp.R;
import com.ht.robotvacuumapp.api.dto.UserDto;
import com.ht.robotvacuumapp.model.repository.AuthRepository;
import com.ht.robotvacuumapp.view.auth.LoginActivity;
import com.ht.robotvacuumapp.viewmodel.profile.ProfileUiState;
import com.ht.robotvacuumapp.viewmodel.profile.ProfileViewModel;

public class ProfileActivity extends AppCompatActivity {
    private ProfileViewModel viewModel;
    private View feedback, progress, retry, logout, listTab, accountTab;
    private TextView feedbackText;
    private boolean navigatingToLogin;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profile_root), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        feedback = findViewById(R.id.layout_profile_feedback);
        progress = findViewById(R.id.progress_profile);
        feedbackText = findViewById(R.id.tv_profile_feedback);
        retry = findViewById(R.id.btn_retry_profile);
        logout = findViewById(R.id.btn_profile_logout);
        listTab = findViewById(R.id.tab_robot);
        accountTab = findViewById(R.id.tab_mine);
        viewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> type) {
                return type.cast(new ProfileViewModel(new AuthRepository(getApplicationContext())));
            }
        }).get(ProfileViewModel.class);
        retry.setOnClickListener(v -> viewModel.loadProfile());
        accountTab.setOnClickListener(v -> viewModel.loadProfile());
        listTab.setOnClickListener(v -> finish());
        logout.setOnClickListener(v -> viewModel.logout());
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                if (!viewModel.getUiState().getValue().isLoggingOut) finish();
            }
        });
        viewModel.getUiState().observe(this, this::render);
    }

    private void render(ProfileUiState state) {
        if (state.requiresLogin) {
            if (!navigatingToLogin) {
                navigatingToLogin = true;
                if (state.error != null) Toast.makeText(this, state.error, Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, LoginActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
            }
            return;
        }
        UserDto account = state.account;
        String name = account == null ? null : account.getName();
        String username = account == null ? null : account.getUsername();
        String role = account == null ? null : account.getRole();
        if ("ADMIN".equals(role)) role = "Quản trị viên";
        else if ("USER".equals(role)) role = "Người dùng";
        setText(R.id.tv_profile_name, name);
        setText(R.id.tv_profile_full_name, name);
        setText(R.id.tv_profile_username, username);
        setText(R.id.tv_profile_login_name, username);
        setText(R.id.tv_profile_role_badge, role);
        setText(R.id.tv_profile_role, role);
        boolean busy = state.isLoading || state.isLoggingOut;
        feedback.setVisibility(busy || state.error != null ? View.VISIBLE : View.GONE);
        progress.setVisibility(busy ? View.VISIBLE : View.GONE);
        feedbackText.setText(state.error != null ? state.error
                : state.isLoggingOut ? "Đang đăng xuất…" : "Đang tải thông tin tài khoản…");
        retry.setVisibility(state.error != null && !busy ? View.VISIBLE : View.GONE);
        logout.setEnabled(!state.isLoggingOut);
        listTab.setEnabled(!state.isLoggingOut);
        accountTab.setEnabled(!busy);
    }

    private void setText(int id, String value) {
        ((TextView) findViewById(id)).setText(value == null || value.trim().isEmpty() ? "—" : value);
    }
}
