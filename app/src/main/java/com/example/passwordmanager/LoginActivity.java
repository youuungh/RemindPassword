package com.example.passwordmanager;


import androidx.appcompat.app.AppCompatActivity;

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

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {
    static final Pattern PASSWORD = Pattern.compile("^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*])[a-zA-Z0-9!@#$%^&*]{8,15}$");
    MaterialToolbar mToolbar;
    TextInputLayout layout_email, layout_password;
    TextInputEditText edt_email, edt_password;
    ProgressBar progressBar;
    Button button;
    TextView tv_signup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mToolbar = findViewById(R.id.login_toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(view -> {
            this.finish();
        });

        layout_email = findViewById(R.id.login_layout_email);
        layout_password = findViewById(R.id.login_layout_password);

        edt_email = findViewById(R.id.login_email);
        edt_email.addTextChangedListener(new LoginTextWatcher(edt_email));
        edt_password = findViewById(R.id.login_password);
        edt_password.addTextChangedListener(new LoginTextWatcher(edt_password));

        progressBar = findViewById(R.id.login_progressBar);
        button = findViewById(R.id.login_button);

        tv_signup = findViewById(R.id.tv_signup);
        tv_signup.setOnClickListener(v -> {
            Intent intent = new Intent(this, SignUpActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void loginAccountInFirebase(String email, String password) {
        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        loginChangeInProgress(true);
        fAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    loginChangeInProgress(false);
                    if(task.isSuccessful()) {
                        if(fAuth.getCurrentUser().isEmailVerified()) {
                            Intent intent = new Intent(this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        } else {
                            Utility.showSnack(findViewById(R.id.loginScreen), "이메일 인증이 필요합니다");
                        }
                    } else {
                        Utility.showSnack(findViewById(R.id.loginScreen), "계정 혹은 비밀번호가 일치하지 않습니다");
                    }
                });
    }

    void loginChangeInProgress(boolean inProgress) {
        if (inProgress) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    private class LoginTextWatcher implements TextWatcher {
        private View v;

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

            button.setEnabled(Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()
                    && PASSWORD.matcher(passwordInput).matches());
            button.setOnClickListener(view -> {
                InputMethodManager manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

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
}