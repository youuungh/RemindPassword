package com.example.passwordmanager;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

public class HomeActivity extends AppCompatActivity {
    Button home_signup, home_login;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Window window = getWindow();
        window.setStatusBarColor(Color.TRANSPARENT);
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                        | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        );
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        home_signup = findViewById(R.id.home_signup);
        home_signup.setOnClickListener(view -> {
            startActivity(new Intent(this, SignUpActivity.class));
        });

        home_login = findViewById(R.id.home_login);
        home_login.setOnClickListener(view -> {
            startActivity(new Intent(this, LoginActivity.class));
        });
    }
}