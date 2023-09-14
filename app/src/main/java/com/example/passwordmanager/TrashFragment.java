package com.example.passwordmanager;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.passwordmanager.adapter.Adapter;
import com.example.passwordmanager.model.Content;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.Query;

public class TrashFragment extends Fragment {
    FirestoreRecyclerOptions<Content> options;
    FirestoreRecyclerAdapter<Content, TrashViewHolder> adapter;
    Query query;
    RecyclerView recyclerView;
    FloatingActionButton trash_fab_top;
    RelativeLayout trash_emptyView;
    ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trash, container, false);

        trash_emptyView = view.findViewById(R.id.trash_view_empty);
        progressBar = view.findViewById(R.id.trash_progressBar);

        recyclerView = view.findViewById(R.id.recycler_trash);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        trash_fab_top = view.findViewById(R.id.trash_fab_top);
        trash_fab_top.setOnClickListener(v -> recyclerView.smoothScrollToPosition(0));
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                Handler handler = new Handler();
                Runnable runnable = () -> trash_fab_top.hide();

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    handler.removeCallbacks(runnable);
                    handler.postDelayed(runnable, 3000);
                }
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    trash_fab_top.show();
                } else if (dy < 0) {
                    trash_fab_top.show();
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        return view;
    }

    private void showEmptyView(boolean flag) {
        trash_emptyView.setVisibility(flag ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();

        query = Utils.getTrashReference().orderBy("timestamp", Query.Direction.DESCENDING);
        query.get().addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            showEmptyView(options.getSnapshots().isEmpty());
        });
        options = new FirestoreRecyclerOptions.Builder<Content>()
                .setQuery(query, Content.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<Content, TrashViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull TrashViewHolder holder, int position, @NonNull Content trash) {
                holder.trash_title.setText(trash.getTitle());
                holder.trash_id.setText(trash.getId());
                holder.trash_timestamp.setText(Utils.timeStampToString(trash.getTimestamp()));
            }

            @NonNull
            @Override
            public TrashViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.trash_view_layout, parent, false);
                return new TrashViewHolder(v);
            }

            @Override
            public int getItemCount() {
                return getSnapshots().size();
            }
        };
        recyclerView.setAdapter(adapter);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                showEmptyView(options.getSnapshots().isEmpty());
                ((MainActivity)getActivity()).trashCounterChanged(String.valueOf(adapter.getItemCount()));
                recyclerView.smoothScrollToPosition(0);
                super.onItemRangeInserted(positionStart, itemCount);
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                showEmptyView(options.getSnapshots().isEmpty());
                ((MainActivity)getActivity()).trashCounterChanged(String.valueOf(adapter.getItemCount()));
                recyclerView.smoothScrollToPosition(0);
                super.onItemRangeRemoved(positionStart, itemCount);
            }
        });
        adapter.startListening();
    }

    public static class TrashViewHolder extends RecyclerView.ViewHolder {
        TextView trash_title, trash_id, trash_timestamp;

        public TrashViewHolder(@NonNull View itemView) {
            super(itemView);
            trash_title = itemView.findViewById(R.id.trash_title);
            trash_id = itemView.findViewById(R.id.trash_id);
            trash_timestamp = itemView.findViewById(R.id.trash_timestamp);
        }
    }
}