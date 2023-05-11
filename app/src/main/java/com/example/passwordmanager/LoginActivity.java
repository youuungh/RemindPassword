package com.example.passwordmanager;


import androidx.appcompat.app.AppCompatActivity;

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

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {
    static final Pattern PASSWORD = Pattern.compile("^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*])[a-zA-Z0-9!@#$%^&*]{8,15}$");
    TextInputLayout layout_email, layout_password;
    TextInputEditText login_email, login_password;
    ProgressBar progressBar;
    FirebaseAuth fAuth;
    MaterialToolbar mToolbar;
    Button button_login;

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
        login_email.setOnFocusChangeListener((v, hasFocus) -> {
            if(hasFocus) {
                layout_email.setError(null);
                layout_email.setErrorEnabled(false);
            }
        });

        login_password = findViewById(R.id.login_password);
        login_password.setOnFocusChangeListener((v, hasFocus) -> {
            if(hasFocus) {
                layout_password.setError(null);
                layout_password.setErrorEnabled(false);
            }
        });

        progressBar = findViewById(R.id.progressBar);

        button_login = findViewById(R.id.button_login);
        button_login.setOnClickListener(view -> loginUser());
    }

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
                        //Utility.showToast(LoginActivity.this, "비밀번호가 일치하지 않습니다");
                        Snackbar.make(getWindow().getDecorView().getRootView(), "비밀번호가 일치하지 않습니다.", Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    boolean loginValidateData(String email, String password) {
        // 데이터 유효성 검사
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            layout_email.setError("올바른 형식의 이메일 주소를 입력해 주세요");
            return false;
        }
        if(password.isEmpty()) {
            layout_password.setError("비밀번호를 입력해 주세요");
            return false;
        }
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

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View focusView = getCurrentFocus();

        if (focusView != null) {
            Rect rect = new Rect();
            focusView.getGlobalVisibleRect(rect);

            int x = (int) ev.getX();
            int y = (int) ev.getY();

            if (!rect.contains(x, y)) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null)
                    imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
                focusView.clearFocus();
            }
        }
        return super.dispatchTouchEvent(ev);
    }
}