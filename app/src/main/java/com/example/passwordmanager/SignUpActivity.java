package com.example.passwordmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {
    static final Pattern PASSWORD = Pattern.compile("^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*])[a-zA-Z0-9!@#$%^&*]{8,15}$");
    TextInputLayout layout_email, layout_password, layout_passCheck;
    TextInputEditText edt_email, edt_password, edt_passCheck;
    MaterialToolbar mToolbar;
    Button button;
    ProgressBar progressBar;
    TextView tv_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mToolbar = findViewById(R.id.signup_toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(view -> {
            this.finish();
        });

        layout_email = findViewById(R.id.signup_layout_email);
        layout_password = findViewById(R.id.signup_layout_password);
        layout_passCheck = findViewById(R.id.signup_layout_passCheck);

        edt_email = findViewById(R.id.signup_email);
        edt_email.addTextChangedListener(new SignUpTextWatcher(edt_email));
        edt_password = findViewById(R.id.signup_password);
        edt_password.addTextChangedListener(new SignUpTextWatcher(edt_password));
        edt_passCheck = findViewById(R.id.signup_passCheck);
        edt_passCheck.addTextChangedListener(new SignUpTextWatcher(edt_passCheck));

        tv_login = findViewById(R.id.tv_login);
        tv_login.setPaintFlags(tv_login.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tv_login.setOnClickListener(view -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        button = findViewById(R.id.signup_button);
        progressBar = findViewById(R.id.signup_progressBar);
    }

    private void signUpInFirebase(String email, String password) {
        signUpChangeInProgress(true);
        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        fAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        signUpChangeInProgress(false);
                        if (task.isSuccessful()) {
                            Toast.makeText(SignUpActivity.this, "등록 성공, 이메일을 발송했습니다", Toast.LENGTH_LONG).show();
                            fAuth.getCurrentUser().sendEmailVerification();
                            fAuth.signOut();
                            onBackPressed();
                        } else {
                            Utility.showSnack(findViewById(R.id.signupScreen), "이미 존재하는 계정입니다");
                        }
                    }
                });
    }

    void signUpChangeInProgress(boolean inProgress) {
        if (inProgress) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    private class SignUpTextWatcher implements TextWatcher {
        private View v;

        private SignUpTextWatcher(View v) {
            this.v = v;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String emailInput = edt_email.getText().toString().trim();
            String passwordInput = edt_password.getText().toString().trim();
            String passCheckInput = edt_passCheck.getText().toString().trim();

            switch (v.getId()) {
                case R.id.signup_email:
                    validateEmail(emailInput);
                    break;
                case R.id.signup_password:
                    validatePassword(passwordInput, passCheckInput);
                    break;
                case R.id.signup_passCheck:
                    validatePassCheck(passwordInput, passCheckInput);
                    break;
            }

            button.setEnabled(Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()
                    && PASSWORD.matcher(passwordInput).matches()
                    && passCheckInput.equals(passwordInput));
            button.setOnClickListener(v -> {
                InputMethodManager manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                signUpInFirebase(emailInput, passwordInput);
            });
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

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
                layout_email.setBoxStrokeColor(Color.parseColor("#3B9B70"));
            }
        }
    }

    private void validatePassword(String passwordInput, String passCheckInput) {
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
                layout_password.setBoxStrokeColor(Color.parseColor("#3B9B70"));

                if (passCheckInput.equals(passwordInput)) {
                    layout_passCheck.setErrorEnabled(false);
                    layout_passCheck.setBoxStrokeColor(Color.parseColor("#3B9B70"));
                } else if (!passCheckInput.isEmpty()){
                    layout_passCheck.setError("비밀번호가 일치하지 않습니다");
                    layout_passCheck.setErrorIconDrawable(null);
                }
            }
        }
    }

    private void validatePassCheck(String passwordInput, String passCheckInput) {
        boolean isValid = passCheckInput.equals(passwordInput);

        if (passCheckInput.isEmpty()) {
            layout_passCheck.setErrorEnabled(false);
            layout_passCheck.setBoxStrokeColor(Color.BLACK);
        } else {
            if (!isValid) {
                layout_passCheck.setError("비밀번호가 일치하지 않습니다");
                layout_passCheck.setErrorIconDrawable(null);
            } else {
                layout_passCheck.setErrorEnabled(false);
                layout_passCheck.setBoxStrokeColor(Color.parseColor("#3B9B70"));
            }
        }
    }
}

