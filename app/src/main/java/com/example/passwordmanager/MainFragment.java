package com.example.passwordmanager;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.passwordmanager.adapter.Adapter;
import com.example.passwordmanager.model.Content;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends Fragment {
    FirestoreRecyclerOptions<Content> options;
    Adapter adapter;
    RecyclerView recycler_content;
    SearchBar search_bar;
    RelativeLayout main_empty_view, main_loading_view;
    FloatingActionButton main_fab_write, main_fab_top;
    boolean isSwitch = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        main_empty_view = view.findViewById(R.id.main_view_empty);
        main_loading_view = view.findViewById(R.id.main_view_loading);

        recycler_content = view.findViewById(R.id.recycler_contents);
        recycler_content.setHasFixedSize(true);

        search_bar = ((MainActivity)getActivity()).searchBar;
        search_bar.getMenu().clear();
        search_bar.inflateMenu(R.menu.menu_searchbar);
        search_bar.getMenu().getItem(0).setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_column) {
                if (!isSwitch) {
                    recycler_content.setLayoutManager(new LinearLayoutManager(this.getContext()));
                    item.setIcon(R.drawable.ic_column_grid);
                } else {
                    recycler_content.setLayoutManager(new GridLayoutManager(this.getContext(), 2));
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
        main_fab_top.setOnClickListener(v -> {
            recycler_content.scrollToPosition(0);
            ((MainActivity)getActivity()).appBarLayout.setExpanded(true);
            if (recycler_content.getVerticalScrollbarPosition() == 0)
                main_fab_top.hide();
        });

        recycler_content.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
        main_empty_view.setVisibility(flag ? View.VISIBLE : View.GONE);
    }

    private void onChangeMainMenuItem() {
        if (isSwitch) {
            recycler_content.setLayoutManager(new LinearLayoutManager(this.getContext()));
            search_bar.getMenu().getItem(0).setIcon(R.drawable.ic_column_grid);
        } else {
            recycler_content.setLayoutManager(new GridLayoutManager(this.getContext(), 2));
            search_bar.getMenu().getItem(0).setIcon(R.drawable.ic_column_linear);
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
            main_loading_view.setVisibility(View.GONE);
            showEmptyView(options.getSnapshots().isEmpty());
        });
        options = new FirestoreRecyclerOptions.Builder<Content>()
                .setQuery(query, Content.class)
                .build();
        adapter = new Adapter(options, getContext());
        recycler_content.setAdapter(adapter);
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
        adapter.notifyDataSetChanged();
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
        //adapter.notifyDataSetChanged();
    }
}