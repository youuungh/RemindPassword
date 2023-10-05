package com.example.passwordmanager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {
    TextInputLayout layout_reset;
    TextInputEditText edt_reset;
    MaterialToolbar mToolbar;
    Button button;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        mToolbar = findViewById(R.id.reset_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mToolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
        });

        layout_reset = findViewById(R.id.reset_layout_email);
        edt_reset = findViewById(R.id.reset_email);
        edt_reset.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        edt_reset.addTextChangedListener(new fPasswordTextWatcher(edt_reset));

        button = findViewById(R.id.reset_button);
        progressBar = findViewById(R.id.reset_progressBar);
    }

    private void resetInFirebase(String email) {
        resetChangeInProgress(true);
        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        fAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Utils.showToast(ResetPasswordActivity.this, "이메일을 발송했습니다");
                        finish();
                    } else {
                        resetChangeInProgress(false);
                        Utils.showSnack(findViewById(R.id.resetScreen), "계정 정보를 찾을 수 없습니다");
                    }
                });
    }

    private void resetChangeInProgress(boolean inProgress) {
        progressBar.setVisibility(inProgress ? View.VISIBLE : View.GONE);
    }

    private class fPasswordTextWatcher implements TextWatcher {
        private View v;

        private fPasswordTextWatcher(View v) {
            this.v = v;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String fPasswordInput = edt_reset.getText().toString().trim();

            button.setEnabled(Patterns.EMAIL_ADDRESS.matcher(fPasswordInput).matches());
            button.setOnClickListener(v -> {
                edt_reset.clearFocus();
                InputMethodManager manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                View focusedView = getCurrentFocus();
                if (focusedView != null)
                    manager.hideSoftInputFromWindow(focusedView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                resetInFirebase(fPasswordInput);
            });
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }
}

