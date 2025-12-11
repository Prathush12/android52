package com.photosapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    private RecyclerView searchCriteriaRecyclerView;
    private RecyclerView resultsRecyclerView;
    private SearchCriteriaAdapter criteriaAdapter;
    private PhotoAdapter resultsAdapter;
    private PhotoManager photoManager;
    private Button addCriteriaButton;
    private Button searchButton;
    private RadioGroup searchTypeRadioGroup;
    private List<Tag> searchCriteria;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        
        photoManager = PhotoManager.getInstance(this);
        searchCriteria = new ArrayList<>();
        
        searchCriteriaRecyclerView = findViewById(R.id.searchCriteriaRecyclerView);
        resultsRecyclerView = findViewById(R.id.resultsRecyclerView);
        addCriteriaButton = findViewById(R.id.addCriteriaButton);
        searchButton = findViewById(R.id.searchButton);
        searchTypeRadioGroup = findViewById(R.id.searchTypeRadioGroup);
        
        searchCriteriaRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        resultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        criteriaAdapter = new SearchCriteriaAdapter(searchCriteria, tag -> {
            searchCriteria.remove(tag);
            updateCriteriaList();
        });
        searchCriteriaRecyclerView.setAdapter(criteriaAdapter);
        
        addCriteriaButton.setOnClickListener(v -> showAddCriteriaDialog());
        searchButton.setOnClickListener(v -> performSearch());
    }
    
    private void showAddCriteriaDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_search_criteria, null);
        TextInputLayout typeLayout = dialogView.findViewById(R.id.tagTypeLayout);
        TextInputLayout valueLayout = dialogView.findViewById(R.id.tagValueLayout);
        Button autocompleteButton = dialogView.findViewById(R.id.autocompleteButton);
        
        autocompleteButton.setOnClickListener(v -> {
            String type = typeLayout.getEditText().getText() != null ? 
                typeLayout.getEditText().getText().toString().trim() : "";
            String prefix = valueLayout.getEditText().getText() != null ? 
                valueLayout.getEditText().getText().toString().trim() : "";
            
            if (type.isEmpty()) {
                Toast.makeText(this, "Please enter a tag type first", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (prefix.isEmpty()) {
                Toast.makeText(this, "Please enter some text to get suggestions", Toast.LENGTH_SHORT).show();
                return;
            }
            
            List<String> suggestions = photoManager.getAutocompleteSuggestions(type, prefix);
            if (suggestions.isEmpty()) {
                Toast.makeText(this, "No suggestions found", Toast.LENGTH_SHORT).show();
            } else {
                showAutocompleteDialog(suggestions, valueLayout);
            }
        });
        
        new AlertDialog.Builder(this)
            .setTitle("Add Search Criteria")
            .setView(dialogView)
            .setPositiveButton(R.string.ok, (dialog, which) -> {
                String type = typeLayout.getEditText().getText() != null ? 
                    typeLayout.getEditText().getText().toString().trim() : "";
                String value = valueLayout.getEditText().getText() != null ? 
                    valueLayout.getEditText().getText().toString().trim() : "";
                
                if (type.isEmpty() || value.isEmpty()) {
                    Toast.makeText(this, "Tag type and value cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if (!type.equalsIgnoreCase("person") && !type.equalsIgnoreCase("location")) {
                    Toast.makeText(this, "Tag type must be 'person' or 'location'", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                Tag tag = new Tag(type, value);
                if (!searchCriteria.contains(tag)) {
                    searchCriteria.add(tag);
                    updateCriteriaList();
                } else {
                    Toast.makeText(this, "This criteria already exists", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }
    
    private void showAutocompleteDialog(List<String> suggestions, TextInputLayout valueLayout) {
        String[] items = suggestions.toArray(new String[0]);
        
        new AlertDialog.Builder(this)
            .setTitle("Select from suggestions")
            .setItems(items, (dialog, which) -> {
                valueLayout.getEditText().setText(items[which]);
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }
    
    private void updateCriteriaList() {
        criteriaAdapter.notifyDataSetChanged();
    }
    
    private void performSearch() {
        if (searchCriteria.isEmpty()) {
            Toast.makeText(this, "Please add at least one search criteria", Toast.LENGTH_SHORT).show();
            return;
        }
        
        RadioButton selectedRadio = findViewById(searchTypeRadioGroup.getCheckedRadioButtonId());
        boolean isConjunction = selectedRadio.getId() == R.id.andRadioButton;
        
        List<Photo> results = photoManager.searchPhotosWithMultipleCriteria(searchCriteria, isConjunction);
        
        if (results.isEmpty()) {
            Toast.makeText(this, R.string.no_results, Toast.LENGTH_SHORT).show();
        }
        
        resultsAdapter = new PhotoAdapter(results, photo -> {
            // Find which album contains this photo
            String albumName = findAlbumForPhoto(photo);
            if (albumName != null) {
                Album album = photoManager.getAlbumByName(albumName);
                if (album != null) {
                    int photoIndex = album.getPhotos().indexOf(photo);
                    Intent intent = new Intent(SearchActivity.this, PhotoDisplayActivity.class);
                    intent.putExtra("albumName", albumName);
                    intent.putExtra("photoIndex", photoIndex);
                    startActivity(intent);
                }
            }
        });
        resultsRecyclerView.setAdapter(resultsAdapter);
    }
    
    private String findAlbumForPhoto(Photo photo) {
        for (Album album : photoManager.getAlbums()) {
            if (album.getPhotos().contains(photo)) {
                return album.getName();
            }
        }
        return null;
    }
}
