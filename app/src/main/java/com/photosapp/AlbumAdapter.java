package com.photosapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder> {
    private List<Album> albums;
    private OnAlbumClickListener listener;
    
    public interface OnAlbumClickListener {
        void onAlbumClick(Album album);
    }
    
    public AlbumAdapter(List<Album> albums, OnAlbumClickListener listener) {
        this.albums = albums;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_album, parent, false);
        return new AlbumViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        Album album = albums.get(position);
        holder.albumNameTextView.setText(album.getName());
        holder.photoCountTextView.setText(album.getPhotoCount() + " photos");
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAlbumClick(album);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return albums.size();
    }
    
    static class AlbumViewHolder extends RecyclerView.ViewHolder {
        TextView albumNameTextView;
        TextView photoCountTextView;
        
        AlbumViewHolder(View itemView) {
            super(itemView);
            albumNameTextView = itemView.findViewById(R.id.albumNameTextView);
            photoCountTextView = itemView.findViewById(R.id.photoCountTextView);
        }
    }
}

