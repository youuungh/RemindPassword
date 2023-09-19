package com.example.passwordmanager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.example.passwordmanager.adapter.Adapter;
import com.example.passwordmanager.model.Content;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.search.SearchBar;
import com.google.firebase.firestore.Query;

public class MainFragment extends Fragment {
    FirestoreRecyclerOptions<Content> options;
    RecyclerView recyclerView;
    Adapter adapter;
    FloatingActionButton main_fab_write, main_fab_top;
    RelativeLayout main_emptyView;
    ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        main_emptyView = view.findViewById(R.id.main_view_empty);
        progressBar = view.findViewById(R.id.main_progressBar);

        ((MainActivity)getActivity()).searchBar.getMenu().clear();
        ((MainActivity)getActivity()).searchBar.inflateMenu(R.menu.menu_searchbar);

        Query query = Utils.getContentReference().orderBy("timestamp", Query.Direction.DESCENDING);
        query.get().addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            showEmptyView(options.getSnapshots().isEmpty());
        });

        options = new FirestoreRecyclerOptions.Builder<Content>()
                .setQuery(query, Content.class)
                .build();
        adapter = new Adapter(options);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                showEmptyView(options.getSnapshots().isEmpty());
                ((MainActivity)getActivity()).mainCounterChanged(String.valueOf(adapter.getItemCount()));
                recyclerView.smoothScrollToPosition(0);
                super.onItemRangeInserted(positionStart, itemCount);
            }
            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                showEmptyView(options.getSnapshots().isEmpty());
                ((MainActivity)getActivity()).mainCounterChanged(String.valueOf(adapter.getItemCount()));
                recyclerView.smoothScrollToPosition(0);
                super.onItemRangeRemoved(positionStart, itemCount);
            }
        });

        recyclerView = view.findViewById(R.id.recycler_contents);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        main_fab_write = view.findViewById(R.id.main_fab_write);
        main_fab_write.setOnClickListener(v -> startActivity(new Intent(getActivity(), AddContentActivity.class)));
        main_fab_top = view.findViewById(R.id.main_fab_top);
        main_fab_top.setOnClickListener(v -> recyclerView.smoothScrollToPosition(0));

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                Handler handler = new Handler();
                Runnable runnable = () -> main_fab_top.hide();

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    new Handler().postDelayed(() -> main_fab_write.show(), 500);
                    handler.removeCallbacks(runnable);
                    handler.postDelayed(runnable, 3000);
                }
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    main_fab_top.show();
                    main_fab_write.hide();
                } else if (dy < 0) {
                    main_fab_top.show();
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        return view;
    }

    private void showEmptyView(boolean flag) {
        main_emptyView.setVisibility(flag ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.startListening();
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

}