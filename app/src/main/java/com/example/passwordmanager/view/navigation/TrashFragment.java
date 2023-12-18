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
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.passwordmanager.R;
import com.example.passwordmanager.util.Utils;
import com.example.passwordmanager.model.Content;
import com.example.passwordmanager.view.common.MainActivity;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.transition.MaterialSharedAxis;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class TrashFragment extends Fragment {
    private FirestoreRecyclerOptions<Content> options;
    private FirestoreRecyclerAdapter<Content, TrashViewHolder> trash_adapter;
    private MaterialToolbar mToolbar;
    private RecyclerView recyclerView;
    private RelativeLayout trash_emptyView, trash_loadingView;
    private FloatingActionButton trash_fab_top;
    private boolean isSwitch = false;
    private long lastClickTime = 0;

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
        View view = inflater.inflate(R.layout.fragment_trash, container, false);

        init(view);
        setToolbarClickListener();
        restoreSwitchState(savedInstanceState);

        trash_fab_top.setOnClickListener(v -> {
            recyclerView.scrollToPosition(0);
            if (recyclerView.getVerticalScrollbarPosition() == 0)
                trash_fab_top.hide();
        });


        return view;
    }

    private void init(View view) {
        trash_emptyView = view.findViewById(R.id.trash_view_empty);
        trash_loadingView = view.findViewById(R.id.trash_view_loading);
        mToolbar = view.findViewById(R.id.trash_toolbar);
        trash_fab_top = ((MainActivity) requireActivity()).fab_top;

        recyclerView = view.findViewById(R.id.recycler_trash);
        recyclerView.setHasFixedSize(true);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            final Handler handler = new Handler(Looper.getMainLooper());
            final Runnable runnable = () -> trash_fab_top.hide();
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    handler.postDelayed(runnable, 3000);
                }
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    trash_fab_top.show();
                    handler.removeCallbacks(runnable);
                } else if (dy < 0) {
                    trash_fab_top.show();
                    handler.removeCallbacks(runnable);
                    if (!recyclerView.canScrollVertically(-1))
                        trash_fab_top.hide();
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    private void setToolbarClickListener() {
        mToolbar.setNavigationOnClickListener(v -> ((MainActivity) requireActivity()).drawerLayout.open());
        mToolbar.inflateMenu(R.menu.menu_options);
        configureOptionsMenu();
        setOptionsMenuClickListener();
    }

    private void configureOptionsMenu() {
        mToolbar.getMenu().getItem(0).setIcon(isSwitch ? R.drawable.ic_column_grid : R.drawable.ic_column_linear);
    }

    private void setOptionsMenuClickListener() {
        mToolbar.getMenu().getItem(0).setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_column) {
                toggleRecyclerViewLayout();
            }
            return false;
        });

        mToolbar.getMenu().getItem(1).setOnMenuItemClickListener(item -> {
            if (SystemClock.elapsedRealtime() - lastClickTime < 1000) return false;
            lastClickTime = SystemClock.elapsedRealtime();
            showBottomSheetDialog();
            return false;
        });
    }

    private void toggleRecyclerViewLayout() {
        isSwitch = !isSwitch;
        updateRecyclerViewLayout();
    }

    private void updateRecyclerViewLayout() {
        if (isSwitch) {
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            mToolbar.getMenu().getItem(0).setIcon(R.drawable.ic_column_grid);
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
            mToolbar.getMenu().getItem(0).setIcon(R.drawable.ic_column_linear);
        }
    }

    private void restoreSwitchState(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            SharedPreferences prefer = requireActivity().getSharedPreferences("TRASH_MENU_STATE", Context.MODE_PRIVATE);
            isSwitch = prefer.getBoolean("SWITCH_DATA", true);
        } else {
            isSwitch = savedInstanceState.getBoolean("TRASH_MENU_STATE");
        }
        updateRecyclerViewLayout();
    }

    private void showEmptyView(boolean flag) {
        trash_emptyView.setVisibility(flag ? View.VISIBLE : View.GONE);
    }

    private void showBottomSheetDialog() {
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
                Utils.showSnack(getView().findViewById(R.id.recycler_trash), "복원할 항목이 없습니다");
            }
        });

        bottomSheetView.findViewById(R.id.option_delete).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            if (!options.getSnapshots().isEmpty()) {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity(), R.style.LogoutAlertDialog)
                        .setMessage("휴지통에 있는 모든 항목이\n완전히 삭제됩니다")
                        .setCancelable(false)
                        .setPositiveButton("삭제", (dialog, which) -> {
                            trash_loadingView.setVisibility(View.VISIBLE);
                            deleteAllFirebaseDocument();
                        })
                        .setNegativeButton("취소", (dialog, which) -> {
                            dialog.cancel();
                        });
                builder.create();
                builder.show();
            } else {
                Utils.showSnack(getView().findViewById(R.id.recycler_trash), "휴지통이 비어 있습니다");
            }
        });
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
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
                                ((MainActivity)getActivity()).updateCounter();
                            })
                            .addOnFailureListener(e -> Utils.showSnack(getView().findViewById(R.id.recycler_trash), "오류, 다시 시도하세요"));
                }
            }
        });
    }

    private void deleteFromFirebase(DocumentReference docRef) {
        docRef.delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Utils.showSnack(getView().findViewById(R.id.recycler_trash), "삭제됨");
                ((MainActivity)getActivity()).updateCounter();
            } else {
                Utils.showSnack(getView().findViewById(R.id.recycler_trash), "오류, 다시 시도하세요");
            }
        });
    }

    private void deleteAllFirebaseDocument() {
        Utils.getTrashReference().get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                    Utils.getTrashReference().document(queryDocumentSnapshot.getId())
                            .delete()
                            .addOnSuccessListener(unused -> {
                                trash_loadingView.setVisibility(View.GONE);
                                ((MainActivity)getActivity()).updateCounter();
                            })
                            .addOnFailureListener(e -> Utils.showSnack(getView().findViewById(R.id.recycler_trash), "오류, 다시 시도하세요"));
                }
            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("TRASH_MENU_STATE", isSwitch);
    }

    @Override
    public void onStart() {
        super.onStart();
        ((MainActivity) requireActivity()).fab_write.hide();
        Query query = Utils.getTrashReference().orderBy("timestamp", Query.Direction.DESCENDING);
        query.get().addOnCompleteListener(task -> {
            trash_loadingView.setVisibility(View.GONE);
            showEmptyView(options.getSnapshots().isEmpty());
        });
        options = new FirestoreRecyclerOptions.Builder<Content>()
                .setQuery(query, Content.class)
                .build();
        trash_adapter = new FirestoreRecyclerAdapter<Content, TrashViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull TrashViewHolder holder, int position, @NonNull Content trash) {
                String label = getSnapshots().getSnapshot(position).getId();
                holder.trash_title.setText(trash.getTitle());
                holder.trash_id.setText(trash.getId());

                holder.trash_option.setOnClickListener(v -> {
                    if (SystemClock.elapsedRealtime() - lastClickTime < 1000) return;
                    lastClickTime = SystemClock.elapsedRealtime();
                    BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity(), R.style.BottomSheetDialogTheme);
                    View bottomSheetView = LayoutInflater.from(getActivity().getApplicationContext()).inflate(R.layout.trash_item_bottom_sheet, getView().findViewById(R.id.tibs_container));
                    TextView item_title = bottomSheetView.findViewById(R.id.trash_option_title);
                    item_title.setText(trash.getTitle());

                    bottomSheetView.findViewById(R.id.trash_option_restore).setOnClickListener(v1 -> {
                        bottomSheetDialog.dismiss();
                        DocumentReference fromPath = Utils.getTrashReference().document(label);
                        DocumentReference toPath = Utils.getContentReference().document();
                        moveFirebaseDocument(fromPath, toPath);
                    });

                    bottomSheetView.findViewById(R.id.trash_option_delete).setOnClickListener(v1 -> {
                        bottomSheetDialog.dismiss();
                        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity(), R.style.LogoutAlertDialog)
                                .setMessage("이 항목을 완전히 삭제할까요?")
                                .setCancelable(false)
                                .setPositiveButton("삭제", (dialog, which) -> {
                                    DocumentReference docRef = Utils.getTrashReference().document(label);
                                    deleteFromFirebase(docRef);
                                })
                                .setNegativeButton("취소", (dialog, which) -> {
                                    dialog.cancel();
                                });
                        builder.create();
                        builder.show();
                    });
                    bottomSheetDialog.setContentView(bottomSheetView);
                    bottomSheetDialog.show();
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

        configureTrashAdapter();
    }

    private void configureTrashAdapter() {
        recyclerView.setAdapter(trash_adapter);
        trash_adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                showEmptyView(options.getSnapshots().isEmpty());
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                showEmptyView(options.getSnapshots().isEmpty());
            }
        });
        trash_adapter.startListening();
        trash_adapter.notifyDataSetChanged();
    }

    @Override
    public void onStop() {
        super.onStop();
        trash_adapter.startListening();
    }

    @Override
    public void onPause() {
        super.onPause();
        saveSwitchState();
    }

    private void saveSwitchState() {
        SharedPreferences prefer = requireActivity().getSharedPreferences("TRASH_MENU_STATE", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefer.edit();
        editor.putBoolean("SWITCH_DATA", isSwitch).apply();
    }

    public static class TrashViewHolder extends RecyclerView.ViewHolder {
        private final TextView trash_title;
        private final TextView trash_id;
        private final ImageButton trash_option;

        public TrashViewHolder(@NonNull View itemView) {
            super(itemView);
            trash_title = itemView.findViewById(R.id.trash_title);
            trash_id = itemView.findViewById(R.id.trash_id);
            trash_option = itemView.findViewById(R.id.trash_option);
        }
    }
}