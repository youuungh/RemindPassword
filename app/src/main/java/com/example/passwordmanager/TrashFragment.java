package com.example.passwordmanager;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
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
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.passwordmanager.adapter.Adapter;
import com.example.passwordmanager.model.Content;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.search.SearchBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class TrashFragment extends Fragment {
    FirestoreRecyclerOptions<Content> options;
    FirestoreRecyclerAdapter<Content, TrashViewHolder> adapter;
    Query query;
    RecyclerView recyclerView;
    FloatingActionButton trash_fab_top;
    RelativeLayout trash_emptyView, trash_loadingView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trash, container, false);

        trash_emptyView = view.findViewById(R.id.trash_view_empty);
        trash_loadingView = view.findViewById(R.id.trash_view_loading);

        ((MainActivity)getActivity()).searchBar.getMenu().clear();
        ((MainActivity)getActivity()).searchBar.inflateMenu(R.menu.menu_options);
        ((MainActivity)getActivity()).searchBar.setOnMenuItemClickListener(item -> {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity(), R.style.BottomSheetDialogTheme);
            View bottomSheetView = LayoutInflater.from(getActivity().getApplicationContext()).inflate(R.layout.trash_bottom_sheet, getView().findViewById(R.id.tbs_container));
            bottomSheetView.findViewById(R.id.option_restore).setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                if (!options.getSnapshots().isEmpty()) {
                    Utils.getTrashReference().get().addOnCompleteListener(task -> {
                        trash_loadingView.setVisibility(View.VISIBLE);

                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();

                            for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                                DocumentReference fromPath = Utils.getTrashReference().document(documentSnapshot.getReference().getId());
                                DocumentReference toPath = Utils.getContentReference().document(documentSnapshot.getReference().getId());
                                moveFirebaseDocument(fromPath, toPath);
                            }
                        }
                    });
                } else {
                     Utils.showSnack(view.findViewById(R.id.recycler_trash), "복원할 항목이 없습니다");
                }
            });
            bottomSheetDialog.setContentView(bottomSheetView);
            bottomSheetDialog.show();
            return false;
        });

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

    private void moveFirebaseDocument(DocumentReference fromPath, DocumentReference toPath) {
        fromPath.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot != null) {
                    toPath.set(documentSnapshot.getData())
                            .addOnSuccessListener(unused -> {
                                fromPath.delete();
                                trash_loadingView.setVisibility(View.GONE);
                            })
                            .addOnFailureListener(e -> Utils.showSnack(getView().findViewById(R.id.recycler_trash), "오류, 다시 시도하세요"));
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        query = Utils.getTrashReference().orderBy("timestamp", Query.Direction.DESCENDING);
        query.get().addOnCompleteListener(task -> {
            trash_loadingView.setVisibility(View.GONE);
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
                holder.trash_timestamp.setText("99일");

                holder.trash_option.setOnClickListener(v -> {
                    Toast.makeText(v.getContext(), "클릭", Toast.LENGTH_SHORT).show();
                });
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

    public static class TrashViewHolder extends RecyclerView.ViewHolder {
        TextView trash_title, trash_id, trash_timestamp;
        ImageButton trash_option;

        public TrashViewHolder(@NonNull View itemView) {
            super(itemView);
            trash_title = itemView.findViewById(R.id.trash_title);
            trash_id = itemView.findViewById(R.id.trash_id);
            trash_timestamp = itemView.findViewById(R.id.trash_timestamp);
            trash_option = itemView.findViewById(R.id.trash_option);
        }
    }
}