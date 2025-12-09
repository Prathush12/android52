package com.photosapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SearchCriteriaAdapter extends RecyclerView.Adapter<SearchCriteriaAdapter.CriteriaViewHolder> {
    private List<Tag> criteria;
    private OnCriteriaRemoveListener listener;
    
    public interface OnCriteriaRemoveListener {
        void onRemove(Tag tag);
    }
    
    public SearchCriteriaAdapter(List<Tag> criteria, OnCriteriaRemoveListener listener) {
        this.criteria = criteria;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public CriteriaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_search_criteria, parent, false);
        return new CriteriaViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull CriteriaViewHolder holder, int position) {
        Tag tag = criteria.get(position);
        holder.criteriaTextView.setText(tag.toString());
        holder.removeButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemove(tag);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return criteria.size();
    }
    
    static class CriteriaViewHolder extends RecyclerView.ViewHolder {
        TextView criteriaTextView;
        Button removeButton;
        
        CriteriaViewHolder(View itemView) {
            super(itemView);
            criteriaTextView = itemView.findViewById(R.id.criteriaTextView);
            removeButton = itemView.findViewById(R.id.removeCriteriaButton);
        }
    }
}

