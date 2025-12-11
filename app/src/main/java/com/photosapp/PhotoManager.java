package com.photosapp;

import android.content.Context;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class PhotoManager {
    private static final String FILENAME = "photos_data.dat";
    private static PhotoManager instance;
    private List<Album> albums;
    private Context context;
    
    private PhotoManager(Context context) {
        this.context = context.getApplicationContext();
        this.albums = new ArrayList<>();
        loadData();
    }
    
    public static synchronized PhotoManager getInstance(Context context) {
        if (instance == null) {
            instance = new PhotoManager(context);
        }
        return instance;
    }
    
    public List<Album> getAlbums() {
        return new ArrayList<>(albums);
    }
    
    public void addAlbum(Album album) {
        albums.add(album);
        saveData();
    }
    
    public void removeAlbum(Album album) {
        albums.remove(album);
        saveData();
    }
    
    public Album getAlbumByName(String name) {
        for (Album album : albums) {
            if (album.getName().equals(name)) {
                return album;
            }
        }
        return null;
    }
    
    public boolean albumExists(String name) {
        return getAlbumByName(name) != null;
    }
    
    public List<Photo> searchPhotos(String type, String value, boolean isConjunction) {
        List<Photo> results = new ArrayList<>();
        
        for (Album album : albums) {
            for (Photo photo : album.getPhotos()) {
                if (photo.hasTag(type, value)) {
                    if (!results.contains(photo)) {
                        results.add(photo);
                    }
                }
            }
        }
        
        return results;
    }
    
    public List<Photo> searchPhotosWithMultipleCriteria(List<Tag> searchTags, boolean isConjunction) {
        List<Photo> results = new ArrayList<>();
        
        if (searchTags.isEmpty()) {
            return results;
        }
        
        for (Album album : albums) {
            for (Photo photo : album.getPhotos()) {
                boolean matches;
                
                if (isConjunction) {
                    matches = true;
                    for (Tag searchTag : searchTags) {
                        if (!photoHasMatchingTag(photo, searchTag)) {
                            matches = false;
                            break;
                        }
                    }
                } else {
                    matches = false;
                    for (Tag searchTag : searchTags) {
                         if (photoHasMatchingTag(photo, searchTag)) {
                            matches = true;
                            break;
                        }
                    }
                }
                
                if (matches && !results.contains(photo)) {
                    results.add(photo);
                }
            }
        }
        
        return results;
    }

    private boolean photoHasMatchingTag(Photo photo, Tag searchTag) {
        for (Tag photoTag : photo.getTags()) {
            if (photoTag.getType().equalsIgnoreCase(searchTag.getType()) &&
                photoTag.getValue().equalsIgnoreCase(searchTag.getValue())) {
                return true;
            }
        }
        return false;
    }
    
    public List<String> getAutocompleteSuggestions(String type, String prefix) {
        List<String> suggestions = new ArrayList<>();
        prefix = prefix.toLowerCase();
        
        for (Album album : albums) {
            for (Photo photo : album.getPhotos()) {
                for (Tag tag : photo.getTags()) {
                    if (tag.getType().equalsIgnoreCase(type)) {
                        String value = tag.getValue();
                        if (value.toLowerCase().startsWith(prefix)) {
                             boolean exists = false;
                             for (String s : suggestions) {
                                 if (s.equalsIgnoreCase(value)) {
                                     exists = true;
                                     break;
                                 }
                             }
                            if (!exists) {
                                suggestions.add(value);
                            }
                        }
                    }
                }
            }
        }
        
        return suggestions;
    }
    
    public void saveData() {
        try {
            FileOutputStream fos = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(albums);
            oos.close();
            fos.close();
        } catch (Exception e) {
            Log.e("PhotoManager", "Error saving data", e);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void loadData() {
        try {
            FileInputStream fis = context.openFileInput(FILENAME);
            ObjectInputStream ois = new ObjectInputStream(fis);
            albums = (List<Album>) ois.readObject();
            ois.close();
            fis.close();
        } catch (Exception e) {
            Log.d("PhotoManager", "No existing data found, starting fresh");
            albums = new ArrayList<>();
        }
    }
}
