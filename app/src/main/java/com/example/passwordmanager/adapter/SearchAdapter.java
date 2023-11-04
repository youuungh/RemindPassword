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

import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> implements Filterable {
    SearchFragment context;
    List<Content> searchList;
    List<Content> filterList;

    public SearchAdapter(SearchFragment context, List<Content> searchList) {
        this.context = context;
        this.searchList = searchList;
        filterList = searchList;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String pattern = constraint.toString().toLowerCase().trim();
                if (pattern.isEmpty()) {
                    filterList = searchList;
                } else {
                    List<Content> filteredList = new ArrayList<>();
                    for (Content contents : searchList) {
                        if (contents.getSearch().contains(pattern)) {
                            filteredList.add(contents);
                        }
                    }
                    filterList = filteredList;
                }
                FilterResults results = new FilterResults();
                results.values = filterList;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filterList = (ArrayList<Content>) results.values;
                if (filterList.isEmpty()) {
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
        String label = filterList.get(position).getDocId();
        holder.search_id.setText(filterList.get(position).getId());
        holder.search_title.setText(filterList.get(position).getTitle());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), EditContentActivity.class);
            Bundle bundle = ActivityOptions.makeSceneTransitionAnimation((Activity) v.getContext()).toBundle();
            intent.putExtra("title", filterList.get(position).getTitle());
            intent.putExtra("id", filterList.get(position).getId());
            intent.putExtra("pw", filterList.get(position).getPw());
            intent.putExtra("memo", filterList.get(position).getMemo());
            intent.putExtra("label", label);
            v.getContext().startActivity(intent, bundle);
        });
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return filterList.size();
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
