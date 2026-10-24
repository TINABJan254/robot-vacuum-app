package com.ht.robotvacuumapp;

import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "AuthPrefs";
    private static final String KEY_REMEMBER_ME = "remember_me";
    private static final String KEY_SAVED_USERNAME = "saved_username";

    private EditText edtUsername;
    private EditText edtPassword;
    private ImageView btnTogglePassword;
    private CheckBox cbRememberMe;
    private TextView tvForgotPassword;
    private AppCompatButton btnLogin;
    private TextView tvRegisterPrompt;

    private boolean isPasswordVisible = false;
    private SharedPreferences sharedPreferences;

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

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        initViews();
        loadSavedCredentials();
        setupPasswordToggle();
        setupRegisterPrompt();
        setupForgotPassword();
        setupLoginAction();
    }

    private void initViews() {
        edtUsername = findViewById(R.id.edt_username);
        edtPassword = findViewById(R.id.edt_password);
        btnTogglePassword = findViewById(R.id.btn_toggle_password);
        cbRememberMe = findViewById(R.id.cb_remember_me);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegisterPrompt = findViewById(R.id.tv_register_prompt);
    }

    private void loadSavedCredentials() {
        boolean rememberMe = sharedPreferences.getBoolean(KEY_REMEMBER_ME, false);
        cbRememberMe.setChecked(rememberMe);
        if (rememberMe) {
            String savedUsername = sharedPreferences.getString(KEY_SAVED_USERNAME, "");
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
            startActivity(intent);
        });
    }

    private void setupForgotPassword() {
        tvForgotPassword.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng khôi phục mật khẩu đang phát triển", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupLoginAction() {
        btnLogin.setOnClickListener(v -> {
            String username = edtUsername.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (username.isEmpty()) {
                edtUsername.setError("Vui lòng nhập tên đăng nhập");
                edtUsername.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                edtPassword.setError("Vui lòng nhập mật khẩu");
                edtPassword.requestFocus();
                return;
            }

            // Save or clear remember me preference
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if (cbRememberMe.isChecked()) {
                editor.putBoolean(KEY_REMEMBER_ME, true);
                editor.putString(KEY_SAVED_USERNAME, username);
            } else {
                editor.putBoolean(KEY_REMEMBER_ME, false);
                editor.remove(KEY_SAVED_USERNAME);
            }
            editor.apply();

            Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

            // Navigate to MainActivity after successful login
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
