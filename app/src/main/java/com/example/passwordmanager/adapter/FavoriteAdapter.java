package com.example.passwordmanager.adapter;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.passwordmanager.view.navigation.AddContentActivity;
import com.example.passwordmanager.view.navigation.EditContentActivity;
import com.example.passwordmanager.view.common.MainActivity;
import com.example.passwordmanager.view.navigation.MainFragment;
import com.example.passwordmanager.R;
import com.example.passwordmanager.util.Utils;
import com.example.passwordmanager.model.Content;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

public class FavoriteAdapter extends FirestoreRecyclerAdapter<Content, FavoriteAdapter.FavoriteViewHolder> {
    MainFragment context;
    private long mLastClickTime = 0;

    public FavoriteAdapter(@NonNull FirestoreRecyclerOptions<Content> options, MainFragment context) {
        super(options);
        this.context = context;
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.favorite_view_layout, parent, false);
        v.setLayoutParams(new ViewGroup.LayoutParams((int) (parent.getMeasuredWidth() * 0.6), ViewGroup.LayoutParams.WRAP_CONTENT));
        return new FavoriteViewHolder(v);
    }

    @Override
    protected void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position, @NonNull Content favorite) {
        String label = getSnapshots().getSnapshot(position).getId();
        holder.favorite_title.setText(favorite.getTitle());
        holder.favorite_id.setText(favorite.getId());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), EditContentActivity.class);
            Bundle bundle = ActivityOptions.makeSceneTransitionAnimation((Activity) v.getContext()).toBundle();
            intent.putExtra("title", favorite.getTitle());
            intent.putExtra("id", favorite.getId());
            intent.putExtra("pw", favorite.getPw());
            intent.putExtra("memo", favorite.getMemo());
            intent.putExtra("label", label);
            intent.putExtra("favorite", favorite.isFavorite());
            v.getContext().startActivity(intent, bundle);
        });

        holder.favorite_option.setOnClickListener(v -> {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) return;
            mLastClickTime = SystemClock.elapsedRealtime();

            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(v.getContext(), R.style.BottomSheetDialogTheme);
            View bottomSheetView = LayoutInflater.from(v.getContext()).inflate(R.layout.content_item_bottom_sheet, v.findViewById(R.id.cibs_container));
            bottomSheetDialog.setContentView(bottomSheetView);

            View parent = (View) bottomSheetView.getParent();
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) parent.getLayoutParams();
            CoordinatorLayout.Behavior<View> behavior = params.getBehavior();

            if (behavior instanceof BottomSheetBehavior) {
                ((BottomSheetBehavior) behavior).setState(BottomSheetBehavior.STATE_EXPANDED);
                ((BottomSheetBehavior) behavior).addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                    @Override
                    public void onStateChanged(@NonNull View bottomSheet, int newState) {
                    }

                    @Override
                    public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                        if (slideOffset >= 0.25) {
                            ((BottomSheetBehavior) behavior).setState(BottomSheetBehavior.STATE_HIDDEN);
                        }
                    }
                });
            }

            TextView item_title = bottomSheetView.findViewById(R.id.main_option_title);
            ImageView iv_favorite = bottomSheetView.findViewById(R.id.iv_favorite);
            TextView tv_favorite = bottomSheetView.findViewById(R.id.tv_favorite);

            item_title.setText(favorite.getTitle());
            iv_favorite.setImageResource(R.drawable.ic_star_filled);
            tv_favorite.setText("즐겨찾기에서 삭제");

            bottomSheetView.findViewById(R.id.main_option_favorite).setOnClickListener(v1 -> {
                bottomSheetDialog.dismiss();
                DocumentReference favPath = Utils.getFavoriteReference().document(label);
                DocumentReference contentPath = Utils.getContentReference().document(label);
                deleteFavorite(favPath, contentPath);
            });

            bottomSheetView.findViewById(R.id.main_option_edit).setOnClickListener(v1 -> {
                bottomSheetDialog.dismiss();
                Intent intent = new Intent(v.getContext(), AddContentActivity.class);
                Bundle bundle = ActivityOptions.makeSceneTransitionAnimation((Activity) v.getContext()).toBundle();
                intent.putExtra("title", favorite.getTitle());
                intent.putExtra("id", favorite.getId());
                intent.putExtra("pw", favorite.getPw());
                intent.putExtra("memo", favorite.getMemo());
                intent.putExtra("label", label);
                intent.putExtra("favorite", favorite.isFavorite());
                v.getContext().startActivity(intent, bundle);
            });

            bottomSheetView.findViewById(R.id.main_option_trash).setOnClickListener(v1 -> {
                bottomSheetDialog.dismiss();
                DocumentReference favPath = Utils.getFavoriteReference().document(label);
                DocumentReference trashPath = Utils.getTrashReference().document(label);
                moveFirebaseDocument(favPath, trashPath);
            });
            bottomSheetDialog.show();
        });
    }

    @Override
    public int getItemCount() {
        return getSnapshots().size();
    }

    public static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        private final TextView favorite_title;
        private final TextView favorite_id;
        private final ImageButton favorite_option;
        public FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            favorite_title = itemView.findViewById(R.id.favorite_title);
            favorite_id = itemView.findViewById(R.id.favorite_id);
            favorite_option = itemView.findViewById(R.id.favorite_option);
        }
    }

    private void deleteFavorite(DocumentReference fromPath, DocumentReference toPath) {
        fromPath.update("favorite", false);
        fromPath.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot != null) {
                    toPath.set(documentSnapshot.getData()).addOnSuccessListener(unused -> {
                        fromPath.delete();
                        Utils.showSnack(context.getActivity().findViewById(R.id.fragment_container), "즐겨찾기에서 삭제되었습니다");
                    });
                }
            }
        });
    }

    private void moveFirebaseDocument(DocumentReference fromPath, DocumentReference toPath) {
        fromPath.update("favorite", false);
        fromPath.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot != null) {
                    toPath.set(documentSnapshot.getData())
                            .addOnSuccessListener(unused -> {
                                fromPath.delete();
                                ((MainActivity) context.getActivity()).updateCounter();
                            });
                }
            }
        });
    }
}
