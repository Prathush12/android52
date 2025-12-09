package com.photosapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.io.InputStream;
import java.util.List;

public class PhotoDisplayActivity extends AppCompatActivity {
    private ImageView photoImageView;
    private TextView photoNameTextView;
    private TextView tagsTextView;
    private Button previousButton;
    private Button nextButton;
    private Button addTagButton;
    private Button deleteTagButton;
    
    private PhotoManager photoManager;
    private Album currentAlbum;
    private List<Photo> photos;
    private int currentPhotoIndex;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_display);
        
        String albumName = getIntent().getStringExtra("albumName");
        int photoIndex = getIntent().getIntExtra("photoIndex", 0);
        
        if (albumName == null) {
            finish();
            return;
        }
        
        photoManager = PhotoManager.getInstance(this);
        currentAlbum = photoManager.getAlbumByName(albumName);
        
        if (currentAlbum == null) {
            finish();
            return;
        }
        
        photos = currentAlbum.getPhotos();
        currentPhotoIndex = photoIndex;
        
        if (photos.isEmpty() || currentPhotoIndex < 0 || currentPhotoIndex >= photos.size()) {
            finish();
            return;
        }
        
        photoImageView = findViewById(R.id.photoImageView);
        photoNameTextView = findViewById(R.id.photoNameTextView);
        tagsTextView = findViewById(R.id.tagsTextView);
        previousButton = findViewById(R.id.previousButton);
        nextButton = findViewById(R.id.nextButton);
        addTagButton = findViewById(R.id.addTagButton);
        deleteTagButton = findViewById(R.id.deleteTagButton);
        
        previousButton.setOnClickListener(v -> showPreviousPhoto());
        nextButton.setOnClickListener(v -> showNextPhoto());
        addTagButton.setOnClickListener(v -> showAddTagDialog());
        deleteTagButton.setOnClickListener(v -> showDeleteTagDialog());
        
        updatePhotoDisplay();
    }
    
    private void updatePhotoDisplay() {
        if (currentPhotoIndex < 0 || currentPhotoIndex >= photos.size()) {
            return;
        }
        
        Photo photo = photos.get(currentPhotoIndex);
        photoNameTextView.setText(photo.getFileName());
        
        List<Tag> tags = photo.getTags();
        if (tags.isEmpty()) {
            tagsTextView.setText(getString(R.string.no_tags));
        } else {
            StringBuilder tagsText = new StringBuilder();
            for (Tag tag : tags) {
                tagsText.append(tag.toString()).append("\n");
            }
            tagsTextView.setText(tagsText.toString().trim());
        }
        
        try {
            Uri uri = Uri.parse(photo.getFilePath());
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (bitmap != null) {
                photoImageView.setImageBitmap(bitmap);
            }
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (Exception e) {
            photoImageView.setImageResource(android.R.drawable.ic_menu_report_image);
        }
        
        previousButton.setEnabled(currentPhotoIndex > 0);
        nextButton.setEnabled(currentPhotoIndex < photos.size() - 1);
    }
    
    private void showPreviousPhoto() {
        if (currentPhotoIndex > 0) {
            currentPhotoIndex--;
            updatePhotoDisplay();
        }
    }
    
    private void showNextPhoto() {
        if (currentPhotoIndex < photos.size() - 1) {
            currentPhotoIndex++;
            updatePhotoDisplay();
        }
    }
    
    private void showAddTagDialog() {
        Photo photo = photos.get(currentPhotoIndex);
        
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_tag, null);
        TextInputEditText typeInput = dialogView.findViewById(R.id.tagTypeInput);
        TextInputEditText valueInput = dialogView.findViewById(R.id.tagValueInput);
        
        new AlertDialog.Builder(this)
            .setTitle(R.string/add_tag)
            .setView(dialogView)
            .setPositiveButton(R.string.ok, (dialog, which) -> {
                String type = typeInput.getText() != null ? typeInput.getText().toString().trim() : "";
                String value = valueInput.getText() != null ? valueInput.getText().toString().trim() : "";
                
                if (type.isEmpty() || value.isEmpty()) {
                    Toast.makeText(this, "Tag type and value cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if (!type.equalsIgnoreCase("person") && !type.equalsIgnoreCase("location")) {
                    Toast.makeText(this, "Tag type must be 'person' or 'location'", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                Tag tag = new Tag(type, value);
                photo.addTag(tag);
                photoManager.saveData();
                updatePhotoDisplay();
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }
    
    private void showDeleteTagDialog() {
        Photo photo = photos.get(currentPhotoIndex);
        List<Tag> tags = photo.getTags();
        
        if (tags.isEmpty()) {
            Toast.makeText(this, "No tags to delete", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String[] tagStrings = new String[tags.size()];
        for (int i = 0; i < tags.size(); i++) {
            tagStrings[i] = tags.get(i).toString();
        }
        
        new AlertDialog.Builder(this)
            .setTitle(R.string.delete_tag)
            .setItems(tagStrings, (dialog, which) -> {
                Tag tag = tags.get(which);
                photo.removeTag(tag);
                photoManager.saveData();
                updatePhotoDisplay();
                Toast.makeText(this, "Tag deleted", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }
}

