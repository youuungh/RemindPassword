package com.example.passwordmanager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.transition.Explode;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.passwordmanager.adapter.Adapter;
import com.example.passwordmanager.model.Content;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.search.SearchBar;
import com.google.firebase.firestore.Query;

public class MainFragment extends Fragment {
    FirestoreRecyclerOptions<Content> options;
    RecyclerView recyclerView;
    Adapter adapter;
    RelativeLayout main_emptyView, main_loadingView;
    FloatingActionButton main_fab_write, main_fab_top;
    boolean isSwitch = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        getActivity().getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        getActivity().getWindow().setExitTransition(new Explode());

        main_emptyView = view.findViewById(R.id.main_view_empty);
        main_loadingView = view.findViewById(R.id.main_view_loading);

        recyclerView = view.findViewById(R.id.recycler_contents);
        recyclerView.setHasFixedSize(true);

        ((MainActivity)getActivity()).searchBar.getMenu().clear();
        ((MainActivity)getActivity()).searchBar.inflateMenu(R.menu.menu_searchbar);
        ((MainActivity)getActivity()).searchBar.getMenu().getItem(0).setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_column) {
                if (!isSwitch) {
                    recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                    item.setIcon(R.drawable.ic_column_grid);
                } else {
                    recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
                    item.setIcon(R.drawable.ic_column_linear);
                }
                isSwitch = !isSwitch;
            }
            return false;
        });

        if (savedInstanceState == null) {
            SharedPreferences prefer = getActivity().getSharedPreferences("MAIN_MENU_STATE", Context.MODE_PRIVATE);
            isSwitch = prefer.getBoolean("SWITCH_DATA", true);
            onChangeMainMenuItem();
        } else {
            isSwitch = savedInstanceState.getBoolean("MAIN_MENU_STATE");
            onChangeMainMenuItem();
        }

        main_fab_write = ((MainActivity)getActivity()).fab_write;
        main_fab_top = ((MainActivity)getActivity()).fab_top;
        main_fab_write.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), AddContentActivity.class));
        });
        main_fab_top.setOnClickListener(v -> {
            recyclerView.scrollToPosition(0);
            ((MainActivity)getActivity()).appBarLayout.setExpanded(true);
            if (recyclerView.getVerticalScrollbarPosition() == 0)
                main_fab_top.hide();
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            final Handler handler = new Handler();
            final Runnable runnable = () -> main_fab_top.hide();
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    new Handler().postDelayed(() -> main_fab_write.show(), 500);
                    handler.postDelayed(runnable, 3000);
                }
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    main_fab_top.show();
                    main_fab_write.hide();
                    handler.removeCallbacks(runnable);
                } else if (dy < 0) {
                    main_fab_top.show();
                    handler.removeCallbacks(runnable);
                    if (!recyclerView.canScrollVertically(-1))
                        main_fab_top.hide();
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        return view;
    }

    private void showEmptyView(boolean flag) {
        main_emptyView.setVisibility(flag ? View.VISIBLE : View.GONE);
    }

    private void onChangeMainMenuItem() {
        if (isSwitch) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            ((MainActivity)getActivity()).searchBar.getMenu().getItem(0).setIcon(R.drawable.ic_column_grid);
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
            ((MainActivity)getActivity()).searchBar.getMenu().getItem(0).setIcon(R.drawable.ic_column_linear);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("MAIN_MENU_STATE", isSwitch);
    }

    @Override
    public void onStart() {
        super.onStart();
        Query query = Utils.getContentReference().orderBy("timestamp", Query.Direction.DESCENDING);
        query.get().addOnCompleteListener(task -> {
            main_loadingView.setVisibility(View.GONE);
            showEmptyView(options.getSnapshots().isEmpty());
        });

        options = new FirestoreRecyclerOptions.Builder<Content>()
                .setQuery(query, Content.class)
                .build();
        adapter = new Adapter(options, this);
        recyclerView.setAdapter(adapter);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                showEmptyView(options.getSnapshots().isEmpty());
                super.onItemRangeInserted(positionStart, itemCount);
            }
            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                showEmptyView(options.getSnapshots().isEmpty());
                super.onItemRangeRemoved(positionStart, itemCount);
            }
        });
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.startListening();
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences prefer = getActivity().getSharedPreferences("MAIN_MENU_STATE", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefer.edit();
        editor.putBoolean("SWITCH_DATA", isSwitch).apply();
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }
}