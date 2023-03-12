package com.example.testdb.Db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.testdb.Db.SaveText;
import com.example.testdb.Db.SaveTextDAO;


@Database(entities = {SaveText.class},version = 1,exportSchema = false)
public abstract class SaveTextDatabase extends RoomDatabase {

    public abstract SaveTextDAO saveTextDAO();
}
