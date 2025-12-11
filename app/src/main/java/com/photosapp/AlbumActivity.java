package com.photosapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AlbumActivity extends AppCompatActivity {
    private TextView albumTitleTextView;
    private RecyclerView photosRecyclerView;
    private PhotoAdapter photoAdapter;
    private PhotoManager photoManager;
    private Album currentAlbum;
    private Button addPhotoButton;
    private Button removePhotoButton;
    private Button movePhotoButton;
    
    private ActivityResultLauncher<Intent> pickImageLauncher;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        
        String albumName = getIntent().getStringExtra("albumName");
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
        
        albumTitleTextView = findViewById(R.id.albumTitleTextView);
        photosRecyclerView = findViewById(R.id.photosRecyclerView);
        addPhotoButton = findViewById(R.id.addPhotoButton);
        removePhotoButton = findViewById(R.id.removePhotoButton);
        movePhotoButton = findViewById(R.id.movePhotoButton);
        
        albumTitleTextView.setText(currentAlbum.getName());
        photosRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        
        pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        try {
                            getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        
                        String filePath = imageUri.toString();
                        String fileName = getFileName(imageUri);
                        Photo photo = new Photo(filePath, fileName);
                        if(currentAlbum.addPhoto(photo)){
                           photoManager.saveData();
                           updatePhotoList();
                        } else {
                           Toast.makeText(this, "Photo already exists in this album", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        );
        
        addPhotoButton.setOnClickListener(v -> pickImage());
        removePhotoButton.setOnClickListener(v -> showRemovePhotoDialog());
        movePhotoButton.setOnClickListener(v -> showMovePhotoDialog());
        
        updatePhotoList();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        updatePhotoList();
    }
    
    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }
    
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                // Ignore
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
    
    private void updatePhotoList() {
        List<Photo> photos = currentAlbum.getPhotos();
        photoAdapter = new PhotoAdapter(photos, photo -> {
            Intent intent = new Intent(AlbumActivity.this, PhotoDisplayActivity.class);
            intent.putExtra("albumName", currentAlbum.getName());
            intent.putExtra("photoIndex", photos.indexOf(photo));
            startActivity(intent);
        });
        photosRecyclerView.setAdapter(photoAdapter);
    }
    
    private void showRemovePhotoDialog() {
        List<Photo> photos = currentAlbum.getPhotos();
        if (photos.isEmpty()) {
            Toast.makeText(this, "No photos to remove", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String[] photoNames = new String[photos.size()];
        for (int i = 0; i < photos.size(); i++) {
            photoNames[i] = photos.get(i).getFileName();
        }
        
        new AlertDialog.Builder(this)
            .setTitle(R.string.remove_photo)
            .setItems(photoNames, (dialog, which) -> {
                Photo photo = photos.get(which);
                currentAlbum.removePhoto(photo);
                photoManager.saveData();
                updatePhotoList();
                Toast.makeText(this, "Photo removed", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }
    
    private void showMovePhotoDialog() {
        List<Photo> photos = currentAlbum.getPhotos();
        if (photos.isEmpty()) {
            Toast.makeText(this, "No photos to move", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String[] photoNames = new String[photos.size()];
        for (int i = 0; i < photos.size(); i++) {
            photoNames[i] = photos.get(i).getFileName();
        }
        
        new AlertDialog.Builder(this)
            .setTitle(R.string.move_photo)
            .setItems(photoNames, (dialog, which) -> {
                Photo photo = photos.get(which);
                showSelectTargetAlbumDialog(photo);
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }
    
    private void showSelectTargetAlbumDialog(Photo photo) {
        List<Album> allAlbums = photoManager.getAlbums();
        List<Album> albums = new ArrayList<>();
        for (Album album : allAlbums) {
            if (!album.equals(currentAlbum)) {
                albums.add(album);
            }
        }
        
        if (albums.isEmpty()) {
            Toast.makeText(this, "No other albums available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String[] albumNames = new String[albums.size()];
        for (int i = 0; i < albums.size(); i++) {
            albumNames[i] = albums.get(i).getName();
        }
        
        new AlertDialog.Builder(this)
            .setTitle(R.string.select_album)
            .setItems(albumNames, (dialog, which) -> {
                Album targetAlbum = albums.get(which);
                currentAlbum.removePhoto(photo);
                targetAlbum.addPhoto(photo);
                photoManager.saveData();
                updatePhotoList();
                Toast.makeText(this, "Photo moved", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }
}
