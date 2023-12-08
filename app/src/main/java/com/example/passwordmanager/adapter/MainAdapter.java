package com.example.passwordmanager.adapter;

import android.annotation.SuppressLint;
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
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.passwordmanager.AddContentActivity;
import com.example.passwordmanager.EditContentActivity;
import com.example.passwordmanager.MainActivity;
import com.example.passwordmanager.MainFragment;
import com.example.passwordmanager.R;
import com.example.passwordmanager.Utils;
import com.example.passwordmanager.model.Content;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Random;

public class MainAdapter extends FirestoreRecyclerAdapter<Content, MainAdapter.ViewHolder> {
    MainFragment context;
    private long mLastClickTime = 0;

    public MainAdapter(@NonNull FirestoreRecyclerOptions<Content> options, MainFragment context) {
        super(options);
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.content_view_layout, parent, false);
        int[] colorList = v.getResources().getIntArray(R.array.cv_colors);
        int randomColor = colorList[new Random().nextInt(colorList.length)];
        CardView cv_content = v.findViewById(R.id.cv_content);
        cv_content.setCardBackgroundColor(randomColor);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") final int position, Content content) {
        String label = getSnapshots().getSnapshot(position).getId();
        holder.content_title.setText(content.getTitle());
        holder.content_id.setText(content.getId());
        holder.content_timestamp.setText(Utils.timeStampToString(content.getTimestamp()));

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), EditContentActivity.class);
            Bundle bundle = ActivityOptions.makeSceneTransitionAnimation((Activity) view.getContext()).toBundle();
            intent.putExtra("title", content.getTitle());
            intent.putExtra("id", content.getId());
            intent.putExtra("pw", content.getPw());
            intent.putExtra("memo", content.getMemo());
            intent.putExtra("label", label);
            intent.putExtra("favorite", content.isFavorite());
            view.getContext().startActivity(intent, bundle);
        });

        holder.content_option.setOnClickListener(v -> {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) return;
            mLastClickTime = SystemClock.elapsedRealtime();

            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(v.getContext(), R.style.BottomSheetDialogTheme);
            View bottomSheetView = LayoutInflater.from(v.getContext()).inflate(R.layout.content_item_bottom_sheet, v.findViewById(R.id.cibs_container));

            TextView item_title = bottomSheetView.findViewById(R.id.main_option_title);
            ImageView iv_favorite = bottomSheetView.findViewById(R.id.iv_favorite);
            TextView tv_favorite = bottomSheetView.findViewById(R.id.tv_favorite);

            item_title.setText(content.getTitle());
            iv_favorite.setImageResource(R.drawable.ic_star_not_filled);
            tv_favorite.setText("즐겨찾기에 추가");

            bottomSheetView.findViewById(R.id.main_option_favorite).setOnClickListener(v1 -> {
                bottomSheetDialog.dismiss();
                DocumentReference contentPath = Utils.getContentReference().document(label);
                DocumentReference favPath = Utils.getFavoriteReference().document(label);
                addFavorite(contentPath, favPath);
            });

            bottomSheetView.findViewById(R.id.main_option_edit).setOnClickListener(v1 -> {
                bottomSheetDialog.dismiss();
                Intent intent = new Intent(v.getContext(), AddContentActivity.class);
                Bundle bundle = ActivityOptions.makeSceneTransitionAnimation((Activity) v.getContext()).toBundle();
                intent.putExtra("title", content.getTitle());
                intent.putExtra("id", content.getId());
                intent.putExtra("pw", content.getPw());
                intent.putExtra("memo", content.getMemo());
                intent.putExtra("label", label);
                intent.putExtra("favorite", content.isFavorite());
                v.getContext().startActivity(intent, bundle);
            });

            bottomSheetView.findViewById(R.id.main_option_trash).setOnClickListener(v1 -> {
                bottomSheetDialog.dismiss();
                DocumentReference contentPath = Utils.getContentReference().document(label);
                DocumentReference trashPath = Utils.getTrashReference().document(label);
                moveFirebaseDocument(contentPath, trashPath);
            });
            bottomSheetDialog.setContentView(bottomSheetView);
            bottomSheetDialog.show();
        });
    }

    @Override
    public int getItemCount() {
        return getSnapshots().size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView content_title, content_id, content_timestamp;
        ImageButton content_option;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            content_title = itemView.findViewById(R.id.content_title);
            content_id = itemView.findViewById(R.id.content_id);
            content_timestamp = itemView.findViewById(R.id.content_timestamp);
            content_option = itemView.findViewById(R.id.content_option);
        }
    }

    private void addFavorite(DocumentReference fromPath, DocumentReference toPath) {
        fromPath.update("favorite", true);
        fromPath.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot != null) {
                    toPath.set(documentSnapshot.getData()).addOnSuccessListener(unused -> {
                        fromPath.delete();
                        Utils.showSnack(context.getActivity().findViewById(R.id.fragment_container), "즐겨찾기에 추가되었습니다");
                    });
                }
            }
        });
    }

    private void moveFirebaseDocument(DocumentReference fromPath, DocumentReference toPath) {
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
