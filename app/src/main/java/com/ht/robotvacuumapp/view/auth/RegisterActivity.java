package com.ht.robotvacuumapp.view.auth;

import android.content.Intent;
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

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.ht.robotvacuumapp.R;
import com.ht.robotvacuumapp.model.repository.AuthRepository;
import com.ht.robotvacuumapp.viewmodel.auth.RegisterUiState;
import com.ht.robotvacuumapp.viewmodel.auth.RegisterViewModel;

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

    private RegisterViewModel viewModel;

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
        viewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                return modelClass.cast(new RegisterViewModel(new AuthRepository(getApplicationContext())));
            }
        }).get(RegisterViewModel.class);
        setupPasswordToggles();
        setupPasswordStrengthChecker();
        setupLoginPrompt();
        setupRegisterAction();
        viewModel.getUiState().observe(this, this::render);
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
                viewModel.updatePasswordStrength(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    private void renderPasswordStrength(int score) {
        if (score == 0) {
            resetStrengthBars();
            tvPasswordStrength.setText(R.string.password_strength);
            tvPasswordStrength.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            return;
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
            viewModel.register(edtFullName.getText().toString(), edtUsername.getText().toString(),
                    edtPassword.getText().toString(), edtConfirmPassword.getText().toString());
            RegisterUiState state = viewModel.getUiState().getValue();
            if (state.nameError != null) edtFullName.requestFocus();
            else if (state.usernameError != null) edtUsername.requestFocus();
            else if (state.passwordError != null) edtPassword.requestFocus();
            else if (state.confirmError != null) edtConfirmPassword.requestFocus();
        });
    }

    private void render(RegisterUiState state) {
        btnRegister.setEnabled(!state.loading);
        btnRegister.setText(state.loading ? "Đang đăng ký..." : getString(R.string.btn_register));
        edtFullName.setEnabled(!state.loading);
        edtUsername.setEnabled(!state.loading);
        edtPassword.setEnabled(!state.loading);
        edtConfirmPassword.setEnabled(!state.loading);
        edtFullName.setError(state.nameError);
        edtUsername.setError(state.usernameError);
        edtPassword.setError(state.passwordError);
        edtConfirmPassword.setError(state.confirmError);
        renderPasswordStrength(state.passwordStrength);
        if (state.registeredUsername != null) {
            viewModel.acknowledgeFeedback();
            Toast.makeText(this, "Đăng ký thành công! Vui lòng đăng nhập.", Toast.LENGTH_LONG).show();
            Intent result = new Intent().putExtra("registered_username", state.registeredUsername);
            setResult(RESULT_OK, result);
            finish();
        } else if (state.message != null) {
            viewModel.acknowledgeFeedback();
            Toast.makeText(this, state.message, Toast.LENGTH_LONG).show();
        }
    }
}
