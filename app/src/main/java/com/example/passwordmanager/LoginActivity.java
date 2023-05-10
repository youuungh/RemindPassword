package com.example.passwordmanager;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethod;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
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
        login_password = findViewById(R.id.login_password);

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

    void loginChangeInProgress(boolean inProgress) {
        if (inProgress) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    boolean loginValidateData(String email, String password) {
        // 데이터 유효성 검사
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            login_email.setError("유효하지 않은 이메일입니다", null);
            return false;
        }
        if(password.isEmpty()) {
            login_password.setError("비밀번호를 입력하세요", null);
            return false;
        }
        if(password.length() < 6 || password.length() > 15) {
            login_password.setError("비밀번호는 8글자 이상 15자 이하여야 합니다", null);
            return false;
        }
        return true;
    }
}