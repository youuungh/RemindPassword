package com.example.passwordmanager.view.navigation;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.passwordmanager.R;
import com.example.passwordmanager.util.Utils;
import com.example.passwordmanager.adapter.MainAdapter;
import com.example.passwordmanager.adapter.FavoriteAdapter;
import com.example.passwordmanager.model.Content;
import com.example.passwordmanager.view.common.MainActivity;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.search.SearchBar;
import com.google.android.material.transition.MaterialSharedAxis;
import com.google.android.material.transition.platform.MaterialFade;
import com.google.firebase.firestore.Query;

public class MainFragment extends Fragment {
    private FirestoreRecyclerOptions<Content> mOptions, fOptions;
    private MainAdapter mAdapter;
    private FavoriteAdapter favAdapter;
    private AppBarLayout appBarLayout;
    private MaterialCardView cv_favorite;
    private RecyclerView rv_content, rv_favorite;
    private SearchBar search_bar;
    private RelativeLayout main_empty_view, main_loading_view;
    private ImageView expand_button;
    private FloatingActionButton main_fab_write, main_fab_top;
    private boolean isSwitch = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSharedAxisTransitions();
    }

    private void setSharedAxisTransitions() {
        setEnterTransition(new MaterialSharedAxis(MaterialSharedAxis.Z, true));
        setExitTransition(new MaterialSharedAxis(MaterialSharedAxis.Z, false));
        setReenterTransition(new MaterialSharedAxis(MaterialSharedAxis.Z, true));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        init(view);
        setSearchBarClickListener();
        restoreSwitchState(savedInstanceState);

        setExpandButtonClickListener();
        setFabButtonClickEvents();
        initAdapters();

        return view;
    }

    private void init(View view) {
        main_empty_view = view.findViewById(R.id.main_view_empty);
        main_loading_view = view.findViewById(R.id.main_view_loading);
        appBarLayout = view.findViewById(R.id.main_layout_appbar);
        cv_favorite = view.findViewById(R.id.cv_favorite);
        rv_favorite = view.findViewById(R.id.rv_favorites);
        rv_content = view.findViewById(R.id.rv_contents);
        search_bar = view.findViewById(R.id.main_searchbar);
        expand_button = view.findViewById(R.id.button_expand);
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            main_fab_write = mainActivity.fab_write;
            main_fab_top = mainActivity.fab_top;
        }
    }

    private void setSearchBarClickListener() {
        search_bar.setNavigationOnClickListener(v -> ((MainActivity) getActivity()).drawerLayout.open());
        search_bar.setOnClickListener(v -> openSearchFragment());
        configureSearchBarMenu();
    }

    private void openSearchFragment() {
        main_fab_write.hide();
        SearchFragment searchFragment = new SearchFragment();
        getChildFragmentManager().beginTransaction()
                .add(R.id.layout_content, searchFragment).addToBackStack(null).commit();
    }

    private void configureSearchBarMenu() {
        search_bar.getMenu().clear();
        search_bar.inflateMenu(R.menu.menu_searchbar);
        search_bar.getMenu().getItem(0).setOnMenuItemClickListener(item -> {
            handleSearchBarMenuItemClick(item);
            return false;
        });
    }

    private void handleSearchBarMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_column) {
            toggleRecyclerViewLayout();
        }
    }

    private void toggleRecyclerViewLayout() {
        isSwitch = !isSwitch;
        updateRecyclerViewLayout();
    }

    private void updateRecyclerViewLayout() {
        if (isSwitch) {
            rv_content.setLayoutManager(new LinearLayoutManager(getContext()));
            search_bar.getMenu().getItem(0).setIcon(R.drawable.ic_column_grid);
        } else {
            rv_content.setLayoutManager(new GridLayoutManager(getContext(), 2));
            search_bar.getMenu().getItem(0).setIcon(R.drawable.ic_column_linear);
        }
    }

    private void restoreSwitchState(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            SharedPreferences prefer = getActivity().getSharedPreferences("MAIN_MENU_STATE", Context.MODE_PRIVATE);
            isSwitch = prefer.getBoolean("SWITCH_DATA", true);
        } else {
            isSwitch = savedInstanceState.getBoolean("MAIN_MENU_STATE");
        }
        updateRecyclerViewLayout();
    }

    private void setExpandButtonClickListener() {
        expand_button.setOnClickListener(v -> onExpandRecycler());
    }

    private void onExpandRecycler() {
        if (rv_favorite.getVisibility() == View.GONE) {
            TransitionManager.beginDelayedTransition(cv_favorite, new MaterialFade());
            rv_favorite.setVisibility(View.VISIBLE);
            expand_button.setImageResource(R.drawable.ic_expand_up);
        } else {
            TransitionManager.beginDelayedTransition(cv_favorite, new MaterialFade());
            rv_favorite.setVisibility(View.GONE);
            expand_button.setImageResource(R.drawable.ic_expand_down);
        }
    }

    private void setFabButtonClickEvents() {
        main_fab_top.setOnClickListener(v -> {
            rv_content.scrollToPosition(0);
            appBarLayout.setExpanded(true);
            if (rv_content.getVerticalScrollbarPosition() == 0)
                main_fab_top.hide();
        });
    }

    private void initAdapters() {
        // content
        Query mquery = Utils.getContentReference().orderBy("timestamp", Query.Direction.DESCENDING);
        mquery.get().addOnCompleteListener(task -> {
            main_loading_view.setVisibility(View.GONE);
            onChangeEmptyView();
        });
        mOptions = new FirestoreRecyclerOptions.Builder<Content>()
                .setQuery(mquery, Content.class).build();
        mAdapter = new MainAdapter(mOptions, this);
        mAdapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);
        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                onChangeEmptyView();
                rv_content.scrollToPosition(0);
                appBarLayout.setExpanded(true);
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                onChangeEmptyView();
                if (mOptions.getSnapshots().isEmpty()) appBarLayout.setExpanded(true);
            }
        });
        mAdapter.notifyDataSetChanged();

        // favorite
        Query fquery = Utils.getFavoriteReference().orderBy("timestamp", Query.Direction.DESCENDING);
        fquery.get().addOnCompleteListener(task -> onChangeFavView(fOptions.getSnapshots().isEmpty()));
        fOptions = new FirestoreRecyclerOptions.Builder<Content>()
                .setQuery(fquery, Content.class).build();
        favAdapter = new FavoriteAdapter(fOptions, this);
        favAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                if (rv_favorite.getVisibility() == View.GONE) {
                    rv_favorite.setVisibility(View.VISIBLE);
                    expand_button.setImageResource(R.drawable.ic_expand_up);
                }
                onChangeFavView(fOptions.getSnapshots().isEmpty());
                rv_content.scrollToPosition(0);
                rv_favorite.scrollToPosition(0);
                appBarLayout.setExpanded(true);
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                onChangeFavView(fOptions.getSnapshots().isEmpty());
                if (fOptions.getSnapshots().isEmpty()) appBarLayout.setExpanded(true);
                rv_favorite.scrollToPosition(0);
            }
        });
        favAdapter.notifyDataSetChanged();

        rv_favorite.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rv_favorite.setAdapter(favAdapter);
        rv_content.setHasFixedSize(true);
        rv_content.setAdapter(mAdapter);
        rv_content.addOnScrollListener(new RecyclerView.OnScrollListener() {
            final Handler handler = new Handler(Looper.getMainLooper());
            final Runnable runnable = () -> main_fab_top.hide();

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> main_fab_write.show(), 500);
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
    }

    private void onChangeFavView(boolean flag) {
        cv_favorite.setVisibility(flag ? View.GONE : View.VISIBLE);
    }

    private void onChangeEmptyView() {
        if (mOptions.getSnapshots().isEmpty() && fOptions.getSnapshots().isEmpty()) {
            main_empty_view.setVisibility(View.VISIBLE);
        } else {
            main_empty_view.setVisibility(View.GONE);
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
        saveSwitchState();
    }

    private void saveSwitchState() {
        SharedPreferences prefer = getActivity().getSharedPreferences("MAIN_MENU_STATE", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefer.edit();
        editor.putBoolean("SWITCH_DATA", isSwitch).apply();
    }
}