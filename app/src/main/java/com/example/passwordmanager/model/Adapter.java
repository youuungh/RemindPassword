package com.example.passwordmanager.model;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.passwordmanager.AddContentActivity;
import com.example.passwordmanager.MainFragment;
import com.example.passwordmanager.R;
import com.example.passwordmanager.Utility;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

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
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") final int position, Content content) {
        holder.content_title.setText(content.title);
        holder.content_id.setText(content.id);
        holder.content_timestamp.setText(Utility.timeStampToString(content.timestamp));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), AddContentActivity.class);
                intent.putExtra("title", content.title);
                intent.putExtra("id", content.id);
                intent.putExtra("pw", content.pw);
                intent.putExtra("memo", content.memo);
                String label = getSnapshots().getSnapshot(position).getId();
                intent.putExtra("label", label);
                view.getContext().startActivity(intent);
            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView content_title, content_id, content_timestamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            content_title = itemView.findViewById(R.id.content_title);
            content_id = itemView.findViewById(R.id.content_id);
            content_timestamp = itemView.findViewById(R.id.content_timestamp);
        }
    }
}
