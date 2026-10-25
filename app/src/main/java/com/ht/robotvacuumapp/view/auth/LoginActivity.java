package com.ht.robotvacuumapp.view.auth;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.ht.robotvacuumapp.R;
import com.ht.robotvacuumapp.model.repository.AuthRepository;
import com.ht.robotvacuumapp.view.home.MainActivity;
import com.ht.robotvacuumapp.viewmodel.auth.LoginUiState;
import com.ht.robotvacuumapp.viewmodel.auth.LoginViewModel;

public class LoginActivity extends AppCompatActivity {

    private EditText edtUsername;
    private EditText edtPassword;
    private ImageView btnTogglePassword;
    private CheckBox cbRememberMe;
    private TextView tvForgotPassword;
    private AppCompatButton btnLogin;
    private TextView tvRegisterPrompt;
    private ImageView btnServerSettings;

    private boolean isPasswordVisible = false;
    private LoginViewModel viewModel;

    private final ActivityResultLauncher<Intent> registerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String registeredUsername = result.getData().getStringExtra("registered_username");
                    if (registeredUsername != null && !registeredUsername.isEmpty()) {
                        edtUsername.setText(registeredUsername);
                        edtPassword.requestFocus();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.scroll_view), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        viewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                return modelClass.cast(new LoginViewModel(new AuthRepository(getApplicationContext())));
            }
        }).get(LoginViewModel.class);
        if (savedInstanceState == null) loadSavedCredentials();
        setupPasswordToggle();
        setupRegisterPrompt();
        setupForgotPassword();
        setupServerSettings();
        setupLoginAction();
        viewModel.getUiState().observe(this, this::render);
    }

    private void initViews() {
        edtUsername = findViewById(R.id.edt_username);
        edtPassword = findViewById(R.id.edt_password);
        btnTogglePassword = findViewById(R.id.btn_toggle_password);
        cbRememberMe = findViewById(R.id.cb_remember_me);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegisterPrompt = findViewById(R.id.tv_register_prompt);
        btnServerSettings = findViewById(R.id.btn_server_settings);
    }

    private void loadSavedCredentials() {
        boolean rememberMe = viewModel.isRememberMe();
        cbRememberMe.setChecked(rememberMe);
        if (rememberMe) {
            String savedUsername = viewModel.getSavedUsername();
            edtUsername.setText(savedUsername);
            if (!savedUsername.isEmpty()) {
                edtPassword.requestFocus();
            }
        }
    }

    private void setupPasswordToggle() {
        btnTogglePassword.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            if (isPasswordVisible) {
                edtPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                btnTogglePassword.setImageResource(R.drawable.ic_eye_off_outline);
            } else {
                edtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                btnTogglePassword.setImageResource(R.drawable.ic_eye_outline);
            }
            edtPassword.setSelection(edtPassword.getText().length());
        });
    }

    private void setupRegisterPrompt() {
        String prefix = getString(R.string.prompt_no_account);
        String action = getString(R.string.action_register_now);
        SpannableStringBuilder builder = new SpannableStringBuilder(prefix + action);

        int primaryGreen = ContextCompat.getColor(this, R.color.primary_green);
        int startIndex = prefix.length();
        int endIndex = prefix.length() + action.length();

        builder.setSpan(new ForegroundColorSpan(primaryGreen), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        tvRegisterPrompt.setText(builder);
        tvRegisterPrompt.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            registerLauncher.launch(intent);
        });
    }

    private void setupForgotPassword() {
        tvForgotPassword.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng khôi phục mật khẩu đang phát triển", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupServerSettings() {
        if (btnServerSettings != null) {
            btnServerSettings.setOnClickListener(v -> showServerConfigDialog());
        }
    }

    private void showServerConfigDialog() {
        if (viewModel.getUiState().getValue().loading) return;
        EditText input = new EditText(this);
        input.setText(viewModel.getBaseUrl());
        input.setHint("http://192.168.x.x:8080/");
        input.setSingleLine(true);
        input.setPadding(40, 30, 40, 30);

        new AlertDialog.Builder(this)
                .setTitle("Cấu hình IP Backend (LAN)")
                .setMessage("Nhập địa chỉ máy chủ backend trong mạng LAN:")
                .setView(input)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    viewModel.setServer(input.getText().toString(), false);
                })
                .setNegativeButton("Hủy", null)
                .setNeutralButton("Mặc định", (dialog, which) -> {
                    viewModel.setServer("", true);
                })
                .show();
    }

    private void setupLoginAction() {
        btnLogin.setOnClickListener(v -> viewModel.login(edtUsername.getText().toString(),
                edtPassword.getText().toString(), cbRememberMe.isChecked()));
    }

    private void render(LoginUiState state) {
        btnLogin.setEnabled(!state.loading);
        btnLogin.setText(state.loading ? "Đang đăng nhập..." : getString(R.string.btn_login));
        btnServerSettings.setEnabled(!state.loading);
        tvRegisterPrompt.setEnabled(!state.loading);
        cbRememberMe.setEnabled(!state.loading);
        edtUsername.setEnabled(!state.loading);
        edtPassword.setEnabled(!state.loading);
        edtUsername.setError(state.usernameError);
        edtPassword.setError(state.passwordError);
        if (state.usernameError != null) edtUsername.requestFocus();
        else if (state.passwordError != null) edtPassword.requestFocus();
        if (state.loginSucceeded) {
            viewModel.acknowledgeFeedback();
            Toast.makeText(this, "Đăng nhập thành công! Chào " + state.displayName, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else if (state.message != null) {
            viewModel.acknowledgeFeedback();
            Toast.makeText(this, state.message, Toast.LENGTH_LONG).show();
        }
    }
}
