package com.example.testdb.Db;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import androidx.room.Room;

import com.example.testdb.Db.SaveText;
import com.example.testdb.Db.SaveTextDatabase;

import java.util.List;

public class SaveTextRepository {
    // private String DB_NAME = "saveTextdb";
    private SaveTextDatabase saveTextDatabase;
    Context context;

    public SaveTextRepository(Context context) {
        this.context = context;
        saveTextDatabase = Room.databaseBuilder(context, SaveTextDatabase.class, "test.db").build();
    }

    public void  InsertTask(final SaveText saveText) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                saveTextDatabase.saveTextDAO().insertText(saveText);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Toast.makeText(context, saveText.file_name + " eklendi", Toast.LENGTH_SHORT).show();
            }
        }.execute();
    }
    public List<SaveText> getSaveText(){
        List<SaveText> saveTextList=saveTextDatabase.saveTextDAO().getAll();
        return saveTextList;

    }

    public void UpdateTask(final SaveText saveText) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                saveTextDatabase.saveTextDAO().updateText(saveText);
                return null;
            }

            @Override
            protected void onPostExecute(Void unused) {
                super.onPostExecute(unused);
                Toast.makeText(context, saveText.file_name + " silindi", Toast.LENGTH_SHORT).show();
            }
        }.execute();
    }
    public void  DeleteTask(final SaveText saveText) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                saveTextDatabase.saveTextDAO().deleteText(saveText);
                return null;
            }

            @Override
            protected void onPostExecute(Void unused) {
                super.onPostExecute(unused);
                Toast.makeText(context, saveText.file_name + " silindi", Toast.LENGTH_SHORT).show();
            }
        }.execute();
    }
}



