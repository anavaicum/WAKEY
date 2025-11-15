package com.wakey.models;

public class ObjectItem {
    private String name;
    private int imageResId;
    private boolean selected;

    public ObjectItem(String name, int imageResId) {
        this.name = name;
        this.imageResId = imageResId;
        this.selected = false; // implicit ne-selectat
    }

    public String getName() {
        return name;
    }

    public int getImageResId() {
        return imageResId;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
