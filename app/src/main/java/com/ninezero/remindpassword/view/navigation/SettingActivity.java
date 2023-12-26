package com.ninezero.remindpassword.view.navigation;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.ninezero.remindpassword.R;
import com.ninezero.remindpassword.util.Utils;
import com.ninezero.remindpassword.view.common.HomeActivity;
import com.ninezero.remindpassword.view.user.FingerPassFragment;
import com.ninezero.remindpassword.view.user.PassCheckFragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class SettingActivity extends AppCompatActivity implements FingerPassFragment.Callback {
    private FirebaseAuth fAuth;
    private MaterialSwitch switch_finger;
    private boolean isBiometricEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        initAuth();
        setupToolbar();
        setupPasswordSwitch();
        setupBiometricSwitch();
        setupChangePasswordButton();
        setupQuitButton();
        setupOpenGitHubButton();
    }

    private void initAuth() {
        isBiometricEnabled = getBiometric();
        fAuth = FirebaseAuth.getInstance();
        FirebaseUser fUser = fAuth.getCurrentUser();

        TextView tvUser = findViewById(R.id.tv_user);
        if (fUser != null) tvUser.setText(fUser.getEmail());
    }

    private void setupToolbar() {
        MaterialToolbar mToolbar = findViewById(R.id.setting_toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupPasswordSwitch() {
        MaterialSwitch switchPassword = findViewById(R.id.switch_password);
        switchPassword.setChecked(getPassCode().length() != 0);
    }

    private void setupBiometricSwitch() {
        switch_finger = findViewById(R.id.switch_finger);
        switch_finger.setChecked(getBiometric());
        switch_finger.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && !isBiometricEnabled) {
                switch_finger.setChecked(false);
                authenticateWithBiometric();
            } else if (isBiometricEnabled && !isChecked) {
                isBiometricEnabled = false;
                clearBiometric();
            }
        });
    }

    private void authenticateWithBiometric() {
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("지문 인증")
                .setNegativeButtonText("취소")
                .build();

        BiometricPrompt biometricPrompt = new BiometricPrompt(this, command ->
                new Handler(Looper.getMainLooper()).post(command),
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        isBiometricEnabled = true;
                        Utils.saveBiometric(getApplicationContext(), true);
                        switch_finger.setChecked(true);
                    }
                });

        biometricPrompt.authenticate(promptInfo);
    }

    private void setupChangePasswordButton() {
        Button buttonPassword = findViewById(R.id.button_password);
        buttonPassword.setOnClickListener(v -> {
            PassCheckFragment passCheckFragment = new PassCheckFragment();
            Bundle bundle = new Bundle();
            bundle.putBoolean("SET_PASSWORD", true);
            passCheckFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.layout_setting, passCheckFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void setupQuitButton() {
        Button buttonQuit = findViewById(R.id.button_quit);
        buttonQuit.setOnClickListener(v -> showQuitDialog());
    }

    private void setupOpenGitHubButton() {
        Button buttonDev = findViewById(R.id.button_dev);
        buttonDev.setOnClickListener(v -> openGitHubPage());
    }

    private void openGitHubPage() {
        String gitHubUrl = "https://github.com/youuungh";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(gitHubUrl));
        startActivity(intent);
    }

    private void showQuitDialog() {
        View customView = getLayoutInflater().inflate(R.layout.setting_quit_dialog, null);
        CheckBox checkBoxClearData = customView.findViewById(R.id.checkbox_clear_data);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.QuitAlertDialog)
                .setView(customView)
                .setIcon(R.drawable.ic_alert)
                .setCancelable(false)
                .setPositiveButton("확인", null)
                .setNegativeButton("취소", (dialog, which) -> dialog.cancel());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (checkBoxClearData.isChecked()) {
                QuitFirebaseAccount();
                alertDialog.dismiss();
            } else {
                Utils.showToast(getApplicationContext(), "동의 여부를 선택하세요");
            }
        });
    }

    private void QuitFirebaseAccount() {
        clearPassCode();
        clearBiometric();
        FirebaseUser fUser = fAuth.getCurrentUser();
        fUser.delete().addOnCompleteListener(task -> {
            Utils.showToast(getApplicationContext(), "계정이 완전히 삭제됨");
            navigateToHomeActivity();
        });
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

    @Override
    public void getCallback(boolean value) {
        if (value) {
            isBiometricEnabled = true;
            switch_finger.setChecked(true);
        }
    }
}