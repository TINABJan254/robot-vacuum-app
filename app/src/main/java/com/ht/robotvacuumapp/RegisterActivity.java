package com.ht.robotvacuumapp;

import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
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

public class RegisterActivity extends AppCompatActivity {

    private EditText edtFullName;
    private EditText edtUsername;
    private EditText edtPassword;
    private EditText edtConfirmPassword;
    private ImageView btnTogglePassword;
    private ImageView btnToggleConfirmPassword;
    private View barStrength1, barStrength2, barStrength3, barStrength4;
    private TextView tvPasswordStrength;
    private AppCompatButton btnRegister;
    private TextView tvLoginPrompt;

    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.scroll_view), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupPasswordToggles();
        setupPasswordStrengthChecker();
        setupLoginPrompt();
        setupRegisterAction();
    }

    private void initViews() {
        edtFullName = findViewById(R.id.edt_full_name);
        edtUsername = findViewById(R.id.edt_username);
        edtPassword = findViewById(R.id.edt_password);
        edtConfirmPassword = findViewById(R.id.edt_confirm_password);
        btnTogglePassword = findViewById(R.id.btn_toggle_password);
        btnToggleConfirmPassword = findViewById(R.id.btn_toggle_confirm_password);
        barStrength1 = findViewById(R.id.bar_strength_1);
        barStrength2 = findViewById(R.id.bar_strength_2);
        barStrength3 = findViewById(R.id.bar_strength_3);
        barStrength4 = findViewById(R.id.bar_strength_4);
        tvPasswordStrength = findViewById(R.id.tv_password_strength);
        btnRegister = findViewById(R.id.btn_register);
        tvLoginPrompt = findViewById(R.id.tv_login_prompt);
    }

    private void setupPasswordToggles() {
        // Toggle Password visibility
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

        // Toggle Confirm Password visibility
        btnToggleConfirmPassword.setOnClickListener(v -> {
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
            if (isConfirmPasswordVisible) {
                edtConfirmPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                btnToggleConfirmPassword.setImageResource(R.drawable.ic_eye_off_outline);
            } else {
                edtConfirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                btnToggleConfirmPassword.setImageResource(R.drawable.ic_eye_outline);
            }
            edtConfirmPassword.setSelection(edtConfirmPassword.getText().length());
        });
    }

    private void setupPasswordStrengthChecker() {
        edtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                evaluatePasswordStrength(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    private void evaluatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            resetStrengthBars();
            tvPasswordStrength.setText(R.string.password_strength);
            tvPasswordStrength.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            return;
        }

        int score = 0;
        if (password.length() >= 8) score++;
        if (password.matches(".*[a-z].*") && password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*[0-9].*")) score++;
        if (password.matches(".*[!@#$%^&*()\\-_=+\\[\\]{}|;:,.<>?].*")) score++;

        // At least 1 point if not empty
        if (score == 0 && !password.isEmpty()) {
            score = 1;
        }

        int color;
        String strengthText;

        switch (score) {
            case 1:
                color = ContextCompat.getColor(this, R.color.strength_weak);
                strengthText = "Độ mạnh mật khẩu: Yếu";
                break;
            case 2:
                color = ContextCompat.getColor(this, R.color.strength_fair);
                strengthText = "Độ mạnh mật khẩu: Trung bình";
                break;
            case 3:
                color = ContextCompat.getColor(this, R.color.strength_good);
                strengthText = "Độ mạnh mật khẩu: Khá";
                break;
            case 4:
            default:
                color = ContextCompat.getColor(this, R.color.strength_strong);
                strengthText = "Độ mạnh mật khẩu: Mạnh";
                break;
        }

        int inactiveColor = ContextCompat.getColor(this, R.color.strength_inactive);
        setBarColor(barStrength1, score >= 1 ? color : inactiveColor);
        setBarColor(barStrength2, score >= 2 ? color : inactiveColor);
        setBarColor(barStrength3, score >= 3 ? color : inactiveColor);
        setBarColor(barStrength4, score >= 4 ? color : inactiveColor);

        tvPasswordStrength.setText(strengthText);
        tvPasswordStrength.setTextColor(color);
    }

    private void resetStrengthBars() {
        int inactiveColor = ContextCompat.getColor(this, R.color.strength_inactive);
        setBarColor(barStrength1, inactiveColor);
        setBarColor(barStrength2, inactiveColor);
        setBarColor(barStrength3, inactiveColor);
        setBarColor(barStrength4, inactiveColor);
    }

    private void setBarColor(View bar, int color) {
        if (bar.getBackground() instanceof GradientDrawable) {
            GradientDrawable drawable = (GradientDrawable) bar.getBackground().mutate();
            drawable.setColor(color);
        } else {
            bar.setBackgroundColor(color);
        }
    }

    private void setupLoginPrompt() {
        String prefix = getString(R.string.prompt_have_account);
        String action = getString(R.string.action_login_now);
        SpannableStringBuilder builder = new SpannableStringBuilder(prefix + action);

        int primaryGreen = ContextCompat.getColor(this, R.color.primary_green);
        int startIndex = prefix.length();
        int endIndex = prefix.length() + action.length();

        builder.setSpan(new ForegroundColorSpan(primaryGreen), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        tvLoginPrompt.setText(builder);
        tvLoginPrompt.setOnClickListener(v -> {
            finish();
        });
    }

    private void setupRegisterAction() {
        btnRegister.setOnClickListener(v -> {
            String fullName = edtFullName.getText().toString().trim();
            String username = edtUsername.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            String confirmPassword = edtConfirmPassword.getText().toString().trim();

            if (fullName.isEmpty()) {
                edtFullName.setError("Vui lòng nhập họ và tên");
                edtFullName.requestFocus();
                return;
            }

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

            if (password.length() < 8) {
                edtPassword.setError("Mật khẩu phải có tối thiểu 8 ký tự");
                edtPassword.requestFocus();
                return;
            }

            if (!password.equals(confirmPassword)) {
                edtConfirmPassword.setError("Mật khẩu xác nhận không khớp");
                edtConfirmPassword.requestFocus();
                return;
            }

            // Valid registration form
            Toast.makeText(this, "Đăng ký tài khoản thành công!", Toast.LENGTH_LONG).show();
        });
    }
}
