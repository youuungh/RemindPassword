package com.example.passwordmanager.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.passwordmanager.EditContentActivity;
import com.example.passwordmanager.R;
import com.example.passwordmanager.model.Content;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class SearchAdapter extends FirestoreRecyclerAdapter<Content, SearchAdapter.SearchViewHolder> {
    Context context;

    public SearchAdapter(@NonNull FirestoreRecyclerOptions<Content> options, Context context) {
        super(options);
        this.context = context;
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_view_layout, parent, false);
        return new SearchViewHolder(v);
    }

    @Override
    protected void onBindViewHolder(@NonNull SearchViewHolder holder, int position, @NonNull Content titles) {
        String label = getSnapshots().getSnapshot(position).getId();
        holder.search_title.setText(titles.getTitle());
        holder.search_id.setText(titles.getId());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), EditContentActivity.class);
            intent.putExtra("title", titles.getTitle());
            intent.putExtra("id", titles.getId());
            intent.putExtra("pw", titles.getPw());
            intent.putExtra("memo", titles.getMemo());
            intent.putExtra("label", label);
            v.getContext().startActivity(intent);
        });
    }

    public static class SearchViewHolder extends RecyclerView.ViewHolder {
        TextView search_title, search_id;
        public SearchViewHolder(@NonNull View itemView) {
            super(itemView);
            search_title = itemView.findViewById(R.id.search_title);
            search_id = itemView.findViewById(R.id.search_id);
        }
    }
}
