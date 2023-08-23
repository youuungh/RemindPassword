package com.example.passwordmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
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
    FirebaseAuth fAuth;

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
        edt_password = findViewById(R.id.signup_password);
        edt_passCheck = findViewById(R.id.signup_passCheck);

        //edt_email.addTextChangedListener(signUpTextWatcher);

        tv_login = findViewById(R.id.tv_login);
        tv_login.setPaintFlags(tv_login.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tv_login.setOnClickListener(view -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        button = findViewById(R.id.signup_button);
        button.setOnClickListener(view -> signUp());

        progressBar = findViewById(R.id.signup_progressBar);
    }


    void signUp() {
        String email = edt_email.getText().toString().trim();
        String password = edt_password.getText().toString().trim();
        String passCheck = edt_passCheck.getText().toString().trim();

        boolean isValidated = createValidateData(email, password, passCheck);
        if(!isValidated) {
            return;
        }
        signUpInFirebase(email, password);
    }

    void signUpInFirebase(String email, String password) {
        signUpChangeInProgress(true);
        fAuth = FirebaseAuth.getInstance();
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
                            Toast.makeText(SignUpActivity.this, "다시 시도하세요", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    boolean createValidateData(String email, String password, String passCheck) {
        if(password.isEmpty()) {
            edt_password.setError("비밀번호를 입력하세요");
            return false;
        }
        if(password.length() < 6 || password.length() > 15) {
            edt_password.setError("비밀번호는 8글자 이상 15자 이하여야 합니다");
            return false;
        }
        if(!password.equals(passCheck)) {
            edt_passCheck.setError("비밀번호가 일치하지 않습니다");
            return false;
        }
        return true;
    }

    void signUpChangeInProgress(boolean inProgress) {
        if (inProgress) {
            progressBar.setVisibility(View.VISIBLE);
            button.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            button.setVisibility(View.VISIBLE);
        }
    }
}