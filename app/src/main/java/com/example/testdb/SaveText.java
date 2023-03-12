package com.example.testdb;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class SaveText {
    @PrimaryKey(autoGenerate=true)
    public int id;

    @ColumnInfo(name="text")
    public String text;

    @ColumnInfo(name = "field_name")
    public  String file_name;

    @Ignore
    public SaveText(int id, String text, String file_name) {
        this.id = id;
        this.text = text;
        this.file_name = file_name;
    }

    public SaveText(String text, String file_name) {
        this.text = text;
        this.file_name = file_name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }
}