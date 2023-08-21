package com.example.passwordmanager;


import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.MotionEvent;
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
    TextInputEditText login_email, login_password;
    ProgressBar progressBar;
    FirebaseAuth fAuth;
    Button button_login;
    TextView tvCreateAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mToolbar = findViewById(R.id.topAppbar);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(view -> {
            this.finish();
        });

        layout_email = findViewById(R.id.layout_email);
        layout_password = findViewById(R.id.layout_password);

        login_email = findViewById(R.id.login_email);
        login_password = findViewById(R.id.login_password);
        login_email.addTextChangedListener(loginTextWatcher);
        login_password.addTextChangedListener(loginTextWatcher);

        progressBar = findViewById(R.id.progressBar);
        button_login = findViewById(R.id.button_login);
        tvCreateAccount = findViewById(R.id.tvCreateAccount);
        tvCreateAccount.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateAccountActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    TextWatcher loginTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String emailInput = login_email.getText().toString().trim();
            String passwordInput = login_password.getText().toString().trim();

            button_login.setEnabled(!emailInput.isEmpty()
                    && Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()
                    && !passwordInput.isEmpty());

            button_login.setOnClickListener(view -> {
                InputMethodManager manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                layout_email.setErrorEnabled(false);
                layout_password.setErrorEnabled(false);

                loginUser();
            });
        }

        @Override
        public void afterTextChanged(Editable s) { }
    };

    void loginUser() {
        String email = login_email.getText().toString().trim();
        String password = login_password.getText().toString().trim();

        boolean isValidated = loginValidateData(email, password);
        if(!isValidated) {
            return;
        }

        loginAccountInFirebase(email, password);
    }

    void loginAccountInFirebase(String email, String password) {
        fAuth = FirebaseAuth.getInstance();
        loginChangeInProgress(true);
        fAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    loginChangeInProgress(false);
                    if(task.isSuccessful()) {
                        if(fAuth.getCurrentUser().isEmailVerified()) {
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            Utility.showToast(LoginActivity.this, "로그인 성공");
                        } else {
                            Utility.showToast(LoginActivity.this, "이메일 인증이 필요합니다");
                        }
                    } else {
                        Snackbar.make(findViewById(R.id.loginScreen), "계정 혹은 비밀번호가 일치하지 않습니다. 다시 시도하세요.", Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    boolean loginValidateData(String email, String password) {
        // 데이터 유효성 검사
        if(!PASSWORD.matcher(password).matches()) {
            layout_password.setError("숫자/영문/특수문자 8자~15자로 입력해 주세요");
            return false;
        }
        return true;
    }

    void loginChangeInProgress(boolean inProgress) {
        if (inProgress) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }
}