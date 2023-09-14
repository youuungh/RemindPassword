package com.example.passwordmanager.adapter;

import android.annotation.SuppressLint;
import android.content.Intent;
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
import com.example.passwordmanager.MainFragment;
import com.example.passwordmanager.R;
import com.example.passwordmanager.Utils;
import com.example.passwordmanager.model.Content;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Random;

public class Adapter extends FirestoreRecyclerAdapter<Content, Adapter.ViewHolder> {

    public Adapter(@NonNull FirestoreRecyclerOptions<Content> options) {
        super(options);
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
        holder.content_title.setText(content.getTitle());
        holder.content_id.setText(content.getId());
        holder.content_timestamp.setText(Utils.timeStampToString(content.getTimestamp()));

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), AddContentActivity.class);
            intent.putExtra("title", content.getTitle());
            intent.putExtra("id", content.getId());
            intent.putExtra("pw", content.getPw());
            intent.putExtra("memo", content.getMemo());
            String label = getSnapshots().getSnapshot(position).getId();
            intent.putExtra("label", label);
            view.getContext().startActivity(intent);
        });

        holder.content_option.setOnClickListener(v -> {
            Toast.makeText(v.getContext(), "클릭", Toast.LENGTH_SHORT).show();
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
}
