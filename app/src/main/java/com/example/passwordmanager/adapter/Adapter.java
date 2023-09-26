package com.example.passwordmanager.adapter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.passwordmanager.AddContentActivity;
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

public class Adapter extends FirestoreRecyclerAdapter<Content, Adapter.ViewHolder> {
    MainFragment context;

    public Adapter(@NonNull FirestoreRecyclerOptions<Content> options, MainFragment context) {
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
            Intent intent = new Intent(view.getContext(), AddContentActivity.class);
            intent.putExtra("title", content.getTitle());
            intent.putExtra("id", content.getId());
            intent.putExtra("pw", content.getPw());
            intent.putExtra("memo", content.getMemo());
            intent.putExtra("label", label);
            view.getContext().startActivity(intent);
        });

        holder.content_option.setOnClickListener(v -> {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(v.getContext(), R.style.BottomSheetDialogTheme);
            View bottomSheetView = LayoutInflater.from(v.getContext()).inflate(R.layout.content_item_bottom_sheet, v.findViewById(R.id.cibs_container));
            TextView item_title = bottomSheetView.findViewById(R.id.main_option_title);
            item_title.setText(content.getTitle());
            bottomSheetView.findViewById(R.id.main_option_edit).setOnClickListener(v1 -> {
                bottomSheetDialog.dismiss();
                Intent intent = new Intent(v.getContext(), AddContentActivity.class);
                intent.putExtra("title", content.getTitle());
                intent.putExtra("id", content.getId());
                intent.putExtra("pw", content.getPw());
                intent.putExtra("memo", content.getMemo());
                intent.putExtra("label", label);
                intent.putExtra("editable", true);
                v.getContext().startActivity(intent);
            });
            bottomSheetView.findViewById(R.id.main_option_trash).setOnClickListener(v1 -> {
                bottomSheetDialog.dismiss();
                DocumentReference fromPath = Utils.getContentReference().document(label);
                DocumentReference toPath = Utils.getTrashReference().document(label);
                moveFirebaseDocument(fromPath, toPath);
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

    private void moveFirebaseDocument(DocumentReference fromPath, DocumentReference toPath) {
        fromPath.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot != null) {
                    toPath.set(documentSnapshot.getData())
                            .addOnSuccessListener(unused -> {
                                fromPath.delete();
                                ((MainActivity)context.getContext()).updateCounter();
                            });
                }
            }
        });
    }
}
