package com.example.passwordmanager.view.navigation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Button;
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

public class SettingActivity extends AppCompatActivity {
    private FirebaseAuth fAuth;
    private MaterialSwitch switch_password, switch_finger;
    private TextView tv_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        fAuth = FirebaseAuth.getInstance();
        FirebaseUser fUser = fAuth.getCurrentUser();

        tv_user = findViewById(R.id.tv_user);
        if (fUser != null) tv_user.setText(fUser.getEmail());

        MaterialToolbar mToolbar = findViewById(R.id.setting_toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(v -> onBackPressed());

        switch_password = findViewById(R.id.switch_password);

        switch_finger = findViewById(R.id.switch_finger);

        Button button_password = findViewById(R.id.button_password);
        button_password.setOnClickListener(v -> {
            PassCheckFragment passCheckFragment = new PassCheckFragment();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(android.R.id.content, passCheckFragment).commit();
        });

        Button button_quit = findViewById(R.id.button_quit);
        button_quit.setOnClickListener(v -> Utils.showSnack(findViewById(android.R.id.content), "클릭"));
    }
}