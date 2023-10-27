package com.example.passwordmanager;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import com.example.passwordmanager.adapter.SearchAdapter;
import com.example.passwordmanager.model.Content;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.transition.MaterialSharedAxis;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.protobuf.NullValue;


public class SearchFragment extends Fragment {
    FirestoreRecyclerOptions<Content> content_options;
    SearchAdapter search_adapter;
    Query content_query;
    MaterialToolbar mToolbar;
    RecyclerView recycler_search;
    SearchView searchView;
    FloatingActionButton search_fab_top;
    LinearLayout search_emptyView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEnterTransition(new MaterialSharedAxis(MaterialSharedAxis.Z, true).setDuration(300));
        setReturnTransition(new MaterialSharedAxis(MaterialSharedAxis.Z, false).setDuration(300));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        ((MainActivity)getActivity()).drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        mToolbar = view.findViewById(R.id.search_toolbar);
        mToolbar.setNavigationOnClickListener(v -> {
            searchView.clearFocus();
            ((MainActivity)getActivity()).drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            ((MainActivity)getActivity()).fab_write.show();
            getParentFragmentManager().popBackStack();
        });

        recycler_search = view.findViewById(R.id.recycler_search);
        recycler_search.setHasFixedSize(true);
        recycler_search.setLayoutManager(new LinearLayoutManager(this.getContext()));

        searchView = view.findViewById(R.id.search_view);
        searchView.requestFocus();
        searchView.setOnQueryTextFocusChangeListener( (v, hasFocus) -> {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY);
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchData(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchData(newText);
                return false;
            }
        });

        search_emptyView = view.findViewById(R.id.search_view_empty);
        search_fab_top = view.findViewById(R.id.search_fab_top);
        search_fab_top.setOnClickListener(v -> {
            recycler_search.scrollToPosition(0);
            if (recycler_search.getVerticalScrollbarPosition() == 0)
                search_fab_top.hide();
        });

        recycler_search.addOnScrollListener(new RecyclerView.OnScrollListener() {
            final Handler handler = new Handler();
            final Runnable runnable = () -> search_fab_top.hide();
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    handler.postDelayed(runnable, 2000);
                }
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    searchView.clearFocus();
                    search_fab_top.show();
                    handler.removeCallbacks(runnable);
                } else if (dy < 0) {
                    searchView.clearFocus();
                    search_fab_top.show();
                    handler.removeCallbacks(runnable);
                    if (!recyclerView.canScrollVertically(-1))
                        search_fab_top.hide();
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        return view;
    }

    private void searchData(String s) {
        if (!s.isEmpty()) {
            content_query = Utils.getContentReference()
                    .orderBy("search")
                    .startAt(s.toLowerCase().trim())
                    .endAt(s.toLowerCase().trim() + "\uf8ff");
            content_query.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    search_emptyView.setVisibility(querySnapshot.isEmpty() ? View.VISIBLE : View.GONE);
                }
            });
            content_options = new FirestoreRecyclerOptions.Builder<Content>()
                    .setQuery(content_query, Content.class)
                    .build();
            search_adapter = new SearchAdapter(content_options, getContext());
            search_adapter.startListening();
            recycler_search.setAdapter(search_adapter);
        } else {
            content_options.getSnapshots().clear();
            search_emptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        ((MainActivity)getActivity()).drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity)getActivity()).drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }
}