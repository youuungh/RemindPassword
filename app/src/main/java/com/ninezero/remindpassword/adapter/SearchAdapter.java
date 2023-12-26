package com.ninezero.remindpassword.adapter;

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

import com.ninezero.remindpassword.view.navigation.EditContentActivity;
import com.ninezero.remindpassword.R;
import com.ninezero.remindpassword.view.navigation.SearchFragment;
import com.ninezero.remindpassword.model.Content;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> implements Filterable {
    SearchFragment context;
    List<Content> originalList;
    List<Content> filteredList;

    public SearchAdapter(SearchFragment context, List<Content> searchList) {
        this.context = context;
        this.originalList = searchList;
        filteredList = searchList;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String pattern = constraint.toString().toLowerCase().trim();
                if (pattern.isEmpty()) {
                    filteredList = originalList;
                } else {
                    List<Content> tempList = new ArrayList<>();
                    for (Content content : originalList) {
                        if (content.getSearch().contains(pattern)) {
                            tempList.add(content);
                        }
                    }
                    filteredList = tempList;
                }
                FilterResults results = new FilterResults();
                results.values = filteredList;
                return results;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredList = (ArrayList<Content>) results.values;
                if (filteredList.isEmpty()) {
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
        Content search = filteredList.get(position);
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
        return filteredList.size();
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
