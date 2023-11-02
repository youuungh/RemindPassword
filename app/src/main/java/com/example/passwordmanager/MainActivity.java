package com.example.passwordmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.search.SearchBar;
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback;
import com.google.android.material.transition.platform.MaterialElevationScale;
import com.google.android.material.transition.platform.MaterialFade;
import com.google.android.material.transition.platform.MaterialFadeThrough;
import com.google.android.material.transition.platform.MaterialSharedAxis;
import com.google.android.material.transition.platform.SlideDistanceProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    FirebaseAuth fAuth;
    FirebaseUser fUser;
    DrawerLayout drawerLayout;
    NavigationView main_nav;
    MaterialButton button;
    TextView tv_userEmail, main_count, trash_count;
    FloatingActionButton fab_write, fab_top;
    boolean isSelect = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            showFragments(new MainFragment());
        }

        fAuth = FirebaseAuth.getInstance();
        fUser = fAuth.getCurrentUser();

        drawerLayout = findViewById(R.id.layout_drawer);
        main_nav = findViewById(R.id.main_nav);
        main_nav.setNavigationItemSelectedListener(this);
        main_nav.setCheckedItem(R.id.nav_main);
        main_nav.getMenu().getItem(0).setActionView(R.layout.menu_main_counter);
        main_nav.getMenu().getItem(1).setActionView(R.layout.menu_trash_counter);

        main_count = main_nav.getMenu().getItem(0).getActionView().findViewById(R.id.tv_main_count);
        trash_count = main_nav.getMenu().getItem(1).getActionView().findViewById(R.id.tv_trash_count);

        View header = main_nav.getHeaderView(0);
        tv_userEmail = header.findViewById(R.id.tv_userMail);
        if (fUser != null) {
            tv_userEmail.setText(fUser.getEmail());
        }

        fab_top = findViewById(R.id.main_fab_top);
        fab_write = findViewById(R.id.main_fab_write);
        fab_write.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddContentActivity.class);
            Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(this, fab_write, "fab").toBundle();
            startActivity(intent, bundle);
        });

        button = findViewById(R.id.nav_button);
        button.setOnClickListener(v -> {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.CustomAlertDialog)
                    .setMessage("로그아웃 하시겠습니까?")
                    .setCancelable(false)
                    .setPositiveButton("확인", (dialog, which) -> {
                        fAuth.signOut();
                        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
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

        int id = item.getItemId();
        if (id == R.id.nav_main) {
            showFragments(new MainFragment());
            isSelect = false;
            fab_write.show();
        } else if (id == R.id.nav_trash) {
            showFragments(new TrashFragment());
            isSelect = true;
            fab_write.hide();
        } else if (id == R.id.nav_setting) {
            //startActivity(new Intent(MainActivity.this, SettingActivity.class));
        }
        return true;
    }

    public void updateCounter() {
        Utils.getContentReference().get().addOnCompleteListener(task -> {
            if (task.isSuccessful())
                main_count.setText(String.valueOf(task.getResult().size()));
        });
        Utils.getTrashReference().get().addOnCompleteListener(task -> {
            if (task.isSuccessful())
                trash_count.setText(String.valueOf(task.getResult().size()));
        });
    }

    private void showFragments(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.replace(R.id.fragment_container, fragment).commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCounter();
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.close();
        } else {
            super.onBackPressed();
        }
    }
}