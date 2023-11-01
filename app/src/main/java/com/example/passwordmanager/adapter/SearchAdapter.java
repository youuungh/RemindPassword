package com.example.passwordmanager.adapter;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.passwordmanager.EditContentActivity;
import com.example.passwordmanager.R;
import com.example.passwordmanager.SearchFragment;
import com.example.passwordmanager.model.Content;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.ObservableSnapshotArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> implements Filterable {
    SearchFragment context;
    List<Content> searchList;
    List<Content> searchListFiltered;

    public SearchAdapter(SearchFragment context, List<Content> searchList) {
        this.context = context;
        this.searchList = searchList;
        searchListFiltered = searchList;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String pattern = constraint.toString().toLowerCase().trim();

                if (pattern.isEmpty()) {
                    searchListFiltered = searchList;
                } else {
                    List<Content> filteredList = new ArrayList<>();

                    for (Content contents : searchList) {
                        if (contents.getSearch().contains(pattern)) {
                            filteredList.add(contents);
                        }
                    }
                    searchListFiltered = filteredList;
                }
                FilterResults results = new FilterResults();
                results.values = searchListFiltered;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                searchListFiltered = (ArrayList<Content>) results.values;
                if (searchListFiltered.isEmpty()) {
                    context.showEmptyView(true);
                } else {
                    context.showEmptyView(false);
                }
                notifyDataSetChanged();
            }
        };
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_view_layout, parent, false);
        return new SearchViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        final Content content = searchListFiltered.get(position);
        holder.search_id.setText(searchListFiltered.get(position).getId());
        holder.search_title.setText(searchListFiltered.get(position).getTitle());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), EditContentActivity.class);
            Bundle bundle = ActivityOptions.makeSceneTransitionAnimation((Activity) v.getContext()).toBundle();
            intent.putExtra("title", searchListFiltered.get(position).getTitle());
            intent.putExtra("id", searchListFiltered.get(position).getId());
            intent.putExtra("pw", searchListFiltered.get(position).getPw());
            intent.putExtra("memo", searchListFiltered.get(position).getMemo());
            intent.putExtra("label", String.valueOf(content));
            v.getContext().startActivity(intent, bundle);
        });
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return searchListFiltered.size();
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
