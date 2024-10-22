package com.ninezero.remindpassword.view.common;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ninezero.remindpassword.R;
import com.ninezero.remindpassword.util.Utils;
import com.ninezero.remindpassword.view.user.PassCodeFragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {
    private static final Pattern PASSWORD = Pattern.compile("^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*])[a-zA-Z0-9!@#$%^&*]{8,15}$");
    private FirebaseAuth fAuth;
    private TextInputLayout layout_email, layout_password;
    private TextInputEditText edt_email, edt_password;
    private ProgressBar progressBar;
    private Button button_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        fAuth = FirebaseAuth.getInstance();

        init();
        setupToolbar();
        setupNavigation();
        setupTextWatchers();
    }

    private void setupToolbar() {
        MaterialToolbar mToolbar = findViewById(R.id.login_toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(view -> this.finish());
    }

    private void init() {
        layout_email = findViewById(R.id.login_layout_email);
        layout_password = findViewById(R.id.login_layout_password);
        edt_email = findViewById(R.id.login_email);
        edt_password = findViewById(R.id.login_password);
        button_login = findViewById(R.id.login_button);
        progressBar = findViewById(R.id.login_progressBar);
    }

    private void setupNavigation() {
        TextView tv_reset = findViewById(R.id.tv_reset);
        tv_reset.setOnClickListener(v -> startActivity(new Intent(this, ResetPasswordActivity.class)));

        TextView tv_signup = findViewById(R.id.tv_signup);
        tv_signup.setOnClickListener(v -> startActivity(new Intent(this, SignUpActivity.class)));
    }

    private void setupTextWatchers() {
        edt_email.addTextChangedListener(new LoginTextWatcher(edt_email));
        edt_password.addTextChangedListener(new LoginTextWatcher(edt_password));
    }

    private void loginAccountInFirebase(String email, String password) {
        loginChangeInProgress(true);
        fAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    clearFocus();
                    if (task.isSuccessful()) {
                        if (fAuth.getCurrentUser() != null && fAuth.getCurrentUser().isEmailVerified()) {
                            navigateToPassCodeFragment();
                        } else {
                            loginChangeInProgress(false);
                            Utils.showSnack(findViewById(R.id.loginScreen), "이메일 인증이 필요합니다");
                        }
                    } else {
                        loginChangeInProgress(false);
                        Utils.showSnack(findViewById(R.id.loginScreen), "계정 혹은 비밀번호가 일치하지 않습니다");
                    }
                });
    }

    private void loginChangeInProgress(boolean inProgress) {
        progressBar.setVisibility(inProgress ? View.VISIBLE : View.GONE);
    }

    private void clearFocus() {
        edt_email.clearFocus();
        edt_password.clearFocus();
    }

    private class LoginTextWatcher implements TextWatcher {
        private final View v;

        private LoginTextWatcher(View v) {
            this.v = v;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String emailInput = edt_email.getText().toString().trim();
            String passwordInput = edt_password.getText().toString().trim();

            switch (v.getId()) {
                case R.id.login_email:
                    validateEmail(emailInput);
                    break;
                case R.id.login_password:
                    validatePassword(passwordInput);
                    break;
            }
            button_login.setEnabled(Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()
                    && PASSWORD.matcher(passwordInput).matches());

            button_login.setOnClickListener(view -> {
                InputMethodManager manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                View focusedView = getCurrentFocus();
                if (focusedView != null)
                    manager.hideSoftInputFromWindow(focusedView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                loginAccountInFirebase(emailInput, passwordInput);
            });
        }

        @Override
        public void afterTextChanged(Editable s) { }
    };

    private void validateEmail(String emailInput) {
        boolean isValid = Patterns.EMAIL_ADDRESS.matcher(emailInput).matches();
        if (emailInput.isEmpty()) {
            layout_email.setErrorEnabled(false);
            layout_email.setBoxStrokeColor(Color.BLACK);
        } else {
            if (!isValid) {
                layout_email.setError("올바른 형식의 이메일 주소를 입력해 주세요.");
                layout_email.setErrorIconDrawable(null);
            } else {
                layout_email.setErrorEnabled(false);
            }
        }
    }

    private void validatePassword(String passwordInput) {
        boolean isValid = PASSWORD.matcher(passwordInput).matches();
        if (passwordInput.isEmpty()) {
            layout_password.setErrorEnabled(false);
            layout_password.setBoxStrokeColor(Color.BLACK);
        } else {
            if (!isValid) {
                layout_password.setError("숫자/영문/특수문자 8자~15자로 입력해 주세요");
                layout_password.setErrorIconDrawable(null);
            } else {
                layout_password.setErrorEnabled(false);
            }
        }
    }

    private void navigateToPassCodeFragment() {
        PassCodeFragment passCodeFragment = new PassCodeFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.layout_login, passCodeFragment)
                .addToBackStack(null)
                .commit();
        loginChangeInProgress(false);
    }
}