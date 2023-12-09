package com.example.passwordmanager.adapter;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> implements Filterable {
    SearchFragment context;
    List<Content> searchList;
    List<Content> filterList;

    public SearchAdapter(SearchFragment context, List<Content> dataList) {
        this.context = context;
        this.searchList = dataList;
        filterList = dataList;
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
        Content search = filterList.get(position);
        holder.search_title.setText(search.getTitle());
        holder.search_id.setText(search.getId());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), EditContentActivity.class);
            Bundle bundle = ActivityOptions.makeSceneTransitionAnimation((Activity) v.getContext()).toBundle();
            intent.putExtra("title", search.getTitle());
            intent.putExtra("id", search.getId());
            intent.putExtra("pw", search.getPw());
            intent.putExtra("memo", search.getMemo());
            intent.putExtra("label", search.getDocId());
            intent.putExtra("favorite", search.isFavorite());
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
        private final TextView search_title;
        private final TextView search_id;

        public SearchViewHolder(@NonNull View itemView) {
            super(itemView);
            search_title = itemView.findViewById(R.id.search_title);
            search_id = itemView.findViewById(R.id.search_id);
        }
    }
}
