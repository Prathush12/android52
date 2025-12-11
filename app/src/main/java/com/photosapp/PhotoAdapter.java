package com.photosapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.InputStream;
import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {
    private List<Photo> photos;
    private OnPhotoClickListener listener;
    
    public interface OnPhotoClickListener {
        void onPhotoClick(Photo photo);
    }
    
    public PhotoAdapter(List<Photo> photos, OnPhotoClickListener listener) {
        this.photos = photos;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_photo, parent, false);
        return new PhotoViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        Photo photo = photos.get(position);
        holder.photoNameTextView.setText(photo.getFileName());
        
        try {
            Uri uri = Uri.parse(photo.getFilePath());
            InputStream inputStream = holder.itemView.getContext().getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (bitmap != null) {
                int targetSize = 300;
                Bitmap thumbnail = Bitmap.createScaledBitmap(bitmap, targetSize, targetSize, true);
                holder.photoImageView.setImageBitmap(thumbnail);
            }
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (Exception e) {
            holder.photoImageView.setImageResource(android.R.drawable.ic_menu_report_image);
        }
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPhotoClick(photo);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return photos.size();
    }
    
    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView photoImageView;
        TextView photoNameTextView;
        
        PhotoViewHolder(View itemView) {
            super(itemView);
            photoImageView = itemView.findViewById(R.id.photoImageView);
            photoNameTextView = itemView.findViewById(R.id.photoNameTextView);
        }
    }
}
