package com.wakey.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "selected_objects")
public class SelectedObjectEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;
    private String imagePath;

    public SelectedObjectEntity(String name, String imagePath) {
        this.name = name;
        this.imagePath = imagePath;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
}
