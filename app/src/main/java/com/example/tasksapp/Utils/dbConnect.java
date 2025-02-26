package com.example.tasksapp.Utils;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.tasksapp.Model.Todomodel;

import java.util.ArrayList;
import java.util.List;

public class dbConnect extends SQLiteOpenHelper {
    private static final String dbName = "db";
    private static final int dbVersion = 1;
    private static final String TODO_TABLE = "TODO";
    private static final String TODO_ID = "id";
    private static final String TASK = "task";
    private static final String STATUS = "status";
    private static final String DESCRIPTION = "description";
    private static final String TIME = "time";
    private static final String CREATE_TODO_TABLE = "CREATE TABLE " + TODO_TABLE + " ("
            + TODO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + TASK + " TEXT, "
            + DESCRIPTION + " TEXT, "
            + TIME + " TEXT, "
            + STATUS + " INTEGER)";

    public dbConnect(Context context){
        super(context,dbName,null,dbVersion);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TODO_TABLE);

    }

        @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TODO_TABLE);
        onCreate(db);
    }
    public void openDatabase() {

        this.getWritableDatabase();
    }
    public void addTask(Todomodel task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(TASK, task.getTask());
        cv.put(DESCRIPTION, task.getDescription());
        cv.put(TIME, task.getTime());
        cv.put(STATUS, 0);
        db.insert(TODO_TABLE, null, cv);
        db.close();
    }

    @SuppressLint("Range")
    public List<Todomodel> getAllTasks() {
        List<Todomodel> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TODO_TABLE, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Todomodel task = new Todomodel();
                task.setId(cursor.getInt(cursor.getColumnIndex(TODO_ID)));
                task.setTask(cursor.getString(cursor.getColumnIndex(TASK)));
                task.setStatus(cursor.getInt(cursor.getColumnIndex(STATUS)));
                task.setDescription(cursor.getString(cursor.getColumnIndex(DESCRIPTION)));
                task.setTime(cursor.getString(cursor.getColumnIndex(TIME)));
                taskList.add(task);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return taskList;
    }

    public void updateTaskStatus(int id, int status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(STATUS, status);
        db.update(TODO_TABLE, cv, TODO_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void updateTask(int id, String task,String description,String time) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(TASK, task);
        cv.put(DESCRIPTION, description);
        cv.put(TIME, time);
        db.update(TODO_TABLE, cv, TODO_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void deleteTask(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TODO_TABLE, TODO_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

}
