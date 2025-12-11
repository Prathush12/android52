package com.photosapp;

import java.io.Serializable;

public class Tag implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String type;
    private String value;
    
    public Tag(String type, String value) {
        this.type = type;
        this.value = value;
    }
    
    public String getType() {
        return type;
    }
    
    public String getValue() {
        return value;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Tag tag = (Tag) obj;
        return type.equalsIgnoreCase(tag.type) && value.equalsIgnoreCase(tag.value);
    }
    
    @Override
    public int hashCode() {
        return type.toLowerCase().hashCode() + value.toLowerCase().hashCode();
    }
    
    @Override
    public String toString() {
        return type + ": " + value;
    }
}
