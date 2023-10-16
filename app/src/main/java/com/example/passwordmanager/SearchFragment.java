package com.example.passwordmanager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.example.passwordmanager.model.Content;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.Query;

public class SearchFragment extends Fragment {
    FirestoreRecyclerOptions<Content> options;
    FirestoreRecyclerAdapter<Content, SearchViewHolder> search_adapter;
    Query query;
    MaterialToolbar mToolbar;
    RecyclerView recycler_search;
    SearchView searchView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        ((MainActivity)getActivity()).drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        mToolbar = view.findViewById(R.id.search_toolbar);
        mToolbar.setNavigationOnClickListener(v -> {
            ((MainActivity)getActivity()).drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            FragmentTransaction fm = getActivity().getSupportFragmentManager().beginTransaction();
            fm.remove(SearchFragment.this).commit();
        });

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

        recycler_search = view.findViewById(R.id.recycler_search);
        recycler_search.setHasFixedSize(true);
        recycler_search.setLayoutManager(new LinearLayoutManager(getActivity()));
        return view;
    }

    private void searchData(String s) {
        if (!s.isEmpty()) {
            query = Utils.getContentReference()
                    .orderBy("search")
                    .startAt(s.toLowerCase().trim())
                    .endAt(s.toLowerCase().trim() + "\uf8ff");
            options = new FirestoreRecyclerOptions.Builder<Content>()
                    .setQuery(query, Content.class)
                    .build();
            search_adapter = new FirestoreRecyclerAdapter<Content, SearchViewHolder>(options) {
                @NonNull
                @Override
                public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_view_layout, parent, false);
                    return new SearchViewHolder(v);
                }

                @Override
                protected void onBindViewHolder(@NonNull SearchViewHolder holder, int position, @NonNull Content titles) {
                    String label = getSnapshots().getSnapshot(position).getId();
                    holder.search_title.setText(titles.getTitle());
                    holder.search_id.setText(titles.getId());

                    holder.itemView.setOnClickListener(v -> {
                        Intent intent = new Intent(v.getContext(), EditContentActivity.class);
                        intent.putExtra("title", titles.getTitle());
                        intent.putExtra("id", titles.getId());
                        intent.putExtra("pw", titles.getPw());
                        intent.putExtra("memo", titles.getMemo());
                        intent.putExtra("label", label);
                        v.getContext().startActivity(intent);
                    });
                }
            };
            search_adapter.startListening();
            recycler_search.setAdapter(search_adapter);
        } else {
            options.getSnapshots().clear();
        }
    }

    @Override
    public void onResume() {
        ((MainActivity)getActivity()).drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        super.onResume();
    }

    @Override
    public void onStop() {
        ((MainActivity)getActivity()).drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        super.onStop();
    }

    public static class SearchViewHolder extends RecyclerView.ViewHolder {
        TextView search_title, search_id;
        public SearchViewHolder(@NonNull View itemView) {
            super(itemView);
            search_title = itemView.findViewById(R.id.search_title);
            search_id = itemView.findViewById(R.id.search_id);
        }
    }
}