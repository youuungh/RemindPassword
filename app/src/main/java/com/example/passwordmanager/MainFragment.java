package com.example.passwordmanager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.passwordmanager.model.Adapter;
import com.example.passwordmanager.model.Content;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class MainFragment extends Fragment {
    RecyclerView recyclerView;
    FirebaseFirestore fStore;
    Adapter adapter;
    FloatingActionButton fab_write, fab_top;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        fStore = FirebaseFirestore.getInstance();
        recyclerView = view.findViewById(R.id.recycler_contents);

        Query query = Utils.getContentReference().orderBy("timestamp", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<Content> options = new FirestoreRecyclerOptions.Builder<Content>()
                .setQuery(query, Content.class).build();
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new Adapter(options, this);
        recyclerView.setAdapter(adapter);

        fab_write = view.findViewById(R.id.fab_write);
        fab_write.setOnClickListener(v -> startActivity(new Intent(getActivity(), AddContentActivity.class)));

        fab_top = view.findViewById(R.id.fab_top);
        fab_top.hide();
        fab_top.setOnClickListener(v -> recyclerView.smoothScrollToPosition(0));

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                Handler handler = new Handler();
                Runnable runnable = () -> fab_top.hide();

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    fab_write.show();
                    handler.removeCallbacks(runnable);
                    handler.postDelayed(runnable, 3000);
                } else {
                    fab_top.show();
                    fab_write.hide();
                }
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    fab_top.show();
                } else if (dy < 0) {
                    fab_top.show();
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        return view;
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