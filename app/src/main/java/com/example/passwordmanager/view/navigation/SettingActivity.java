package com.example.passwordmanager.view.navigation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.Menu;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.example.passwordmanager.R;
import com.example.passwordmanager.util.Utils;
import com.example.passwordmanager.view.common.MainActivity;
import com.example.passwordmanager.view.user.PassCheckFragment;
import com.example.passwordmanager.view.user.PassCodeFragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.Executor;

public class SettingActivity extends AppCompatActivity {
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
        button_quit.setOnClickListener(v -> Utils.showSnack(findViewById(android.R.id.content), "클릭"));
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


    private String getPassCode() {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("PASSCODE_PREF", Context.MODE_PRIVATE);
        return prefs.getString("PASSCODE", "");
    }

    private boolean getBiometric() {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("BIOMETRIC_PREF", Context.MODE_PRIVATE);
        return prefs.getBoolean("BIOMETRIC", false);
    }

    private void clearBiometric() {
        SharedPreferences prefs = getSharedPreferences("BIOMETRIC_PREF", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }
}