package com.photosapp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Album implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String name;
    private List<Photo> photos;
    
    public Album(String name) {
        this.name = name;
        this.photos = new ArrayList<>();
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public List<Photo> getPhotos() {
        return new ArrayList<>(photos);
    }
    
    public boolean addPhoto(Photo photo) {
        if (photos.contains(photo)) {
            return false;
        }
        return photos.add(photo);
    }
    
    public boolean removePhoto(Photo photo) {
        return photos.remove(photo);
    }
    
    public int getPhotoCount() {
        return photos.size();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Album album = (Album) obj;
        return name.equals(album.name);
    }
    
    @Override
    public int hashCode() {
        return name.hashCode();
    }
}

