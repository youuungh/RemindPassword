package com.example.passwordmanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.passwordmanager.adapter.MainAdapter;
import com.example.passwordmanager.adapter.FavoriteAdapter;
import com.example.passwordmanager.model.Content;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.search.SearchBar;
import com.google.android.material.transition.MaterialSharedAxis;
import com.google.firebase.firestore.Query;

public class MainFragment extends Fragment {
    private FirestoreRecyclerOptions<Content> mOptions, fOptions;
    private MainAdapter mAdapter;
    private FavoriteAdapter favAdapter;
    private AppBarLayout appBarLayout;
    private RecyclerView rv_content;
    private SearchBar search_bar;
    private RelativeLayout main_empty_view, main_loading_view;
    private TextView tv_fav;
    private FloatingActionButton main_fab_write, main_fab_top;
    private boolean isSwitch = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEnterTransition(new MaterialSharedAxis(MaterialSharedAxis.Z, true));
        setExitTransition(new MaterialSharedAxis(MaterialSharedAxis.Z, false));
        setReenterTransition(new MaterialSharedAxis(MaterialSharedAxis.Z, true));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        main_empty_view = view.findViewById(R.id.main_view_empty);
        main_loading_view = view.findViewById(R.id.main_view_loading);
        tv_fav = view.findViewById(R.id.tv_favorite);
        appBarLayout = view.findViewById(R.id.main_layout_appbar);
        RecyclerView rv_favorite = view.findViewById(R.id.rv_favorites);
        rv_content = view.findViewById(R.id.rv_contents);

        search_bar = view.findViewById(R.id.main_searchbar);
        search_bar.setNavigationOnClickListener(v -> ((MainActivity) getActivity()).drawerLayout.open());
        search_bar.setOnClickListener(v -> {
            main_fab_write.hide();
            SearchFragment searchFragment = new SearchFragment();
            FragmentTransaction ft = getChildFragmentManager().beginTransaction();
            ft.add(R.id.layout_content, searchFragment).addToBackStack(null).commit();
        });
        search_bar.getMenu().clear();
        search_bar.inflateMenu(R.menu.menu_searchbar);
        search_bar.getMenu().getItem(0).setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_column) {
                if (!isSwitch) {
                    rv_content.setLayoutManager(new LinearLayoutManager(getContext()));
                    item.setIcon(R.drawable.ic_column_grid);
                } else {
                    rv_content.setLayoutManager(new GridLayoutManager(getContext(), 2));
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
            rv_content.scrollToPosition(0);
            appBarLayout.setExpanded(true);
            if (rv_content.getVerticalScrollbarPosition() == 0)
                main_fab_top.hide();
        });

        Query mquery = Utils.getContentReference().orderBy("timestamp", Query.Direction.DESCENDING);
        mquery.get().addOnCompleteListener(task -> {
            main_loading_view.setVisibility(View.GONE);
            onChangeEmptyView(mOptions.getSnapshots().isEmpty());
        });
        mOptions = new FirestoreRecyclerOptions.Builder<Content>()
                .setQuery(mquery, Content.class).build();
        mAdapter = new MainAdapter(mOptions, this);
        mAdapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);
        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                onChangeEmptyView(mOptions.getSnapshots().isEmpty());
                rv_content.scrollToPosition(0);
                appBarLayout.setExpanded(true);
            }
            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                onChangeEmptyView(mOptions.getSnapshots().isEmpty());
                if (mOptions.getSnapshots().isEmpty()) appBarLayout.setExpanded(true);
            }
        });
        mAdapter.notifyDataSetChanged();

        Query fquery = Utils.getFavoriteReference().orderBy("favorite");
        fquery.get().addOnCompleteListener(task -> {
            onChangeTextView(fOptions.getSnapshots().isEmpty());
        });
        fOptions = new FirestoreRecyclerOptions.Builder<Content>()
                .setQuery(fquery, Content.class).build();
        favAdapter = new FavoriteAdapter(fOptions, this);
        favAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                onChangeTextView(fOptions.getSnapshots().isEmpty());
                rv_content.scrollToPosition(0);
                appBarLayout.setExpanded(true);
            }
            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                onChangeTextView(fOptions.getSnapshots().isEmpty());
                if (fOptions.getSnapshots().isEmpty()) appBarLayout.setExpanded(true);
            }
        });
        favAdapter.notifyDataSetChanged();

        rv_favorite.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rv_favorite.setAdapter(favAdapter);
        rv_content.setHasFixedSize(true);
        rv_content.setAdapter(mAdapter);
        rv_content.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

    private void onChangeTextView(boolean flag) {
        tv_fav.setVisibility(flag ? View.GONE : View.VISIBLE);
    }

    private void onChangeEmptyView(boolean flag) {
        main_empty_view.setVisibility(flag ? View.VISIBLE : View.GONE);
    }

    private void onChangeMainMenuItem() {
        if (isSwitch) {
            rv_content.setLayoutManager(new LinearLayoutManager(getContext()));
            search_bar.getMenu().getItem(0).setIcon(R.drawable.ic_column_grid);
        } else {
            rv_content.setLayoutManager(new GridLayoutManager(getContext(), 2));
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
        mAdapter.startListening();
        favAdapter.startListening();
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences prefer = getActivity().getSharedPreferences("MAIN_MENU_STATE", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefer.edit();
        editor.putBoolean("SWITCH_DATA", isSwitch).apply();
    }
}