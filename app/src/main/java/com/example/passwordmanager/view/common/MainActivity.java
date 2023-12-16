package com.example.passwordmanager.view.common;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.core.view.WindowCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.passwordmanager.R;
import com.example.passwordmanager.util.Utils;
import com.example.passwordmanager.view.navigation.AddContentActivity;
import com.example.passwordmanager.view.navigation.MainFragment;
import com.example.passwordmanager.view.navigation.SettingActivity;
import com.example.passwordmanager.view.navigation.TrashFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.transition.platform.MaterialElevationScale;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private FirebaseAuth fAuth;
    public NavigationView main_nav;
    private TextView main_count;
    private TextView trash_count;
    public DrawerLayout drawerLayout;
    public FloatingActionButton fab_write;
    public FloatingActionButton fab_top;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setEnterTransition(new MaterialElevationScale(true));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fAuth = FirebaseAuth.getInstance();
        FirebaseUser fUser = fAuth.getCurrentUser();

        initializeUI();

        if (savedInstanceState == null) showFragments(new MainFragment());

        if (fUser != null) {
            setHeaderEmail(fUser.getEmail());
        }

        setupFabWrite();

        setupLogoutButton();
    }

    private void initializeUI() {
        drawerLayout = findViewById(R.id.layout_drawer);
        main_nav = findViewById(R.id.main_nav);
        main_nav.setNavigationItemSelectedListener(this);
        main_nav.setCheckedItem(R.id.nav_main);
        main_nav.getMenu().getItem(0).setActionView(R.layout.menu_main_counter);
        main_nav.getMenu().getItem(1).setActionView(R.layout.menu_trash_counter);

        main_count = main_nav.getMenu().getItem(0).getActionView().findViewById(R.id.tv_main_count);
        trash_count = main_nav.getMenu().getItem(1).getActionView().findViewById(R.id.tv_trash_count);

        fab_top = findViewById(R.id.main_fab_top);
        fab_write = findViewById(R.id.main_fab_write);
    }

    private void setHeaderEmail(String email) {
        View header = main_nav.getHeaderView(0);
        TextView tv_userEmail = header.findViewById(R.id.tv_userMail);
        tv_userEmail.setText(email);
    }

    private void setupFabWrite() {
        fab_write.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddContentActivity.class);
            Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(this).toBundle();
            startActivity(intent, bundle);
        });
    }

    private void setupLogoutButton() {
        MaterialButton button = findViewById(R.id.nav_button);
        button.setOnClickListener(v -> showLogoutDialog());
    }

    private void showLogoutDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.CustomAlertDialog)
                .setMessage("로그아웃 하시겠습니까?")
                .setCancelable(false)
                .setPositiveButton("확인", (dialog, which) -> {
                    clearPassCode();
                    clearBiometric();
                    fAuth.signOut();
                    navigateToHomeActivity();
                })
                .setNegativeButton("취소", (dialog, which) -> {
                    dialog.cancel();
                });
        builder.create().show();
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
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void showFragments(Fragment fragment) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            getSupportFragmentManager().beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .replace(R.id.fragment_container, fragment).commit();
        }, 200);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCounter();
        if (main_nav.getCheckedItem().getItemId() == R.id.nav_setting) {
            main_nav.setCheckedItem(R.id.nav_main);
            MainFragment mainFragment = new MainFragment();
            getSupportFragmentManager().beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .replace(R.id.fragment_container, mainFragment).commit();
        }
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.close();
        } else {
            super.onBackPressed();
        }
    }

    public void updateCounter() {
        Utils.getContentReference().get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                int value = task.getResult().size();
                Utils.getFavoriteReference().get().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        main_count.setText(String.valueOf(value + task1.getResult().size()));
                    }
                });
            }
        });
        Utils.getTrashReference().get().addOnCompleteListener(task -> {
            if (task.isSuccessful())
                trash_count.setText(String.valueOf(task.getResult().size()));
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);
        int id = item.getItemId();
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (id == R.id.nav_main && !(fragment instanceof MainFragment)) {
            showFragments(new MainFragment());
            fab_write.show();
        } else if (id == R.id.nav_trash && !(fragment instanceof TrashFragment)) {
            showFragments(new TrashFragment());
            fab_write.hide();
        } else if (id == R.id.nav_setting) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                startActivity(new Intent(MainActivity.this, SettingActivity.class));
            }, 200);
        }
        return true;
    }
}