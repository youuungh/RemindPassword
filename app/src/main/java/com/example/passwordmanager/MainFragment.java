package com.example.passwordmanager;

import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.passwordmanager.model.Adapter;
import com.example.passwordmanager.model.Content;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class MainFragment extends Fragment {
    RecyclerView recyclerView;
    FirestoreRecyclerOptions<Content> options;
    Adapter adapter;
    FloatingActionButton fab_write, fab_top;
    RelativeLayout emptyView;
    ProgressBar progressBar;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        emptyView = view.findViewById(R.id.view_empty);
        progressBar = view.findViewById(R.id.main_progressBar);

        Query query = Utils.getContentReference().orderBy("timestamp", Query.Direction.DESCENDING);
        query.get().addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            showEmptyView(options.getSnapshots().isEmpty());
        });

        options = new FirestoreRecyclerOptions.Builder<Content>()
                .setQuery(query, Content.class)
                .build();
        adapter = new Adapter(options, this);
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

        fab_write = view.findViewById(R.id.fab_write);
        fab_write.setOnClickListener(v -> startActivity(new Intent(getActivity(), AddContentActivity.class)));
        fab_top = view.findViewById(R.id.fab_top);
        fab_top.setOnClickListener(v -> recyclerView.smoothScrollToPosition(0));

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                Handler handler = new Handler();
                Runnable runnable = () -> fab_top.hide();

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    new Handler().postDelayed(() -> fab_write.show(), 500);
                    handler.removeCallbacks(runnable);
                    handler.postDelayed(runnable, 3000);
                }
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    fab_top.show();
                    fab_write.hide();
                } else if (dy < 0) {
                    fab_top.show();
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        return view;
    }

    private void showEmptyView(boolean flag) {
        emptyView.setVisibility(flag ? View.VISIBLE : View.GONE);
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