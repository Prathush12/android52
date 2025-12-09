package com.photosapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView albumsRecyclerView;
    private AlbumAdapter albumAdapter;
    private PhotoManager photoManager;
    private Button createAlbumButton;
    private Button deleteAlbumButton;
    private Button renameAlbumButton;
    private Button searchButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        photoManager = PhotoManager.getInstance(this);
        
        albumsRecyclerView = findViewById(R.id.albumsRecyclerView);
        albumsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        createAlbumButton = findViewById(R.id.createAlbumButton);
        deleteAlbumButton = findViewById(R.id.deleteAlbumButton);
        renameAlbumButton = findViewById(R.id.renameAlbumButton);
        searchButton = findViewById(R.id.searchButton);
        
        createAlbumButton.setOnClickListener(v -> showCreateAlbumDialog());
        deleteAlbumButton.setOnClickListener(v -> showDeleteAlbumDialog());
        renameAlbumButton.setOnClickListener(v -> showRenameAlbumDialog());
        searchButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(intent);
        });
        
        updateAlbumList();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        updateAlbumList();
    }
    
    private void updateAlbumList() {
        List<Album> albums = photoManager.getAlbums();
        albumAdapter = new AlbumAdapter(albums, album -> {
            Intent intent = new Intent(MainActivity.this, AlbumActivity.class);
            intent.putExtra("albumName", album.getName());
            startActivity(intent);
        });
        albumsRecyclerView.setAdapter(albumAdapter);
    }
    
    private void showCreateAlbumDialog() {
        TextInputEditText input = new TextInputEditText(this);
        input.setHint(getString(R.string.enter_album_name));
        
        new AlertDialog.Builder(this)
            .setTitle(R.string.create_album)
            .setView(input)
            .setPositiveButton(R.string.ok, (dialog, which) -> {
                String albumName = input.getText() != null ? input.getText().toString().trim() : "";
                if (albumName.isEmpty()) {
                    Toast.makeText(this, "Album name cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (photoManager.albumExists(albumName)) {
                    Toast.makeText(this, "Album already exists", Toast.LENGTH_SHORT).show();
                    return;
                }
                Album album = new Album(albumName);
                photoManager.addAlbum(album);
                updateAlbumList();
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }
    
    private void showDeleteAlbumDialog() {
        List<Album> albums = photoManager.getAlbums();
        if (albums.isEmpty()) {
            Toast.makeText(this, "No albums to delete", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String[] albumNames = new String[albums.size()];
        for (int i = 0; i < albums.size(); i++) {
            albumNames[i] = albums.get(i).getName();
        }
        
        new AlertDialog.Builder(this)
            .setTitle(R.string.delete_album)
            .setItems(albumNames, (dialog, which) -> {
                Album album = albums.get(which);
                photoManager.removeAlbum(album);
                updateAlbumList();
                Toast.makeText(this, "Album deleted", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }
    
    private void showRenameAlbumDialog() {
        List<Album> albums = photoManager.getAlbums();
        if (albums.isEmpty()) {
            Toast.makeText(this, "No albums to rename", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String[] albumNames = new String[albums.size()];
        for (int i = 0; i < albums.size(); i++) {
            albumNames[i] = albums.get(i).getName();
        }
        
        new AlertDialog.Builder(this)
            .setTitle(R.string.rename_album)
            .setItems(albumNames, (dialog, which) -> {
                Album album = albums.get(which);
                showRenameInputDialog(album);
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }
    
    private void showRenameInputDialog(Album album) {
        TextInputEditText input = new TextInputEditText(this);
        input.setText(album.getName());
        input.setHint(getString(R.string.enter_album_name));
        
        new AlertDialog.Builder(this)
            .setTitle(R.string.rename_album)
            .setView(input)
            .setPositiveButton(R.string.ok, (dialog, which) -> {
                String newName = input.getText() != null ? input.getText().toString().trim() : "";
                if (newName.isEmpty()) {
                    Toast.makeText(this, "Album name cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (photoManager.albumExists(newName) && !newName.equals(album.getName())) {
                    Toast.makeText(this, "Album name already exists", Toast.LENGTH_SHORT).show();
                    return;
                }
                album.setName(newName);
                photoManager.saveData();
                updateAlbumList();
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }
}

