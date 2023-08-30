package com.example.passwordmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;
    NavigationView main_nav;
    MaterialToolbar mToolbar;
    MaterialButton button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        showFragments(new MainFragment());

        mToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(null);

        drawerLayout = findViewById(R.id.layout_drawer);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, mToolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        main_nav = findViewById(R.id.main_nav);
        main_nav.setNavigationItemSelectedListener(this);
        main_nav.setCheckedItem(R.id.nav_main);
        main_nav.getMenu().getItem(0).setActionView(R.layout.menu_main_counter);
        main_nav.getMenu().getItem(1).setActionView(R.layout.menu_trash_counter);

        button = findViewById(R.id.nav_button);
        button.setOnClickListener(v -> {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                    .setMessage("로그아웃 하시겠습니까?")
                    .setPositiveButton("확인", (dialog, which) -> {
                        FirebaseAuth.getInstance().signOut();
                        Utility.showToast(this, "로그아웃 되었습니다");
                        startActivity(new Intent(MainActivity.this, HomeActivity.class));
                        finish();
                    })
                    .setNegativeButton("취소", (dialog, which) -> {
                        dialog.cancel();
                    });
            builder.create();
            builder.show();
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);

        switch (item.getItemId()) {
            case R.id.nav_main:
                showFragments(new MainFragment());
                break;
            case R.id.nav_trash:
                showFragments(new TrashFragment());
                break;
            case R.id.nav_setting:
                //startActivity(new Intent(MainActivity.this, SettingActivity.class);
                break;
        }
        return true;
    }

    private void showFragments(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.layout_fragment, fragment);
        ft.commit();
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }
}