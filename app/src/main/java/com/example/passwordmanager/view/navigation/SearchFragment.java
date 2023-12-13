package com.example.passwordmanager.view.navigation;

import android.content.Context;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import com.example.passwordmanager.R;
import com.example.passwordmanager.util.Utils;
import com.example.passwordmanager.adapter.SearchAdapter;
import com.example.passwordmanager.model.Content;
import com.example.passwordmanager.view.common.MainActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.transition.MaterialSharedAxis;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;


public class SearchFragment extends Fragment {
    private SearchAdapter search_adapter;
    private RecyclerView recycler_search;
    private SearchView searchView;
    private FloatingActionButton search_fab_top;
    private LinearLayout search_emptyView;
    private InputMethodManager imm;
    private final List<Content> searchList = new ArrayList<>();
    private final Query content_query =  Utils.getContentReference().orderBy("timestamp", Query.Direction.DESCENDING);
    private final Query fav_query = Utils.getFavoriteReference().orderBy("timestamp", Query.Direction.DESCENDING);

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        requireActivity().getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
    }

    private final OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            searchView.clearFocus();
            ((MainActivity)getActivity()).drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            ((MainActivity)getActivity()).fab_write.show();
            getParentFragmentManager().popBackStack();
        }
    };

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
        ((MainActivity) getActivity()).drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        MaterialToolbar mToolbar = view.findViewById(R.id.search_toolbar);
        mToolbar.setNavigationOnClickListener(v -> {
            searchView.clearFocus();
            ((MainActivity) getActivity()).drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            ((MainActivity) getActivity()).fab_write.show();
            getParentFragmentManager().popBackStack();
        });

        recycler_search = view.findViewById(R.id.recycler_search);
        recycler_search.setHasFixedSize(true);
        recycler_search.setLayoutManager(new LinearLayoutManager(getContext()));
        search_adapter = new SearchAdapter(this, searchList);
        recycler_search.setAdapter(search_adapter);

        searchView = view.findViewById(R.id.search_view);
        searchView.requestFocus();
        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                imm.showSoftInput(view.findFocus(), InputMethodManager.SHOW_IMPLICIT);
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
            final Handler handler = new Handler(Looper.getMainLooper());
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
                    imm.hideSoftInputFromWindow(getView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    search_fab_top.show();
                    handler.removeCallbacks(runnable);
                } else if (dy < 0) {
                    searchView.clearFocus();
                    imm.hideSoftInputFromWindow(getView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
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

    public void showEmptyView(boolean isEmpty) {
        search_emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity)getActivity()).drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        ((MainActivity)getActivity()).fab_write.hide();
        content_query.get().addOnCompleteListener(task -> {
            searchList.clear();
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    searchList.add(document.toObject(Content.class));
                }
                fav_query.get().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task1.getResult()) {
                            searchList.add(document.toObject(Content.class));
                        }
                        search_adapter.notifyDataSetChanged();
                    }
                });
            }
        });
        searchView.requestFocus();
        searchView.setQuery("", true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                search_adapter.getFilter().filter(newText);
                return true;
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        ((MainActivity)getActivity()).drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }
}