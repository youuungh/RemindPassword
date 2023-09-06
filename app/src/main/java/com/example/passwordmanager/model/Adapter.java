package com.example.passwordmanager.model;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
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
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.content_view_layout, parent, false);
        int[] colorList = context.getResources().getIntArray(R.array.cv_colors);
        int randomColor = colorList[new Random().nextInt(colorList.length)];
        CardView cv_content = view.findViewById(R.id.cv_content);
        cv_content.setCardBackgroundColor(randomColor);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") final int position, Content content) {
        holder.content_title.setText(content.title);
        holder.content_id.setText(content.id);
        holder.content_timestamp.setText(Utils.timeStampToString(content.timestamp));

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), AddContentActivity.class);
            intent.putExtra("title", content.title);
            intent.putExtra("id", content.id);
            intent.putExtra("pw", content.pw);
            intent.putExtra("memo", content.memo);
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
