package com.example.passwordmanager.view.navigation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.passwordmanager.R;
import com.example.passwordmanager.util.Utils;
import com.example.passwordmanager.view.common.HomeActivity;
import com.example.passwordmanager.view.user.FingerPassFragment;
import com.example.passwordmanager.view.user.PassCheckFragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class SettingActivity extends AppCompatActivity implements FingerPassFragment.Callback {
    private FirebaseAuth fAuth;
    private MaterialSwitch switch_finger;
    private boolean isEnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        if (getBiometric()) {
            isEnable = true;
        } else {
            isEnable = false;
        }

        fAuth = FirebaseAuth.getInstance();
        FirebaseUser fUser = fAuth.getCurrentUser();

        TextView tv_user = findViewById(R.id.tv_user);
        if (fUser != null) tv_user.setText(fUser.getEmail());

        MaterialToolbar mToolbar = findViewById(R.id.setting_toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(v -> onBackPressed());

        MaterialSwitch switch_password = findViewById(R.id.switch_password);
        if (getPassCode().length() != 0) {
            switch_password.setChecked(true);
        }

        Log.d("getBiometric()", ""+getBiometric());
        Log.d("isEnable", ""+isEnable);
        switch_finger = findViewById(R.id.switch_finger);
        switch_finger.setChecked(getBiometric());

        switch_finger.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && !isEnable) {
                switch_finger.setChecked(false);
                authenticateWithBiometric();
            } else if (isEnable && !isChecked){
                isEnable = false;
                clearBiometric();
            }
        });

        Button button_password = findViewById(R.id.button_password);
        button_password.setOnClickListener(v -> {
            PassCheckFragment passCheckFragment = new PassCheckFragment();
            Bundle bundle = new Bundle();
            bundle.putBoolean("SET_PASSWORD", true);
            passCheckFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.layout_setting, passCheckFragment)
                    .addToBackStack(null)
                    .commit();
        });

        Button button_quit = findViewById(R.id.button_quit);
        button_quit.setOnClickListener(v -> showQuitDialog());
    }

    private void authenticateWithBiometric() {
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("지문 인증")
                .setNegativeButtonText("취소")
                .build();

        BiometricPrompt biometricPrompt = new BiometricPrompt(this, command -> new Handler(Looper.getMainLooper()).post(command), new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                isEnable = true;
                Utils.saveBiometric(getApplicationContext(), true);
                switch_finger.setChecked(true);
            }
        });
        biometricPrompt.authenticate(promptInfo);
    }

    @Override
    public void getCallback(boolean value) {
        if (value) {
            isEnable = true;
            switch_finger.setChecked(true);
        }
    }

    private void showQuitDialog() {
        View customView = getLayoutInflater().inflate(R.layout.quit_dialog, null);
        CheckBox checkBoxClearData = customView.findViewById(R.id.checkbox_clear_data);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.QuitAlertDialog)
                .setView(customView)
                .setIcon(R.drawable.ic_alert)
                .setCancelable(false)
                .setPositiveButton("확인", (dialog, which) -> {
                    if (checkBoxClearData.isChecked()) {
//                        clearPassCode();
//                        clearBiometric();
//                        fAuth.signOut();
//                        navigateToHomeActivity();
                        Utils.showSnack(findViewById(R.id.layout_setting), "삭제 완료");
                        dialog.dismiss();
                    } else {
                        Utils.showSnack(findViewById(android.R.id.content), "삭제 동의에 선택해주세요");
                    }
                })
                .setNegativeButton("취소", (dialog, which) -> {
                    dialog.cancel();
                });
        builder.create().show();
    }

    private String getPassCode() {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("PASSCODE_PREF", Context.MODE_PRIVATE);
        return prefs.getString("PASSCODE", "");
    }

    private boolean getBiometric() {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("BIOMETRIC_PREF", Context.MODE_PRIVATE);
        return prefs.getBoolean("BIOMETRIC", false);
    }

    private void clearPassCode() {
        SharedPreferences pref = getSharedPreferences("PASSCODE_PREF", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.apply();
    }

    private void clearBiometric() {
        SharedPreferences prefs = getSharedPreferences("BIOMETRIC_PREF", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }

    private void navigateToHomeActivity() {
        Intent intent = new Intent(SettingActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}