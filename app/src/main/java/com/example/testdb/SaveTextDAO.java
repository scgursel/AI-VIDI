package com.example.testdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SaveTextDAO {

    @Insert
    Long insertText(SaveText saveText);

    @Update
    void updateText(SaveText saveText);

    @Delete
    void deleteText(SaveText saveText);

    @Query("select * from savetext order by id asc")
    List<SaveText> getAll();

}
