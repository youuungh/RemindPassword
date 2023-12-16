package com.example.passwordmanager.view.common;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import com.example.passwordmanager.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        FirebaseUser fUser = fAuth.getCurrentUser();

        if (hasPassCode() && fUser != null) {
            navigateToMainActivity();
        } else {
            fAuth.signOut();
        }

        Button home_signup = findViewById(R.id.home_signup);
        home_signup.setOnClickListener(view -> startActivity(new Intent(this, SignUpActivity.class)));

        Button home_login = findViewById(R.id.home_login);
        home_login.setOnClickListener(view -> startActivity(new Intent(this, LoginActivity.class)));
    }

    private boolean hasPassCode() {
        return getPassCode().length() != 0;
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private String getPassCode() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("PASSCODE_PREF", Context.MODE_PRIVATE);
        return pref.getString("PASSCODE", "");
    }
}