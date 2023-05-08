package com.example.passwordmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class CreateAccountActivity extends AppCompatActivity {
    TextInputEditText create_email, create_password, create_passCheck;
    ProgressBar progressBar;
    Button button_register;
    TextView tv_login;
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 상태바, 네비게이션
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Color.TRANSPARENT);
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                            | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            );
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        button_register = findViewById(R.id.button_register);
        create_email = findViewById(R.id.create_email);
        create_password = findViewById(R.id.create_password);
        create_passCheck = findViewById(R.id.create_passCheck);
        progressBar = findViewById(R.id.progressBar);

        // 로그인 화면으로
        tv_login = findViewById(R.id.tv_login);
        tv_login.setPaintFlags(tv_login.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tv_login.setOnClickListener(view -> {
            finish();
        });

        // 등록 버튼
        button_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAccount();
            }
        });
    }

    void createAccount() {
        String email = create_email.getText().toString().trim();
        String password = create_password.getText().toString().trim();
        String passCheck = create_passCheck.getText().toString().trim();

        boolean isValidated = createValidateData(email, password, passCheck);
        if(!isValidated) {
            return;
        }
        createAccountInFirebase(email, password);
    }

    // 계정 등록
    void createAccountInFirebase(String email, String password) {
        createChangeInProgress(true);
        fAuth = FirebaseAuth.getInstance();
        fAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(CreateAccountActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        createChangeInProgress(false);

                        if (task.isSuccessful()) {
                            Toast.makeText(CreateAccountActivity.this, "등록 성공, 이메일을 발송했습니다", Toast.LENGTH_LONG).show();
                            fAuth.getCurrentUser().sendEmailVerification();
                            fAuth.signOut();
                            onBackPressed();
                        } else {
                            Toast.makeText(CreateAccountActivity.this, "다시 시도하세요", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    void createChangeInProgress(boolean inProgress) {
        if (inProgress) {
            progressBar.setVisibility(View.VISIBLE);
            button_register.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            button_register.setVisibility(View.VISIBLE);
        }
    }

    boolean createValidateData(String email, String password, String passCheck) {
        // 데이터 유효성 검사
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            create_email.setError("유효하지 않은 이메일입니다");
            return false;
        }
        if(password.isEmpty()) {
            create_password.setError("비밀번호를 입력하세요");
            return false;
        }
        if(password.length() < 6 || password.length() > 15) {
            create_password.setError("비밀번호는 8글자 이상 15자 이하여야 합니다");
            return false;
        }
        if(!password.equals(passCheck)) {
            create_passCheck.setError("비밀번호가 일치하지 않습니다");
            return false;
        }
        return true;
    }
}