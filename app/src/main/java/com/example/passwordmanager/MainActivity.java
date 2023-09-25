package com.example.passwordmanager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.elevation.SurfaceColors;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.units.qual.A;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    FirebaseAuth fAuth;
    FirebaseUser fUser;
    DrawerLayout drawerLayout;
    NavigationView main_nav;
    MaterialButton button;
    SearchBar searchBar;
    SearchView searchView;
    TextView tv_userEmail, main_count, trash_count;
    MainFragment mFragment;

    @Override
    protected void onResume() {
        super.onResume();
        updateCounter();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(null);
        if (savedInstanceState == null) {
            showFragments(new MainFragment());
        }

        fAuth = FirebaseAuth.getInstance();
        fUser = fAuth.getCurrentUser();

        drawerLayout = findViewById(R.id.layout_drawer);
        searchBar = findViewById(R.id.main_searchbar);
        searchBar.setNavigationOnClickListener(v -> drawerLayout.open());
        searchView = findViewById(R.id.main_searchView);
        searchView.setupWithSearchBar(searchBar);

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
        } else if (id == R.id.nav_trash) {
            showFragments(new TrashFragment());
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
        ft.replace(R.id.layout_fragment, fragment);
        ft.commit();
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