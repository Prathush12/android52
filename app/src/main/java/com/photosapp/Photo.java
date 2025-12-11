package com.photosapp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Photo implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String filePath;
    private String fileName;
    private List<Tag> tags;
    
    public Photo(String filePath, String fileName) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.tags = new ArrayList<>();
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public List<Tag> getTags() {
        return new ArrayList<>(tags);
    }
    
    public void addTag(Tag tag) {
        if (!tags.contains(tag)) {
            tags.add(tag);
        }
    }
    
    public void removeTag(Tag tag) {
        tags.remove(tag);
    }
    
    public boolean hasTag(String type, String value) {
        for (Tag tag : tags) {
            if (tag.getType().equalsIgnoreCase(type) && tag.getValue().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }
}
