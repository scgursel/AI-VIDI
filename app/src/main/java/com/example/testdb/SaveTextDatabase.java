package com.example.testdb;

import androidx.room.Database;
import androidx.room.RoomDatabase;


@Database(entities = {SaveText.class},version = 1,exportSchema = false)
public abstract class SaveTextDatabase extends RoomDatabase {

    public abstract SaveTextDAO saveTextDAO();
}
